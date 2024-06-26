package breeze.stats
package distributions

import breeze.linalg._
import breeze.numerics.log
import breeze.numerics.multidigammalog

case class InvWishart(df: Double, scale: DenseMatrix[Double])(implicit rand: RandBasis)
    extends ContinuousDistr[DenseMatrix[Double]]
    with Moments[DenseMatrix[Double], DenseMatrix[Double]] {
  private val p = scale.rows
  require(scale.rows == scale.cols, "The scale matrix must be square")

  private val inverseScale = inv(scale)
  private val w = Wishart(df, inverseScale)

  def mean: DenseMatrix[Double] = {
    require(df > p + 1)
    scale /:/ (df - p - 1)
  }

  def variance: DenseMatrix[Double] = {
    require(df > p + 3)
    val a = (scale *:* scale) *:* (df - p + 1)
    val d = diag(scale).toDenseMatrix
    val b = (d.t * d) *:* (df - p - 1)

    (a + b) /:/ ((df - p) * (df - p - 1) * (df - p - 1) * (df - p - 3))
  }

  def entropy: Double = Double.NaN

  def mode: DenseMatrix[Double] = scale /:/ (df - p - 1)

  def unnormalizedLogPdf(x: DenseMatrix[Double]): Double = (
    -log(det(x)) * 0.5 * (df + p + 1)
      - 0.5 * trace(scale * inv(x))
  )

  def logNormalizer: Double = {
    (
      -log(2) * 0.5 * (df * p)
        - multidigammalog(0.5 * df, p)
        - log(det(scale)) * 0.5 * df
    )
  }

  def draw(): DenseMatrix[Double] = inv(w.draw())
}
