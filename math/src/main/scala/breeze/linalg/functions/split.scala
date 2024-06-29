package breeze.linalg

import breeze.collection.compat._
import breeze.generic.UFunc
import breeze.macros._
import breeze.storage.Zero

import scala.collection.compat.immutable.ArraySeq
import scala.collection.mutable
import scala.reflect.ClassTag

/**
 * split the array
 *
 * @author stucchio
 */
object split extends UFunc {

  implicit def implIntVec[T: ClassTag]: Impl2[DenseVector[T], Int, IndexedSeq[DenseVector[T]]] = {
    (v: DenseVector[T], n: Int) =>
      {
        require(n >= 0)
        require(n <= v.size)
        require(v.size % n == 0)

        val individualVectorSize = v.size / n
        val result = mutable.ArrayBuilder.make[DenseVector[T]]

        cforRange(0 until n) { k =>
          val offsetInOriginalVector = k * individualVectorSize
          val chunk = new DenseVector(new Array[T](individualVectorSize))
          chunk := v(offsetInOriginalVector until (offsetInOriginalVector + individualVectorSize))
          result += chunk
        }
        ArraySeq.unsafeWrapArray(result.result())
      }
  }

  implicit def implSeqVec[T: ClassTag]: Impl2[DenseVector[T], Seq[Int], IndexedSeq[DenseVector[T]]] =
    (v: DenseVector[T], nSeq: Seq[Int]) => {
      require(nSeq.size < v.size)

      val result = arraySeqBuilder[DenseVector[T]]
      var lastN: Int = 0
      nSeq.foreach { n =>
        val chunk = new Array[T](n - lastN)
        cforRange(lastN until n) { i =>
          chunk(i - lastN) = v(i)
        }
        result += DenseVector[T](chunk)
        lastN = n
      }
      if (lastN < v.size) { // If we did not already add last chunk to result, do it now.
        val chunk = new Array[T](v.size - lastN)
        cforRange(lastN until v.size) { i =>
          chunk(i - lastN) = v(i)
        }
        result += DenseVector[T](chunk)
      }
      result.result()
    }

  implicit def implIntMatrix[T: ClassTag](implicit
    zero: Zero[T]
  ): Impl3[DenseMatrix[T], Int, Int, IndexedSeq[DenseMatrix[T]]] =
    (v: DenseMatrix[T], n: Int, axis: Int) =>
      axis match {
        case 0 => vsplit(v, n)
        case 1 => hsplit(v, n)
        case _ => throw new IllegalArgumentException("Matrices have only two axes.")
      }
}

object hsplit extends UFunc {
  implicit def implIntVec[T: ClassTag]: Impl2[DenseVector[T], Int, IndexedSeq[DenseVector[T]]] =
    (v: DenseVector[T], n: Int) => hsplit(v, n)

  implicit def implSeqVec[T: ClassTag]: Impl2[DenseVector[T], Seq[Int], IndexedSeq[DenseVector[T]]] =
    (v: DenseVector[T], n: Seq[Int]) => hsplit(v, n)

  implicit def implIntMat[T: ClassTag](implicit zero: Zero[T]): Impl2[DenseMatrix[T], Int, IndexedSeq[DenseMatrix[T]]] =
    (v: DenseMatrix[T], n: Int) => {
      require(n >= 0)
      require(n <= v.cols)
      require(v.cols % n == 0)

      val result = arraySeqBuilder[DenseMatrix[T]]
      val newCols = v.cols / n
      val newSize = v.rows * newCols

      cforRange(0 until n) { k =>
        val offsetInOriginalMatrix = k * newCols
        val chunk = DenseMatrix.create(v.rows, newCols, new Array[T](newSize))
        cforRange2(0 until v.rows, 0 until newCols) { (i, j) =>
          chunk(i, j) = v(i, j + offsetInOriginalMatrix)
        }
        result += chunk
      }
      result.result()
    }
}

object vsplit extends UFunc {
  implicit def implIntMat[T: ClassTag](implicit zero: Zero[T]): Impl2[DenseMatrix[T], Int, IndexedSeq[DenseMatrix[T]]] =
    (v: DenseMatrix[T], n: Int) => {
      require(n >= 0)
      require(n <= v.cols)
      require(v.cols % n == 0)

      val result = arraySeqBuilder[DenseMatrix[T]]
      val newRows = v.rows / n

      cforRange(0 until n) { k =>
        val offsetInOriginalMatrix = k * newRows
        val chunk = DenseMatrix.create(newRows, v.cols, new Array[T](v.cols * newRows))
        cforRange2(0 until newRows, 0 until v.cols) { (i, j) =>
          chunk(i, j) = v(i + offsetInOriginalMatrix, j)
        }
        result += chunk
      }
      result.result()
    }
}
