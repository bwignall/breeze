package breeze.math

import breeze.compat.Scala3Compat._
import breeze.compat._
import breeze.generic.UFunc
import breeze.generic.UFunc.UImpl2
import breeze.linalg._
import breeze.linalg.operators._
import breeze.linalg.support.CanTraverseValues.ValuesVisitor
import breeze.linalg.support._
import breeze.util.Isomorphism

import scala.language.implicitConversions

/**
 *
 *
 * @author dlwh
 */
trait MutablizingAdaptor[+VS[_, _], MVS[_, _], V, S] {
  type Wrapper
  val underlying: VS[V, S]
  implicit val mutaVspace: MVS[Wrapper, S]

  implicit val isomorphism: Isomorphism[V, Wrapper] = new Isomorphism[V, Wrapper] {
    def forward(value: V): Wrapper = wrap(value)

    def backward(u: Wrapper): V = unwrap(u)
  }

  implicit def mutaVSpaceIdent(wrapper: Wrapper): MVS[Wrapper, S] = mutaVspace

  def wrap(v: V): Wrapper
  def unwrap(w: Wrapper): V
}

object MutablizingAdaptor {

  def ensureMutable[V, S](vs: VectorSpace[V, S]): MutablizingAdaptor[VectorSpace, MutableVectorSpace, V, S] = {
    if (vs.isInstanceOf[MutableVectorSpace[_, _]])
      IdentityWrapper[MutableVectorSpace, V, S](vs.asInstanceOf[MutableVectorSpace[V, S]])
    else VectorSpaceAdaptor(vs)
  }

  def ensureMutable[V, S](
    vs: InnerProductVectorSpace[V, S]
  ): MutablizingAdaptor[InnerProductVectorSpace, MutableInnerProductVectorSpace, V, S] = {
    if (vs.isInstanceOf[MutableInnerProductVectorSpace[_, _]])
      IdentityWrapper[MutableInnerProductVectorSpace, V, S](vs.asInstanceOf[MutableInnerProductVectorSpace[V, S]])
    else InnerProductSpaceAdaptor(vs)
  }

  def ensureMutable[V, S](vs: VectorField[V, S])(implicit
    canIterate: CanTraverseValues[V, S],
    canMap: CanMapValues[V, S, S, V],
    canZipMap: CanZipMapValues[V, S, S, V]
  ): MutablizingAdaptor[VectorField, MutableVectorField, V, S] = {
    if (vs.isInstanceOf[MutableVectorField[_, _]])
      IdentityWrapper[MutableVectorField, V, S](vs.asInstanceOf[MutableVectorField[V, S]])
    else VectorFieldAdaptor(vs)
  }

  def ensureMutable[V, S](vs: VectorRing[V, S])(implicit
    canIterate: CanTraverseValues[V, S],
    canMap: CanMapValues[V, S, S, V],
    canZipMap: CanZipMapValues[V, S, S, V]
  ): MutablizingAdaptor[VectorRing, MutableVectorRing, V, S] = {
    if (vs.isInstanceOf[MutableVectorRing[_, _]])
      IdentityWrapper[MutableVectorRing, V, S](vs.asInstanceOf[MutableVectorRing[V, S]])
    else VectorRingAdaptor(vs)
  }

  def ensureMutable[V, S](vs: CoordinateField[V, S])(implicit
    canIterate: CanTraverseValues[V, S],
    canMap: CanMapValues[V, S, S, V],
    canZipMap: CanZipMapValues[V, S, S, V]
  ): MutablizingAdaptor[CoordinateField, MutableCoordinateField, V, S] = {
    if (vs.isInstanceOf[MutableCoordinateField[_, _]])
      IdentityWrapper[MutableCoordinateField, V, S](vs.asInstanceOf[MutableCoordinateField[V, S]])
    else CoordinateFieldAdaptor(vs)
  }

  case class IdentityWrapper[VS[_, _], V, S](underlying: VS[V, S]) extends MutablizingAdaptor[VS, VS, V, S] {
    type Wrapper = V
    implicit val mutaVspace: VS[Wrapper, S] = underlying

    def wrap(v: V): Wrapper = v
    def unwrap(v: Wrapper): V = v
  }

