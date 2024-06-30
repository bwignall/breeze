package breeze.linalg.support

import breeze.benchmark._
import breeze.linalg._
import breeze.stats.distributions.RandBasis
import com.google.caliper.Benchmark

object CanTraverseValuesBenchmark extends MyRunner(classOf[CanTraverseValuesBenchmark])

class CanTraverseValuesBenchmark extends BreezeBenchmark with BuildsRandomVectors {
  protected implicit val randBasis: RandBasis = RandBasis.mt0

  /*
  @Benchmark
  def timeSumWithCanTraverseValues(reps: Int) = runWith(reps, { randomArray(1024*8) })(arr => {
    val visitor = new ValuesVisitor[Double] {
      var sum: Double = 0
      @inline
      def visit(a: Double) = { sum += a }
      def zeros(numZero: Int, zeroValue: Double) = ()
    }
    DenseVector.canIterateValues[Double].traverse(arr, visitor)
    visitor.sum
  })

  @Benchmark
  def timeUFuncSum(reps: Int) = runWith(reps, { randomArray(1024*8) })(arr => {
    sum(arr)
  })

  @Benchmark
  def timePrimitiveSum(reps: Int) = runWith(reps, {randomArray(1024 * 8)}){ arr =>
    val d = arr.data
    var sum = 0.0
    import breeze.macros._
    cforRange(0 until d.length) {
      sum += d(_)
    }
    sum
  }

  @Benchmark
  def timeUFuncSumStrided(reps: Int) = runWith(reps, { randomArray(1024*8 * 5) })(arr => {
    sum(arr(0 to -1 by 5))
  })

  @Benchmark
  def timePrimitiveSumStrided(reps: Int) = runWith(reps, {randomArray(1024 * 8 * 5)}){ arr =>
    val d = arr.data
    var sum = 0.0
    import breeze.macros._
    cforRange(0 until d.length by 5) {
      sum += d(_)
    }
    sum
  }
   */

  /*
  @Benchmark
  def timeSumMatrix(reps: Int) = runWith(reps, {randomMatrix(1024, 40)}){ arr =>
    sum(arr)
  }

  @Benchmark
  def timeSumMatrixRows(reps: Int) = runWith(reps, {randomMatrix(1024, 40)}){ arr =>
    sum(arr(*, ::))
  }

  @Benchmark
  def timeSumMatrixRowsLoop(reps: Int) = runWith(reps, {randomMatrix(1024, 40)}){ arr =>
    val result = DenseVector.zeros[Double](1024)
    for (i <- 0 until arr.cols) {
      result += arr(::, i)
    }
    result
  }

  @Benchmark
  def timeSumMatrixCols(reps: Int) = runWith(reps, {randomMatrix(40, 1024)}){ arr =>
    sum(arr(::, *))
  }

  @Benchmark
  def timeSumMatrixColsLoop(reps: Int) = runWith(reps, {randomMatrix(40, 1024)}){ arr =>
    val result = DenseVector.zeros[Double](1024)
    for (i <- 0 until arr.rows) {
      result += arr(i, ::).t
    }
    result
  }
   */

  @Benchmark
  def timeMaxMatrixCols(reps: Int) = runWith(reps, { randomMatrix(40, 1024) }) { arr =>
    max(arr(::, *))
  }

  @Benchmark
  def timeMaxMatrixRows(reps: Int) = runWith(reps, { randomMatrix(40, 1024) }) { arr =>
    max(arr(*, ::))
  }

  @Benchmark
  def timeMinMatrixCols(reps: Int) = runWith(reps, { randomMatrix(40, 1024) }) { arr =>
    min(arr(::, *))
  }

  @Benchmark
  def timeMinMatrixRows(reps: Int) = runWith(reps, { randomMatrix(40, 1024) }) { arr =>
    max(arr(*, ::))
  }

}
