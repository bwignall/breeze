package breeze.linalg.operators

import breeze.generic.UFunc
import breeze.linalg._
import breeze.linalg.support.CanCopy
import breeze.linalg.support.CanTransformValues
import breeze.linalg.support.CanTraverseValues
import breeze.linalg.support.CanZipMapKeyValues
import breeze.linalg.support.CanZipMapValues
import breeze.math.Field
import breeze.math.Ring
import breeze.math.Semiring
import breeze.storage.Zero
import breeze.util.ScalaVersion

trait CounterOps {
  implicit def canCopy[K1, V: Zero: Semiring]: CanCopy[Counter[K1, V]] = new CanCopy[Counter[K1, V]] {
    def apply(t: Counter[K1, V]): Counter[K1, V] = {
      Counter(t.iterator)
    }
  }

  def binaryOpFromBinaryUpdateOp[K, V, Other, Op <: OpType](implicit
    copy: CanCopy[Counter[K, V]],
    op: UFunc.InPlaceImpl2[Op, Counter[K, V], Other]
  ): UFunc.UImpl2[Op, Counter[K, V], Other, Counter[K, V]] = {
    new UFunc.UImpl2[Op, Counter[K, V], Other, Counter[K, V]] {
      override def apply(a: Counter[K, V], b: Other) = {
        val c = copy(a)
        op(c, b)
        c
      }
    }
  }

  implicit def addIntoVV[K1, V: Semiring]: OpAdd.InPlaceImpl2[Counter[K1, V], Counter[K1, V]] =
    new OpAdd.InPlaceImpl2[Counter[K1, V], Counter[K1, V]] {
      val field = implicitly[Semiring[V]]
      def apply(a: Counter[K1, V], b: Counter[K1, V]): Unit = {
        // scala 2.13 hashmaps invalidate iterators if you change values, even if the keys are already there
        val it = if (ScalaVersion.is213 && (b eq a)) {
          b.activeIterator.toSet.iterator
        } else {
          b.activeIterator
        }

        for ((k, v) <- it) {
          a(k) = field.+(a(k), v)
        }
      }
    }

  implicit def canAxpy[K1, V: Semiring]: scaleAdd.InPlaceImpl3[Counter[K1, V], V, Counter[K1, V]] =
    new scaleAdd.InPlaceImpl3[Counter[K1, V], V, Counter[K1, V]] {
      val field = implicitly[Semiring[V]]
      def apply(a: Counter[K1, V], s: V, b: Counter[K1, V]): Unit = {
        // scala 2.13 hashmaps invalidate iterators if you change values, even if the keys are already there
        val it = if (ScalaVersion.is213 && (b eq a)) {
          b.activeIterator.toSet.iterator
        } else {
          b.activeIterator
        }

        for ((k, v) <- it) {
          a(k) = field.+(a(k), field.*(s, v))
        }
      }
    }

  implicit def addVV[K1, V: Semiring: Zero]: OpAdd.Impl2[Counter[K1, V], Counter[K1, V], Counter[K1, V]] = {
    binaryOpFromBinaryUpdateOp(canCopy, addIntoVV)
  }

  implicit def addIntoVS[K1, V: Semiring]: OpAdd.InPlaceImpl2[Counter[K1, V], V] =
    new OpAdd.InPlaceImpl2[Counter[K1, V], V] {
      val field = implicitly[Semiring[V]]
      def apply(a: Counter[K1, V], b: V): Unit = {
        // scala 2.13 hashmaps invalidate iterators if you change values, even if the keys are already there
        val it = if (ScalaVersion.is213) {
          a.activeIterator.toSet.iterator
        } else {
          a.activeIterator
        }

        for ((k, v) <- it) {
          a(k) = field.+(v, b)
        }
      }
    }

  implicit def addVS[K1, V: Semiring: Zero]: OpAdd.Impl2[Counter[K1, V], V, Counter[K1, V]] = {
    binaryOpFromBinaryUpdateOp(canCopy, addIntoVS)
  }

