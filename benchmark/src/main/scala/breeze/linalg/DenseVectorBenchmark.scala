package breeze.linalg

import breeze.benchmark.*
import breeze.stats.distributions.*
import breeze.macros.*
import com.google.caliper.Benchmark

object DenseVectorBenchmark extends MyRunner(classOf[DenseVectorBenchmark])

trait BuildsRandomVectors {
  implicit val randBasis: RandBasis = RandBasis.mt0
  private val uniform = Uniform(0, 1)
  def randomArray(size: Int, offset: Int = 0, stride: Int = 1): DenseVector[Double] = {
    require(offset >= 0)
    require(stride >= 1)
    val result = new DenseVector(new Array[Double](offset + stride * size), offset, stride, size)
    var i = 0
    while (i < size) {
      result.update(i, uniform.draw())
      i += 1
    }
    result
  }

  def randomMatrix(m: Int, n: Int): DenseMatrix[Double] = {
    require(m > 0)
    require(n > 0)
    val d = new Array[Double](m * n)
    var i = 0
    while (i < m * n) {
      d(i) = uniform.draw()
      i += 1
    }
    new DenseMatrix(m, n, d, 0, m)
  }

  def randomSparseVector(size: Int, sparsity: Double = 0.01): SparseVector[Double] = {
    val nnz = (size * sparsity).toInt
    val vb = VectorBuilder.zeros[Double](size)
    cforRange(0 until nnz) { i =>
      val ind = (Math.random() * size).toInt
      val v = Math.random()
      vb.add(ind, v)
    }
//    val values = Array.fill(size)((size * Math.random()).toInt -> Math.random())
//    val result = SparseVector(size)(values:_*)
    vb.toSparseVector
  }
}

class DenseVectorBenchmark extends BreezeBenchmark with BuildsRandomVectors {
  implicit override val randBasis: RandBasis = RandBasis.mt0

  @Benchmark
  def timeAllocate(reps: Int): Unit = run(reps) {
    DenseVector.zeros[Double](1024)
  }

  @Benchmark
  def timeFill(reps: Int): Unit = run(reps) {
    DenseVector.fill[Double](1024, 23)
  }

  @Benchmark
  def timeForeach(reps: Int): Unit = runWith(reps, randomArray(4000)) { arr =>
    var sum = 0.0
    arr.foreach(sum += _)
    sum
  }

  @Benchmark
  def timeLoop(reps: Int): Unit = runWith(reps, randomArray(4000)) { arr =>
    var sum = 0.0
    val d = arr.data
    cforRange(0 until arr.length) { i =>
      sum += d(i)
    }
    sum
  }

  def valueAtBench(reps: Int, size: Int, stride: Int) =
    runWith(reps, { randomArray(size, stride = stride) })(arr => {
      var i = 0
      var t: Double = 0
      while (i < arr.size) {
        t += arr.valueAt(i)
        // This is not strictly part of the benchmark, but done so that the JIT doensn't eliminate everything
        i += 1
      }
      t
    })

  @Benchmark
  def timeValueAt(reps: Int) = valueAtBench(reps, 1024 * 8, 1)

  @Benchmark
  def timeValueAtStride4(reps: Int) = valueAtBench(reps, 1024 * 8, 4)

  def updateBench(reps: Int, size: Int, stride: Int) =
    runWith(reps, { randomArray(size, stride = stride) })(arr => {
      var i = 0
      while (i < arr.size) {
        arr.update(i, i.toDouble)
        i += 1
      }
      arr
    })

  @Benchmark
  def timeUpdate(reps: Int) = updateBench(reps, 1024 * 8, 1)

  @Benchmark
  def timeUpdateStride4(reps: Int) = updateBench(reps, 1024 * 8, 4)
}
