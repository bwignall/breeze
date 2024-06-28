package breeze.linalg
/*
 Copyright 2012 David Hall

 Licensed under the Apache License, Version 2.0 (the "License")
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

import breeze.generic._
import breeze.linalg.operators._
import breeze.linalg.support.CanMapValues.DenseCanMapValues
import breeze.linalg.support._
import breeze.macros._
import breeze.macros._
import breeze.macros.expand
import breeze.math._
import breeze.storage.Zero
import breeze.util._
import dev.ludovic.netlib.blas.BLAS.{getInstance => blas}

import java.io.ObjectStreamException
import scala.collection.mutable.ArrayBuilder
import scala.math.BigInt
import scala.reflect.ClassTag
import scala.{specialized => spec}

import CanTraverseValues.ValuesVisitor
import CanZipAndTraverseValues.PairValuesVisitor

/**
 * A DenseVector is the "obvious" implementation of a Vector, with one twist.
 * The underlying data may have more data than the Vector, represented using an offset
 * into the array (for the 0th element), and a stride that is how far elements are apart
 * from one another.
 *
 * The i'th element is at offset + i * stride
 *
 * @author dlwh
 *
 * @param data data array
 * @param offset index of the 0'th element
 * @param stride separation between elements
 * @param length number of elements
 */
