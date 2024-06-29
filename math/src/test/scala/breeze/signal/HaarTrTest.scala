package breeze.signal

import breeze.linalg.DenseVector
import breeze.linalg.norm
import org.scalatest._
import org.scalatest.funsuite._

/**
 * Test for correctness of the haar transform
 */

class HaarTrTest extends AnyFunSuite {

  // Test Values
  private val testNormThreshold = 1e-12

  private val test16: DenseVector[Double] = DenseVector[Double](0.814723686393179, 0.905791937075619, 0.126986816293506,
                                                                0.913375856139019, 0.63235924622541, 0.0975404049994095,
                                                                0.278498218867048, 0.546881519204984, 0.957506835434298,
                                                                0.964888535199277, 0.157613081677548, 0.970592781760616,
                                                                0.957166948242946, 0.485375648722841, 0.8002804688888,
                                                                0.141886338627215)

  private val test16haarTransformed: DenseVector[Double] = DenseVector[Double](
    2.43786708093792887498, -0.27978823833884162499, 0.42624358112555448700, 0.23542831411988068719,
    0.34007647551813650000, -0.04774004342360625000, 0.39709475359770550000, 0.25018789472488599999,
    -0.06439497760834975834, -0.55606102272554036332, 0.37817402933723643789, -0.18977565162618037741,
    -0.00521964996049979503, -0.57486345889574299652, 0.33360682719547979029, 0.46555495420138585238
  )

  test("haarTr 1d of DenseVector[Double]") {
    assert(norm(haarTr(test16) - test16haarTransformed) < testNormThreshold)
  }
  test("iHaarTr 1d of DenseVector[Double]") {
    assert(norm(iHaarTr(test16haarTransformed) - test16) < testNormThreshold)
  }

}
