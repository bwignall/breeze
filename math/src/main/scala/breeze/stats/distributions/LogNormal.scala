package breeze.stats.distributions

import breeze.numerics._
import breeze.optimize.DiffFunction

import scala.math.exp
import scala.math.log
import scala.math.sqrt
import scala.runtime.ScalaRunTime

/**
 * A log normal distribution is distributed such that log X ~ Normal(\mu, \sigma)
 *
 *
 * @author dlwh
 **/
case class LogNormal(mu: Double, sigma: Double)(implicit rand: RandBasis)
    extends ContinuousDistr[Double]
    with Moments[Double, Double]
    with HasCdf
    with HasInverseCdf {
  private val myGaussian = Gaussian(mu, sigma)
  require(sigma > 0, "Sigma must be positive, but got " + sigma)

  /**
   * Gets one sample from the distribution. Equivalent to sample()
   */
  def draw(): Double = {
    exp(myGaussian.draw())
  }

  def unnormalizedLogPdf(x: Double): Double = {
    if (x <= 0.0) return Double.NegativeInfinity
    val logx = log(x)
    val rad = (logx - mu) / sigma
    -(rad * rad / 2) - logx
  }

  lazy val logNormalizer: Double = log(sqrt(2 * Math.PI)) + log(sigma)

  /**
   * Computes the inverse cdf of the p-value for this gaussian.
   *
   * @param p: a probability in [0,1]
   * @return x s.t. cdf(x) = numYes
   */
  def inverseCdf(p: Double): Double = exp(myGaussian.inverseCdf(p))

  /**
   * Computes the cumulative density function of the value x.
   */
  def cdf(x: Double): Double = myGaussian.cdf(log(x))

  override def probability(x: Double, y: Double): Double = {
    myGaussian.probability(log(x), log(y))
  }

  override def toString: String = ScalaRunTime._toString(this)

  def mean: Double = exp(mu + sigma * sigma / 2)

  def variance: Double = expm1(sigma * sigma) * exp(2 * mu + sigma * sigma)

  def entropy: Double = 0.5 + 0.5 * math.log(2 * math.Pi * sigma * sigma) + mu

  def mode: Double = exp(mu - sigma * sigma)

}

object LogNormal
    extends ExponentialFamily[LogNormal, Double]
    with ContinuousDistributionUFuncProvider[Double, LogNormal] {
  type Parameter = (Double, Double)

  import Gaussian.SufficientStatistic
  type SufficientStatistic = Gaussian.SufficientStatistic

  def emptySufficientStatistic = Gaussian.emptySufficientStatistic

  def sufficientStatisticFor(t: Double): SufficientStatistic = {
    SufficientStatistic(1, math.log(t), 0)
  }

  def mle(stats: SufficientStatistic): (Double, Double) = Gaussian.mle(stats)

  override def distribution(p: (Double, Double))(implicit rand: RandBasis) = new LogNormal(p._1, math.sqrt(p._2))

  def likelihoodFunction(stats: SufficientStatistic): DiffFunction[(Double, Double)] =
    Gaussian.likelihoodFunction(stats)
}
