package breeze.stats.distributions

/*
 Copyright 2009 David Hall, Daniel Ramage

 Licensed under the Apache License, Version 2.0 (the "License")
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

import breeze.linalg.DenseVector
import breeze.macros.cforRange
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator

import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

/**
 * A trait for monadic distributions. Provides support for use in for-comprehensions
 * @author dlwh
 */
trait Rand[@specialized(Int, Double) +T] extends Serializable { outer =>

  /**
   * Gets one sample from the distribution. Equivalent to sample()
   */
  def draw(): T

  def get(): T = draw()

  /** Overridden by filter/map/flatmap for monadic invocations. Basically, rejeciton samplers will return None here */
  def drawOpt(): Option[T] = Some(draw())

  /**
   * Gets one sample from the distribution. Equivalent to get()
   */
  def sample(): T = get()

  /**
   * Gets n samples from the distribution.
   */
  def sample(n: Int): IndexedSeq[T] = IndexedSeq.fill(n)(draw())

  /**
   * An infinitely long iterator that samples repeatedly from the Rand
   * @return an iterator that repeatedly samples
   */
  def samples: Iterator[T] = Iterator.continually(draw())

  /**
   * Return a vector of samples.
   */
  def samplesVector[U >: T](size: Int)(implicit m: ClassTag[U]): DenseVector[U] = {
    val result = new DenseVector[U](new Array[U](size))
    cforRange(0 until size)(i => {
      result(i) = draw()
    })
    result
  }

  /**
   * Converts a random sampler of one type to a random sampler of another type.
   * Examples:
   * randInt(10).flatMap(x => randInt(3 * x.asInstanceOf[Int]) gives a Rand[Int] in the range [0,30]
   * Equivalently, for(x &lt;- randInt(10); y &lt;- randInt(30 *x)) yield y
   *
   * @param f the transform to apply to the sampled value.
   *
   */
  def flatMap[E](f: T => Rand[E]): Rand[E] = FlatMappedRand(outer, f)

  /**
   * Converts a random sampler of one type to a random sampler of another type.
   * Examples:
   * uniform.map(_*2) gives a Rand[Double] in the range [0,2]
   * Equivalently, for(x &lt;- uniform) yield 2*x
   *
   * @param f the transform to apply to the sampled value.
   *
   */
  def map[E](f: T => E): Rand[E] = MappedRand(outer, f)

  /**
   * Samples one element and qpplies the provided function to it.
   * Despite the name, the function is applied once. Sample usage:
   * <pre> for(x &lt;- Rand.uniform) { println(x) } </pre>
   *
   * @param f the function to be applied
   */
  def foreach(f: T => Unit): Unit = f(get())

  def filter(p: T => Boolean): Rand[T] = condition(p)

  def withFilter(p: T => Boolean): Rand[T] = condition(p)

  // Not the most efficient implementation ever, but meh.
  def condition(p: T => Boolean): Rand[T] = SinglePredicateRand[T](outer, p)
}

final private case class MappedRand[@specialized(Int, Double) T, @specialized(Int, Double) U](rand: Rand[T],
                                                                                              func: T => U
) extends Rand[U] {
  def draw(): U = func(rand.draw())
  override def drawOpt(): Option[U] = rand.drawOpt().map(func)
  override def map[E](f: U => E): Rand[E] = MappedRand(rand, (x: T) => f(func(x)))
}

final private case class FlatMappedRand[@specialized(Int, Double) T, @specialized(Int, Double) U](rand: Rand[T],
                                                                                                  func: T => Rand[U]
) extends Rand[U] {
  def draw(): U = func(rand.draw()).draw()
  override def drawOpt(): Option[U] = rand.drawOpt().flatMap(x => func(x).drawOpt())
  override def flatMap[E](f: U => Rand[E]): Rand[E] = FlatMappedRand(rand, (x: T) => f(func(x).draw()))
}

private trait PredicateRandDraws[@specialized(Int, Double) T] extends Rand[T] {
  protected val rand: Rand[T]
  protected def predicate(x: T): Boolean

