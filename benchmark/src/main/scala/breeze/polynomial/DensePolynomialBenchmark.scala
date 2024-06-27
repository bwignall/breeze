package breeze.polynomial

import algebra.instances.all.doubleAlgebra
import breeze.benchmark.*
import breeze.linalg.{BuildsRandomVectors, DenseVector}
import breeze.stats.distributions.*
import spire.math.*
import spire.math.poly.*
import breeze.macros.*
import com.google.caliper.Benchmark

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
  def timePolyOnDenseVector(reps: Int): DenseVector[Double] =
    runWith2(reps, { randomPoly(10) }, { randomArray(1024 * 4) })((poly, arr) => {
      poly(arr)
    })

  @Benchmark
  def timePolyOnDenseMatrix(reps: Int): DenseVector[Double] =
    runWith2(reps, { randomPoly(10) }, { randomMatrix(256, 256) })((poly, arr) => {
      poly(arr)
    })

}
