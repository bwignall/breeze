package breeze.stats.regression

import breeze.generic.UFunc
import breeze.linalg._
import breeze.linalg.operators.OpMulInner
import breeze.linalg.operators.OpMulMatrix
import dev.ludovic.netlib.lapack.LAPACK.{getInstance => lapack}
import org.netlib.util.intW

import java.util.Arrays

private object leastSquaresImplementation {

  def doLeastSquaresDouble(data: DenseMatrix[Double],
                           outputs: DenseVector[Double],
                           workArray: Array[Double]
  ): LeastSquaresRegressionResult[Double] = {
    require(data.rows == outputs.size)
    require(data.rows > data.cols + 1)
    require(workArray.length >= 2 * data.rows * data.cols)

    val info = new intW(0)
    lapack.dgels("N",
                 data.rows,
                 data.cols,
                 1,
                 data.data,
                 data.rows,
                 outputs.data,
                 data.rows,
                 workArray,
                 workArray.length,
                 info
    )
    if (info.`val` < 0) {
      throw new ArithmeticException("Least squares did not converge.")
    }

    val coefficients = new DenseVector[Double](Arrays.copyOf(outputs.data, data.cols))
    var r2 = 0.0
    for (i <- 0 until (data.rows - data.cols)) {
      r2 = r2 + math.pow(outputs.data(data.cols + i), 2)
    }
    LeastSquaresRegressionResult[Double](coefficients, r2)
  }

  def doLeastSquaresFloat(data: DenseMatrix[Float],
                          outputs: DenseVector[Float],
                          workArray: Array[Float]
  ): LeastSquaresRegressionResult[Float] = {
    require(data.rows == outputs.size)
    require(data.rows > data.cols + 1)
    require(workArray.length >= 2 * data.rows * data.cols)

    val info = new intW(0)
    lapack.sgels("N",
                 data.rows,
                 data.cols,
                 1,
                 data.data,
                 data.rows,
                 outputs.data,
                 data.rows,
                 workArray,
                 workArray.length,
                 info
    )
    if (info.`val` < 0) {
      throw new ArithmeticException("Least squares did not converge.")
    }

    val coefficients = new DenseVector[Float](Arrays.copyOf(outputs.data, data.cols))
    var r2 = 0.0
    for (i <- 0 until (data.rows - data.cols)) {
      r2 = r2 + math.pow(outputs.data(data.cols + i), 2)
    }
    LeastSquaresRegressionResult[Float](coefficients, r2.toFloat)
  }
}

case class LeastSquaresRegressionResult[T](coefficients: DenseVector[T], rSquared: T)(implicit
  can_dot: OpMulInner.Impl2[DenseVector[T], DenseVector[T], T]
) extends RegressionResult[DenseVector[T], T] {

  def apply(x: DenseVector[T]): T = coefficients.dot(x)

  def apply(
    X: DenseMatrix[T]
  )(implicit can_dot: OpMulMatrix.Impl2[DenseMatrix[T], DenseVector[T], DenseVector[T]]): DenseVector[T] = {
    X * coefficients
  }
}

object leastSquares extends UFunc {
  implicit val matrixVectorWithWorkArrayDouble
    : Impl3[DenseMatrix[Double], DenseVector[Double], Array[Double], LeastSquaresRegressionResult[Double]] =
    (data: DenseMatrix[Double], outputs: DenseVector[Double], workArray: Array[Double]) => leastSquaresImplementation.doLeastSquaresDouble(data.copy, outputs.copy, workArray)

  implicit val matrixVectorWithWorkArrayFloat
    : Impl3[DenseMatrix[Float], DenseVector[Float], Array[Float], LeastSquaresRegressionResult[Float]] =
    (data: DenseMatrix[Float], outputs: DenseVector[Float], workArray: Array[Float]) => leastSquaresImplementation.doLeastSquaresFloat(data.copy, outputs.copy, workArray)

