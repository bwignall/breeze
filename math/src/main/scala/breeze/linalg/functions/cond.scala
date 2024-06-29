package breeze.linalg

import breeze.generic.UFunc
import breeze.linalg.svd.DenseSVD
import breeze.linalg.svd.SVD

/**
 * Computes the condition number of the given real matrix.
 */
object cond extends UFunc {
  implicit def canDetUsingSVD[T](implicit svdImpl: svd.Impl[T, DenseSVD]): Impl[T, Double] = { (X: T) =>
    {
      val SVD(_, vecs, _) = svd(X)
      vecs(0) / vecs(vecs.length - 1)
    }
  }
}
