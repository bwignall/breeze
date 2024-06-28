package breeze.linalg

import breeze.generic.UFunc

/**
 * breeze
 * 7/15/14
 * @author Gabriel Schubiner <gabeos@cs.washington.edu>
 *
 *
 */
object dim extends UFunc {
  implicit def implVDim[T, V](implicit view: V <:< Vector[T]): Impl[V, Int] = (v: V) => v.length

  implicit def implMDim[T, M](implicit view: M <:< Matrix[T]): Impl[M, (Int, Int)] = (v: M) => (v.rows, v.cols)
}
