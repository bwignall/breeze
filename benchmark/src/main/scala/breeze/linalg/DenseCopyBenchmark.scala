package breeze.linalg

import breeze.benchmark.BreezeBenchmark
import breeze.benchmark.MyRunner
import breeze.linalg.DenseVector
import breeze.macros._

/**
 * Created by dlwh on 8/14/15.
 */
class DenseCopyBenchmark extends BreezeBenchmark {
  assert(usingNatives)

  val dv, dv2 = DenseVector.rand(100000).apply(0 to -1 by 2)

  def timeSmallDVset(reps: Int): DenseVector[Double] = {
    cforRange(0 until reps) { _ =>
      dv := dv2
    }
    dv2
  }

  def timeSmallDVInlineRange(reps: Int): DenseVector[Double] = {
    cforRange(0 until reps) { _ =>
      val ad = dv.data
      val bd = dv2.data
      cforRange(0 until dv.length) { i =>
        ad(2 * i) = bd(2 * i)
      }
    }
    dv
  }

  def timeDVMemcopy(reps: Int): DenseVector[Double] = {
    cforRange(0 until reps) { _ =>
      val ad = dv.data
      val bd = dv2.data
      System.arraycopy(ad, 0, bd, 0, ad.length)
    }
    dv
  }

  def timeSmallDVSetInline(reps: Int): DenseVector[Double] = {
    cforRange(0 until reps) { _ =>
      dv(0) = dv2(0)
      dv(1) = dv2(1)
      dv(2) = dv2(2)
      dv(3) = dv2(3)
      dv(4) = dv2(4)
    }
    dv
  }

}

object DenseCopyBenchmark extends MyRunner(classOf[DenseCopyBenchmark])
