package breeze.stats.hypothesis

import org.scalatest._
import org.scalatest.funsuite._

import matchers.should.Matchers._

class TTestTest extends AnyFunSuite {
  val threshold = 0.01
  test("T Test two sample") {
    tTest(List(1.0, 1, 2, 3), List(9.0, 9, 8, 9)) should be(4.752e-5 +- threshold)
  }
  test("T Test one sample") {
    tTest(Array(1.0, 1, 2, 3)) should be(0.982 +- threshold)
  }
  test("T Test one sample for Traversable") {
    // This test is designed to detect this bug, just in case a refactoring re-introduces it: https://github.com/scalanlp/breeze/issues/486
    tTest(List(1.0, 1, 2, 3)) should be(0.982323 +- threshold)
  }
  test("T Test one sample should throw error when given vector of length 1") {
    intercept[IllegalArgumentException] {
      tTest(Array(1.0))
    }
  }
  test("T Test two sample should throw error when given vector of length 1") {
    intercept[IllegalArgumentException] {
      tTest(Array(1.0, 2), Array(9.0))
    }
  }
}
