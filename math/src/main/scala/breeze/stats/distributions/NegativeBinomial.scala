package breeze.stats.distributions

import breeze.numerics._

/**
 * Negative Binomial Distribution
 * @param r number of failures until stop
 * @param p prob of success
 * @author dlwh
 */
case class NegativeBinomial(r: Double, p: Double)(implicit rand: RandBasis) extends DiscreteDistr[Int] {
  private val gen = for {
    lambda <- Gamma(r, p / (1 - p))
    i <- Poisson(lambda)
  } yield i
  def draw(): Int = gen.draw()

  def probabilityOf(x: Int): Double = exp(logProbabilityOf(x))

  override def logProbabilityOf(k: Int): Double = {
    lgamma(r + k) - lgamma(k + 1) - lgamma(r) + r * math.log(1 - p) + k * math.log(p)
  }
}
