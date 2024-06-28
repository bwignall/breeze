package breeze.linalg

import breeze.generic.UFunc
import breeze.linalg.operators.OpAdd
import breeze.macros.expand
import breeze.storage.Zero
import breeze.util.ReflectionUtil

import scala.reflect.ClassTag

/**
 * Returns a cumulative sum of the vector (ie cumsum).
 * @author ktakagaki
 */
object accumulate extends UFunc {

  implicit def dvAccumulate[T](implicit
    zero: Zero[T],
    add: OpAdd.Impl2[T, T, T]
  ): Impl[DenseVector[T], DenseVector[T]] =
    (dv: DenseVector[T]) => {
      implicit val ct: ClassTag[T] = ReflectionUtil.elemClassTagFromArray(dv.data)
      DenseVector(dv.valuesIterator.scanLeft(zero.zero)(add(_, _)).drop(1).toArray)
    }

}
