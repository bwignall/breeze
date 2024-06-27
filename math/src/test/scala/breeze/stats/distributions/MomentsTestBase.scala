package breeze.stats
package distributions

import org.scalacheck._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck._

trait RandTestBase extends AnyFunSuite with Checkers {
  implicit val basis: RandBasis = RandBasis.mt0
  val numSamples = 10000

}

/**
 * @author dlwh
 */
trait MomentsTestBase[T] extends RandTestBase {
  type Distr <: Density[T] with Rand[T] with Moments[Double, Double]
  implicit def arbDistr: Arbitrary[Distr]

  def asDouble(x: T): Double
  def fromDouble(x: Double): T

  def numFailures: Int = 2

  test("mean") {
    check(Prop.forAll { (distr: Distr) =>
      val sample = distr.sample(numSamples).map(asDouble _)
      val m = mean(sample)
      if ((m - distr.mean).abs / (m.abs.max(1)) > 1e-1) {
        println("MExpected " + distr.mean + " but got " + m)
        false
      } else {
        true
      }

    })
  }

  val VARIANCE_TOLERANCE = 5e-2
  test("variance") {
    check(Prop.forAll { (distr: Distr) =>
      // try twice, and only fail if both fail.
      // just a little more robustness...
      Iterator.range(0, numFailures).exists { _ =>
        val sample = distr.sample(numSamples).map(asDouble _)
        val vari = variance(sample)

        if ((vari - distr.variance).abs / (vari.max(1)) > VARIANCE_TOLERANCE) {
          println("Expected " + distr.variance + " but got " + vari)
          false
        } else true
      }
    })
  }

  test("mode") {
    check(Prop.forAll { (distr: Distr) =>
      val sample = distr.sample(40)
      val probMode = distr(fromDouble(distr.mode))
      sample.find(x => probMode < distr(x) - 1e-4) match {
        case Some(x) => println(s"$x has higher prob (${distr(x)}) than mode ${distr.mode} ($probMode)"); false
        case None    => true
      }
    })
  }

}