  case class VectorSpaceAdaptor[V, S](underlying: VectorSpace[V, S])
      extends MutablizingAdaptor[VectorSpace, MutableVectorSpace, V, S] {
    type Wrapper = Ref[V]

    def wrap(v: V): Wrapper = Ref(v)

    def unwrap(w: Wrapper): V = w.value

    implicit val mutaVspace: MutableVectorSpace[Wrapper, S] = new MutableVectorSpace[Wrapper, S] {
      val u: VectorSpace[V, S] = underlying
      def scalars: Field[S] = underlying.scalars

      override val hasOps: ConversionOrSubtype[Wrapper, NumericOps[Wrapper]] = identity

      implicit def zeroLike: CanCreateZerosLike[Wrapper, Wrapper] = (from: Wrapper) =>
        from.map(underlying.zeroLike.apply)

      implicit def copy: CanCopy[Wrapper] = (from: Wrapper) => from

      def liftUpdate[Op <: OpType](implicit op: UFunc.UImpl2[Op, V, S, V]): UFunc.InPlaceImpl2[Op, Wrapper, S] =
        (a: Wrapper, b: S) => {
          a.value = op(a.value, b)
        }

      def liftUpdateV[Op <: OpType](implicit op: UFunc.UImpl2[Op, V, V, V]): UFunc.InPlaceImpl2[Op, Wrapper, Wrapper] =
        (a: Wrapper, b: Wrapper) => {
          a.value = op(a.value, b.value)
        }

      def liftOp[Op <: OpType](implicit op: UFunc.UImpl2[Op, V, S, V]): UFunc.UImpl2[Op, Wrapper, S, Wrapper] =
        (a: Wrapper, b: S) => {
          a.map(op(_, b))
        }

      def liftOpV[Op <: OpType](implicit op: UFunc.UImpl2[Op, V, V, V]): UFunc.UImpl2[Op, Wrapper, Wrapper, Wrapper] =
        (a: Wrapper, b: Wrapper) => {
          a.map(op(_, b.value))
        }

      implicit def mulIntoVS: OpMulScalar.InPlaceImpl2[Wrapper, S] = liftUpdate(u.mulVS)

      implicit def divIntoVS: OpDiv.InPlaceImpl2[Wrapper, S] = liftUpdate(u.divVS)

      // TODO: we should be able to get rid of these...
      implicit def addIntoVV: OpAdd.InPlaceImpl2[Wrapper, Wrapper] = liftUpdateV(u.addVV)

      implicit def subIntoVV: OpSub.InPlaceImpl2[Wrapper, Wrapper] = liftUpdateV(u.subVV)

      implicit def setIntoVV: OpSet.InPlaceImpl2[Wrapper, Wrapper] = (a: Wrapper, b: Wrapper) => {
        a.value = b.value
      }

      implicit def scaleAddVV: scaleAdd.InPlaceImpl3[Wrapper, S, Wrapper] = { (y: Wrapper, a: S, x: Wrapper) =>
        {
          y += x * a
        }
      }

      implicit def mulVS: OpMulScalar.Impl2[Wrapper, S, Wrapper] = liftOp(u.mulVS)

      implicit def divVS: OpDiv.Impl2[Wrapper, S, Wrapper] = liftOp(u.divVS)

      implicit def addVV: OpAdd.Impl2[Wrapper, Wrapper, Wrapper] = liftOpV(u.addVV)

      implicit def subVV: OpSub.Impl2[Wrapper, Wrapper, Wrapper] = liftOpV(u.subVV)

      def close(a: Wrapper, b: Wrapper, tolerance: Double): Boolean = u.close(a.value, b.value, tolerance)
    }
  }