  def draw(): T = { // Not the most efficient implementation ever, but meh.
    var x = rand.draw()
    while (!predicate(x)) {
      x = rand.draw()
    }
    x
  }

  override def drawOpt(): Option[T] = {
    val x = rand.get()
    Some(x).filter(predicate)
  }
}

final private case class SinglePredicateRand[@specialized(Int, Double) T](rand: Rand[T], pred: T => Boolean)
    extends PredicateRandDraws[T] {
  protected def predicate(x: T): Boolean = pred(x)

  override def condition(p: T => Boolean): Rand[T] = {
    val newPredicates = new Array[T => Boolean](2)
    newPredicates(0) = pred
    newPredicates(1) = p
    MultiplePredicatesRand(rand, newPredicates)
  }
}

final private case class MultiplePredicatesRand[@specialized(Int, Double) T](rand: Rand[T],
                                                                             private val predicates: Array[T => Boolean]
) extends PredicateRandDraws[T] {
  override def condition(p: T => Boolean): Rand[T] = {
    val newPredicates = new Array[T => Boolean](predicates.length + 1)
    cforRange(predicates.indices)(i => {
      newPredicates(i) = predicates(i)
    })
    newPredicates(predicates.length) = p
    MultiplePredicatesRand(rand, newPredicates)
  }

  protected def predicate(x: T): Boolean = {
    var result: Boolean = true
    var i = 0
    while ((i < predicates.length) && result) {
      result = result && predicates(i)(x)
      i = i + 1
    }
    result
  }
}

/**
 * Provides standard combinators and such to use
 * to compose new Rands.
 */
class RandBasis(val generator: RandomGenerator) extends Serializable {

  /**
   * Chooses an element from a collection.
   */
  def choose[T](c: Iterable[T]): Rand[T] = new Rand[T] {
    def draw(): T = {
      val sz = uniform.draw() * c.size
      val elems = c.iterator
      var i = 1
      var e = elems.next()
      while (i < sz) {
        e = elems.next()
        i += 1
      }
      e
    }
  }

  def choose[T](c: Seq[T]): Rand[T] = randInt(c.size).map(c(_))

  /**
   * The trivial random generator: always returns the argument
   */
  def always[T](t: T): Rand[T] = new Rand[T] {
    def draw(): T = t
  }

  /**
   * Simply reevaluate the body every time get is called
   */
  def fromBody[T](f: => T): Rand[T] = new Rand[T] {
    def draw(): T = f
  }

  /**
   * Convert an Seq of Rand[T] into a Rand[Seq[T]]
   */
  def promote[U](col: Seq[Rand[U]]): Rand[Seq[U]] = fromBody(col.map(_.draw()))

  def promote[T1, T2](t: (Rand[T1], Rand[T2])): Rand[(T1, T2)] = fromBody((t._1.draw(), t._2.draw()))
  def promote[T1, T2, T3](t: (Rand[T1], Rand[T2], Rand[T3])): Rand[(T1, T2, T3)] = fromBody(
    (t._1.draw(), t._2.draw(), t._3.draw())
  )
  def promote[T1, T2, T3, T4](t: (Rand[T1], Rand[T2], Rand[T3], Rand[T4])): Rand[(T1, T2, T3, T4)] =
    fromBody((t._1.draw(), t._2.draw(), t._3.draw(), t._4.draw()))

  /**
   * Uniformly samples in [0,1)
   */
  val uniform: Rand[Double] = new Rand[Double] {
    def draw(): Double = generator.nextDouble
  }

  /**
   * Uniformly samples an integer in [0,MAX_INT]
   */
  val randInt: Rand[Int] = new Rand[Int] {
    def draw(): Int = generator.nextInt & Int.MaxValue
  }

  /**
   * Uniformly samples an integer in [0,n)
   */
  def randInt(n: Int): Rand[Int] = new Rand[Int] {
    def draw(): Int = generator.nextInt(n)
  }

  /**
   * Uniformly samples an integer in [n,m)
   */
  def randInt(n: Int, m: Int): Rand[Int] = new Rand[Int] {
    def draw(): Int = generator.nextInt(m - n) + n
  }

