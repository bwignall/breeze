package breeze.linalg

import breeze.benchmark._

import breeze.linalg._
import breeze.stats.distributions._
import breeze.macros.cforRange

object ProbMonadRunner extends MyRunner(classOf[ProbMonadBenchmark])

class ProbMonadBenchmark extends BreezeBenchmark {

  val f: Double => Double = x => math.exp(-x * x)
  val f2: Double => Double = x => x * x
  val f3: Double => Double = x => math.log(x)
  val fm: Double => Rand[Double] = x => Uniform(min(x, 2 * x), max(x, 2 * x))
  val gaussian: Gaussian = Gaussian(0, 1)

  val size: Int = 1024 * 1024

  def timeMonad(reps: Int): DenseVector[Double] = run(reps) {
    /* The purpose of this benchmark is to compare monadic usage of rand to non-monadic usage (see timeRaw).
     */
    val monadic = for {
      x <- gaussian
      y <- gaussian
    } yield x + y
    monadic.samplesVector(size)
  }

  def timeRaw(reps: Int): DenseVector[Double] = run(reps) {
    /* The purpose of this benchmark is to compare monadic usage of rand to non-monadic usage (see timeMonad).
     */
    val nonmonadic = new Rand[Double] {
      def draw() = gaussian.draw() + gaussian.draw()
    }
    nonmonadic.samplesVector(size)
  }

  def timeMap(reps: Int): DenseVector[Double] = run(reps) {
    val mg = gaussian.map(f)
    mg.samplesVector(size)
  }

  def timeMapRepeated(reps: Int): DenseVector[Double] = run(reps) {
    val mg = gaussian.map(f).map(f2).map(f3)
    mg.samplesVector(size)
  }

  def timeFlatMap(reps: Int): DenseVector[Double] = run(reps) {
    val mg = gaussian.flatMap(fm)
    mg.samplesVector(size)
  }

  def timeFlatMapRepeated(reps: Int): DenseVector[Double] = run(reps) {
    val mg = gaussian.flatMap(fm).flatMap(fm).flatMap(fm)
    mg.samplesVector(size)
  }

  def timeCondition(reps: Int): DenseVector[Double] = run(reps) {
    val mg = gaussian.condition(x => x > 0)
    mg.samplesVector(size)
  }

  def timeRepeatCondition(reps: Int): DenseVector[Double] = run(reps) {
    val mg = gaussian.condition(x => x > 0).condition(x => x < 1).condition(x => x > -1)
    mg.samplesVector(size)
  }

  def timeDrawOpt(reps: Int): Array[Option[Double]] = run(reps) {
    val mg = gaussian.condition(x => x > 0)
    val result = new Array[Option[Double]](size)
    cforRange(0 until size)(i => {
      result(i) = mg.drawOpt()
    })
    result
  }
  def timeDrawOptMultipleCondition(reps: Int): Array[Option[Double]] = run(reps) {
    val mg = gaussian.condition(x => x > 0).condition(x => x < 1).condition(x => x > -1)
    val result = new Array[Option[Double]](size)
    cforRange(0 until size)(i => {
      result(i) = mg.drawOpt()
    })
    result
  }

}