  case class InnerProductSpaceAdaptor[V, S](underlying: InnerProductVectorSpace[V, S])
      extends MutablizingAdaptor[InnerProductVectorSpace, MutableInnerProductVectorSpace, V, S] {
    type Wrapper = Ref[V]

    def wrap(v: V): Wrapper = Ref(v)

    def unwrap(w: Wrapper): V = w.value

    implicit val mutaVspace: MutableInnerProductVectorSpace[Wrapper, S] =
      new MutableInnerProductVectorSpace[Wrapper, S] {
        val u: InnerProductVectorSpace[V, S] = underlying
        def scalars: Field[S] = underlying.scalars

        val hasOps: ConversionOrSubtype[Wrapper, NumericOps[Wrapper]] = identity

        implicit def zeroLike: CanCreateZerosLike[Wrapper, Wrapper] = (from: Wrapper) =>
          from.map(underlying.zeroLike.apply)

//      implicit def zero: CanCreateZeros[Wrapper,I] = new CanCreateZeros[Wrapper,I] {
//        override def apply(d: I): Wrapper = wrap(u.zero(d))
//      }

        implicit def copy: CanCopy[Wrapper] = (from: Wrapper) => from

//      implicit def normImplDouble: norm.Impl2[Wrapper, Double, Double] = new norm.Impl2[Wrapper, Double, Double] {
//        def apply(v1: Wrapper, v2: Double): Double = u.normImplDouble(v1.value, v2)
//      }

//      implicit def iterateValues: CanTraverseValues[Wrapper, S] = new CanTraverseValues[Wrapper,S] {
//        /** Traverses all values from the given collection. */
//        override def traverse(from: Wrapper, fn: ValuesVisitor[S]): Unit = {
//          from.map(u.iterateValues.traverse(_,fn))
//        }
//
//        override def isTraversableAgain(from: Wrapper): Boolean = u.iterateValues.isTraversableAgain(from.value)
//      }
//
//      implicit def mapValues: CanMapValues[Wrapper, S, S, Wrapper] = new CanMapValues[Wrapper, S, S, Wrapper] {
//        /** Maps all key-value pairs from the given collection. */
//        def map(from: Wrapper, fn: (S) => S): Wrapper = {
//          from.map(u.mapValues.map(_, fn))
//        }
//
//        /** Maps all active key-value pairs from the given collection. */
//        def mapActive(from: Wrapper, fn: (S) => S): Wrapper = {
//          from.map(u.mapValues.mapActive(_, fn))
//        }
//      }
//
//      implicit def zipMapValues: CanZipMapValues[Wrapper, S, S, Wrapper] = new CanZipMapValues[Wrapper, S, S, Wrapper] {
//        /** Maps all corresponding values from the two collections. */
//        def map(from: Wrapper, from2: Wrapper, fn: (S, S) => S): Wrapper = {
//          from.map(u.zipMapValues.map(_, from2.value, fn))
//        }
//      }

        def liftUpdate[Op <: OpType](implicit op: UImpl2[Op, V, S, V]): UFunc.InPlaceImpl2[Op, Wrapper, S] =
          (a: Wrapper, b: S) => {
            a.value = op(a.value, b)
          }

        def liftUpdateV[Op <: OpType](implicit op: UImpl2[Op, V, V, V]): UFunc.InPlaceImpl2[Op, Wrapper, Wrapper] =
          (a: Wrapper, b: Wrapper) => {
            a.value = op(a.value, b.value)
          }

        def liftOp[Op <: OpType](implicit op: UImpl2[Op, V, S, V]): UImpl2[Op, Wrapper, S, Wrapper] =
          (a: Wrapper, b: S) => {
            a.map(op(_, b))
          }

        def liftOpV[Op <: OpType](implicit op: UImpl2[Op, V, V, V]): UImpl2[Op, Wrapper, Wrapper, Wrapper] =
          (a: Wrapper, b: Wrapper) => {
            a.map(op(_, b.value))
          }

        implicit def mulIntoVS: OpMulScalar.InPlaceImpl2[Wrapper, S] = liftUpdate(u.mulVS)

        implicit def divIntoVS: OpDiv.InPlaceImpl2[Wrapper, S] = liftUpdate(u.divVS)

        implicit def addIntoVV: OpAdd.InPlaceImpl2[Wrapper, Wrapper] = liftUpdateV(u.addVV)

        implicit def subIntoVV: OpSub.InPlaceImpl2[Wrapper, Wrapper] = liftUpdateV(u.subVV)

        implicit def setIntoVV: OpSet.InPlaceImpl2[Wrapper, Wrapper] = (a: Wrapper, b: Wrapper) => {
          a.value = b.value
        }

        implicit def scaleAddVV: scaleAdd.InPlaceImpl3[Wrapper, S, Wrapper] = { (y: Wrapper, a: S, x: Wrapper) =>
          {
            y += x * a
          }
        }

        implicit def mulVS: OpMulScalar.Impl2[Wrapper, S, Wrapper] = liftOp(u.mulVS)

        implicit def divVS: OpDiv.Impl2[Wrapper, S, Wrapper] = liftOp(u.divVS)

        implicit def addVV: OpAdd.Impl2[Wrapper, Wrapper, Wrapper] = liftOpV(u.addVV)

        implicit def subVV: OpSub.Impl2[Wrapper, Wrapper, Wrapper] = liftOpV(u.subVV)

//      override implicit def addVS: OpAdd.Impl2[Wrapper, S, Wrapper] = liftOp(u.addVS)
//
//      override implicit def addIntoVS: OpAdd.InPlaceImpl2[Wrapper, S] = liftUpdate(u.addVS)
//
//      override implicit def subIntoVS: OpSub.InPlaceImpl2[Wrapper, S] = liftUpdate(u.subVS)
//
//      override implicit def subVS: OpSub.Impl2[Wrapper, S, Wrapper] = liftOp(u.subVS)

        override def close(a: Wrapper, b: Wrapper, tolerance: Double): Boolean = u.close(a.value, b.value, tolerance)

        implicit def dotVV: OpMulInner.Impl2[Wrapper, Wrapper, S] = (a: Wrapper, b: Wrapper) => {
          u.dotVV(a.value, b.value)
        }
      }
  }