  implicit def subIntoVV[K1, V: Ring]: OpSub.InPlaceImpl2[Counter[K1, V], Counter[K1, V]] = {
    new OpSub.InPlaceImpl2[Counter[K1, V], Counter[K1, V]] {
      val field = implicitly[Ring[V]]
      def apply(a: Counter[K1, V], b: Counter[K1, V]): Unit = {
        // scala 2.13 hashmaps invalidate iterators if you change values, even if the keys are already there
        val it = if (ScalaVersion.is213 && (b eq a)) {
          b.activeIterator.toSet.iterator
        } else {
          b.activeIterator
        }

        for ((k, v) <- it) {
          a(k) = field.-(a(k), v)
        }
      }
    }
  }

  implicit def subVV[K1, V: Ring: Zero]: OpSub.Impl2[Counter[K1, V], Counter[K1, V], Counter[K1, V]] = {
    binaryOpFromBinaryUpdateOp(canCopy, subIntoVV)
  }

  implicit def subIntoVS[K1, V: Ring]: OpSub.InPlaceImpl2[Counter[K1, V], V] =
    new OpSub.InPlaceImpl2[Counter[K1, V], V] {
      val field = implicitly[Ring[V]]
      def apply(a: Counter[K1, V], b: V): Unit = {
        for ((k, v) <- a.activeIterator) {
          a(k) = field.-(v, b)
        }
      }
    }

  implicit def subVS[K1, V: Ring: Zero]: OpSub.Impl2[Counter[K1, V], V, Counter[K1, V]] = {
    binaryOpFromBinaryUpdateOp(canCopy, subIntoVS)
  }

  implicit def canMulIntoVV[K2, K1 <: K2, V: Semiring]: OpMulScalar.InPlaceImpl2[Counter[K1, V], Counter[K2, V]] =
    new OpMulScalar.InPlaceImpl2[Counter[K1, V], Counter[K2, V]] {
      val field = implicitly[Semiring[V]]
      def apply(a: Counter[K1, V], b: Counter[K2, V]): Unit = {
        // scala 2.13 hashmaps invalidate iterators if you change values, even if the keys are already there
        val it = if (ScalaVersion.is213) {
          a.activeIterator.toSet.iterator
        } else {
          a.activeIterator
        }

        for ((k, v) <- it) {
          a(k) = field.*(v, b(k))
        }
      }
    }

  implicit def canMulVV[K1, V](implicit
    semiring: Semiring[V]
  ): OpMulScalar.Impl2[Counter[K1, V], Counter[K1, V], Counter[K1, V]] = {
    new OpMulScalar.Impl2[Counter[K1, V], Counter[K1, V], Counter[K1, V]] {
      override def apply(a: Counter[K1, V], b: Counter[K1, V]) = {
        val r = Counter[K1, V]()
        for ((k, v) <- a.activeIterator) {
          val vr = semiring.*(v, b(k))
          if (vr != semiring.zero)
            r(k) = vr
        }
        r
      }
    }
  }

  implicit def canMulIntoVS[K2, K1 <: K2, V: Semiring]: OpMulScalar.InPlaceImpl2[Counter[K1, V], V] =
    new OpMulScalar.InPlaceImpl2[Counter[K1, V], V] {
      val field = implicitly[Semiring[V]]
      def apply(a: Counter[K1, V], b: V): Unit = {
        // scala 2.13 hashmaps invalidate iterators if you change values, even if the keys are already there
        val it = if (ScalaVersion.is213) {
          a.activeIterator.toSet.iterator
        } else {
          a.activeIterator
        }

        for ((k, v) <- it) {
          a(k) = field.*(v, b)
        }
      }
    }

  implicit def canMulIntoVS_M[K2, K1 <: K2, V: Semiring]: OpMulMatrix.InPlaceImpl2[Counter[K1, V], V] =
    new OpMulMatrix.InPlaceImpl2[Counter[K1, V], V] {
      val field = implicitly[Semiring[V]]
      def apply(a: Counter[K1, V], b: V): Unit = {
        // scala 2.13 hashmaps invalidate iterators if you change values, even if the keys are already there
        val it = if (ScalaVersion.is213) {
          a.activeIterator.toSet.iterator
        } else {
          a.activeIterator
        }

        for ((k, v) <- it) {
          a(k) = field.*(v, b)
        }
      }
    }

