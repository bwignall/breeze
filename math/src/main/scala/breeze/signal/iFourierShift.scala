package breeze.signal

import breeze.generic.UFunc
import breeze.linalg.DenseVector
import breeze.macros.expand
import breeze.math.Complex
import breeze.numerics._
import breeze.storage.Zero

import scala.reflect.ClassTag

//ToDo: 2D fourierShift/iFourierShift, make horz/vert join function first

/**Inverse shift the zero-frequency component to the center of the spectrum. For odd sequences, this is not
 * equivalent to [[breeze.signal.fourierShift]]
 */
object iFourierShift extends UFunc {

  implicit def implIFourierShift[T: Zero: ClassTag]: Impl[DenseVector[T], DenseVector[T]] = {
    (dft: DenseVector[T]) => {
      if (isEven(dft.length)) DenseVector.vertcat(dft(dft.length / 2 to -1), dft(0 until dft.length / 2))
      else DenseVector.vertcat(dft((dft.length - 1) / 2 to -1), dft(0 until (dft.length - 1) / 2))
    }

  }

}
