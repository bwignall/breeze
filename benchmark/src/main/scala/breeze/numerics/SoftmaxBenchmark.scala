package breeze.numerics

import breeze.benchmark.{BreezeBenchmark, MyRunner}
import breeze.linalg.{DenseVector, softmax}
import breeze.stats.distributions.Rand
import breeze.macros._
import com.google.caliper.Benchmark

/**
 * Created by dlwh on 10/3/15.
 */
class SoftmaxBenchmark extends BreezeBenchmark {
  val dv = DenseVector.rand(5000, Rand.uniform.map(_.toFloat))

  @Benchmark
  def timeSoftmaxFloat(reps: Int) = {
    var sum = 0.0
    cforRange(0 until reps) { _ =>
      sum += softmax(dv)
    }

    sum
  }

}

object SoftmaxBenchmark extends MyRunner(classOf[SoftmaxBenchmark])