  implicit def canMulVS[K2, K1 <: K2, V](implicit
    semiring: Semiring[V]
  ): OpMulScalar.Impl2[Counter[K1, V], V, Counter[K1, V]] = {
    new OpMulScalar.Impl2[Counter[K1, V], V, Counter[K1, V]] {
      override def apply(a: Counter[K1, V], b: V) = {
        val r = Counter[K1, V]()
        for ((k, v) <- a.activeIterator) {
          val vr = semiring.*(v, b)
          r(k) = vr
        }
        r
      }
    }
  }

  implicit def canMulVS_M[K2, K1 <: K2, V](implicit
    semiring: Semiring[V]
  ): OpMulMatrix.Impl2[Counter[K1, V], V, Counter[K1, V]] = {
    new OpMulMatrix.Impl2[Counter[K1, V], V, Counter[K1, V]] {
      override def apply(a: Counter[K1, V], b: V) = {
        val r = Counter[K1, V]()
        for ((k, v) <- a.activeIterator) {
          val vr = semiring.*(v, b)
          r(k) = vr
        }
        r
      }
    }
  }

  implicit def canDivIntoVV[K1, V: Field]: OpDiv.InPlaceImpl2[Counter[K1, V], Counter[K1, V]] = {
    new OpDiv.InPlaceImpl2[Counter[K1, V], Counter[K1, V]] {
      val field = implicitly[Field[V]]
      def apply(a: Counter[K1, V], b: Counter[K1, V]): Unit = {
        // scala 2.13 hashmaps invalidate iterators if you change values, even if the keys are already there
        val it = if (ScalaVersion.is213) {
          a.activeIterator.toSet.iterator
        } else {
          a.activeIterator
        }

        for ((k, v) <- it) {
          a(k) = field./(v, b(k))
        }
      }
    }
  }

  implicit def canDivVV[K1, V](implicit
    copy: CanCopy[Counter[K1, V]],
    semiring: Field[V]
  ): OpDiv.Impl2[Counter[K1, V], Counter[K1, V], Counter[K1, V]] = {
    new OpDiv.Impl2[Counter[K1, V], Counter[K1, V], Counter[K1, V]] {
      override def apply(a: Counter[K1, V], b: Counter[K1, V]) = {
        val r = Counter[K1, V]()
        for ((k, v) <- a.activeIterator) {
          val vr = semiring./(v, b(k))
          r(k) = vr
        }
        r
      }
    }
  }

  implicit def canDivVS[K1, V](implicit
    copy: CanCopy[Counter[K1, V]],
    semiring: Field[V]
  ): OpDiv.Impl2[Counter[K1, V], V, Counter[K1, V]] = {
    new OpDiv.Impl2[Counter[K1, V], V, Counter[K1, V]] {
      override def apply(a: Counter[K1, V], b: V) = {
        val r = Counter[K1, V]()
        for ((k, v) <- a.activeIterator) {
          val vr = semiring./(v, b)
          r(k) = vr
        }
        r
      }
    }
  }

  implicit def canDivIntoVS[K1, V: Field]: OpDiv.InPlaceImpl2[Counter[K1, V], V] =
    new OpDiv.InPlaceImpl2[Counter[K1, V], V] {
      val field = implicitly[Field[V]]
      def apply(a: Counter[K1, V], b: V): Unit = {
        // scala 2.13 hashmaps invalidate iterators if you change values, even if the keys are already there
        val it = if (ScalaVersion.is213) {
          a.activeIterator.toSet.iterator
        } else {
          a.activeIterator
        }

        for ((k, v) <- it) {
          a(k) = field./(v, b)
        }
      }
    }

  implicit def impl_OpSet_InPlace_C_C[K1, K2 <: K1, V]: OpSet.InPlaceImpl2[Counter[K1, V], Counter[K2, V]] =
    new OpSet.InPlaceImpl2[Counter[K1, V], Counter[K2, V]] {
      def apply(a: Counter[K1, V], b: Counter[K2, V]): Unit = {
        a.data.clear()
        for ((k, v) <- b.activeIterator) {
          a(k) = v
        }
      }
    }