@SerialVersionUID(1L)
class DenseVector[@spec(Double, Int, Float, Long) V](val data: Array[V],
                                                     val offset: Int,
                                                     val stride: Int,
                                                     val length: Int
) extends StorageVector[V]
    with VectorLike[V, DenseVector[V]]
    with Serializable {
  def this(data: Array[V]) = this(data, 0, 1, data.length)
  def this(data: Array[V], offset: Int) = this(data, offset, 1, data.length)
  def this(length: Int)(implicit man: ClassTag[V]) = this(new Array[V](length), 0, 1, length)

  // ensure that operators are all loaded.
  DenseVector.init()

  def repr: DenseVector[V] = this

  def activeSize = length

  def apply(i: Int): V = {
    if (i < -size || i >= size) throw new IndexOutOfBoundsException(s"$i not in [-$size,$size)")
    val trueI = if (i < 0) i + size else i
    if (noOffsetOrStride) {
      data(trueI)
    } else {
      data(offset + trueI * stride)
    }
  }

  def update(i: Int, v: V): Unit = {
    if (i < -size || i >= size) throw new IndexOutOfBoundsException(s"$i not in [-$size,$size)")
    val trueI = if (i < 0) i + size else i
    if (noOffsetOrStride) {
      data(trueI) = v
    } else {
      data(offset + trueI * stride) = v
    }
  }

  private[linalg] val noOffsetOrStride = offset == 0 && stride == 1

  private def checkIfSpecialized(): Unit = {
    if (data.isInstanceOf[Array[Double]] && getClass.getName() == "breeze.linalg.DenseVector")
      throw new Exception("...")
  }
  // uncomment to debug places where specialization fails
  //  checkIfSpecialized()

  def activeIterator: Iterator[(Int, V)] = iterator

  def activeValuesIterator: Iterator[V] = valuesIterator

  def activeKeysIterator: Iterator[Int] = keysIterator

  override def equals(p1: Any): Boolean = p1 match {
    case y: DenseVector[_] =>
      y.length == length && ArrayUtil
        .nonstupidEquals(data, offset, stride, length, y.data, y.offset, y.stride, y.length)
    case _ => super.equals(p1)
  }

  /**
   * This hashcode is consistent with over [[breeze.linalg.Vector]] hashcodes so long as the hashcode of "0" is 0.
   **/
  override def hashCode(): Int = ArrayUtil.zeroSkippingHashCode(data, offset, stride, length)

  override def toString: String = {
    valuesIterator.mkString("DenseVector(", ", ", ")")
  }

  /**
   * Returns a copy of this DenseVector. stride will always be 1, offset will always be 0.
   * @return
   */
  def copy: DenseVector[V] = {
    if (stride == 1) {
      val newData = ArrayUtil.copyOfRange(data, offset, offset + length)
      new DenseVector(newData)
    } else {
      implicit val man: ClassTag[V] = ReflectionUtil.elemClassTagFromArray(data)
      val r = new DenseVector(new Array[V](length))
      r := this
      r
    }
  }

  /**
   * same as apply(i). Gives the value at the underlying offset.
   * @param i index into the data array
   * @return apply(i)
   */
  def valueAt(i: Int): V = apply(i)

  /**
   * Gives the logical index from the physical index.
   * @param i
   * @return i
   */
  def indexAt(i: Int): Int = i

  /**
   * Always returns true.
   *
   * Some storages (namely HashStorage) won't have active
   * indices packed. This lets you know if the bin is
   * actively in use.
   * @param i index into index/data arrays
   * @return
   */
  def isActive(i: Int): Boolean = true

  /**
   * Always returns true.
   * @return
   */
  def allVisitableIndicesActive: Boolean = true

  /**
   * Faster foreach
   * @param fn
   * @tparam U
   */
  override def foreach[@spec(Unit) U](fn: (V) => U): Unit = {
    if (stride == 1) { // ABCE stuff
      cforRange(offset until (offset + length)) { j =>
        fn(data(j))
      }
    } else {
      var i = offset
      cforRange(0 until length) { j =>
        fn(data(i))
        i += stride
      }
    }
  }

  /**
   * Slices the DenseVector, in the range [start,end] with a stride stride.
   * @param start
   * @param end
   * @param stride
   */
  def slice(start: Int, end: Int, stride: Int = 1): DenseVector[V] = {
    if (start > end || start < 0)
      throw new IllegalArgumentException("Slice arguments " + start + ", " + end + " invalid.")
    if (end > length || end < 0)
      throw new IllegalArgumentException("End " + end + "is out of bounds for slice of DenseVector of length " + length)
    val len = (end - start + stride - 1) / stride
    new DenseVector(data, start * this.stride + offset, stride * this.stride, len)
  }

  // <editor-fold defaultstate="collapsed" desc=" Conversions (DenseMatrix, Array, Scala Vector) ">

  /** Creates a copy of this DenseVector that is represented as a 1 by length DenseMatrix */
  def toDenseMatrix: DenseMatrix[V] = {
    copy.asDenseMatrix
  }

  /** Creates a view of this DenseVector that is represented as a 1 by length DenseMatrix */
  def asDenseMatrix: DenseMatrix[V] = {
    new DenseMatrix[V](1, length, data, offset, stride)
  }

  override def toArray(implicit ct: ClassTag[V]): Array[V] = {
//    implicit val ct: ClassTag[V] = ReflectionUtil.elemClassTagFromArray(data)
    if (stride == 1) {
      ArrayUtil.copyOfRange(data, offset, offset + length)
    } else {
      val arr = new Array[V](length)
      var i = 0
      var off = offset
      while (i < length) {
        arr(i) = data(off)
        off += stride
        i += 1
      }
      arr
    }
  }

  /**Returns copy of this [[breeze.linalg.Vector]] as a [[scala.Vector]]*/
  override def toScalaVector: scala.Vector[V] = {
    implicit val ct: ClassTag[V] = ReflectionUtil.elemClassTagFromArray(data)
    this.toArray.toVector
  }
  // </editor-fold>

  @throws(classOf[ObjectStreamException])
  protected def writeReplace(): Object = {
    new DenseVector.SerializedForm(data, offset, stride, length)
  }

  /** Returns true if this overlaps any content with the other vector */
  private[linalg] def overlaps(other: DenseVector[V]): Boolean =
    (this.data eq other.data) && RangeUtils.overlaps(footprint, other.footprint)

  private def footprint: Range = {
    if (length == 0) {
      Range(offset, offset)
    } else {
      val r = offset.to(offset + stride * (length - 1), stride)
      if (stride < 0) {
        r.reverse
      } else {
        r
      }
    }
  }
}

