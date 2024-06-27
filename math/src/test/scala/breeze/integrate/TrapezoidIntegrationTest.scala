package breeze.integrate

import breeze.integrate
import breeze.linalg._
import breeze.numerics._
import org.scalatest.funsuite.AnyFunSuite

/**
 *
 * @author chrismedrela
 **/
class TrapezoidIntegrationTest extends AnyFunSuite {
  val f: Double => Double = ((x: Double)) => 2 * x
  val f2: Double => Double = ((x: Double)) => x * x

  test("basics") {
    assert(closeTo(integrate.trapezoid(f, 0, 1, 2), 1))
    assert(closeTo(integrate.trapezoid(f, 0, 1, 3), 1))
    assert(closeTo(integrate.trapezoid(f2, 0, 1, 2), 0.5))
    assert(closeTo(integrate.trapezoid(f2, 0, 1, 3), 0.375))
  }

  test("not enough nodes") {
    intercept[Exception] {
      integrate.trapezoid(f, 0, 1, 1)
    }
  }
}