  case class VectorFieldAdaptor[V, S](underlying: VectorField[V, S])(implicit
    canIterate: CanTraverseValues[V, S],
    canMap: CanMapValues[V, S, S, V],
    canZipMap: CanZipMapValues[V, S, S, V]
  ) extends MutablizingAdaptor[VectorField, MutableVectorField, V, S] {
    type Wrapper = Ref[V]

    def wrap(v: V): Wrapper = Ref(v)

    def unwrap(w: Wrapper): V = w.value

    implicit val mutaVspace: MutableVectorField[Wrapper, S] = new MutableVectorField[Wrapper, S] {
      val u: VectorField[V, S] = underlying
      def scalars: Field[S] = underlying.scalars

      val hasOps: ConversionOrSubtype[Wrapper, NumericOps[Wrapper]] = identity

      implicit def zeroLike: CanCreateZerosLike[Wrapper, Wrapper] = new CanCreateZerosLike[Wrapper, Wrapper] {
        // Should not inherit from Form=>To because the compiler will try to use it to coerce types.
        def apply(from: Wrapper): Wrapper = from.map(underlying.zeroLike.apply)
      }

      implicit def copy: CanCopy[Wrapper] = new CanCopy[Wrapper] {
        // Should not inherit from Form=>To because the compiler will try to use it to coerce types.
        def apply(from: Wrapper): Wrapper = from
      }

//      implicit def normImplDouble: norm.Impl2[Wrapper, Double, Double] = new norm.Impl2[Wrapper, Double, Double] {
//        def apply(v1: Wrapper, v2: Double): Double = u.normImplDouble(v1.value, v2)
//      }

      implicit def iterateValues: CanTraverseValues[Wrapper, S] = new CanTraverseValues[Wrapper, S] {

        /** Traverses all values from the given collection. */
        override def traverse(from: Wrapper, fn: ValuesVisitor[S]): fn.type = {
          from.map(canIterate.traverse(_, fn))
          fn
        }

        override def isTraversableAgain(from: Wrapper): Boolean = canIterate.isTraversableAgain(from.value)
      }

      implicit def mapValues: CanMapValues[Wrapper, S, S, Wrapper] = new CanMapValues[Wrapper, S, S, Wrapper] {

        override def map(from: Wrapper, fn: (S) => S): Wrapper = {
          from.map(canMap.map(_, fn))
        }

        def mapActive(from: Wrapper, fn: (S) => S): Wrapper = {
          from.map(canMap.mapActive(_, fn))
        }
      }

      implicit def zipMapValues: CanZipMapValues[Wrapper, S, S, Wrapper] =
        (from: Wrapper, from2: Wrapper, fn: (S, S) => S) => {
          from.map(canZipMap.map(_, from2.value, fn))
        }

      def liftUpdate[Op <: OpType](implicit op: UImpl2[Op, V, S, V]): UFunc.InPlaceImpl2[Op, Wrapper, S] =
        (a: Wrapper, b: S) => {
          a.value = op(a.value, b)
        }

      def liftUpdateV[Op <: OpType](implicit op: UImpl2[Op, V, V, V]): UFunc.InPlaceImpl2[Op, Wrapper, Wrapper] =
        (a: Wrapper, b: Wrapper) => {
          a.value = op(a.value, b.value)
        }

      def liftOp[Op <: OpType](implicit op: UImpl2[Op, V, S, V]): UImpl2[Op, Wrapper, S, Wrapper] =
        (a: Wrapper, b: S) => {
          a.map(op(_, b))
        }

      def liftOpV[Op <: OpType](implicit op: UImpl2[Op, V, V, V]): UImpl2[Op, Wrapper, Wrapper, Wrapper] =
        (a: Wrapper, b: Wrapper) => {
          a.map(op(_, b.value))
        }

      implicit def mulIntoVS: OpMulScalar.InPlaceImpl2[Wrapper, S] = liftUpdate(u.mulVS)

      implicit def divIntoVS: OpDiv.InPlaceImpl2[Wrapper, S] = liftUpdate(u.divVS)

      implicit def addIntoVV: OpAdd.InPlaceImpl2[Wrapper, Wrapper] = liftUpdateV(u.addVV)

      implicit def subIntoVV: OpSub.InPlaceImpl2[Wrapper, Wrapper] = liftUpdateV(u.subVV)

      implicit def setIntoVV: OpSet.InPlaceImpl2[Wrapper, Wrapper] = (a: Wrapper, b: Wrapper) => {
        a.value = b.value
      }

//      implicit def addIntoVS: OpAdd.InPlaceImpl2[Wrapper, S] = liftUpdate(u.addVS)
//
//      implicit def subIntoVS: OpSub.InPlaceImpl2[Wrapper, S] = liftUpdate(u.subVS)
//
//
//      implicit def setIntoVS: OpSet.InPlaceImpl2[Wrapper, S] = new OpSet.InPlaceImpl2[Wrapper,S] {
//        override def apply(v: Wrapper, v2: S): Unit = ???
//      }

      implicit def mulVV: OpMulScalar.Impl2[Wrapper, Wrapper, Wrapper] = liftOpV(u.mulVV)

      implicit def mulIntoVV: OpMulScalar.InPlaceImpl2[Wrapper, Wrapper] = liftUpdateV(u.mulVV)

//      implicit def subVS: OpSub.Impl2[Wrapper, S, Wrapper] = liftOp(u.subVS)
//
//      implicit def addVS: OpAdd.Impl2[Wrapper, S, Wrapper] = liftOp(u.addVS)

      implicit def divVV: OpDiv.Impl2[Wrapper, Wrapper, Wrapper] = liftOpV(u.divVV)

      implicit def divIntoVV: OpDiv.InPlaceImpl2[Wrapper, Wrapper] = liftUpdateV(u.divVV)

      implicit def scaleAddVV: scaleAdd.InPlaceImpl3[Wrapper, S, Wrapper] = { (y: Wrapper, a: S, x: Wrapper) =>
        {
          y += x * a
        }
      }

      implicit def mulVS: OpMulScalar.Impl2[Wrapper, S, Wrapper] = liftOp(u.mulVS)

      implicit def divVS: OpDiv.Impl2[Wrapper, S, Wrapper] = liftOp(u.divVS)

      implicit def addVV: OpAdd.Impl2[Wrapper, Wrapper, Wrapper] = liftOpV(u.addVV)

      implicit def subVV: OpSub.Impl2[Wrapper, Wrapper, Wrapper] = liftOpV(u.subVV)

      override def close(a: Wrapper, b: Wrapper, tolerance: Double): Boolean = u.close(a.value, b.value, tolerance)

      // default implementations
      implicit def neg: OpNeg.Impl[Wrapper, Wrapper] = (a: Wrapper) => a.map(u.neg.apply)

      implicit def dotVV: OpMulInner.Impl2[Wrapper, Wrapper, S] = (a: Wrapper, b: Wrapper) => {
        u.dotVV(a.value, b.value)
      }
    }
  }