object DenseVector extends VectorConstructors[DenseVector] {

  def zeros[@spec(Double, Int, Float, Long) V: ClassTag: Zero](size: Int): DenseVector[V] = {
    val data = new Array[V](size)
    if (size != 0 && data(0) != implicitly[Zero[V]].zero)
      ArrayUtil.fill(data, 0, data.length, implicitly[Zero[V]].zero)
    apply(data)
  }

  def apply[@spec(Double, Int, Float, Long) V](values: Array[V]): DenseVector[V] = {
    // ensure we get specialized implementations even from non-specialized calls
    (values: AnyRef) match {
      case v: Array[Double] => new DenseVector(v).asInstanceOf[DenseVector[V]]
      case v: Array[Float]  => new DenseVector(v).asInstanceOf[DenseVector[V]]
      case v: Array[Int]    => new DenseVector(v).asInstanceOf[DenseVector[V]]
      case v: Array[Long]   => new DenseVector(v).asInstanceOf[DenseVector[V]]
      case _                => new DenseVector(values)
    }
  }

  /**
   * Analogous to Array.tabulate
   * @param size
   * @param f
   * @tparam V
   * @return
   */
  def tabulate[@spec(Double, Int, Float, Long) V: ClassTag](size: Int)(f: Int => V): DenseVector[V] = {
    val b = ArrayBuilder.make[V]
    b.sizeHint(size)
    var i = 0
    while (i < size) {
      b += f(i)
      i += 1
    }

    apply(b.result())
  }

  /**
   * Analogous to Array.tabulate, but taking a scala.Range to iterate over, instead of an index.
   * @param f
   * @tparam V
   * @return
   */
  def tabulate[@spec(Double, Int, Float, Long) V: ClassTag](range: Range)(f: Int => V): DenseVector[V] = {
    val b = ArrayBuilder.make[V]
    b.sizeHint(range.length)
    var i = 0
    while (i < range.length) {
      b += f(range(i))
      i += 1
    }
    apply(b.result())
  }

  /**
   * Analogous to Array.fill
   * @param size
   * @param v
   * @tparam V
   * @return
   */
  def fill[@spec(Double, Int, Float, Long) V: ClassTag](size: Int)(v: => V): DenseVector[V] = {
    apply(Array.fill(size)(v))
  }

  /**
   *
   * Creates a new DenseVector using the provided array (not making a copy!). In generic contexts, prefer to
   * use this (or apply) instead of `new DenseVector[V](data, offset, stride, length)`, which in general
   * won't give specialized implementations.
   * @param data
   * @param offset
   * @param stride
   * @param length
   * @tparam V
   * @return
   */
  def create[V](data: Array[V], offset: Int, stride: Int, length: Int): DenseVector[V] = {
    (data: AnyRef) match {
      case v: Array[Double] =>
        new DenseVector(v, offset = offset, stride = stride, length = length).asInstanceOf[DenseVector[V]]
      case v: Array[Float] =>
        new DenseVector(v, offset = offset, stride = stride, length = length).asInstanceOf[DenseVector[V]]
      case v: Array[Int] =>
        new DenseVector(v, offset = offset, stride = stride, length = length).asInstanceOf[DenseVector[V]]
      case v: Array[Long] =>
        new DenseVector(v, offset = offset, stride = stride, length = length).asInstanceOf[DenseVector[V]]
      case _ => new DenseVector(data, offset = offset, stride = stride, length = length)
    }
  }

  def ones[@spec(Double, Int, Float, Long) V: ClassTag: Semiring](size: Int): DenseVector[V] =
    fill[V](size, implicitly[Semiring[V]].one)

