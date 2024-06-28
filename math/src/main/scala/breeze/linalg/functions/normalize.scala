package breeze.linalg

import breeze.generic.UFunc
import breeze.linalg.operators.OpDiv
import breeze.linalg.support.ScalarOf

/**
 * Normalizes the argument such that its norm is 1.0 (with respect to the argument n).
 * Returns value if value's norm is 0.
 */
object normalize extends UFunc with normalizeLowPrio {
  implicit def normalizeDoubleImpl[T, U >: T](implicit
    div: OpDiv.Impl2[T, Double, U],
    canNorm: norm.Impl2[T, Double, Double]
  ): Impl2[T, Double, U] = {
    (t: T, n: Double) => {
      val norm = canNorm(t, n)
      if (norm == 0) t
      else div(t, norm)
    }
  }

  implicit def normalizeFloatImpl[T, U >: T](implicit
    scalarOf: ScalarOf[T, Float],
    div: OpDiv.Impl2[T, Float, U],
    canNorm: norm.Impl2[T, Double, Double]
  ): Impl2[T, Float, U] = {
    (t: T, n: Float) => {
      val norm = canNorm(t, n)
      if (norm == 0) t
      else div(t, norm.toFloat)
    }
  }

  implicit def normalizeInPlaceDoubleImpl[T, U >: T](implicit
    div: OpDiv.InPlaceImpl2[T, Double],
    canNorm: norm.Impl2[T, Double, Double]
  ): InPlaceImpl2[T, Double] = {
    (t: T, n: Double) => {
      val norm = canNorm(t, n)
      if (norm != 0)
        div(t, norm)
    }
  }

  implicit def normalizeInPlaceFloatImpl[T, U >: T](implicit
    div: OpDiv.InPlaceImpl2[T, Float],
    canNorm: norm.Impl2[T, Float, Float]
  ): InPlaceImpl2[T, Float] = {
    (t: T, n: Float) => {
      val norm = canNorm(t, n)
      if (norm != 0)
        div(t, norm)
    }
  }

  implicit def normalizeImpl[T, U](implicit impl: Impl2[T, Double, U]): Impl[T, U] = {
    (v: T) => impl(v, 2.0)

  }

  implicit def normalizeIntImpl[T, U >: T](implicit impl: Impl2[T, Double, U]): Impl2[T, Int, U] = {
    (v: T, n: Int) => impl(v, n)

  }
}

sealed trait normalizeLowPrio { self: normalize.type =>
  implicit def normalizeImplForFloat[T, U >: T](implicit impl: Impl2[T, Float, U]): Impl[T, U] = {
    (v: T) => impl(v, 2.0f)
  }
}