  case class Ref[T](var value: T) extends NumericOps[Ref[T]] {
    def repr: Ref[T] = this
    def map[U](f: T => U): Ref[U] = Ref(f(value))
  }

  case class VectorRingAdaptor[V, S](underlying: VectorRing[V, S])(implicit
    canIterate: CanTraverseValues[V, S],
    canMap: CanMapValues[V, S, S, V],
    canZipMap: CanZipMapValues[V, S, S, V]
  ) extends MutablizingAdaptor[VectorRing, MutableVectorRing, V, S] {
    type Wrapper = Ref[V]

    def wrap(v: V): Wrapper = Ref(v)

    def unwrap(w: Wrapper): V = w.value

    implicit val mutaVspace: MutableVectorRing[Wrapper, S] = new MutableVectorRing[Wrapper, S] {
      val u: VectorRing[V, S] = underlying
      def scalars: Ring[S] = underlying.scalars

      val hasOps: ConversionOrSubtype[Wrapper, NumericOps[Wrapper]] = identity

      implicit def zeroLike: CanCreateZerosLike[Wrapper, Wrapper] = (from: Wrapper) =>
        from.map(underlying.zeroLike.apply)

      implicit def copy: CanCopy[Wrapper] = (from: Wrapper) => from

//      implicit def normImplDouble: norm.Impl2[Wrapper, Double, Double] = new norm.Impl2[Wrapper, Double, Double] {
//        def apply(v1: Wrapper, v2: Double): Double = u.normImplDouble(v1.value, v2)
//      }

      implicit def iterateValues: CanTraverseValues[Wrapper, S] = new CanTraverseValues[Wrapper, S] {

        /** Traverses all values from the given collection. */
        override def traverse(from: Wrapper, fn: ValuesVisitor[S]): fn.type = {
          from.map(canIterate.traverse(_, fn))
          fn
        }

        override def isTraversableAgain(from: Wrapper): Boolean = canIterate.isTraversableAgain(from.value)
      }

      implicit def mapValues: CanMapValues[Wrapper, S, S, Wrapper] = new CanMapValues[Wrapper, S, S, Wrapper] {

        /** Maps all key-value pairs from the given collection. */
        override def map(from: Wrapper, fn: (S) => S): Wrapper = {
          from.map(canMap.map(_, fn))
        }

        override def mapActive(from: Wrapper, fn: (S) => S): Wrapper = {
          from.map(canMap.mapActive(_, fn))
        }
      }

      implicit def zipMapValues: CanZipMapValues[Wrapper, S, S, Wrapper] =
        (from: Wrapper, from2: Wrapper, fn: (S, S) => S) => {
          from.map(canZipMap.map(_, from2.value, fn))
        }

      def liftUpdate[Op <: OpType](implicit op: UImpl2[Op, V, S, V]): UFunc.InPlaceImpl2[Op, Wrapper, S] =
        (a: Wrapper, b: S) => {
          a.value = op(a.value, b)
        }

      def liftUpdateV[Op <: OpType](implicit op: UImpl2[Op, V, V, V]): UFunc.InPlaceImpl2[Op, Wrapper, Wrapper] =
        (a: Wrapper, b: Wrapper) => {
          a.value = op(a.value, b.value)
        }

      def liftOp[Op <: OpType](implicit op: UImpl2[Op, V, S, V]): UImpl2[Op, Wrapper, S, Wrapper] =
        (a: Wrapper, b: S) => {
          a.map(op(_, b))
        }

      def liftOpV[Op <: OpType](implicit op: UImpl2[Op, V, V, V]): UImpl2[Op, Wrapper, Wrapper, Wrapper] =
        (a: Wrapper, b: Wrapper) => {
          a.map(op(_, b.value))
        }

      implicit def mulIntoVS: OpMulScalar.InPlaceImpl2[Wrapper, S] = liftUpdate(u.mulVS)

      implicit def addIntoVV: OpAdd.InPlaceImpl2[Wrapper, Wrapper] = liftUpdateV(u.addVV)

      implicit def subIntoVV: OpSub.InPlaceImpl2[Wrapper, Wrapper] = liftUpdateV(u.subVV)

      implicit def setIntoVV: OpSet.InPlaceImpl2[Wrapper, Wrapper] = (a: Wrapper, b: Wrapper) => {
        a.value = b.value
      }

//      implicit def addIntoVS: OpAdd.InPlaceImpl2[Wrapper, S] = liftUpdate(u.addVS)
//
//      implicit def subIntoVS: OpSub.InPlaceImpl2[Wrapper, S] = liftUpdate(u.subVS)
//
//
//      implicit def setIntoVS: OpSet.InPlaceImpl2[Wrapper, S] = new OpSet.InPlaceImpl2[Wrapper,S] {
//        override def apply(v: Wrapper, v2: S): Unit = ???
//      }

      implicit def mulVV: OpMulScalar.Impl2[Wrapper, Wrapper, Wrapper] = liftOpV(u.mulVV)

      implicit def mulIntoVV: OpMulScalar.InPlaceImpl2[Wrapper, Wrapper] = liftUpdateV(u.mulVV)

//      implicit def subVS: OpSub.Impl2[Wrapper, S, Wrapper] = liftOp(u.subVS)
//
//      implicit def addVS: OpAdd.Impl2[Wrapper, S, Wrapper] = liftOp(u.addVS)

      implicit def scaleAddVV: scaleAdd.InPlaceImpl3[Wrapper, S, Wrapper] = { (y: Wrapper, a: S, x: Wrapper) =>
        {
          y += x * a
        }
      }

      implicit def mulVS: OpMulScalar.Impl2[Wrapper, S, Wrapper] = liftOp(u.mulVS)

      implicit def addVV: OpAdd.Impl2[Wrapper, Wrapper, Wrapper] = liftOpV(u.addVV)

      implicit def subVV: OpSub.Impl2[Wrapper, Wrapper, Wrapper] = liftOpV(u.subVV)

      override def close(a: Wrapper, b: Wrapper, tolerance: Double): Boolean = u.close(a.value, b.value, tolerance)

      // default implementations
      implicit def neg: OpNeg.Impl[Wrapper, Wrapper] = (a: Wrapper) => a.map(u.neg.apply)

      implicit def dotVV: OpMulInner.Impl2[Wrapper, Wrapper, S] = (a: Wrapper, b: Wrapper) => {
        u.dotVV(a.value, b.value)
      }
    }
  }