  def fill[@spec(Double, Int, Float, Long) V: ClassTag: Semiring](size: Int, v: V): DenseVector[V] = {
    val r = apply(new Array[V](size))
    assert(r.stride == 1)
    ArrayUtil.fill(r.data, r.offset, r.length, v)
    r
  }

  // concatenation
  /**
   * Horizontal concatenation of two or more vectors into one matrix.
   * @throws IllegalArgumentException if vectors have different sizes
   */
  def horzcat[V: ClassTag: Zero](vectors: DenseVector[V]*): DenseMatrix[V] = {
    val size = vectors.head.size
    if (!vectors.forall(_.size == size))
      throw new IllegalArgumentException("All vectors must have the same size!")
    val result = DenseMatrix.zeros[V](size, vectors.size)
    for ((v, col) <- vectors.zipWithIndex)
      result(::, col) := v
    result
  }

  /**
   * Vertical concatenation of two or more column vectors into one large vector.
   */
  def vertcat[V](vectors: DenseVector[V]*)(implicit
    canSet: OpSet.InPlaceImpl2[DenseVector[V], DenseVector[V]],
    vman: ClassTag[V],
    zero: Zero[V]
  ): DenseVector[V] = {
    val size = vectors.foldLeft(0)(_ + _.size)
    val result = zeros[V](size)
    var offset = 0
    for (v <- vectors) {
      result.slice(offset, offset + v.size) := v
      offset += v.size
    }
    result
  }

  // capabilities

  implicit def canCreateZerosLike[V: ClassTag: Zero]: CanCreateZerosLike[DenseVector[V], DenseVector[V]] =
    (v1: DenseVector[V]) => {
      zeros[V](v1.length)
    }

  implicit def canCopyDenseVector[V: ClassTag]: CanCopy[DenseVector[V]] = DenseVectorDeps.canCopyDenseVector[V]

  implicit def DV_canMapValues[@specialized(Int, Float, Double) V, @specialized(Int, Float, Double) V2](implicit
    man: ClassTag[V2]
  ): CanMapValues[DenseVector[V], V, V2, DenseVector[V2]] = {
    new DenseCanMapValues[DenseVector[V], V, V2, DenseVector[V2]] {

      /**Maps all key-value pairs from the given collection. */
      def map(from: DenseVector[V], fn: (V) => V2): DenseVector[V2] = {
        val out = new Array[V2](from.length)

        // threeway fork, following benchmarks and hotspot docs on Array Bounds Check Elimination (ABCE)
        // https://wikis.oracle.com/display/HotSpotInternals/RangeCheckElimination
        if (from.noOffsetOrStride) {
          fastestPath(out, fn, from.data)
        } else if (from.stride == 1) {
          mediumPath(out, fn, from.data, from.offset)
        } else {
          slowPath(out, fn, from.data, from.offset, from.stride)
        }
        DenseVector[V2](out)
      }

      private def mediumPath(out: Array[V2], fn: (V) => V2, data: Array[V], off: Int): Unit = {
        cforRange(out.indices) { j =>
          out(j) = fn(data(j + off))
        }
      }

      private def fastestPath(out: Array[V2], fn: (V) => V2, data: Array[V]): Unit = {
        cforRange(out.indices) { j =>
          out(j) = fn(data(j))
        }
      }

      final private def slowPath(out: Array[V2], fn: (V) => V2, data: Array[V], off: Int, stride: Int): Unit = {
        var i = 0
        var j = off
        while (i < out.length) {
          out(i) = fn(data(j))
          i += 1
          j += stride
        }
      }
    }
  }

