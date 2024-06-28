package breeze.linalg.operators

import breeze.generic.ElementwiseUFunc
import breeze.generic.UFunc
import breeze.gymnastics._
import breeze.linalg._
import breeze.linalg.support._
import breeze.math.Complex
import breeze.math.Semiring
import breeze.storage.Zero

import scala.reflect.ClassTag
import scala.util._

trait TransposeOps extends TransposeOps_Generic with TransposeOps_Complex with CSCMatrix_TransposeOps {}

trait TransposeOps_Generic extends TransposeOps_LowPrio {

  implicit def canUntranspose[T]: CanTranspose[Transpose[T], T] = { (from: Transpose[T]) =>
    from.inner
  }

  implicit def transTimesNormalFromDot[T, U, R](implicit
    dot: OpMulInner.Impl2[T, U, R]
  ): OpMulMatrix.Impl2[Transpose[T], U, R] = { (v: Transpose[T], v2: U) =>
    {
      dot(v.inner, v2)
    }
  }

  implicit def transposeTensor[K, V, T](implicit ev: T <:< Tensor[K, V]): CanTranspose[T, Transpose[T]] = { (from: T) =>
    new Transpose(from)
  }

}

trait TransposeOps_LowPrio extends TransposeOps_LowPrio2 {

  implicit def impl_OpMulMatrix_Ut_T_from_Tt_U[T, TT, U, R, RT](implicit
    transT: CanTranspose[T, TT],
    op: OpMulMatrix.Impl2[TT, U, R],
    canTranspose: CanTranspose[R, RT]
  ): OpMulMatrix.Impl2[Transpose[U], T, RT] = { (v: Transpose[U], v2: T) =>
    canTranspose(op(transT(v2), v.inner))
  }

  implicit def impl_Op_Tt_S_eq_RT_from_T_S[Op, K, V, T, R, RT](implicit
    ev: ScalarOf[T, V],
    op: UFunc.UImpl2[Op, T, V, R],
    canTranspose: CanTranspose[R, RT]
  ): UFunc.UImpl2[Op, Transpose[T], V, RT] = { (a: Transpose[T], b: V) =>
    {
      canTranspose(op(a.inner, b))
    }

  }

  implicit def impl_Op_InPlace_Tt_S_from_T_S[Op, K, V, T](implicit
    ev: ScalarOf[T, V],
    op: UFunc.InPlaceImpl2[Op, T, V]
  ): UFunc.InPlaceImpl2[Op, Transpose[T], V] = { (a: Transpose[T], b: V) =>
    {
      op(a.inner, b)
    }
  }

}

trait TransposeOps_LowPrio2 extends GenericOps {
  implicit def impl_EOp_Tt_Ut_eq_Rt_from_T_U[Op <: ElementwiseUFunc, T, U, R, RT](implicit
    op: UFunc.UImpl2[Op, T, U, R],
    canTranspose: CanTranspose[R, RT]
  ): UFunc.UImpl2[Op, Transpose[T], Transpose[U], RT] = { (a: Transpose[T], b: Transpose[U]) =>
    {
      canTranspose(op(a.inner, b.inner))
    }

  }

  implicit def liftInPlaceOps[Op <: ElementwiseUFunc, T, U, UT](implicit
    notScalar: NotGiven[ScalarOf[T, U]],
    transU: CanTranspose[U, UT],
    op: UFunc.InPlaceImpl2[Op, T, UT]
  ): UFunc.InPlaceImpl2[Op, Transpose[T], U] = { (a: Transpose[T], b: U) =>
    {
      op(a.inner, transU(b))
    }

  }

  implicit class LiftApply[K, T](_trans: Transpose[Tensor[K, T]]) {
    def apply(i: K): T = _trans.inner(i)
  }

  // TODO: make CanSlice a UFunc
  implicit def liftSlice[Op, T, S, U, UT](implicit
    op: CanSlice[T, S, U],
    trans: CanTranspose[U, UT]
  ): CanSlice[Transpose[T], S, UT] = { (from: Transpose[T], slice: S) =>
    {
      op(from.inner, slice).t
    }
  }

  implicit def liftUFunc[Op, T, U, UT](implicit
    op: UFunc.UImpl[Op, T, U],
    trans: CanTranspose[U, UT]
  ): UFunc.UImpl[Op, Transpose[T], UT] = { (v: Transpose[T]) =>
    trans(op(v.inner))
  }

  implicit def impl_Op_InPlace_Tt_from_Op_T[Op, T, U](implicit
    op: UFunc.InPlaceImpl[Op, T]
  ): UFunc.InPlaceImpl[Op, Transpose[T]] = { (v: Transpose[T]) =>
    op(v.inner)
  }

