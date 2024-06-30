package breeze.polynomial

import algebra.instances.all.doubleAlgebra
import breeze.benchmark._
import breeze.numerics._
import breeze.math._
import breeze.linalg.BuildsRandomVectors
import breeze.stats.distributions._
import spire.math._
import spire.math.poly._
import breeze.macros._
import com.google.caliper.Benchmark

object DensePolynomialBenchmark extends MyRunner(classOf[DensePolynomialBenchmark])

class DensePolynomialBenchmark extends BreezeBenchmark with BuildsRandomVectors {
  protected implicit val randBasis: RandBasis = RandBasis.mt0

  def randomPoly(order: Int): PolyDense[Double] = {
    val uniform = Uniform(0, 1)
    val array = new Array[Double](order)
    var i = 0
    while (i < order) {
      array(i) = uniform.draw()
      i += 1
    }
    Polynomial.dense(array)
  }

  @Benchmark
  def timePolyOnDenseVector(reps: Int) =
    runWith2(reps, { randomPoly(10) }, { randomArray(1024 * 4) })((poly, arr) => {
      poly(arr)
    })

  @Benchmark
  def timePolyOnDenseMatrix(reps: Int) =
    runWith2(reps, { randomPoly(10) }, { randomMatrix(256, 256) })((poly, arr) => {
      poly(arr)
    })

}
