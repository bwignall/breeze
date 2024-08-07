package breeze.stats.distributions

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

import breeze.stats.distributions.LogNormal
import org.scalacheck.Arbitrary
import org.scalacheck._
import org.scalatest._
import org.scalatest.funsuite._
import org.scalatestplus.scalacheck._

class LogNormalTest
    extends AnyFunSuite
    with Checkers
    with UnivariateContinuousDistrTestBase
    with MomentsTestBase[Double]
    with ExpFamTest[LogNormal, Double]
    with HasCdfTestBase {
  import Arbitrary.arbitrary
  val expFam: LogNormal.type = LogNormal

  def arbParameter: Arbitrary[(Double, Double)] = Arbitrary {
    for (
      mean <- arbitrary[Double].map { _ % 10.0 };
      std <- arbitrary[Double].map { x =>
        math.abs(x) % 8.0 + .1
      }
    ) yield (mean, std)
  }

  def paramsClose(p: (Double, Double), b: (Double, Double)): Boolean = {
    val y1 = (p._1 - b._1).abs / (p._1.abs / 2 + b._1.abs / 2 + 1) < 1e-1
    val y2 = (p._2 - b._2).abs / (p._2.abs / 2 + b._2.abs / 2 + 1) < 1e-1
    y1 && y2
  }

  override val VARIANCE_TOLERANCE: Double = 9e-2

  type Distr = LogNormal

  implicit def arbDistr: Arbitrary[LogNormal] = Arbitrary {
    // make scala 2 happy
    implicit val basis: RandBasis = RandBasis.mt0
    for (
      mean <- arbitrary[Double].map { x =>
        math.abs(x) % 10.0
      };
      std <- arbitrary[Double].map { x =>
        math.abs(x) % 1.0 + .1
      }
    ) yield new LogNormal(mean, std)
  }

  def asDouble(x: Double) = x

  def fromDouble(x: Double) = x

  test("pdf should return the correct value") {
    val dist = LogNormal(0.0, 1.0)
    assert(dist.pdf(1.0) == (1.0 / Math.sqrt(2 * Math.PI)));
  }

  test("the probability of non-positive numbers should be 0.0") {
    val dist = LogNormal(0.0, 1.0)
    assert(dist.pdf(0.0) == 0.0)
    assert(dist.pdf(-1.0) == 0.0)
  }

}
