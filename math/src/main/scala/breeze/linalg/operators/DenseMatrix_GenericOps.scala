package breeze.linalg.operators

import breeze.linalg._
import breeze.macros.cforRange
import breeze.macros.cforRange2
import breeze.macros.require
import breeze.math.Semiring
import breeze.storage.Zero
import breeze.util.ReflectionUtil

import scala.reflect.ClassTag

trait DenseMatrix_GenericOps extends MatrixOps {

  implicit def impl_scaleAdd_InPlace_DM_T_DM[T: Semiring]: scaleAdd.InPlaceImpl3[DenseMatrix[T], T, DenseMatrix[T]] = {
    (a: DenseMatrix[T], s: T, b: DenseMatrix[T]) => {
      val ring = implicitly[Semiring[T]]
      require(a.rows == b.rows, "Vector row dimensions must match!")
      require(a.cols == b.cols, "Vector col dimensions must match!")

      cforRange2(0 until a.cols, 0 until a.rows) { (j, i) =>
        a(i, j) = ring.+(a(i, j), ring.*(s, b(i, j)))
      }
    }
  }

  implicit def impl_OpMulMatrix_DM_DM_eq_DM_Generic[T: Semiring]
    : OpMulMatrix.Impl2[DenseMatrix[T], DenseMatrix[T], DenseMatrix[T]] =
    new OpMulMatrix.Impl2[DenseMatrix[T], DenseMatrix[T], DenseMatrix[T]] {
      val ring: Semiring[T] = implicitly[Semiring[T]]
      override def apply(a: DenseMatrix[T], b: DenseMatrix[T]): DenseMatrix[T] = {
        implicit val ct: ClassTag[T] = ReflectionUtil.elemClassTagFromArray(a.data)
        implicit val r: Semiring[T] = ring
        implicit val z: Zero[T] = Zero.zeroFromSemiring(r)

        val res: DenseMatrix[T] = DenseMatrix.zeros[T](a.rows, b.cols)
        require(a.cols == b.rows)

        val colsB = b.cols
        val colsA = a.cols
        val rowsA = a.rows

        var j = 0
        while (j < colsB) {
          var l = 0
          while (l < colsA) {

            val v = b(l, j)
            var i = 0
            while (i < rowsA) {
              res(i, j) = ring.+(res(i, j), ring.*(a(i, l), v))
              i += 1
            }
            l += 1
          }
          j += 1
        }
        res
      }
    }

  implicit def impl_OpMulMatrix_DM_V_eq_DV_Generic[T, Vec <: Vector[T]](implicit
    ring: Semiring[T]
  ): OpMulMatrix.Impl2[DenseMatrix[T], Vec, DenseVector[T]] =
    (a: DenseMatrix[T], b: Vec) => {
      implicit val ct: ClassTag[T] = ReflectionUtil.elemClassTagFromArray(a.data)
      require(a.cols == b.length)
      val res: DenseVector[T] = DenseVector.zeros[T](a.rows)
      var c = 0
      while (c < a.cols) {
        var r = 0
        while (r < a.rows) {
          val v = a(r, c)
          res(r) = ring.+(res(r), ring.*(v, b(c)))
          r += 1
        }
        c += 1
      }

      res
    }

}
