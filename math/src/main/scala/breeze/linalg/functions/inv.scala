package breeze.linalg

import breeze.generic.UFunc
import breeze.linalg.operators.OpMulMatrix
import breeze.linalg.operators.OpSolveMatrixBy
import breeze.linalg.support.CanTranspose
import breeze.macros.expand
import dev.ludovic.netlib.lapack.LAPACK.{getInstance => lapack}
import org.netlib.util.intW

/**
 * Computes the inverse of a given real matrix.
 * In general, you should avoid using this metho in combination with *.
 * Instead, wherever you might want to write inv(A) * B, you should write
 * A \ B.
 */
object inv extends UFunc {
  implicit def canInvUsingLU_Double[T](implicit
    luImpl: LU.primitive.Impl[T, (DenseMatrix[Double], Array[Int])]
  ): Impl[T, DenseMatrix[Double]] = {
    (X: T) => {
      // Should these type hints be necessary?
      val (m: DenseMatrix[Double], ipiv: Array[Int]) = LU.primitive(X)
      val N = m.rows
      val lwork = scala.math.max(1, N)
      val work = Array.ofDim[Double](lwork)
      val info = new intW(0)
      lapack.dgetri(
        N,
        m.data,
        scala.math.max(1, N) /* LDA */ ,
        ipiv,
        work /* workspace */ ,
        lwork /* workspace size */ ,
        info
      )
      assert(info.`val` >= 0, "Malformed argument %d (LAPACK)".format(-info.`val`))

      if (info.`val` > 0)
        throw new MatrixSingularException

      m
    }
  }

  implicit def canInvUsingLU_Float[T](implicit
    luImpl: LU.primitive.Impl[T, (DenseMatrix[Float], Array[Int])]
  ): Impl[T, DenseMatrix[Float]] = {
    (X: T) => {
      // Should these type hints be necessary?
      val (m: DenseMatrix[Float], ipiv: Array[Int]) = LU.primitive(X)
      val N = m.rows
      val lwork = scala.math.max(1, N)
      val work = Array.ofDim[Float](lwork)
      val info = new intW(0)
      lapack.sgetri(
        N,
        m.data,
        scala.math.max(1, N) /* LDA */ ,
        ipiv,
        work /* workspace */ ,
        lwork /* workspace size */ ,
        info
      )
      assert(info.`val` >= 0, "Malformed argument %d (LAPACK)".format(-info.`val`))

      if (info.`val` > 0)
        throw new MatrixSingularException

      m
    }
  }

}
