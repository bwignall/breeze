package breeze.linalg

import breeze.generic.UFunc
import breeze.linalg.operators.OpMulInner
import breeze.linalg.operators.OpSub
import breeze.macros._

/**
 * Computes the squared distance between two vectors.
 *
 * @author dlwh
 **/
object squaredDistance extends UFunc with squaredDistanceLowPrio {

  implicit def squaredDistanceFromZippedValues[T, U](implicit
    zipImpl: zipValues.Impl2[T, U, ZippedValues[Double, Double]]
  ): Impl2[T, U, Double] = { (v: T, v2: U) =>
    {
      var squaredDistance = 0.0
      zipValues(v, v2).foreach { (a, b) =>
        val score = a - b
        squaredDistance += (score * score)
      }
      squaredDistance
    }
  }
}

sealed trait squaredDistanceLowPrio extends UFunc { self: squaredDistance.type =>

  implicit def distanceFromDotAndSub[T, U, V](implicit
    subImpl: OpSub.Impl2[T, U, V],
    dotImpl: OpMulInner.Impl2[V, V, Double]
  ): Impl2[T, U, Double] = { (v: T, v2: U) =>
    {
      val diff = subImpl(v, v2)
      dotImpl(diff, diff)
    }
  }
}
