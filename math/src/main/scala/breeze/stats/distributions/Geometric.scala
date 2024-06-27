package breeze.stats.distributions

import breeze.optimize.DiffFunction
import breeze.util._

import runtime.ScalaRunTime

/**
 * The Geometric distribution calculates the number of trials until the first success, which
 * happens with probability p.
 * @author dlwh
 */
case class Geometric(p: Double)(implicit rand: RandBasis) extends DiscreteDistr[Int] with Moments[Double, Double] {
  require(p >= 0)
  require(p <= 1)

  def draw(): Int = {
    // from "Random Number Generation and Monte Carlo Methods"
    if (p < 1.0 / 3.0) math.ceil(math.log(rand.uniform.draw()) / math.log(1 - p)).toInt
    else {
      // look at the cmf
      var i = 1
      while (rand.uniform.draw() > p) {
        i += 1
      }
      i
    }
  }

  def probabilityOf(x: Int): Double = math.pow(1 - p, x) * p

  def mean: Double = 1 / p

  def variance: Double = (1 - p) / (p * p)

  def mode = 1
  def entropy: Double = (-(1 - p) * math.log(1 - p) - p * math.log(p)) / p

  override def toString(): String = ScalaRunTime._toString(this)
}

object Geometric extends ExponentialFamily[Geometric, Int] with HasConjugatePrior[Geometric, Int] {
  type Parameter = Double
  case class SufficientStatistic(sum: Double, n: Double)
      extends breeze.stats.distributions.SufficientStatistic[SufficientStatistic] {
    def +(t: SufficientStatistic) = SufficientStatistic(sum + t.sum, n + t.n)

    def *(weight: Double) = SufficientStatistic(sum * weight, n * weight)
  }

  def emptySufficientStatistic: SufficientStatistic = SufficientStatistic(0, 0)

  def sufficientStatisticFor(t: Int): SufficientStatistic = SufficientStatistic(t, 1)

  def mle(stats: SufficientStatistic): Parameter = stats.n / stats.sum

  def likelihoodFunction(stats: SufficientStatistic): DiffFunction[Parameter] = new DiffFunction[Geometric.Parameter] {
    def calculate(p: Geometric.Parameter) = {
      val obj = stats.n * math.log(p) + stats.sum * math.log(1 - p)
      val grad = stats.n / p - stats.sum / (1 - p)
      (-obj, -grad)

    }
  }

  override def distribution(p: Geometric.Parameter)(implicit rand: RandBasis) = new Geometric(p)

  type ConjugatePrior = Beta
  val conjugateFamily: Beta.type = Beta

  def predictive(parameter: conjugateFamily.Parameter)(implicit basis: RandBasis) = ???

  def posterior(prior: conjugateFamily.Parameter, evidence: TraversableOnce[Int]): (Double, Double) = {
    evidence.foldLeft(prior) { (acc, x) =>
      (acc._1 + 1, acc._2 + x)
    }
  }
}
