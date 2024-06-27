package breeze.math

import breeze.numerics.IntMath

/**
 * importing this gives numeric enables a "pow" method on basic numeric types
 *
 * @author dlwh
 **/
object PowImplicits {
  implicit class DoublePow(x: Double) {
    def pow(y: Double): Double = math.pow(x, y)
  }

  implicit class FloatPow(x: Float) {
    def pow(y: Float): Float = math.pow(x, y).toFloat
  }

  implicit class IntPow(x: Int) {
    def pow(y: Int): Int = IntMath.ipow(x, y)
  }

  implicit class LongPow(x: Long) {
    def pow(y: Long): Long = IntMath.ipow(x, y)
  }

}
