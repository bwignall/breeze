package breeze.linalg

import breeze.benchmark.*
import breeze.stats.distributions.*
import breeze.macros.*
import com.google.caliper.Benchmark

object DenseMatrixBenchmark extends MyRunner(classOf[DenseMatrixBenchmark])

trait BuildsRandomMatrices {
  def randomMatrix(m: Int, n: Int, transpose: Boolean = false): DenseMatrix[Double] = {
    if (!transpose) {
      DenseMatrix.rand[Double](m, n)
    } else {
      DenseMatrix.rand[Double](m, n).t
    }
  }

  def randomIntMatrix(m: Int, n: Int, transpose: Boolean = false): DenseMatrix[Int] = {
    if (!transpose) {
      DenseMatrix.rand[Int](m, n, rand = Rand.randInt(200))
    } else {
      DenseMatrix.rand[Int](m, n, Rand.randInt(200)).t
    }
  }
}

class DenseMatrixBenchmark extends BreezeBenchmark with BuildsRandomMatrices {
  @Benchmark
  def timeIntMatrixMultiply(reps: Int): DenseMatrix[Int] = runWith(reps, randomIntMatrix(2500, 2500)) { dm =>
    dm * dm
  }
}
