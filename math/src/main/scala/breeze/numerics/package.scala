package breeze

/*
 Copyright 2012 David Hall

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

import breeze.generic.{MappingUFunc, UFunc, ZeroPreservingUFunc}
import breeze.math.Semiring

import scala.math._
import org.apache.commons.math3.special.{Erf, Gamma => G}
import breeze.linalg.support.CanTraverseValues
import CanTraverseValues.ValuesVisitor
import org.apache.commons.math3.util.FastMath

/**
 * Contains several standard numerical functions as MappingUFuncs,
 *
 * @author dlwh, afwlehmann
 */
package object numerics {

  import scala.{math => m}

  // TODO: I should probably codegen this.

  object exp extends MappingUFunc {
    implicit object expDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = java.lang.Math.exp(v)
    }

    implicit object expFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = java.lang.Math.exp(v).toFloat
    }
  }

  object expm1 extends ZeroPreservingUFunc {
    implicit object expm1DoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = java.lang.Math.expm1(v)
    }

    implicit object expm1FloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = java.lang.Math.expm1(v).toFloat
    }
  }

  object pow extends MappingUFunc {
    implicit object powDoubleDoubleImpl extends Impl2[Double, Double, Double] {
      def apply(v: Double, v2: Double) = java.lang.Math.pow(v, v2)
    }

    implicit object powFloatFloatImpl extends Impl2[Float, Float, Float] {
      def apply(v: Float, v2: Float) = java.lang.Math.pow(v, v2).toFloat
    }

    implicit object powDoubleIntImpl extends Impl2[Double, Int, Double] {
      def apply(v: Double, v2: Int) = java.lang.Math.pow(v, v2)
    }

    implicit object powFloatIntImpl extends Impl2[Float, Int, Float] {
      def apply(v: Float, v2: Int) = java.lang.Math.pow(v, v2).toFloat
    }

    implicit object powIntIntImpl extends Impl2[Int, Int, Int] {
      def apply(v: Int, v2: Int) = IntMath.ipow(v, v2)
    }

    implicit object powIntDoubleImpl extends Impl2[Int, Double, Double] {
      def apply(v: Int, v2: Double) = java.lang.Math.pow(v, v2)
    }
  }

  // Logarithms, etc

  private val log2D = m.log(2d)
  private val log10D = m.log(10d)

  object log extends MappingUFunc {
    // ToDo: Clarify in documentation that log(b, x) is the log of x in base b (instead of log b in base x)
    // ToDo???: extend to negative logs (but return type Double/Complex dependent on input???)
    implicit object logIntImpl extends Impl[Int, Double] {
      def apply(v: Int) = m.log(v.toDouble)
    }

    implicit object logDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.log(v)
    }

    implicit object logFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = m.log(v).toFloat
    }

    implicit object logBDoubleImpl extends Impl2[Double, Double, Double] {
      def apply(base: Double, v: Double) = m.log(v) / m.log(base)
    }

    implicit object logBFloatImpl extends Impl2[Float, Float, Float] {
      def apply(base: Float, v: Float) = (m.log(v) / m.log(base)).toFloat
    }
  }

  object log2 extends MappingUFunc {
    implicit object log2IntImpl extends Impl[Int, Double] {
      def apply(v: Int) = m.log(v.toDouble) / log2D
    }

    implicit object log2DoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.log(v) / log2D
    }

    implicit object log2FloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = (m.log(v) / log2D).toFloat
    }
  }

  object log10 extends MappingUFunc {
    implicit object log10IntImpl extends Impl[Int, Double] {
      def apply(v: Int) = m.log10(v.toDouble)
    }

    implicit object log10DoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.log10(v)
    }

    implicit object log10FloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = m.log10(v).toFloat
    }
  }

  object log1p extends MappingUFunc {
    implicit object log1pIntImpl extends Impl[Int, Double] {
      def apply(v: Int) = m.log1p(v)
    }

    implicit object log1pDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.log1p(v)
    }

    implicit object log1pFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = m.log1p(v).toFloat
    }
  }

  object nextExponent extends MappingUFunc {
    implicit object nextExponentIntImpl extends Impl[Int, Double] {
      def apply(v: Int) = m.ceil(m.log(v.toDouble))
    }

    implicit object nextExponentDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.ceil(m.log(v))
    }

    implicit object nextExponentFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = m.ceil(m.log(v)).toFloat
    }

    implicit object nextExponentIntImpl2 extends Impl2[Int, Int, Double] {
      def apply(base: Int, v: Int) = m.ceil(m.log(v.toDouble) / m.log(base.toDouble))
    }

    implicit object nextExponentDoubleImpl2 extends Impl2[Double, Double, Double] {
      def apply(base: Double, v: Double) = m.ceil(m.log(v) / m.log(base))
    }

    implicit object nextExponentFloatImpl2 extends Impl2[Float, Float, Float] {
      def apply(base: Float, v: Float) = m.ceil(m.log(v) / m.log(base)).toFloat
    }
  }

  object nextExponent2 extends MappingUFunc {
    implicit object nextExponent2IntImpl extends Impl[Int, Double] {
      def apply(v: Int) = m.ceil(m.log(v.toDouble) / log2D)
    }

    implicit object nextExponent2DoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.ceil(m.log(v) / log2D)
    }

    implicit object nextExponent2FloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = m.ceil(m.log(v) / log2D).toFloat
    }
  }

  object nextExponent10 extends MappingUFunc {
    implicit object nextExponent10IntImpl extends Impl[Int, Double] {
      def apply(v: Int) = m.ceil(m.log(v.toDouble) / log10D)
    }

    implicit object nextExponent10DoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.ceil(m.log(v) / log10D)
    }

    implicit object nextExponent10FloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = m.ceil(m.log(v) / log10D).toFloat
    }
  }

  object nextPower extends MappingUFunc {
    implicit object nextPowerIntImpl extends Impl[Int, Double] {
      def apply(v: Int) = m.exp(nextExponent(v.toDouble))
    }

    implicit object nextPowerDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.exp(nextExponent(v))
    }

    implicit object nextPowerFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = m.exp(nextExponent.nextExponentFloatImpl(v)).toFloat
    }

    implicit object nextPowerIntImpl2 extends Impl2[Int, Int, Double] {
      def apply(base: Int, v: Int) = m.pow(base.toDouble, nextExponent(v.toDouble))
    }

    implicit object nextPowerDoubleImpl2 extends Impl2[Double, Double, Double] {
      def apply(base: Double, v: Double) = m.pow(base, nextExponent(v))
    }

    implicit object nextPowerFloatImpl2 extends Impl2[Float, Float, Float] {
      def apply(base: Float, v: Float) = m.pow(base, nextExponent(v).toDouble).toFloat
    }
  }

  object nextPower2 extends MappingUFunc {
    implicit object nextPower2IntImpl extends Impl[Int, Double] {
      def apply(v: Int) = m.pow(2d, nextExponent2(v.toDouble))
    }

    implicit object nextPower2DoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.pow(2d, nextExponent2(v))
    }

    implicit object nextPower2FloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = m.pow(2d, nextExponent2(v.toDouble)).toFloat
    }
  }

  object nextPower10 extends MappingUFunc {
    implicit object nextPower10IntImpl extends Impl[Int, Double] {
      def apply(v: Int) = m.pow(10d, nextExponent10(v.toDouble))
    }

    implicit object nextPower10DoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.pow(10d, nextExponent10(v))
    }

    implicit object nextPower10FloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = m.pow(10d, nextExponent10(v.toDouble)).toFloat
    }
  }

  object sqrt extends ZeroPreservingUFunc {
    implicit object sqrtDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.sqrt(v)
    }

    implicit object sqrtFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = m.sqrt(v).toFloat
    }

    implicit object sqrtIntImpl extends Impl[Int, Double] {
      def apply(v: Int) = m.sqrt(v.toDouble)
    }

    implicit object sqrtLongImpl extends Impl[Long, Double] {
      def apply(v: Long) = m.sqrt(v.toDouble)
    }
  }

  object cbrt extends ZeroPreservingUFunc {
    implicit object cbrtIntImpl extends Impl[Int, Double] {
      def apply(v: Int) = m.cbrt(v.toDouble)
    }

    implicit object cbrtDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.cbrt(v)
    }

    implicit object cbrtFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = m.cbrt(v).toFloat
    }
  }

  object sin extends ZeroPreservingUFunc {
    implicit object sinIntImpl extends Impl[Int, Double] {
      def apply(v: Int) = m.sin(v.toDouble)
    }

    implicit object sinDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.sin(v)
    }

    implicit object sinFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = m.sin(v).toFloat
    }
  }

  /** The sine cardinal (sinc) function, as defined by sinc(0)=1, sinc(n != 0)=sin(x)/x.
   * Note that this differs from some signal analysis conventions, where sinc(n != 0)
   * is defined by sin(Pi*x)/(Pi*x). This variant is provided for convenience as
   * [[breeze.numerics.sincpi]]. <b><i>Use it instead when translating from numpy.sinc.</i></b>.
   */
  object sinc extends MappingUFunc {
    implicit object sincIntImpl extends Impl[Int, Double] {
      def apply(v: Int) = if (v == 0) 1d else m.sin(v.toDouble) / v.toDouble
    }

    implicit object sincDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = if (v == 0) 1d else m.sin(v) / v
    }

    implicit object sincFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = if (v == 0) 1f else m.sin(v).toFloat / v
    }
  }

  /** The pi-normalized sine cardinal (sinc) function, as defined by sinc(0)=1, sinc(n != 0)=sin(Pi*x)/(Pi*x).
   * See also [[breeze.numerics.sinc]].
   */
  object sincpi extends MappingUFunc {
    implicit object sincpiIntImpl extends Impl[Int, Double] {
      def apply(v: Int) = if (v == 0) 1d
      else {
        val temp = v.toDouble * m.Pi;
        m.sin(temp) / temp
      }
    }

    implicit object sincpiDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = if (v == 0) 1d
      else {
        val temp = v * m.Pi;
        m.sin(temp) / temp
      }
    }

    implicit object sincpiFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = if (v == 0) 1f
      else {
        val temp = v * m.Pi;
        (m.sin(temp) / temp).toFloat
      }
    }
  }

  object cos extends MappingUFunc {
    implicit object cosIntImpl extends Impl[Int, Double] {
      def apply(v: Int) = m.cos(v.toDouble)
    }

    implicit object cosDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.cos(v)
    }

    implicit object cosFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = m.cos(v).toFloat
    }
  }

  object tan extends ZeroPreservingUFunc {
    implicit object tanIntImpl extends Impl[Int, Double] {
      def apply(v: Int) = m.tan(v.toDouble)
    }

    implicit object tanDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.tan(v)
    }

    implicit object tanFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = m.tan(v).toFloat
    }
  }

  object sinh extends ZeroPreservingUFunc {
    implicit object sinIntImpl extends Impl[Int, Double] {
      def apply(v: Int) = m.sinh(v.toDouble)
    }

    implicit object sinDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.sinh(v)
    }

    implicit object sinFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = m.sinh(v).toFloat
    }
  }

  object cosh extends MappingUFunc {
    implicit object cosIntImpl extends Impl[Int, Double] {
      def apply(v: Int) = m.cosh(v.toDouble)
    }

    implicit object cosDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.cosh(v)
    }

    implicit object cosFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = m.cosh(v).toFloat
    }
  }

  object tanh extends ZeroPreservingUFunc {
    implicit object tanIntImpl extends Impl[Int, Double] {
      def apply(v: Int) = m.tanh(v.toDouble)
    }

    implicit object tanDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.tanh(v)
    }

    implicit object tanFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = m.tanh(v).toFloat
    }
  }

  object sech extends MappingUFunc {
    implicit object tanIntImpl extends Impl[Int, Double] {
      def apply(v: Int) = 1.0 / m.cosh(v.toDouble)
    }

    implicit object tanDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = 1.0 / m.cosh(v)
    }

    implicit object tanFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = 1.0f / m.cosh(v).toFloat
    }
  }

  object asin extends ZeroPreservingUFunc {
    implicit object asinIntImpl extends Impl[Int, Double] {
      def apply(v: Int) = m.asin(v.toDouble)
    }

    implicit object asinDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.asin(v)
    }

    implicit object asinFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = m.asin(v).toFloat
    }
  }

  object acos extends MappingUFunc {
    implicit object acosIntImpl extends Impl[Int, Double] {
      def apply(v: Int) = m.acos(v.toDouble)
    }

    implicit object acosDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.acos(v)
    }

    implicit object acosFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = m.acos(v).toFloat
    }
  }

  object atan extends ZeroPreservingUFunc {
    implicit object atanIntImpl extends Impl[Int, Double] {
      def apply(v: Int) = m.atan(v.toDouble)
    }

    implicit object atanDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.atan(v)
    }

    implicit object atanFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = m.atan(v).toFloat
    }
  }

  object atan2 extends MappingUFunc {
    implicit object atan2IntImpl extends Impl2[Int, Int, Double] {
      def apply(v: Int, v2: Int) = m.atan2(v.toDouble, v2.toDouble)
    }

    implicit object atan2DoubleImpl extends Impl2[Double, Double, Double] {
      def apply(v: Double, v2: Double) = m.atan2(v, v2)
    }

    implicit object atan2FloatImpl extends Impl2[Float, Float, Float] {
      def apply(v: Float, v2: Float) = m.atan2(v, v2).toFloat
    }
  }

  object asinh extends ZeroPreservingUFunc {
    implicit object asinIntImpl extends Impl[Int, Double] {
      def apply(v: Int) = FastMath.asinh(v.toDouble)
    }

    implicit object asinDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = FastMath.asinh(v)
    }

    implicit object asinFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = FastMath.asinh(v).toFloat
    }
  }

  object acosh extends MappingUFunc {
    implicit object acosIntImpl extends Impl[Int, Double] {
      def apply(v: Int) = FastMath.acosh(v.toDouble)
    }

    implicit object acosDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = FastMath.acosh(v)
    }

    implicit object acosFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = FastMath.acosh(v).toFloat
    }
  }

  object atanh extends ZeroPreservingUFunc {
    implicit object atanIntImpl extends Impl[Int, Double] {
      def apply(v: Int) = FastMath.atanh(v.toDouble)
    }

    implicit object atanDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = FastMath.atanh(v)
    }

    implicit object atanFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = FastMath.atanh(v).toFloat
    }
  }

  object toDegrees extends ZeroPreservingUFunc {
    implicit object toDegreesIntImpl extends Impl[Int, Double] {
      def apply(v: Int) = m.toDegrees(v.toDouble)
    }

    implicit object toDegreesDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.toDegrees(v)
    }

    implicit object toDegreesFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = m.toDegrees(v).toFloat
    }
  }

  object toRadians extends ZeroPreservingUFunc {
    implicit object toRadiansIntImpl extends Impl[Int, Double] {
      def apply(v: Int) = m.toRadians(v.toDouble)
    }

    implicit object toRadiansDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.toRadians(v)
    }

    implicit object toRadiansFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = m.toRadians(v).toFloat
    }
  }

  object floor extends ZeroPreservingUFunc {
    implicit object floorIntImpl extends Impl[Int, Int] {
      def apply(v: Int) = v
    }

    implicit object floorDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.floor(v)
    }

    implicit object floorFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = m.floor(v).toFloat
    }
  }

  object ceil extends ZeroPreservingUFunc {
    implicit object ceilIntImpl extends Impl[Int, Int] {
      def apply(v: Int) = v
    }

    implicit object ceilDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.ceil(v)
    }

    implicit object ceilFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = m.ceil(v).toFloat
    }
  }

  object round extends ZeroPreservingUFunc {
    implicit object roundIntImpl extends Impl[Int, Int] {
      def apply(v: Int) = v
    }

    implicit object roundDoubleImpl extends Impl[Double, Long] {
      def apply(v: Double) = m.round(v)
    }

    implicit object roundFloatImpl extends Impl[Float, Int] {
      def apply(v: Float) = m.round(v)
    }
  }

  object rint extends ZeroPreservingUFunc {
    implicit object rintIntImpl extends Impl[Int, Int] {
      def apply(v: Int) = v
    }

    implicit object rintDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.rint(v)
    }

    implicit object rintFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = m.rint(v).toFloat
    }
  }

  object signum extends ZeroPreservingUFunc {
    implicit object signumIntImpl extends Impl[Int, Double] {
      def apply(v: Int) = m.signum(v.toDouble)
    }

    implicit object signumDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.signum(v)
    }

    implicit object signumFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = m.signum(v)
    }
  }

  object abs extends ZeroPreservingUFunc {
    implicit object absDoubleImpl extends Impl[Double, Double] {
      def apply(v: Double) = m.abs(v)
    }

    implicit object absFloatImpl extends Impl[Float, Float] {
      def apply(v: Float) = m.abs(v)
    }

    implicit object absIntImpl extends Impl[Int, Int] {
      def apply(v: Int) = m.abs(v)
    }

    implicit object absLongImpl extends Impl[Long, Long] {
      def apply(v: Long) = m.abs(v)
    }
  }

  /** Whether a number is odd. For Double and Float, isOdd also implies that the number is an integer,
   * and therefore does not necessarily equal !isEven for fractional input.
   */
  object isOdd extends ZeroPreservingUFunc {
    implicit val isOddImpl_Int: Impl[Int, Boolean] = {
      new Impl[Int, Boolean] {
        def apply(v: Int) = {
          v % 2 == 1
        }
      }
    }
    implicit val isOddImpl_Double: Impl[Double, Boolean] = {
      new Impl[Double, Boolean] {
        def apply(v: Double) = {
          v % 2 == 1
        }
      }
    }
    implicit val isOddImpl_Float: Impl[Float, Boolean] = {
      new Impl[Float, Boolean] {
        def apply(v: Float) = {
          v % 2 == 1
        }
      }
    }
    implicit val isOddImpl_Long: Impl[Long, Boolean] = {
      new Impl[Long, Boolean] {
        def apply(v: Long) = {
          v % 2 == 1
        }
      }
    }
  }

  /** Whether a number is even. For Double and Float, isEven also implies that the number is an integer,
   * and therefore does not necessarily equal !isOdd for fractional input.
   */
  object isEven extends MappingUFunc {
    implicit val isEvenImpl_Int: Impl[Int, Boolean] = {
      new Impl[Int, Boolean] {
        def apply(v: Int) = {
          v % 2 == 0
        }
      }
    }
    implicit val isEvenImpl_Double: Impl[Double, Boolean] = {
      new Impl[Double, Boolean] {
        def apply(v: Double) = {
          v % 2 == 0
        }
      }
    }
    implicit val isEvenImpl_Float: Impl[Float, Boolean] = {
      new Impl[Float, Boolean] {
        def apply(v: Float) = {
          v % 2 == 0
        }
      }
    }
    implicit val isEvenImpl_Long: Impl[Long, Boolean] = {
      new Impl[Long, Boolean] {
        def apply(v: Long) = {
          v % 2 == 0
        }
      }
    }
  }

  val inf, Inf = Double.PositiveInfinity
  val nan, NaN = Double.NaN

  object isNonfinite extends ZeroPreservingUFunc {
    implicit val isNonfiniteImpl_Double: Impl[Double, Boolean] = {
      new Impl[Double, Boolean] {
        override def apply(v: Double): Boolean = {
          !isFinite(v)
        }
      }
    }
    implicit val isNonfiniteImpl_Float: Impl[Float, Boolean] = {
      new Impl[Float, Boolean] {
        override def apply(v: Float): Boolean = {
          !isFinite(v)
        }
      }
    }
  }

  object isFinite extends MappingUFunc {
    implicit val isFiniteImpl_Double: Impl[Double, Boolean] = {
      new Impl[Double, Boolean] {
        override def apply(v: Double): Boolean = {
          m.abs(v) <= Double.MaxValue
        }
      }
    }
    implicit val isFiniteImpl_Float: Impl[Float, Boolean] = {
      new Impl[Float, Boolean] {
        override def apply(v: Float): Boolean = {
          m.abs(v) <= Double.MaxValue
        }
      }
    }
  }

  /**
   * Computes the log of the gamma function. The two parameter version
   * is the log Incomplete gamma function = \log \int_0x \exp(-t)pow(t,a-1) dt
   *
   * @return an approximation of the log of the Gamma function of x.
   */
  object lgamma extends MappingUFunc {
    implicit object lgammaImplInt extends Impl[Int, Double] {
      def apply(v: Int): Double = if (v == 0) Double.PositiveInfinity else G.logGamma(v.toDouble)
    }

    implicit object lgammaImplDouble extends Impl[Double, Double] {
      def apply(v: Double): Double = if (v == 0.0) Double.PositiveInfinity else G.logGamma(v)
    }

    implicit object lgammaImplIntInt extends Impl2[Int, Int, Double] {
      def apply(a: Int, x: Int): Double = lgammaImplDoubleDouble(a.toDouble, x.toDouble)
    }

    /**
     * log Incomplete gamma function = \log \int_0x \exp(-t)pow(t,a-1) dt
     * May store lgamma(a) in lgam(0) if it's non-null and needs to be computed.
     * Based on NR
     */
    implicit object lgammaImplDoubleDouble extends Impl2[Double, Double, Double] {
      def apply(a: Double, x: Double): Double = {
        if (x < 0.0 || a <= 0.0) throw new IllegalArgumentException()
        else if (x == 0) 0.0
        else if (x < a + 1.0) {
          var ap = a
          var del, sum = 1.0 / a
          var n = 0
          var result = Double.NaN
          while (n < 100) {
            ap += 1
            del *= x / ap
            sum += del
            if (scala.math.abs(del) < scala.math.abs(sum) * 1e-7) {
              result = -x + a * m.log(x) + m.log(sum)
              n = 100
            }
            n += 1
          }
          if (result.isNaN) throw new ArithmeticException("Convergence failed")
          else result
        } else {
          val gln = lgamma(a)
          var b = x + 1.0 - a
          var c = 1.0 / 1.0e-30
          var d = 1.0 / b
          var h = d
          var n = 0
          while (n < 100) {
            n += 1
            val an = -n * (n - a)
            b += 2.0
            d = an * d + b
            if (scala.math.abs(d) < 1e-30) d = 1e-30
            c = b + an / c
            if (scala.math.abs(c) < 1e-30) c = 1e-30
            d = 1.0 / d
            val del = d * c
            h *= del
            if (scala.math.abs(del - 1.0) < 1e-7) n = 101
          }

          if (n == 100) throw new ArithmeticException("Convergence failed")
          else breeze.linalg.logDiff(gln, -x + a * m.log(x) + m.log(h))
        }
      }
    }
  }

  /**
   * The derivative of the log gamma function
   */
  object digamma extends MappingUFunc {
    implicit object digammaImplInt extends Impl[Int, Double] {
      def apply(v: Int): Double = digammaImplDouble(v.toDouble)
    }

    implicit object digammaImplDouble extends Impl[Double, Double] {
      def apply(v: Double): Double = G.digamma(v)
    }
  }

  /**
   * Multivariate Digamma
   */
  object multidigamma extends MappingUFunc {
    implicit object multidigammaImplDoubleInt extends Impl2[Double, Int, Double] {
      def apply(a: Double, d: Int): Double = (1 to d).map(i => digamma(a + (1 - i).toDouble / 2)).sum
    }
  }

  /**
   * Multivariate digamma log
   */
  object multidigammalog extends MappingUFunc {
    implicit object multidigammalogImplDoubleInt extends Impl2[Double, Int, Double] {
      def apply(a: Double, d: Int): Double = log(multidigamma(a, d))
    }
  }

  /**
   * The second derivative of the log gamma function
   */
  object trigamma extends MappingUFunc {
    implicit object trigammaImplInt extends Impl[Int, Double] {
      def apply(v: Int): Double = trigammaImplDouble(v.toDouble)
    }

    implicit object trigammaImplDouble extends Impl[Double, Double] {
      def apply(v: Double): Double = G.trigamma(v)
    }
  }

  object multiloggamma extends MappingUFunc {
    implicit object multigammalogDoubleInt extends Impl2[Double, Int, Double] {
      def apply(a: Double, d: Int): Double =
        log(constants.Pi) * (d * (d - 1).toDouble / 4) + (1 to d).map(j => lgamma(a + (1 - j).toDouble / 2)).sum
    }
  }

  /**
   * Evaluates the log of the generalized beta function.
   * \sum_a lgamma(c(a))- lgamma(c.sum)
   */
  object lbeta extends UFunc {
    implicit object impl2Double extends Impl2[Double, Double, Double] {
      def apply(v: Double, v2: Double): Double = {
        lgamma(v) + lgamma(v2) - lgamma(v + v2)
      }
    }

    implicit def reduceDouble[T](implicit iter: CanTraverseValues[T, Double]): Impl[T, Double] = new Impl[T, Double] {
      def apply(v: T): Double = {
        object visit extends ValuesVisitor[Double] {
          var sum = 0.0
          var lgSum = 0.0

          def visit(a: Double): Unit = {
            sum += a
            lgSum += lgamma(a)
          }

          def zeros(numZero: Int, zeroValue: Double): Unit = {
            sum += numZero * zeroValue
            lgSum += lgamma(zeroValue)
          }
        }

        iter.traverse(v, visit)

        visit.lgSum - lgamma(visit.sum)
      }

    }
  }

  /**
   * An approximation to the error function
   */
  object erf extends ZeroPreservingUFunc {
    implicit object erfImplInt extends Impl[Int, Double] {
      def apply(v: Int): Double = Erf.erf(v.toDouble)
    }

    implicit object erfImplDouble extends Impl[Double, Double] {
      def apply(v: Double): Double = Erf.erf(v)
    }
  }

  /**
   * An approximation to the complementary error function: erfc(x) = 1 - erfc(x)
   */
  object erfc extends MappingUFunc {
    implicit object erfcImplInt extends Impl[Int, Double] {
      def apply(v: Int): Double = Erf.erfc(v.toDouble)
    }

    implicit object erfcImplDouble extends Impl[Double, Double] {
      def apply(v: Double): Double = Erf.erfc(v)
    }

  }

  /**
   * The imaginary error function for real argument x.
   *
   * Adapted from http://www.mathworks.com/matlabcentral/newsreader/view_thread/24120
   * verified against mathematica
   *
   * @return
   */
  object erfi extends MappingUFunc {
    implicit object erfiImplInt extends Impl[Int, Double] {
      def apply(v: Int): Double = erfiImplDouble(v.toDouble)
    }

    implicit object erfiImplDouble extends Impl[Double, Double] {
      def apply(x: Double): Double = {
        if (x < 0) -apply(-x)
        else { // taylor expansion
          var y = x
          val x2 = x * x
          var xx = x
          var f = 1.0
          var n = 0
          while (n < 100) {
            n += 1
            f /= n
            xx *= x2
            val del = f * xx / (2 * n + 1)
            if (del < 1e-8) n = 101
            y += del
          }
          y = y * 2 / m.sqrt(Pi)
          y
        }
      }
    }

  }

  /**
   * Inverse erf
   */
  object erfinv extends MappingUFunc {
    implicit object erfinvImplInt extends Impl[Int, Double] {
      def apply(v: Int): Double = Erf.erfInv(v.toDouble)
    }

    implicit object erfinvImplDouble extends Impl[Double, Double] {
      def apply(v: Double): Double = Erf.erfInv(v)
    }

  }

  /**
   * Inverse erfc
   */
  object erfcinv extends MappingUFunc {
    implicit object erfcinvImplInt extends Impl[Int, Double] {
      def apply(v: Int): Double = Erf.erfcInv(v.toDouble)
    }

    implicit object erfcinvImplDouble extends Impl[Double, Double] {
      def apply(v: Double): Double = Erf.erfcInv(v)
    }
  }

  /**
   * regularized incomplete gamma function  \int_0x \exp(-t)pow(t,a-1) dt / Gamma(a)
   *
   * @see http://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math3/special/Gamma.html#regularizedGammaP(double, double)
   */
  object gammp extends MappingUFunc {
    implicit object gammpImplIntInt extends Impl2[Int, Int, Double] {
      def apply(v1: Int, v2: Int): Double = G.regularizedGammaP(v1.toDouble, v2.toDouble)
    }

    implicit object gammpImplDoubleDouble extends Impl2[Double, Double, Double] {
      def apply(v1: Double, v2: Double): Double = G.regularizedGammaP(v1, v2)
    }
  }

  /**
   * regularized incomplete gamma function  \int_0x \exp(-t)pow(t,a-1) dt / Gamma(a)
   *
   * @see http://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math3/special/Gamma.html#regularizedGammaP(double, double)
   */
  object gammq extends MappingUFunc {
    implicit object gammqImplIntInt extends Impl2[Int, Int, Double] {
      def apply(v1: Int, v2: Int): Double = G.regularizedGammaP(v1.toDouble, v2.toDouble)
    }

    implicit object gammqImplDoubleDouble extends Impl2[Double, Double, Double] {
      def apply(v1: Double, v2: Double): Double = G.regularizedGammaP(v1, v2)
    }
  }

  /**
   * The sigmoid function: 1/(1 + exp(-x))
   *
   *
   */
  object sigmoid extends MappingUFunc {
    implicit object sigmoidImplInt extends Impl[Int, Double] {
      def apply(x: Int) = 1d / (1d + scala.math.exp(-x.toDouble))
    }

    implicit object sigmoidImplDouble extends Impl[Double, Double] {
      def apply(x: Double) = 1d / (1d + scala.math.exp(-x))
    }

    implicit object sigmoidImplFloat extends Impl[Float, Float] {
      def apply(x: Float) = 1f / (1f + scala.math.exp(-x).toFloat)
    }
  }

  /**
   * The logit (inverse sigmoid) function: -log((1/x) - 1)
   *
   */
  object logit extends MappingUFunc {
    implicit object logitImplInt extends Impl[Int, Double] {
      def apply(x: Int) = -scala.math.log((1d / (x.toDouble)) - 1d)
    }

    implicit object logitImplDouble extends Impl[Double, Double] {
      def apply(x: Double) = -scala.math.log((1d / x) - 1d)
    }

    implicit object logitImplFloat extends Impl[Float, Float] {
      def apply(x: Float) = -scala.math.log((1f / x) - 1f).toFloat
    }
  }

  /**
   * The Relu function: max(0, x)
   *
   * @see https://en.wikipedia.org/wiki/Rectifier_(neural_networks)
   *
   */
  object relu extends ZeroPreservingUFunc {

    implicit object reluImplDouble extends Impl[Double, Double] {
      def apply(x: Double) = max(0d, x)
    }

    implicit object reluImplInt extends Impl[Int, Int] {
      def apply(x: Int) = max(0, x)
    }
  }

  /**
   * The Step function: if (x > 0) 1 else 0
   *
   * @see https://en.wikipedia.org/wiki/Step_function
   */
  object step extends MappingUFunc {
    implicit object stepImplInt extends Impl[Int, Int] {
      def apply(x: Int) = if (x > 0) 1 else 0
    }

    implicit object stepImplDouble extends Impl[Double, Int] {
      def apply(x: Double) = if (x > 0d) 1 else 0
    }

    implicit object stepImplFloat extends Impl[Float, Int] {
      def apply(x: Float) = if (x > 0f) 1 else 0
    }
  }

  /**
   * Computes the polynomial P(x) with coefficients given in the passed in array.
   * coefs(i) is the coef for the x_i term.
   */
  def polyval(coefs: Array[Double], x: Double) = {
    var i = coefs.length - 1
    var p = coefs(i)
    while (i > 0) {
      i -= 1
      p = p * x + coefs(i)
    }
    p
  }

  /**
   * closeTo for Doubles.
   */
  def closeTo(a: Double, b: Double, relDiff: Double = 1e-4) = {
    a == b || (scala.math.abs(a - b) < scala.math
      .max(scala.math.max(scala.math.abs(a), scala.math.abs(b)), 1) * relDiff)
  }

  /**
   * The indicator function. 1.0 iff b, else 0.0
   * For non-boolean arguments, 1.0 iff b != 0, else 0.0
   */
  object I extends ZeroPreservingUFunc {
    implicit object iBoolImpl extends Impl[Boolean, Double] {
      def apply(b: Boolean) = if (b) 1.0 else 0.0
    }

    implicit def vImpl[V: Semiring]: Impl[V, Double] = new Impl[V, Double] {
      def apply(b: V) = if (b != implicitly[Semiring[V]].zero) 1.0 else 0.0
    }
  }

  /**
   * The indicator function in log space: 0.0 iff b else Double.NegativeInfinity
   */
  object logI extends UFunc with breeze.generic.MappingUFunc {
    implicit object logIBoolImpl extends Impl[Boolean, Double] {
      def apply(b: Boolean) = if (b) 0.0 else -inf
    }
  }

}
