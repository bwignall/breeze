package breeze.signal.support

/**
 * @author ktakagaki
 */
import breeze.linalg.DenseVector
import breeze.signal._

/**
 * Construction delegate trait for filtering type InputType.</p>
 * Implementation details (especially
 * option arguments) may be added in the future, so it is recommended not
 * to call these implicit delegates directly. Instead, use convolve(x: DenseVector).
 *
 * @author ktakagaki
 */
trait CanFilter[Input, KernelType, Output] {
  def apply(data: Input, kernel: KernelType, overhang: OptOverhang, padding: OptPadding): Output
}

/**
 * Construction delegate for filtering type InputType.</p>
 * Implementation details (especially
 * option arguments) may be added in the future, so it is recommended not
 * to call these implicit delegates directly. Instead, use convolve(x: DenseVector).
 *
 * @author ktakagaki
 */
object CanFilter {

  /** Use via implicit delegate syntax filter(x: DenseVector)
   *
   */
  implicit val dvDouble1DFilter: CanFilter[DenseVector[Double], FIRKernel1D[Double], DenseVector[Double]] = {
    (data: DenseVector[Double], kernel: FIRKernel1D[Double], overhang: OptOverhang, padding: OptPadding) => {
      convolve(data, kernel.kernel, OptRange.All, overhang, padding)
    }
  }

  /** Use via implicit delegate syntax filter(x: DenseVector)
   *
   */
  implicit val dvInt1DFilter: CanFilter[DenseVector[Int], FIRKernel1D[Int], DenseVector[Int]] = {
    (data: DenseVector[Int], kernel: FIRKernel1D[Int], overhang: OptOverhang, padding: OptPadding) => {
      convolve(data, kernel.kernel, OptRange.All, overhang, padding)
    }
  }

  /** Use via implicit delegate syntax filter(x: DenseVector)
   *
   */
  implicit val dvDouble1DFilterVectorKernel
    : CanFilter[DenseVector[Double], DenseVector[Double], DenseVector[Double]] = {
    (data: DenseVector[Double], kernel: DenseVector[Double], overhang: OptOverhang, padding: OptPadding) => {
      convolve(data, kernel, /*new FIRKernel1D(kernel, "User-specified kernel"),*/ OptRange.All, overhang, padding)
    }
  }

}
