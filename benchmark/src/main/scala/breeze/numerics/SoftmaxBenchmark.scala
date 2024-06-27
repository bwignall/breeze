package breeze.numerics

import breeze.benchmark.BreezeBenchmark
import breeze.benchmark.MyRunner
import breeze.linalg.DenseVector
import breeze.linalg.softmax
import breeze.macros._
import breeze.stats.distributions.Rand

/**
 * Created by dlwh on 10/3/15.
 */
class SoftmaxBenchmark extends BreezeBenchmark {
  val dv: DenseVector[Float] = DenseVector.rand(5000, Rand.uniform.map(_.toFloat))

  def timeSoftmaxFloat(reps: Int): Double = {
    var sum = 0.0
    cforRange(0 until reps) { _ =>
      sum += softmax(dv)
    }

    sum
  }

}

object SoftmaxBenchmark extends MyRunner(classOf[SoftmaxBenchmark])
