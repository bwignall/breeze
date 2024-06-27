package breeze.stats.mcmc

import breeze.benchmark.*
import breeze.stats.distributions.*
import breeze.stats.mcmc.*
import breeze.macros.cforRange
import com.google.caliper.Benchmark

object MetropolisHastingsRunner extends MyRunner(classOf[MetropolisHastingsBenchmark])

class MetropolisHastingsBenchmark extends BreezeBenchmark {

  val burnIn: Int = 1024 * 1024
  val dropCount = 25
  val numSamples: Int = 1024 * 1024
  val bufferSize: Int = 1024 * 32

  val epsilon = 1e-8
  def likelihood(x: Double): Double =
    2 * math.log1p(1 + epsilon - x) + 3 * math.log1p(
      x * x * x + epsilon
    ) // Epsilon is present to avoid throwing exceptions in the unlikely event either 0 or 1 is sampled

  def gaussianJump(x: Double): Gaussian = Gaussian(x, 1)
  def gaussianJumpLogProb(start: Double, end: Double) =
    0.0 // It would actually be this, but due to symmetry why bother? -math.pow(start-end,2)/2.0

  def pullAllSamples(m: Rand[Double]): Double = {
    var result = 0.0
    cforRange(0 until numSamples)(i => {
      result = m.draw()
    })
    result
  }

  def pullAllSamplesWithWork(m: Rand[Double]): Double = {
    var result = 0.0
    cforRange(0 until numSamples / 4) { i =>
      val x = m.draw()
      cforRange(0 until 400) { j =>
        result += math.log(math.exp(x)) / (1 + x * x)
      }
    }
    result
  }

  @Benchmark
  def timeMarkovChainEquiv(reps: Int): Double = run(reps) {
    val m =
      ArbitraryMetropolisHastings(likelihood _, gaussianJump _, gaussianJumpLogProb _, 0.5, burnIn = 0, dropCount = 0)
    pullAllSamples(m)
  }

  @Benchmark
  def timeMetropolisHastings(reps: Int): Double = run(reps) {
    val m = ArbitraryMetropolisHastings(likelihood _,
                                        ((_: Double)) => Uniform(0, 1),
                                        gaussianJumpLogProb _,
                                        0.5,
                                        burnIn = burnIn,
                                        dropCount = dropCount
    )
    pullAllSamples(m)
  }

  @Benchmark
  def timeMetropolisHastingsWithWork(reps: Int): Double = run(reps) {
    val m = ArbitraryMetropolisHastings(likelihood _,
                                        ((_: Double)) => Uniform(0, 1),
                                        gaussianJumpLogProb _,
                                        0.5,
                                        burnIn = 0,
                                        dropCount = dropCount
    )
    pullAllSamplesWithWork(m)
  }

  @Benchmark
  def timeThreadedBufferedWithWork(reps: Int): Double = run(reps) {
    val wrapped = ArbitraryMetropolisHastings(likelihood _,
                                              ((_: Double)) => Uniform(0, 1),
                                              gaussianJumpLogProb _,
                                              0.5,
                                              burnIn = 0,
                                              dropCount = dropCount
    )
    val m = ThreadedBufferedRand(wrapped, bufferSize = bufferSize)
    val result = pullAllSamplesWithWork(m)
    m.stop()
    result
  }
}