  implicit def liftUFunc3_1[Op <: ElementwiseUFunc, T, T2, U2, T3, U3, R, RT](implicit
    t2Trans: CanTranspose[T2, U2],
    t3Trans: CanTranspose[T3, U3],
    op: UFunc.UImpl3[Op, T, U2, U3, R],
    transR: CanTranspose[R, RT]
  ): UFunc.UImpl3[Op, Transpose[T], T2, T3, RT] = { (v: Transpose[T], v2: T2, v3: T3) =>
    {
      transR(op(v.inner, t2Trans(v2), t3Trans(v3)))
    }
  }

  implicit def liftUFuncInplace3_1[Op, T, T2, U2, U3, T3](implicit
    t2Trans: CanTranspose[T2, U2],
    t3Trans: CanTranspose[T3, U3],
    op: UFunc.InPlaceImpl3[Op, T, U2, U3]
  ): UFunc.InPlaceImpl3[Op, Transpose[T], T2, T3] = { (v: Transpose[T], v2: T2, v3: T3) =>
    {
      op(v.inner, t2Trans(v2), t3Trans(v3))
    }
  }

}

trait TransposeOps_Complex extends TransposeOps_Generic with DenseMatrix_TransposeOps {

  implicit def canTranspose_DV_Complex: CanTranspose[DenseVector[Complex], DenseMatrix[Complex]] = {
    (from: DenseVector[Complex]) =>
      {
        new DenseMatrix(data = from.data.map {
                          _.conjugate
                        },
                        offset = from.offset,
                        cols = from.length,
                        rows = 1,
                        majorStride = from.stride
        )
      }
  }

  implicit def canTranspose_SV_Complex: CanTranspose[SparseVector[Complex], CSCMatrix[Complex]] = {
    (from: SparseVector[Complex]) =>
      {
        val transposedMtx: CSCMatrix[Complex] = CSCMatrix.zeros[Complex](1, from.length)
        var i = 0
        while (i < from.activeSize) {
          val c = from.index(i)
          transposedMtx(0, c) = from.data(i).conjugate
          i += 1
        }
        transposedMtx
      }
  }
}

trait DenseMatrix_TransposeOps extends TransposeOps_Generic {

  implicit def canTranspose_DM[V]: CanTranspose[DenseMatrix[V], DenseMatrix[V]] = { (from: DenseMatrix[V]) =>
    {
      DenseMatrix.create(data = from.data,
                         offset = from.offset,
                         cols = from.rows,
                         rows = from.cols,
                         majorStride = from.majorStride,
                         isTranspose = !from.isTranspose
      )
    }
  }

  implicit def canTranspose_DM_Complex: CanTranspose[DenseMatrix[Complex], DenseMatrix[Complex]] = {
    (from: DenseMatrix[Complex]) =>
      {
        new DenseMatrix(data = from.data.map {
                          _.conjugate
                        },
                        offset = from.offset,
                        cols = from.rows,
                        rows = from.cols,
                        majorStride = from.majorStride,
                        isTranspose = !from.isTranspose
        )
      }
  }

}

trait CSCMatrix_TransposeOps extends TransposeOps_Generic {
  implicit def canTranspose_CSC[V: ClassTag: Zero: Semiring]: CanTranspose[CSCMatrix[V], CSCMatrix[V]] = {
    (from: CSCMatrix[V]) =>
      {
        val transposedMtx = new CSCMatrix.Builder[V](from.cols, from.rows, from.activeSize)

        var j = 0
        while (j < from.cols) {
          var ip = from.colPtrs(j)
          while (ip < from.colPtrs(j + 1)) {
            val i = from.rowIndices(ip)
            transposedMtx.add(j, i, from.data(ip))
            ip += 1
          }
          j += 1
        }
        // this doesn't hold if there are zeros in the matrix
        //        assert(transposedMtx.activeSize == from.activeSize,
        //          s"We seem to have lost some elements?!?! ${transposedMtx.activeSize} ${from.activeSize}")
        transposedMtx.result(false, false)
      }
  }

  implicit def canTranspose_CSC_Complex: CanTranspose[CSCMatrix[Complex], CSCMatrix[Complex]] = {
    (from: CSCMatrix[Complex]) =>
      {
        val transposedMtx = CSCMatrix.zeros[Complex](from.cols, from.rows)

        var j = 0
        while (j < from.cols) {
          var ip = from.colPtrs(j)
          while (ip < from.colPtrs(j + 1)) {
            val i = from.rowIndices(ip)
            transposedMtx(j, i) = from.data(ip).conjugate
            ip += 1
          }
          j += 1
        }
        transposedMtx
      }
  }
}
