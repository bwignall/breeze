package breeze.stats.distributions

/*
 Copyright 2014 Jacob Andreas

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

import breeze.linalg.DenseVector
import org.scalatest._
import org.scalatest.funsuite._
import org.scalatestplus.scalacheck._

import matchers.should.Matchers._

class MultinomialTest extends AnyFunSuite with Checkers {
  // can't use the standard moment tester tools for a categorial distribution, so let's just roll our ownkj

  def TestDist: DenseVector[Double] = DenseVector[Double](0.2, 0.5, 0.3)
  def TestParams: DenseVector[Double] = TestDist * 2.0
  def NSamples: Int = 1e7.toInt
  implicit val randBasis: RandBasis = RandBasis.mt0

  test("multinomial with naive sampling") {
    val mult = new Multinomial(TestParams)
    val accumNaive = DenseVector.zeros[Double](3)
    (0 until NSamples).foreach { i =>
      accumNaive(mult.drawNaive()) += 1
    }
    accumNaive /= NSamples.toDouble
    accumNaive(2) should be(TestDist(2) +- 1e-3)
  }

  test("multinomial with alias sampling") {
    val mult = new Multinomial(TestParams)
    val accumAlias = DenseVector.zeros[Double](3)
    (0 until NSamples).foreach { i =>
      accumAlias(mult.draw()) += 1
    }
    accumAlias /= NSamples.toDouble
    accumAlias(2) should be(TestDist(2) +- 1e-3)
  }

  test("multinomial negative indexes") {
    val mult = new Multinomial(TestParams)
    assert(mult.probabilityOf(-1) == 0.0)
  }
}
