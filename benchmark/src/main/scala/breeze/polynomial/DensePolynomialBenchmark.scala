package breeze.polynomial

import algebra.instances.all.doubleAlgebra
import breeze.benchmark._
import breeze.linalg.BuildsRandomVectors
import breeze.linalg.DenseMatrix
import breeze.macros._
import breeze.stats.distributions._
import com.google.caliper.Benchmark
import spire.math._
import spire.math.poly._

object DensePolynomialBenchmark extends MyRunner(classOf[DensePolynomialBenchmark])

class DensePolynomialBenchmark extends BreezeBenchmark with BuildsRandomVectors {
  implicit override val randBasis: RandBasis = RandBasis.mt0

  def randomPoly(order: Int): PolyDense[Double] = {
    val uniform = Uniform(0, 1)
    val array = new Array[Double](order)
    var i = 0
    while (i < order) {
      array(i) = uniform.draw()
      i += 1
    }
    Polynomial.dense[Double](array)
  }

  @Benchmark
  def timePolyOnDenseVector(reps: Int): Any =
    runWith2(reps, { randomPoly(10) }, { randomArray(1024 * 4) })((poly, arr) => {
      poly(arr)
    })

  @Benchmark
  def timePolyOnDenseMatrix(reps: Int): Any =
    runWith2(reps, { randomPoly(10) }, { randomMatrix(256, 256) })((poly, arr) => {
      poly(arr)
    })

}
