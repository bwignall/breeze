package breeze

import breeze.generic.UFunc
import breeze.generic.VariableUFunc
import breeze.linalg.DenseMatrix
import breeze.linalg.DenseVector
import breeze.macros._
import breeze.macros.cforRange
import spire.implicits.DoubleAlgebra
import spire.math.poly.PolyDense

package object polynomial {
  object densePolyval extends UFunc {
    implicit object doubleImpl extends Impl2[PolyDenseUFuncWrapper, Double, Double] {
      def apply(k: PolyDenseUFuncWrapper, v: Double): Double = k.p(v)
    }
    implicit object denseVectorImpl extends Impl2[PolyDenseUFuncWrapper, DenseVector[Double], DenseVector[Double]] {
      /* This implementation uses Horner's Algorithm:
       *  http://en.wikipedia.org/wiki/Horner's_method
       *
       *  Iterating over the polynomial coefficients first and the
       *  vector coefficients second is about 3x faster than
       *  the other way around.
       */
      def apply(k: PolyDenseUFuncWrapper, v: DenseVector[Double]): DenseVector[Double] = {
        val coeffs: Array[Double] = k.p.coeffs
        var i = coeffs.length - 1
        val result = DenseVector.fill[Double](v.size, coeffs(i))
        while (i > 0) {
          i -= 1
          val c = coeffs(i)
          cforRange(0 until result.size)(j => {
            result(j) = result(j) * v(j) + c
          })
        }
        result
      }
    }
    implicit object denseMatrixImpl extends Impl2[PolyDenseUFuncWrapper, DenseMatrix[Double], DenseMatrix[Double]] {
      /* This implementation uses Horner's Algorithm:
       *  http://en.wikipedia.org/wiki/Horner's_method
       *
       *  Iterating over the polynomial coefficients first and the
       *  vector coefficients second is about 3x faster than
       *  the other way around.
       */
      def apply(k: PolyDenseUFuncWrapper, v: DenseMatrix[Double]): DenseMatrix[Double] = {
        if (v.rows != v.cols) {
          throw new IllegalArgumentException("Can only apply polynomial to square matrix.")
        }
        val n = v.rows
        val coeffs: Array[Double] = k.p.coeffs
        var i = coeffs.length - 1
        var result = DenseMatrix.eye[Double](n) * coeffs(i)
        while (i > 0) {
          i -= 1
          result = result * v // WILDLY INEFFICIENT, FIGURE OUT IN PLACE MULTIPLY
          val c = coeffs(i)
          cforRange(0 until n)(i => {
            result.update(i, i, result(i, i) + c)
          })
        }
        result
      }
    }

  }

  implicit class PolyDenseUFuncWrapper(val p: PolyDense[Double])
      extends VariableUFunc[densePolyval.type, PolyDenseUFuncWrapper]
}
