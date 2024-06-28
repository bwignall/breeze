package breeze.signal

import breeze.generic.UFunc
import breeze.linalg._
import breeze.numerics.sqrt

/** Root mean square of a vector. */
object rootMeanSquare extends UFunc {

  implicit def rms1D[Vec](implicit
    normImpl: norm.Impl2[Vec, Int, Double],
    dimImpl: dim.Impl[Vec, Int]
  ): rootMeanSquare.Impl[Vec, Double] = { (v: Vec) =>
    {
      val n: Double = norm(v, 2)
      n / sqrt(dim(v).toDouble)
    }
  }

}