  /**
   * Uniformly samples a long integer in [0,MAX_LONG]
   */
  val randLong: Rand[Long] = new Rand[Long] {
    def draw(): Long = generator.nextLong & Long.MaxValue
  }

  /**
   * Uniformly samples a long integer in [0,n)
   */
  def randLong(n: Long): Rand[Long] = new Rand[Long] {
    require(n > 0)
    def draw(): Long = {
      val maxVal = Long.MaxValue - (Long.MaxValue % n) - 1
      var value = generator.nextLong() & Long.MaxValue
      while (value > maxVal) {
        value = generator.nextLong() & Long.MaxValue
      }
      value % n
    }
  }

  /**
   * Uniformly samples a long integer in [n,m)
   */
  def randLong(n: Long, m: Long): Rand[Long] = new Rand[Long] {
    val inner: Rand[Long] = randLong(m - n)
    def draw(): Long = {
      inner.draw() + n
    }
  }

  /**
   * Samples a gaussian with 0 mean and 1 std
   */
  val gaussian: Rand[Double] = new Rand[Double] {
    def draw(): Double = generator.nextGaussian()
  }

  /**
   * Samples a gaussian with m mean and s std
   */
  def gaussian(m: Double, s: Double): Rand[Double] = new Rand[Double] {
    def draw(): Double = m + s * gaussian.draw()
  }

  /**
   * Implements the Knuth shuffle of numbers from 0 to n.
   */
  def permutation(n: Int): Rand[IndexedSeq[Int]] = new Rand[IndexedSeq[Int]] {
    def draw(): IndexedSeq[Int] = {
      val arr = new ArrayBuffer[Int]()
      arr ++= (0 until n)
      var i = n
      while (i > 1) {
        val k = generator.nextInt(i)
        i -= 1
        val tmp = arr(i)
        arr(i) = arr(k)
        arr(k) = tmp
      }
      arr.toIndexedSeq
    }
  }

  /**
   * Knuth shuffle of a subset of size n from a set
   */
  def subsetsOfSize[T](set: IndexedSeq[T], n: Int): Rand[IndexedSeq[T]] = new Rand[IndexedSeq[T]] {
    def draw(): IndexedSeq[T] = {
      val arr = Array.range(0, set.size)
      var i = 0
      while (i < n.min(set.size)) {
        val k = generator.nextInt(set.size - i) + i
        val temp = arr(i)
        arr(i) = arr(k)
        arr(k) = temp
        i += 1
      }
      arr.take(n).toIndexedSeq.map(set)
    }
  }
}

/**
 * Provides a number of random generators, with random seed set to some function of system time and
 * identity hashcode of some object
 */
object Rand extends RandBasis(new ThreadLocalRandomGenerator(new MersenneTwister())) {

  /** Import the contents of this to make Rands/Distributions that use the "default" generator */
  object VariableSeed {
    implicit val randBasis: RandBasis = Rand
  }

  /** Import the contents of this to use a generator seeded with a consistent seed.*/
  object FixedSeed {
    implicit val randBasis: RandBasis = RandBasis.mt0
  }
}

object RandBasis {

  /**
   * Returns a new MersenneTwister-backed rand basis with "no seed" (i.e. it uses the time plus
   * other metadata to set the seed
   * if multiple threads use this, each thread gets a new generator also initialized with "no seed"
   * @return
   */
  def systemSeed: RandBasis = new RandBasis(new ThreadLocalRandomGenerator(new MersenneTwister()))

  /**
   * Returns a new MersenneTwister-backed rand basis with seed set to 0. Note that
   * if multiple threads use this, each thread gets a new generator with an increasing random
   * seed.
   * @return
   */
  def mt0: RandBasis = withSeed(0)

  /**
   * Returns a new MersenneTwister-backed rand basis with seed set to a specific value
   * if multiple threads use this, each thread gets a new generator with an increasing random (starting from seed)
   */
  def withSeed(seed: Int): RandBasis = {
    val int = new AtomicInteger(seed)
    new RandBasis(new ThreadLocalRandomGenerator(new MersenneTwister(int.getAndIncrement())))
  }

}