  case class CoordinateFieldAdaptor[V, S](underlying: CoordinateField[V, S])(implicit
    canIterate: CanTraverseValues[V, S],
    canMap: CanMapValues[V, S, S, V],
    canZipMap: CanZipMapValues[V, S, S, V]
  ) extends MutablizingAdaptor[CoordinateField, MutableCoordinateField, V, S] {
    type Wrapper = Ref[V]

    def wrap(v: V): Wrapper = Ref(v)

    def unwrap(w: Wrapper): V = w.value

    implicit val mutaVspace: MutableCoordinateField[Wrapper, S] = new MutableCoordinateField[Wrapper, S] {
      val u: CoordinateField[V, S] = underlying
      def scalars: Field[S] = underlying.scalars

      val hasOps: ConversionOrSubtype[Wrapper, NumericOps[Wrapper]] = identity

      implicit def zeroLike: CanCreateZerosLike[Wrapper, Wrapper] = (from: Wrapper) =>
        from.map(underlying.zeroLike.apply)

      implicit def copy: CanCopy[Wrapper] = new CanCopy[Wrapper] {
        // Should not inherit from Form=>To because the compiler will try to use it to coerce types.
        def apply(from: Wrapper): Wrapper = from
      }

//      implicit def normImplDouble: norm.Impl2[Wrapper, Double, Double] = new norm.Impl2[Wrapper, Double, Double] {
//        def apply(v1: Wrapper, v2: Double): Double = u.normImplDouble(v1.value, v2)
//      }

      implicit def iterateValues: CanTraverseValues[Wrapper, S] = new CanTraverseValues[Wrapper, S] {
        override def traverse(from: Wrapper, fn: ValuesVisitor[S]): fn.type = {
          from.map(canIterate.traverse(_, fn))
          fn
        }

        override def isTraversableAgain(from: Wrapper): Boolean = canIterate.isTraversableAgain(from.value)
      }

      implicit def mapValues: CanMapValues[Wrapper, S, S, Wrapper] = new CanMapValues[Wrapper, S, S, Wrapper] {
        override def map(from: Wrapper, fn: (S) => S): Wrapper = {
          from.map(canMap.map(_, fn))
        }

        override def mapActive(from: Wrapper, fn: (S) => S): Wrapper = {
          from.map(canMap.mapActive(_, fn))
        }
      }

      implicit override def scalarOf: ScalarOf[Wrapper, S] = ScalarOf.dummy

      implicit def zipMapValues: CanZipMapValues[Wrapper, S, S, Wrapper] =
        (from: Wrapper, from2: Wrapper, fn: (S, S) => S) => {
          from.map(canZipMap.map(_, from2.value, fn))
        }

      def liftUpdate[Op <: OpType](implicit op: UImpl2[Op, V, S, V]): UFunc.InPlaceImpl2[Op, Wrapper, S] =
        (a: Wrapper, b: S) => {
          a.value = op(a.value, b)
        }

      def liftUpdateV[Op <: OpType](implicit op: UImpl2[Op, V, V, V]): UFunc.InPlaceImpl2[Op, Wrapper, Wrapper] =
        (a: Wrapper, b: Wrapper) => {
          a.value = op(a.value, b.value)
        }

      def liftOp[Op <: OpType, RHS](implicit op: UImpl2[Op, V, RHS, V]): UImpl2[Op, Wrapper, RHS, Wrapper] =
        (a: Wrapper, b: RHS) => {
          a.map(op(_, b))
        }

      def liftOpV[Op <: OpType](implicit op: UImpl2[Op, V, V, V]): UImpl2[Op, Wrapper, Wrapper, Wrapper] =
        (a: Wrapper, b: Wrapper) => {
          a.map(op(_, b.value))
        }

      implicit def mulIntoVS: OpMulScalar.InPlaceImpl2[Wrapper, S] = liftUpdate(u.mulVS)

      implicit def addIntoVV: OpAdd.InPlaceImpl2[Wrapper, Wrapper] = liftUpdateV(u.addVV)

      implicit def subIntoVV: OpSub.InPlaceImpl2[Wrapper, Wrapper] = liftUpdateV(u.subVV)

      implicit def setIntoVV: OpSet.InPlaceImpl2[Wrapper, Wrapper] = (a: Wrapper, b: Wrapper) => {
        a.value = b.value
      }

      implicit def mulVV: OpMulScalar.Impl2[Wrapper, Wrapper, Wrapper] = liftOpV(u.mulVV)

      implicit def mulIntoVV: OpMulScalar.InPlaceImpl2[Wrapper, Wrapper] = liftUpdateV(u.mulVV)

      implicit def scaleAddVV: scaleAdd.InPlaceImpl3[Wrapper, S, Wrapper] = { (y: Wrapper, a: S, x: Wrapper) =>
        {
          y += x * a
        }
      }

      implicit def mulVS: OpMulScalar.Impl2[Wrapper, S, Wrapper] = liftOp(u.mulVS)

      implicit def addVV: OpAdd.Impl2[Wrapper, Wrapper, Wrapper] = liftOpV(u.addVV)

      implicit def subVV: OpSub.Impl2[Wrapper, Wrapper, Wrapper] = liftOpV(u.subVV)

      override def close(a: Wrapper, b: Wrapper, tolerance: Double): Boolean = u.close(a.value, b.value, tolerance)

      // default implementations
      implicit def neg: OpNeg.Impl[Wrapper, Wrapper] = (a: Wrapper) => a.map(u.neg.apply)

      implicit def dotVV: OpMulInner.Impl2[Wrapper, Wrapper, S] = (a: Wrapper, b: Wrapper) => {
        u.dotVV(a.value, b.value)
      }

      implicit override def normImpl2: norm.Impl2[Wrapper, Double, Double] = (v: Wrapper, v2: Double) =>
        underlying.normImpl2(v.value, v2)

      implicit override def divIntoVV: OpDiv.InPlaceImpl2[Wrapper, Wrapper] = liftUpdateV(underlying.divVV)

      implicit override def divVV: OpDiv.Impl2[Wrapper, Wrapper, Wrapper] = liftOpV(underlying.divVV)

      implicit override def divIntoVS: OpDiv.InPlaceImpl2[Wrapper, S] = liftUpdate(underlying.divVS)

      implicit override def divVS: OpDiv.Impl2[Wrapper, S, Wrapper] = liftOp(underlying.divVS)
    }
  }

}
