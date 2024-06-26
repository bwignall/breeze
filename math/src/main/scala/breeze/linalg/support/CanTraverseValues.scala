package breeze.linalg.support

/*
 Copyright 2012 David Hall

 Licensed under the Apache License, Version 2.0 (the "License")
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
import breeze.linalg.support.CanTraverseValues.ValuesVisitor
import breeze.math.Complex

import scala.collection.compat.IterableOnce

/**
 * Marker for being able to traverse over the values in a collection/tensor
 *
 * @author dramage
 * @author dlwh
 */
trait CanTraverseValues[From, A] {

  /**Traverses all values from the given collection. */
  def traverse(from: From, fn: ValuesVisitor[A]): fn.type
  def isTraversableAgain(from: From): Boolean

  def foldLeft[B](from: From, b: B)(fn: (B, A) => B): B = {
    var bb = b

    traverse(
      from,
      new ValuesVisitor[A] {
        override def visit(a: A): Unit = {
          bb = fn(bb, a)
        }

        override def zeros(numZero: Int, zeroValue: A): Unit = {
          for (i <- 0 until numZero) {
            bb = fn(bb, zeroValue)
          }
        }
      }
    )

    bb
  }
}

object CanTraverseValues extends LowPrioCanTraverseValues {

  trait ValuesVisitor[@specialized A] {
    def visit(a: A): Unit
    def visitArray(arr: Array[A]): Unit = visitArray(arr, 0, arr.length, 1)

    def visitArray(arr: Array[A], offset: Int, length: Int, stride: Int): Unit = {
      import breeze.macros._
      // Standard array bounds check stuff
      if (stride == 1) {
        cforRange(offset until length + offset) { i =>
          visit(arr(i))
        }
      } else {
        cforRange(0 until length) { i =>
          visit(arr(i * stride + offset))
        }
      }
    }
    def zeros(numZero: Int, zeroValue: A): Unit
  }

  //
  // Arrays
  //

  // stupid scala 2.12
  class OpArray[ /*@specialized(Double, Int, Float, Long)*/ A] extends CanTraverseValues[Array[A], A] {
    def traverse(from: Array[A], fn: ValuesVisitor[A]): fn.type = {
      fn.visitArray(from)
      fn
    }

    def isTraversableAgain(from: Array[A]): Boolean = true
  }

  implicit def opArray[@specialized A]: OpArray[A] = new OpArray[A]

  implicit object OpArrayII extends OpArray[Int]

  implicit object OpArraySS extends OpArray[Short]

  implicit object OpArrayLL extends OpArray[Long]

  implicit object OpArrayFF extends OpArray[Float]

  implicit object OpArrayDD extends OpArray[Double]

  implicit object OpArrayCC extends OpArray[Complex]

}

trait LowPrioCanTraverseValues2 {
//  implicit def canTraverseSelf[V, V2]: CanTraverseValues[V, V] = {
//    new CanTraverseValues[V, V] {
//
//      override def traverse(from: V, fn: CanTraverseValues.ValuesVisitor[V]): Unit = {
//        fn.visit(from)
//      }
//
//      def isTraversableAgain(from: V): Boolean = true
//    }
//  }
}

trait LowPrioCanTraverseValues extends LowPrioCanTraverseValues2 {
  implicit def canTraverseTraversable[V, X <: IterableOnce[V]]: CanTraverseValues[X, V] = {
    new CanTraverseValues[X, V] {
      override def traverse(from: X, fn: CanTraverseValues.ValuesVisitor[V]): fn.type = {
        for (v <- from) {
          fn.visit(v)
        }
        fn
      }

      def isTraversableAgain(from: X): Boolean = from.isInstanceOf[Iterable[V]]
    }
  }

  implicit def canTraverseIterator[V]: CanTraverseValues[Iterator[V], V] = {
    new CanTraverseValues[Iterator[V], V] {

      override def traverse(from: Iterator[V], fn: CanTraverseValues.ValuesVisitor[V]): fn.type = {
        for (v <- from) {
          fn.visit(v)
        }
        fn
      }

      def isTraversableAgain(from: Iterator[V]): Boolean = from.isInstanceOf[Iterable[_]]
    }
  }
}
