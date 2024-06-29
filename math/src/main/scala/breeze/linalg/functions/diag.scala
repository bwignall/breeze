package breeze.linalg

import breeze.generic.UFunc
import breeze.math.Semiring
import breeze.storage.Zero

import scala.collection.mutable
import scala.reflect.ClassTag

/**
 * returns a vector along the diagonal of v.
 * Requires a square matrix?
 */
object diag extends UFunc with diagLowPrio2 {

  implicit def diagDVDMImpl[V: ClassTag: Zero]: diag.Impl[DenseVector[V], DenseMatrix[V]] =
    (t: DenseVector[V]) => {
      val r = DenseMatrix.zeros[V](t.length, t.length)
      diag(r) := t
      r
    }

  implicit def diagDMDVImpl[V]: diag.Impl[DenseMatrix[V], DenseVector[V]] =
    (m: DenseMatrix[V]) => {
      require(m.rows == m.cols, "m must be square")
      DenseVector.create(m.data, m.offset, m.majorStride + 1, m.rows)
    }

  implicit def diagCSCSVImpl[V: ClassTag: Zero]: diag.Impl[CSCMatrix[V], SparseVector[V]] =
    (cm: CSCMatrix[V]) => {
      require(cm.rows == cm.cols, "CSC Matrix must be square")
      var rc = 0
      val sv = SparseVector.zeros[V](cm.rows)
      while (rc < cm.rows) {
        sv(rc) = cm(rc, rc)
        rc += 1
      }
      sv
    }

  implicit def diagSVCSCImpl[V: ClassTag: Semiring: Zero]: diag.Impl[SparseVector[V], CSCMatrix[V]] =
    (t: SparseVector[V]) => {
      val r = new CSCMatrix.Builder[V](t.length, t.length)
      t.activeIterator.foreach(iv => r.add(iv._1, iv._1, iv._2))
      r.result(keysAlreadyUnique = true, keysAlreadySorted = true)
    }

}

trait diagLowPrio extends UFunc { self: UFunc => }

trait diagLowPrio2 extends UFunc with diagLowPrio { self: UFunc => }
