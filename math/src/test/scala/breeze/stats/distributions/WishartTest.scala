package breeze.stats.distributions

import breeze.linalg._
import breeze.numerics._
import breeze.stats._
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.Checkers

class WishartTest extends AnyFunSuite with Checkers {
  val numSamples = 1000
  val dim = 4
  implicit val basis: RandBasis = RandBasis.withSeed(0)
  implicit val arbWishart: Arbitrary[Wishart] = Arbitrary {
    for {
      nu <- Gen.choose(dim + 0.1, dim + 3.0)
      s <- RandomInstanceSupport.genPositiveDefiniteMatrix(dim)
    } yield {
      Wishart(nu, s)
    }
  }
  test("mean") {
    check(Prop.forAll { ((distr: Wishart)) =>
      val sample = distr.sample(numSamples)
      val m = mean(sample)(mean.canMeanGeneric[IndexedSeq[DenseMatrix[Double]], DenseMatrix[Double]])
      if (max(abs(m - distr.mean)) / (max(abs(distr.mean)).max(1)) > 1e-1) {
        println("Expected " + distr.mean + " but got " + m)
        false
      } else {
        true
      }
    })
  }
}
