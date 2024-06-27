package breeze.numerics.units

import breeze.generic.MappingUFunc
import breeze.generic.UFunc

/**Package for common unit conversions.
 * @author ktakagaki
 * @date 1/31/14.
 */
object Conversions {

  // <editor-fold defaultstate="collapsed" desc=" Temperature ">

  /** Converts Fahrenheit temperature to Celsius
   * @see <a href="http://en.wikipedia.org/wiki/Fahrenheit">http://en.wikipedia.org/wiki/Fahrenheit</a>
   */
  object fahrenheitToCelsius extends MappingUFunc {
    implicit object fahrenheitToCelsiusDImpl extends Impl[Double, Double] { def apply(f: Double): Double = (f - 32d) * 5d / 9d }
    implicit object fahrenheitToCelsiusFImpl extends Impl[Float, Float] { def apply(f: Float): Float = (f - 32f) * 5f / 9f }
  }

  /** Converts Celsius temperature to Fahrenheit
   * @see <a href="http://en.wikipedia.org/wiki/Fahrenheit">http://en.wikipedia.org/wiki/Fahrenheit</a>
   */
  object celsiusToFahrenheit extends MappingUFunc {
    implicit object celsiusToFahrenheitDImpl extends Impl[Double, Double] { def apply(c: Double): Double = c / 5d * 9d + 32d }
    implicit object celsiusToFahrenheitFImpl extends Impl[Float, Float] { def apply(c: Float): Float = c / 5f * 9f + 32f }
  }

  // </editor-fold>

}
