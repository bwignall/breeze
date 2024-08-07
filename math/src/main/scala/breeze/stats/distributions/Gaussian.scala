package breeze.stats
package distributions

/*
 Copyright 2009 David Hall, Daniel Ramage

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

import breeze.numerics._
import breeze.optimize.DiffFunction

import math.{log1p, Pi}

/**
 * Represents a Gaussian distribution over a single real variable.
 *
 * @author dlwh
 */
case class Gaussian(mu: Double, sigma: Double)(implicit rand: RandBasis)
    extends ContinuousDistr[Double]
    with Moments[Double, Double]
    with HasCdf
    with HasInverseCdf {
  private val inner = rand.gaussian(mu, sigma)
  def draw(): Double = inner.get()

  override def toString(): String = "Gaussian(" + mu + ", " + sigma + ")"

  /**
   * Computes the probability that a Gaussian variable Z is within the interval [x, y].
   * This probability is computed as P[Z < y] - P[Z < x].
   * @param x lower-end of the interval
   * @param y upper-end of the interval
   * @return probability that the Gaussian random variable Z lies in the interval [x, y]
   */
  override def probability(x: Double, y: Double): Double = {
    require(x <= y, "Undefined probability: lower-end of the interval should be smaller than its upper-end")
    cdf(y) - cdf(x)
  }

  /**
   * Computes the inverse cdf of the p-value for this gaussian.
   *
   * @param p: a probability in [0,1]
   * @return x s.t. cdf(x) = numYes
   */
  def inverseCdf(p: Double): Double = {
    require(p >= 0)
    require(p <= 1)

    mu + sigma * Gaussian.sqrt2 * erfinv(2 * p - 1)
  }

  /**
   * Computes the cumulative density function of the value x.
   */
  def cdf(x: Double): Double = .5 * (1 + erf((x - mu) / (Gaussian.sqrt2 * sigma)))

  override def unnormalizedLogPdf(t: Double): Double = {
    val d = (t - mu) / sigma
    -d * d / 2.0
  }

  override lazy val normalizer: Double = 1.0 / sqrt(2 * Pi) / sigma
  lazy val logNormalizer: Double = log(sqrt(2 * Pi)) + log(sigma)

  def mean = mu
  def variance: Double = sigma * sigma
  def mode = mean
  def entropy: Double = log(sigma) + .5 * log1p(log(math.Pi * 2))
}

object Gaussian extends ExponentialFamily[Gaussian, Double] with ContinuousDistributionUFuncProvider[Double, Gaussian] {
  private val sqrt2 = math.sqrt(2.0)

  type Parameter = (Double, Double)
  import breeze.stats.distributions.{SufficientStatistic => BaseSuffStat}

  /**
   * @param n running total of examples
   * @param mean running mean
   * @param M2 running variance * n
   */
  final case class SufficientStatistic(n: Double, mean: Double, M2: Double) extends BaseSuffStat[SufficientStatistic] {
    // multiply M2 (which is variance * n)
    def *(weight: Double) = SufficientStatistic(n * weight, mean, M2 * weight)

    // Due to Chan
    def +(t: SufficientStatistic) = {
      val delta = t.mean - mean
      val newMean = mean + delta * (t.n / (t.n + n))
      val newM2 = M2 + t.M2 + delta * delta * (t.n * n) / (t.n + n)
      SufficientStatistic(t.n + n, newMean, newM2)
    }

    def variance: Double = M2 / n
  }

  val emptySufficientStatistic: SufficientStatistic = SufficientStatistic(0, 0, 0)

  def sufficientStatisticFor(t: Double): SufficientStatistic = {
    SufficientStatistic(1, t, 0)
  }

  def mle(stats: SufficientStatistic): Parameter = (stats.mean, stats.variance)

  override def distribution(p: (Double, Double))(implicit rand: RandBasis) = new Gaussian(p._1, math.sqrt(p._2))

  def likelihoodFunction(stats: SufficientStatistic): DiffFunction[(Double, Double)] = new DiffFunction[Parameter] {
    val normPiece = math.log(2 * Pi)
    def calculate(x: (Double, Double)) = {
      val (mu, sigma2) = x
      val SufficientStatistic(n, mean, _) = stats
      val variance = stats.variance
      if (sigma2 <= 0) (Double.PositiveInfinity, (Double.NaN, Double.NaN))
      else {
        val objective = n * ((variance + mean * mean) / sigma2 / 2
          - mean * mu / sigma2
          + mu * mu / sigma2 / 2
          + .5 * (math.log(sigma2) + normPiece))
        val gradientMu = n * (-mean / sigma2 + mu / sigma2)
        val gradientSig = n * (-(variance + mean * mean) / sigma2 / sigma2 / 2
          + mean * mu / sigma2 / sigma2
          - mu * mu / sigma2 / sigma2 / 2
          + .5 / sigma2)
        (objective, (gradientMu, gradientSig))
      }
    }
  }
}