  // TODO: bring back sinks
//  implicit def DV_canMapValuesToSink[@specialized(Int, Float, Double) V, @specialized(Int, Float, Double) V2]
//    : mapValues.SinkImpl2[DenseVector[V2], DenseVector[V], V => V2] = {
//    new mapValues.SinkImpl2[DenseVector[V2], DenseVector[V], V => V2] {
//
//      /**Maps all key-value pairs from the given collection. */
//      def apply(sink: DenseVector[V2], from: DenseVector[V], fn: (V) => V2) = {
//        require(sink.length == from.length)
//
//        // threeway fork, following benchmarks and hotspot docs on Array Bounds Check Elimination (ABCE)
//        // https://wikis.oracle.com/display/HotSpotInternals/RangeCheckElimination
//        if (sink.noOffsetOrStride && from.noOffsetOrStride) {
//          fastestPath(sink, fn, from.data)
//        } else if (sink.stride == 1 && from.stride == 1) {
//          mediumPath(sink, fn, from.data, from.offset)
//        } else {
//          slowPath(sink, fn, from.data, from.offset, from.stride)
//        }
//      }
//
//      private def mediumPath(sink: DenseVector[V2], fn: (V) => V2, data: Array[V], off: Int): Unit = {
//        val out = sink.data
//        val ooff = sink.offset
//        cforRange(0 until sink.length) { j =>
//          out(j + ooff) = fn(data(j + off))
//        }
//      }
//
//      private def fastestPath(sink: DenseVector[V2], fn: (V) => V2, data: Array[V]): Unit = {
//        val out = sink.data
//        cforRange(0 until sink.length) { j =>
//          out(j) = fn(data(j))
//        }
//      }
//
//      final private def slowPath(out: DenseVector[V2], fn: (V) => V2, data: Array[V], off: Int, stride: Int): Unit = {
//        var i = 0
//        var j = off
//        while (i < out.length) {
//          out(i) = fn(data(j))
//          i += 1
//          j += stride
//        }
//      }
//    }
//  }

  implicit def DV_scalarOf[T]: ScalarOf[DenseVector[T], T] = ScalarOf.dummy

  class CanZipMapValuesDenseVector[@spec(Double, Int, Float, Long) V, @spec(Int, Double) RV: ClassTag]
      extends CanZipMapValues[DenseVector[V], V, RV, DenseVector[RV]] {
    def create(length: Int): DenseVector[RV] = DenseVector(new Array[RV](length))

    /**Maps all corresponding values from the two collection. */
    def map(from: DenseVector[V], from2: DenseVector[V], fn: (V, V) => RV): DenseVector[RV] = {
      require(from.length == from2.length, s"Vectors must have same length")
      val result = create(from.length)
      var i = 0
      while (i < from.length) {
        result.data(i) = fn(from(i), from2(i))
        i += 1
      }
      result
    }
  }

  implicit def zipMap[V, R: ClassTag]: CanZipMapValuesDenseVector[V, R] = new CanZipMapValuesDenseVector[V, R]
  implicit val zipMap_d: CanZipMapValuesDenseVector[Double, Double] = new CanZipMapValuesDenseVector[Double, Double]
  implicit val zipMap_f: CanZipMapValuesDenseVector[Float, Float] = new CanZipMapValuesDenseVector[Float, Float]
  implicit val zipMap_i: CanZipMapValuesDenseVector[Int, Int] = new CanZipMapValuesDenseVector[Int, Int]

  class CanZipMapKeyValuesDenseVector[@spec(Double, Int, Float, Long) V, @spec(Int, Double) RV: ClassTag]
      extends CanZipMapKeyValues[DenseVector[V], Int, V, RV, DenseVector[RV]] {
    def create(length: Int): DenseVector[RV] = DenseVector(new Array[RV](length))

    /**Maps all corresponding values from the two collection. */
    def map(from: DenseVector[V], from2: DenseVector[V], fn: (Int, V, V) => RV): DenseVector[RV] = {
      require(from.length == from2.length, "Vector lengths must match!")
      val result = create(from.length)
      var i = 0
      while (i < from.length) {
        result.data(i) = fn(i, from(i), from2(i))
        i += 1
      }
      result
    }

    override def mapActive(from: DenseVector[V], from2: DenseVector[V], fn: (Int, V, V) => RV): DenseVector[RV] = {
      map(from, from2, fn)
    }
  }

