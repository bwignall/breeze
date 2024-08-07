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

import breeze.linalg.DenseVector
import breeze.linalg.SparseVector
import breeze.linalg.softmax
import org.scalatest._
import org.scalatest.funsuite._
import org.scalatestplus.scalacheck._

import math.{abs, exp}

class DirichletTest extends AnyFunSuite with Checkers {
  implicit val rand: RandBasis = RandBasis.mt0

  test("logDraw for small values") {
    val g = new Dirichlet(DenseVector(1e-5, 5.0, 50.0))
    assert(Array.fill(1000)(g.logDraw()).forall(_(0) > Double.NegativeInfinity))
  }

  test("logDraw of SparseVector") {
    val g: Dirichlet[SparseVector[Double], Int] = new Dirichlet(SparseVector(7)(1 -> 1e-5, 3 -> 5.0, 5 -> 50.0))
    Array.fill(1000)(g.logDraw()).foreach { (d: SparseVector[Double]) =>
      assert(d(1) > Double.NegativeInfinity)
      assert(d.activeSize == 3)
      assert(abs(exp(softmax(d.activeValuesIterator)) - 1.0) < 0.0000001, d)
    }
  }

}
