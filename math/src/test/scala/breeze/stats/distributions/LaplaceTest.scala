package breeze.stats.distributions

/*
 Copyright 2009 David Hall, Daniel Ramage

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

import breeze.stats.distributions.Laplace
import org.apache.commons.math3.random.MersenneTwister
import org.scalacheck.Arbitrary
import org.scalacheck._
import org.scalatest._
import org.scalatest.funsuite._
import org.scalatestplus.scalacheck._

class LaplaceTest
    extends AnyFunSuite
    with Checkers
    with UnivariateContinuousDistrTestBase
    with MomentsTestBase[Double]
    with HasCdfTestBase {
  import Arbitrary.arbitrary

  override val numSamples = 50000

  def asDouble(x: Double) = x

  def fromDouble(x: Double) = x

  implicit def arbDistr: Arbitrary[Laplace] = Arbitrary {
    for (
      location <- arbitrary[Double].map { x =>
        math.abs(x) % 1000.0 + 1.1
      }; // Laplace pdf at 0 not defined when location == 1
      scale <- arbitrary[Double].map { x =>
        math.abs(x) % 8.0 + 1.0
      }
    ) yield new Laplace(location, scale)(new RandBasis(new MersenneTwister(0)))
  }

  override type Distr = Laplace
}
