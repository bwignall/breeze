package breeze.stats
package distributions

import breeze.linalg.Axis._1
import breeze.linalg.Counter
import breeze.numerics._
import breeze.optimize.DiffFunction

import scala.collection.compat._

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

/**
 * A Bernoulli distribution represents a distribution over weighted coin flips
 *
 * @author dlwh
 * @param p the probability of true
 */
case class Bernoulli(p: Double)(implicit rand: RandBasis) extends DiscreteDistr[Boolean] with Moments[Double, Double] {
  require(p >= 0.0)
  require(p <= 1.0)
  def probabilityOf(b: Boolean): Double = if (b) p else 1 - p

  override def draw(): Boolean = {
    rand.uniform.draw() < p
  }

  override def toString(): String = "Bernoulli(" + p + ")"

  def mean = p
  def variance: Double = p * (1 - p)
  def mode: Double = I(p >= 0.5)
  def entropy: Double = -p * math.log(p) - (1 - p) * math.log1p(-p)
}

object Bernoulli extends ExponentialFamily[Bernoulli, Boolean] with HasConjugatePrior[Bernoulli, Boolean] {
  type ConjugatePrior = Beta
  val conjugateFamily: Beta.type = Beta

  override def predictive(parameter: Beta.Parameter)(implicit basis: RandBasis) = new Polya(
    Counter(true -> parameter._1, false -> parameter._2)
  )

  override def posterior(prior: Beta.Parameter, evidence: IterableOnce[Boolean]): (Double, Double) = {
    evidence.foldLeft(prior) { (acc, ev) =>
      if (ev) acc.copy(_1 = acc._1 + 1)
      else acc.copy(_2 = acc._2 + 1)
    }
  }

  type Parameter = Double
  case class SufficientStatistic(numYes: Double, n: Double)
      extends distributions.SufficientStatistic[SufficientStatistic] {
    def *(weight: Double) = SufficientStatistic(numYes * weight, n * weight)
    def +(t: SufficientStatistic) = SufficientStatistic(numYes + t.numYes, n + t.n)
  }

  def emptySufficientStatistic: SufficientStatistic = SufficientStatistic(0, 0)

  def sufficientStatisticFor(t: Boolean): SufficientStatistic = SufficientStatistic(I(t), 1)

  def mle(stats: SufficientStatistic): Parameter = stats.numYes / stats.n

  override def distribution(p: Double)(implicit rand: RandBasis) = new Bernoulli(p)

  def likelihoodFunction(stats: SufficientStatistic): DiffFunction[Parameter] = new DiffFunction[Double] {
    val SufficientStatistic(yes, num) = stats
    val no = num - yes
    def calculate(p: Double) = {
      import math._
      val obj = yes * log(p) + no * log1p(-p)
      val grad = yes / p - no / (1 - p)
      (-obj, -grad)
    }
  }
}
