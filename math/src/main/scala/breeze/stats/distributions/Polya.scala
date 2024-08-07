package breeze.stats.distributions

import breeze.linalg.Counter
import breeze.linalg.DenseVector
import breeze.linalg.sum
import breeze.math.MutableEnumeratedCoordinateField
import breeze.numerics._
import breeze.util.ArrayUtil

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
 * Represents a Polya distribution, a.k.a Dirichlet compound Multinomial distribution
 * see
 * http://en.wikipedia.org/wiki/Multivariate_Polya_distribution
 *
 * @author dlwh
 */
class Polya[T, @specialized(Int) I](params: T)(implicit
  space: MutableEnumeratedCoordinateField[T, I, Double],
  rand: RandBasis
) extends DiscreteDistr[I] {
  import space._
  private val innerDirichlet = new Dirichlet(params)
  def draw(): I = {
    Multinomial(innerDirichlet.draw()).draw()
  }

  lazy val logNormalizer: Double = -lbeta(params)

  def probabilityOf(x: I): Double = math.exp(lbeta(sum(params), 1.0) - lbeta(params(x), 1.0))

//  def probabilityOf(x: T) = math.exp(logProbabilityOf(x))
//  def logProbabilityOf(x: T) = {
//    math.exp(unnormalizedLogProbabilityOf(x) + logNormalizer)
//  }
//
//  def unnormalizedLogProbabilityOf(x: T):Double = {
//    val adjustForCount = ev(x).valuesIterator.foldLeft(lgamma(ev(x).sum+1))( (acc,v) => acc-lgamma(v+1))
//    adjustForCount + lbeta(ev(x + params))
//  }

}

object Polya {

  /**
   * Creates a new symmetric Polya of dimension k
   */
  def sym(alpha: Double, k: Int)(implicit rand: RandBasis): Polya[DenseVector[Double], Int] =
    this(ArrayUtil.fillNewArray(k, alpha))

  /**
   * Creates a new Polya of dimension k with the given parameters
   */
  def apply(arr: Array[Double])(implicit rand: RandBasis): Polya[DenseVector[Double], Int] = {
    new Polya(new DenseVector(arr))
  }
}
