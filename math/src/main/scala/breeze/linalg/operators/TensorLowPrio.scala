package breeze.linalg.operators

import breeze.generic.UFunc
import breeze.linalg.support.{CanSlice, CanSlice2, CanTranspose}
import breeze.linalg.{SliceMatrix, SliceVector, Tensor, Transpose}
import breeze.math.Semiring

import scala.reflect.ClassTag

trait TensorLowPrio extends GenericOps {
  implicit def canSliceTensor_Seq_to_2[K, V, Res](implicit
    seqSlice: CanSlice[Tensor[K, V], Seq[K], Res]
  ): CanSlice2[Tensor[K, V], K, K, Res] = {
    new CanSlice2[Tensor[K, V], K, K, Res] {
      def apply(from: Tensor[K, V], slice: K, slice2: K): Res = {
        seqSlice(from, Seq(slice, slice2))
      }
    }
  }

  implicit def canSliceTensor[K, V: ClassTag]: CanSlice[Tensor[K, V], Seq[K], SliceVector[K, V]] =
    new CanSlice[Tensor[K, V], Seq[K], SliceVector[K, V]] {
      def apply(from: Tensor[K, V], slice: Seq[K]): SliceVector[K, V] = new SliceVector(from, slice.toIndexedSeq)
    }

  implicit def canSliceTensorBoolean[K, V: ClassTag]: CanSlice[Tensor[K, V], Tensor[K, Boolean], SliceVector[K, V]] =
    new CanSlice[Tensor[K, V], Tensor[K, Boolean], SliceVector[K, V]] {
      override def apply(from: Tensor[K, V], slice: Tensor[K, Boolean]): SliceVector[K, V] = {
        new SliceVector(from, slice.findAll(_ == true))
      }
    }

  implicit def canSliceTensor2[K1, K2, V: Semiring: ClassTag]
    : CanSlice2[Tensor[(K1, K2), V], Seq[K1], Seq[K2], SliceMatrix[K1, K2, V]] = {
    new CanSlice2[Tensor[(K1, K2), V], Seq[K1], Seq[K2], SliceMatrix[K1, K2, V]] {
      def apply(from: Tensor[(K1, K2), V], slice: Seq[K1], slice2: Seq[K2]): SliceMatrix[K1, K2, V] = {
        new SliceMatrix(from, slice.toIndexedSeq, slice2.toIndexedSeq)
      }
    }
  }

  implicit def canSliceTensor2_CRs[K1, K2, V: Semiring: ClassTag]
    : CanSlice2[Tensor[(K1, K2), V], Seq[K1], K2, SliceVector[(K1, K2), V]] = {
    new CanSlice2[Tensor[(K1, K2), V], Seq[K1], K2, SliceVector[(K1, K2), V]] {
      def apply(from: Tensor[(K1, K2), V], slice: Seq[K1], slice2: K2): SliceVector[(K1, K2), V] = {
        new SliceVector(from, slice.map(k1 => (k1, slice2)).toIndexedSeq)
      }
    }
  }

  implicit def canSliceTensor2_CsR[K1, K2, V: Semiring: ClassTag]
    : CanSlice2[Tensor[(K1, K2), V], K1, Seq[K2], Transpose[SliceVector[(K1, K2), V]]] = {
    new CanSlice2[Tensor[(K1, K2), V], K1, Seq[K2], Transpose[SliceVector[(K1, K2), V]]] {
      def apply(from: Tensor[(K1, K2), V], slice: K1, slice2: Seq[K2]): Transpose[SliceVector[(K1, K2), V]] = {
        new SliceVector(from, slice2.map(k2 => (slice, k2)).toIndexedSeq).t
      }
    }
  }
}
