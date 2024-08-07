package breeze.linalg
package examples

import breeze.benchmark.BreezeBenchmark
import breeze.benchmark.MyRunner
import breeze.linalg.DenseVector
import breeze.numerics.*
import com.google.caliper.Benchmark

/**
 * Created by dlwh on 8/20/15.
 *
 * Based on code from Ivan Nikolaev
 */
class GaussMixtureBenchmark extends BreezeBenchmark {

  val x: DenseVector[Double] = DenseVector(5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0)
  val c: DenseVector[Double] = DenseVector(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)
  private val gamma = 5.0
  private val n: Int = 1000

  @Benchmark
  def timeGMMVectors(reps: Int): Unit = {
    val denseVectors = IndexedSeq.fill(n)(x)
    (0 until reps).foreach { _ =>
      GaussMixtureTransform.samplesTransform(denseVectors, c, gamma)
    }
  }

  @Benchmark
  def timeGMMMat(reps: Int): Unit = {
    val matrix = DenseMatrix.fill(n, 10)(5.0)
    (0 until reps).foreach { _ =>
      GaussMixtureTransform.samplesTransform(matrix, c, gamma)
    }
  }

  @Benchmark
  def timeGMMMatColMajor(reps: Int): Unit = {
    val matrix = DenseMatrix.fill(10, n)(5.0)
    (0 until reps).foreach { _ =>
      GaussMixtureTransform.samplesTransformColMajor(matrix, c, gamma)
    }
  }

  @Benchmark
  def timeCenterMat(reps: Int): Unit = {
    val matrix = DenseMatrix.fill(n, 10)(5.0)
    (0 until reps).foreach { _ =>
      matrix(*, ::) - c
    }
  }

  @Benchmark
  def timeCenterMatColMajor(reps: Int): Unit = {
    val matrix = DenseMatrix.fill(10, n)(5.0)
    (0 until reps).foreach { _ =>
      matrix(::, *) - c
    }
  }

  @Benchmark
  def timeCenterVector(reps: Int): Unit = {
    val denseVectors = IndexedSeq.fill(n)(x)
    (0 until reps).foreach { _ =>
      denseVectors.foreach(_ - c)
    }
  }
}

object GaussMixtureTransform {
  def sampleTransform(sample: DenseVector[Double], centers: DenseVector[Double], gamma: Double): Double = {
    val diff: DenseVector[Double] = sample - centers
    exp(-gamma * diff.dot(diff))
  }

  def samplesTransform(samples: Iterable[DenseVector[Double]], centers: DenseVector[Double], gamma: Double): Double = {
    samples.map((sample: DenseVector[Double]) => sampleTransform(sample, centers, gamma)).sum
  }

  def samplesTransform(samples: DenseMatrix[Double], centers: DenseVector[Double], gamma: Double): Double = {
    val diff: DenseMatrix[Double] = samples(*, ::) - centers
    val prod = diff :*= diff
    val sum1: DenseVector[Double] = sum(prod, Axis._1) *= (-gamma)
    val exped = exp(sum1)
    val sum2 = sum(exped)
    sum2
//    sum(exp(sum(diff :*= diff, Axis._1) *= (-gamma)))
  }

  def samplesTransformColMajor(samples: DenseMatrix[Double], centers: DenseVector[Double], gamma: Double): Double = {
    val diff: DenseMatrix[Double] = samples(::, *) - centers
    val prod = diff :*= diff
    val sum1 = sum(prod, Axis._0) *= (-gamma)
    val exped = exp(sum1)
    val sum2 = sum(exped)
    sum2
    //    sum(exp(sum(diff :*= diff, Axis._1) *= (-gamma)))
  }
}

object GaussMixtureBenchmark extends MyRunner(classOf[GaussMixtureBenchmark])