  implicit def canSetIntoVS[K1, V]: OpSet.InPlaceImpl2[Counter[K1, V], V] = {
    new OpSet.InPlaceImpl2[Counter[K1, V], V] {
      def apply(a: Counter[K1, V], b: V): Unit = {
        // scala 2.13 hashmaps invalidate iterators if you change values, even if the keys are already there
        val it = if (ScalaVersion.is213) {
          a.keysIterator.toSet.iterator
        } else {
          a.keysIterator
        }

        for (k <- it) {
          a(k) = b
        }
      }
    }
  }

  implicit def canNegate[K1, V](implicit ring: Ring[V]): OpNeg.Impl[Counter[K1, V], Counter[K1, V]] = {
    new OpNeg.Impl[Counter[K1, V], Counter[K1, V]] {
      override def apply(a: Counter[K1, V]) = {
        val result = Counter[K1, V]()
        for ((k, v) <- a.activeIterator) {
          val vr = ring.negate(v)
          result(k) = vr
        }
        result
      }
    }
  }

  implicit def canMulInner[K1, V](implicit
    copy: CanCopy[Counter[K1, V]],
    semiring: Semiring[V]
  ): OpMulInner.Impl2[Counter[K1, V], Counter[K1, V], V] = {
    new OpMulInner.Impl2[Counter[K1, V], Counter[K1, V], V] {
      val zero = semiring.zero
      override def apply(a: Counter[K1, V], b: Counter[K1, V]): V = {
        if (a.activeSize > b.activeSize) {
          apply(b, a)
        } else {
          var result = zero
          for ((k, v) <- a.activeIterator) {
            val vr = semiring.*(v, b(k))
            result = semiring.+(result, vr)
          }
          result
        }
      }
    }
  }

  implicit def canNorm[K, V](implicit normImpl: norm.Impl[V, Double]): norm.Impl2[Counter[K, V], Double, Double] =
    norm.fromTraverseValues

  class CanZipMapValuesCounter[K, V, RV: Zero: Semiring] extends CanZipMapValues[Counter[K, V], V, RV, Counter[K, RV]] {

    /**Maps all corresponding values from the two collection. */
    def map(from: Counter[K, V], from2: Counter[K, V], fn: (V, V) => RV) = {
      val result = Counter[K, RV]()
      for (k <- from.keySet ++ from2.keySet) {
        result(k) = fn(from(k), from2(k))
      }
      result
    }

  }

  implicit def zipMap[K, V, R: Zero: Semiring]: CanZipMapValuesCounter[K, V, R] = new CanZipMapValuesCounter[K, V, R]

  class CanZipMapKeyValuesCounter[K, V, RV: Zero: Semiring]
      extends CanZipMapKeyValues[Counter[K, V], K, V, RV, Counter[K, RV]] {

    /**Maps all corresponding values from the two collection. */
    def map(from: Counter[K, V], from2: Counter[K, V], fn: (K, V, V) => RV) = {
      val result = Counter[K, RV]()
      for (k <- from.keySet ++ from2.keySet) {
        result(k) = fn(k, from(k), from2(k))
      }
      result
    }

    override def mapActive(from: Counter[K, V], from2: Counter[K, V], fn: (K, V, V) => RV): Counter[K, RV] = {
      map(from, from2, fn)
    }
  }

  implicit def zipMapKeyValues[K, V, R: Zero: Semiring]: CanZipMapKeyValuesCounter[K, V, R] =
    new CanZipMapKeyValuesCounter[K, V, R]

  implicit def canTransformValues[L, V]: CanTransformValues[Counter[L, V], V] = {
    new CanTransformValues[Counter[L, V], V] {
      def transform(from: Counter[L, V], fn: (V) => V): Unit = {
        for ((k, v) <- from.activeIterator) {
          from(k) = fn(v)
        }
      }

      def transformActive(from: Counter[L, V], fn: (V) => V): Unit = {
        transform(from, fn)
      }
    }
  }

  implicit def canTraverseValues[L, V]: CanTraverseValues[Counter[L, V], V] = {
    new CanTraverseValues[Counter[L, V], V] {
      override def traverse(from: Counter[L, V], fn: CanTraverseValues.ValuesVisitor[V]): fn.type = {
        from.activeValuesIterator.foreach(fn.visit)
        fn
      }

      override def isTraversableAgain(from: Counter[L, V]): Boolean = true
    }
  }
}