  implicit val matrixVectorSpecifiedWorkDouble
    : Impl3[DenseMatrix[Double], DenseVector[Double], Int, LeastSquaresRegressionResult[Double]] =
    (data: DenseMatrix[Double], outputs: DenseVector[Double], workSize: Int) => leastSquaresImplementation.doLeastSquaresDouble(data.copy, outputs.copy, new Array[Double](workSize))

  implicit val matrixVectorSpecifiedWorkFloat
    : Impl3[DenseMatrix[Float], DenseVector[Float], Int, LeastSquaresRegressionResult[Float]] =
    (data: DenseMatrix[Float], outputs: DenseVector[Float], workSize: Int) => leastSquaresImplementation.doLeastSquaresFloat(data.copy, outputs.copy, new Array[Float](workSize))

  implicit val matrixVectorDouble
    : Impl2[DenseMatrix[Double], DenseVector[Double], LeastSquaresRegressionResult[Double]] =
    (data: DenseMatrix[Double], outputs: DenseVector[Double]) => leastSquaresImplementation.doLeastSquaresDouble(data.copy,
      outputs.copy,
      new Array[Double](math.max(1, data.rows * data.cols * 2))
    )

  implicit val matrixVectorFloat: Impl2[DenseMatrix[Float], DenseVector[Float], LeastSquaresRegressionResult[Float]] =
    (data: DenseMatrix[Float], outputs: DenseVector[Float]) => leastSquaresImplementation.doLeastSquaresFloat(data,
      outputs,
      new Array[Float](math.max(1, data.rows * data.cols * 2))
    )
}

object leastSquaresDestructive extends UFunc {
  implicit val matrixVectorWithWorkArrayDouble
    : Impl3[DenseMatrix[Double], DenseVector[Double], Array[Double], LeastSquaresRegressionResult[Double]] =
    (data: DenseMatrix[Double], outputs: DenseVector[Double], workArray: Array[Double]) => leastSquaresImplementation.doLeastSquaresDouble(data, outputs, workArray)

  implicit val matrixVectorWithWorkArrayFloat
    : Impl3[DenseMatrix[Float], DenseVector[Float], Array[Float], LeastSquaresRegressionResult[Float]] =
    (data: DenseMatrix[Float], outputs: DenseVector[Float], workArray: Array[Float]) => leastSquaresImplementation.doLeastSquaresFloat(data, outputs, workArray)

  implicit val matrixVectorSpecifiedWorkDouble
    : Impl3[DenseMatrix[Double], DenseVector[Double], Int, LeastSquaresRegressionResult[Double]] =
    (data: DenseMatrix[Double], outputs: DenseVector[Double], workSize: Int) => leastSquaresImplementation.doLeastSquaresDouble(data, outputs, new Array[Double](workSize))

  implicit val matrixVectorSpecifiedWorkFloat
    : Impl3[DenseMatrix[Float], DenseVector[Float], Int, LeastSquaresRegressionResult[Float]] =
    (data: DenseMatrix[Float], outputs: DenseVector[Float], workSize: Int) => leastSquaresImplementation.doLeastSquaresFloat(data, outputs, new Array[Float](workSize))

  implicit val matrixVectorDouble
    : Impl2[DenseMatrix[Double], DenseVector[Double], LeastSquaresRegressionResult[Double]] =
    (data: DenseMatrix[Double], outputs: DenseVector[Double]) => leastSquaresImplementation.doLeastSquaresDouble(data,
      outputs,
      new Array[Double](math.max(1, data.rows * data.cols * 2))
    )

  implicit val matrixVectorFloat: Impl2[DenseMatrix[Float], DenseVector[Float], LeastSquaresRegressionResult[Float]] =
    (data: DenseMatrix[Float], outputs: DenseVector[Float]) => leastSquaresImplementation.doLeastSquaresFloat(data,
      outputs,
      new Array[Float](math.max(1, data.rows * data.cols * 2))
    )
}