  implicit def zipMapKV[V, R: ClassTag]: CanZipMapKeyValuesDenseVector[V, R] = new CanZipMapKeyValuesDenseVector[V, R]

  // this produces bad spaces for builtins (inefficient because of bad implicit lookup)
  implicit def space[E](implicit
    field: Field[E],
    man: ClassTag[E]
  ): MutableFiniteCoordinateField[DenseVector[E], Int, E] = {
    import field._
    implicit val cmv: CanMapValues[DenseVector[E], E, E, DenseVector[E]] = DenseVector.DV_canMapValues[E, E]
    MutableFiniteCoordinateField.make[DenseVector[E], Int, E]
  }

  implicit val space_Double: MutableFiniteCoordinateField[DenseVector[Double], Int, Double] = {
    MutableFiniteCoordinateField.make[DenseVector[Double], Int, Double]
  }

  implicit val space_Float: MutableFiniteCoordinateField[DenseVector[Float], Int, Float] = {
    MutableFiniteCoordinateField.make[DenseVector[Float], Int, Float]
  }

  implicit val space_Int: MutableFiniteCoordinateField[DenseVector[Int], Int, Int] = {
    MutableFiniteCoordinateField.make[DenseVector[Int], Int, Int]
  }

  implicit val space_Long: MutableFiniteCoordinateField[DenseVector[Long], Int, Long] = {
    MutableFiniteCoordinateField.make[DenseVector[Long], Int, Long]
  }

  object TupleIsomorphisms {
    implicit object doubleIsVector extends Isomorphism[Double, DenseVector[Double]] {
      def forward(t: Double): DenseVector[Double] = DenseVector(t)
      def backward(t: DenseVector[Double]): Double = { assert(t.size == 1); t(0) }
    }

    implicit object pdoubleIsVector extends Isomorphism[(Double, Double), DenseVector[Double]] {
      def forward(t: (Double, Double)): DenseVector[Double] = DenseVector(t._1, t._2)
      def backward(t: DenseVector[Double]): (Double, Double) = { assert(t.size == 2); (t(0), t(1)) }
    }
  }

  /**
   * This class exists because @specialized instances don't respect the serial
   * @param data
   * @param offset
   * @param stride
   * @param length
   */
  @SerialVersionUID(1L)
  case class SerializedForm(data: Array[_], offset: Int, stride: Int, length: Int) extends Serializable {

    @throws(classOf[ObjectStreamException])
    def readResolve(): Object = {
      data match { // switch to make specialized happy
        case x: Array[Int]    => new DenseVector(x, offset, stride, length)
        case x: Array[Long]   => new DenseVector(x, offset, stride, length)
        case x: Array[Double] => new DenseVector(x, offset, stride, length)
        case x: Array[Float]  => new DenseVector(x, offset, stride, length)
        case x: Array[Short]  => new DenseVector(x, offset, stride, length)
        case x: Array[Byte]   => new DenseVector(x, offset, stride, length)
        case x: Array[Char]   => new DenseVector(x, offset, stride, length)
        case x: Array[_]      => new DenseVector(x, offset, stride, length)
      }

    }
  }

  // used to make sure the operators are loaded
  @noinline
  private def init() = {}
}

/** Static initialization of [[DenseVector]] depends on initializing [[operators.HasOps]], whose static
 * initialization in turn depends one some of the implicits in [[DenseVector]]. This object extracts out
 * the definitions of implicits that were known to cause deadlock in initialization
 * (see https://github.com/scalanlp/breeze/issues/825). */
private[linalg] object DenseVectorDeps {
  implicit def canCopyDenseVector[V: ClassTag]: CanCopy[DenseVector[V]] = (v1: DenseVector[V]) => v1.copy
}
