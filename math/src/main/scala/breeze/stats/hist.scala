package breeze.stats

import breeze.generic.UFunc
import breeze.linalg._
import breeze.linalg.support.CanTraverseValues
import breeze.linalg.support.CanTraverseValues.ValuesVisitor
import breeze.linalg.support.CanZipAndTraverseValues
import breeze.linalg.support.CanZipAndTraverseValues.PairValuesVisitor
import breeze.stats.hist.Impl3
import breeze.util.WideningConversion

object hist extends UFunc {

  class Histogram[S] private[breeze] (val hist: DenseVector[S], start: Double, end: Double, bins: Double) {

    lazy val binEdges: DenseVector[Double] =
      DenseVector.rangeD(start, end + ((end - start) / bins), step = (end - start) / bins)
  }

  implicit def defaultHist[T, S](implicit innerImpl: Impl2[T, Int, Histogram[S]]): Impl[T, Histogram[S]] =
    (v: T) => innerImpl.apply(v, 10)

  implicit def defaultHistBins[T, S](implicit
    mm: minMax.Impl[T, (S, S)],
    conv: WideningConversion[S, Double],
    impl3: Impl3[T, Int, (Double, Double), Histogram[Int]]
  ): Impl2[T, Int, Histogram[Int]] = (v: T, bins: Int) => {
    val (minS, maxS) = minMax(v)
    impl3(v, bins, (conv(minS), conv(maxS)))
  }

  implicit def canTraverseValuesImpl[T, @specialized(Int, Float, Double) S](implicit
    iter: CanTraverseValues[T, S],
    conv: WideningConversion[S, Double]
  ): Impl3[T, Int, (Double, Double), Histogram[Int]] =
    (v: T, bins: Int, range: (Double, Double)) => {
      val (minimum, maximum) = range
      if (maximum <= minimum) {
        throw new IllegalArgumentException("Minimum of a histogram must not be greater than the maximum")
      }
      val result = DenseVector.zeros[Int](bins)

      val visitor = new ValuesVisitor[S] {
        def visit(a: S): Unit = {
          val ad = conv(a)
          val i: Int = binOf(ad)
          if ((i >= 0) && (i < bins)) {
            result(i) += 1
          }
          if (ad == maximum) { // Include the endpoint
            result(bins - 1) += 1
          }
        }

        def zeros(numZero: Int, zeroValue: S): Unit = {
          val ad = conv(zeroValue)
          val i = binOf(ad)
          if ((i >= 0) && (i < bins)) {
            result(i) += numZero
          }
          if (ad == maximum) { // Include the endpoint
            result(bins - 1) += 1
          }
        }

        private def binOf(v: Double): Int = {
          math.floor(bins * ((v - minimum) / (maximum - minimum))).toInt
        }
      }
      iter.traverse(v, visitor)
      new Histogram(result, minimum, maximum, bins)
    }

  implicit def defaultHistWeights[T, U, S](implicit
    innerImpl: Impl3[T, Int, U, Histogram[S]]
  ): Impl2[T, U, Histogram[S]] = (v: T, weights: U) => innerImpl.apply(v, 10, weights)

  implicit def defaultHistBinsWeights[T, U, S, R](implicit
    innerImpl: Impl4[T, Int, (Double, Double), U, Histogram[R]],
    mm: minMax.Impl[T, (S, S)],
    conv: WideningConversion[S, Double]
  ): Impl3[T, Int, U, Histogram[R]] = (v: T, bins: Int, weights: U) => {
    val (minS, maxS) = minMax(v)
    innerImpl(v, bins, (conv(minS), conv(maxS)), weights)
  }

  implicit def canTraverseValuesImplWeighted[T, U, S](implicit
    iter: CanZipAndTraverseValues[T, U, S, Double],
    conv: WideningConversion[S, Double]
  ): Impl4[T, Int, (Double, Double), U, Histogram[Double]] =
    (v: T, bins: Int, range: (Double, Double), weights: U) => {
      val (minimum, maximum) = range
      if (maximum <= minimum) {
        throw new IllegalArgumentException("Minimum of a histogram must not be greater than the maximum")
      }
      val result = DenseVector.zeros[Double](bins)

      val visitor = new PairValuesVisitor[S, Double] {
        def visit(a: S, w: Double): Unit = {
          val ad = conv(a).toDouble
          val i: Int = math.floor(bins * ((ad - minimum) / (maximum - minimum))).toInt
          if ((i >= 0) && (i < bins)) {
            result(i) += w
          }
          if (ad == maximum) { // Include the endpoint
            result(bins - 1) += w
          }
        }
      }
      iter.traverse(v, weights, visitor)
      new Histogram(result, minimum, maximum, bins)
    }
}
