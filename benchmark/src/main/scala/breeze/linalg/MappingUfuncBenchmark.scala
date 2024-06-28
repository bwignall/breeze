package breeze.linalg

import breeze.benchmark.*
import breeze.generic.MappingUFunc
import breeze.stats.distributions.RandBasis
import com.google.caliper.Benchmark

object MappingUfuncBenchmark extends MyRunner(classOf[MappingUfuncBenchmark])

object addOne extends MappingUFunc {
  // A custom stupid ufunc that is very fast to run
  implicit object expDoubleImpl extends Impl[Double, Double] { def apply(v: Double): Double = v + 1 }
}

object harderUfunc extends MappingUFunc {
  // A custom stupid ufunc that is very fast to run
  implicit object expDoubleImpl extends Impl[Double, Double] {
    def apply(v: Double): Double = /*(v+1)/(1+v*v)*/ math.exp(v)
  }
}

class MappingUfuncBenchmark extends BreezeBenchmark with BuildsRandomMatrices with BuildsRandomVectors {
  implicit override val randBasis: RandBasis = RandBasis.mt0

  // these should be roughly similar:
  // Group 1
  @Benchmark
  def timeMappingUfuncDenseMat(reps: Int): DenseMatrix[Double] =
    runWith(reps, { randomMatrix(2048, 2048) })((mat: DenseMatrix[Double]) => {
      addOne(mat)
    })

  @Benchmark
  def timeMappingUfuncDenseVec(reps: Int): DenseVector[Double] =
    runWith(reps, { randomArray(2048 * 2048) })((arr: DenseVector[Double]) => {
      addOne(arr)
    })

  // Group1 appendix: slower, but that's ok
  @Benchmark
  def timeMappingUfuncDenseMatWithStride(reps: Int): DenseMatrix[Double] =
    runWith(reps, { randomMatrix(2048, 2048 * 2) })((mat: DenseMatrix[Double]) => {
      val newMat = new DenseMatrix(2048, 2048, mat.data, offset = 0, majorStride = 2048)
      addOne(newMat)
    })

  // Group 2: easy in place
  @Benchmark
  def timeMappingUfuncDenseVecInPlace(reps: Int): DenseVector[Double] =
    runWith(reps, { randomArray(2048 * 2048) })((arr: DenseVector[Double]) => {
      addOne.inPlace(arr)
    })

  @Benchmark
  def timeMappingUfuncArrayInPlace(reps: Int): Array[Double] =
    runWith(reps, { randomArray(2048 * 2048) })((arr: DenseVector[Double]) => {
      val data = arr.data
      var i = 0
      while (i < data.length) {
        data(i) = addOne(data(i))
        i += 1
      }
      data
    })

  @Benchmark
  def timeMappingUfuncDenseMatInPlace(reps: Int): DenseMatrix[Double] =
    runWith(reps, { randomMatrix(2048, 2048) })((mat: DenseMatrix[Double]) => {
      addOne.inPlace(mat)
    })

  // Group 3: harder
  @Benchmark
  def timeMappingUfuncArrayHarder(reps: Int): Array[Double] =
    runWith(reps, { randomArray(2048 * 2048) })((arr: DenseVector[Double]) => {
      val data = arr.data
      val nd = new Array[Double](data.length)
      var i = 0
      while (i < data.length) {
        nd(i) = harderUfunc(data(i))
        i += 1
      }
      nd
    })

  @Benchmark
  def timeMappingUfuncDenseVecHarder(reps: Int): DenseVector[Double] =
    runWith(reps, { randomArray(2048 * 2048) })((arr: DenseVector[Double]) => {
      harderUfunc(arr)
    })

  @Benchmark
  def timeMappingUfuncDenseMatHarder(reps: Int): DenseMatrix[Double] =
    runWith(reps, { randomMatrix(2048, 2048) })((mat: DenseMatrix[Double]) => {
      harderUfunc(mat)
    })

  @Benchmark
  def timeMappingUfuncDenseMatHarderWithStride(reps: Int): DenseMatrix[Double] =
    runWith(reps, { randomMatrix(2048, 2048 * 2) })((mat: DenseMatrix[Double]) => {
      val newMat = new DenseMatrix(2048, 2048, mat.data, offset = 0, majorStride = 2048)
      harderUfunc(newMat)
    })

  @Benchmark
  def timeMappingUfuncDenseVecHarderMapValues(reps: Int): DenseVector[Double] =
    runWith(reps, { randomArray(2048 * 2048) })((arr: DenseVector[Double]) => {
      arr.mapValues(harderUfunc(_))
    })

  @Benchmark
  def timeMappingUfuncDenseMatHarderMapValues(reps: Int): DenseMatrix[Double] =
    runWith(reps, { randomMatrix(2048, 2048) })((mat: DenseMatrix[Double]) => {
      mat.mapValues(harderUfunc(_))
    })

  // Group 4: harder inplace
  @Benchmark
  def timeMappingUfuncArrayHarderInlineInPlace(reps: Int): Array[Double] =
    runWith(reps, { randomArray(2048 * 2048) })((arr: DenseVector[Double]) => {
      val data = arr.data
      var i = 0
      while (i < data.length) {
        val v = data(i)
        data(i) = math.exp(v) // (v+1)/(1+v*v)
        i += 1
      }
      data
    })

  @Benchmark
  def timeMappingUfuncDenseMatHarderInPlace(reps: Int): DenseMatrix[Double] =
    runWith(reps, { randomMatrix(2048, 2048) })((mat: DenseMatrix[Double]) => {
      harderUfunc.inPlace(mat)
    })

  @Benchmark
  def timeMappingUfuncDenseVecHarderInPlace(reps: Int): DenseVector[Double] =
    runWith(reps, { randomArray(2048 * 2048) })((arr: DenseVector[Double]) => {
      harderUfunc.inPlace(arr)
    })

  @Benchmark
  def timeMappingUfuncArrayHarderInPlace(reps: Int): Array[Double] =
    runWith(reps, { randomArray(2048 * 2048) })((arr: DenseVector[Double]) => {
      val data = arr.data
      var i = 0
      while (i < data.length) {
        data(i) = harderUfunc(data(i))
        i += 1
      }
      data
    })
}
