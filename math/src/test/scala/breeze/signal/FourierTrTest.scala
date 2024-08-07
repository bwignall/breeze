package breeze.signal

import breeze.linalg._
import breeze.math.Complex
import org.scalatest._
import org.scalatest.funsuite._

/**
 * Created with IntelliJ IDEA.
 * User: takagaki
 * Date: 14.05.13
 * Time: 02:31
 * To change this template use File | Settings | File Templates.
 */
class FourierTrTest extends AnyFunSuite {

  // <editor-fold desc="FourierTr">
  test("fft 1D of DenseVector[Complex]") {
    assert(norm(fourierTr(test16C) - test16fftC) < testNormThreshold)
  }

  test("fft 1D of DenseVector[Double]") {
    assert(norm(fourierTr(test16) - test16fftC) < testNormThreshold)
  }

  test("ifft 1D of DenseVector[Complex]") {
    assert(norm(iFourierTr(test16fftC) - test16C) < testNormThreshold)
  }

  test("ifft 1D of DenseVector[Double]") {
    assert(norm(iFourierTr(test16) - test16ifftC) < testNormThreshold)
  }

  test("fft 1D of DenseMatrix[Double] columns") {
    val dm = test16.asDenseMatrix.t
    assert(dm.cols === 1)
    val transformed = fourierTr(dm(::, *))
    assert(norm(transformed(::, 0) - test16fftC) < testNormThreshold, s"$transformed $test16fftC")
  }

  test("fft 2D of DenseMatrix[Complex]") {
    assert(norm((fourierTr(test5x5C) - test5x5fftC).toDenseVector) < testNormThreshold)
  }

  test("fft 2D of DenseMatrix[Double]") {
    assert(norm((fourierTr(test5x5) - test5x5fftC).toDenseVector) < testNormThreshold)
  }

  test("ifft 2D of DenseMatrix[Complex]") {
    assert(norm((iFourierTr(test5x5fftC) - test5x5C).toDenseVector) < testNormThreshold)
  }

  test("ifft 2D of DenseMatrix[Double]") {
    assert(norm((iFourierTr(test5x5) - test5x5ifftC).toDenseVector) < testNormThreshold)
  }
  // </editor-fold>
  // <editor-fold desc="Test Values">

  val testNormThreshold = 1e-12

  val test16: DenseVector[Double] = DenseVector[Double](0.814723686393179, 0.905791937075619, 0.126986816293506,
                                                        0.913375856139019, 0.63235924622541, 0.0975404049994095,
                                                        0.278498218867048, 0.546881519204984, 0.957506835434298,
                                                        0.964888535199277, 0.157613081677548, 0.970592781760616,
                                                        0.957166948242946, 0.485375648722841, 0.8002804688888,
                                                        0.141886338627215)

  val test16C: DenseVector[Complex] = DenseVector[Complex](
    Complex(0.814723686393179, 0),
    Complex(0.905791937075619, 0),
    Complex(0.126986816293506, 0),
    Complex(0.913375856139019, 0),
    Complex(0.63235924622541, 0),
    Complex(0.0975404049994095, 0),
    Complex(0.278498218867048, 0),
    Complex(0.546881519204984, 0),
    Complex(0.957506835434298, 0),
    Complex(0.964888535199277, 0),
    Complex(0.157613081677548, 0),
    Complex(0.970592781760616, 0),
    Complex(0.957166948242946, 0),
    Complex(0.485375648722841, 0),
    Complex(0.8002804688888, 0),
    Complex(0.141886338627215, 0)
  )

  val test16fftC: DenseVector[Complex] = DenseVector[Complex](
    Complex(9.75146832375171, 0),
    Complex(-0.0977261644584599, 0.994224442620027),
    Complex(0.248156703823313, -0.961542739609668),
    Complex(-0.973134608372272, -0.424078607189411),
    Complex(1.99837813056893, 0.119139969734688),
    Complex(-0.00703114442532593, -0.5556868176116),
    Complex(0.11725195089493, -2.54990031917926),
    Complex(0.506759321091583, -0.436614575872306),
    Complex(-0.301197719706247, 0),
    Complex(0.506759321091583, 0.436614575872306),
    Complex(0.11725195089493, 2.54990031917926),
    Complex(-0.00703114442532593, 0.5556868176116),
    Complex(1.99837813056893, -0.119139969734688),
    Complex(-0.973134608372272, 0.424078607189411),
    Complex(0.248156703823313, 0.961542739609668),
    Complex(-0.0977261644584599, -0.994224442620027)
  )

  val test16ifftC: DenseVector[Complex] = DenseVector[Complex](
    Complex(0.609466770234482, 0),
    Complex(-0.00610788527865374, -0.0621390276637517),
    Complex(0.0155097939889571, 0.0600964212256043),
    Complex(-0.060820913023267, 0.0265049129493382),
    Complex(0.124898633160558, -0.00744624810841799),
    Complex(-0.000439446526582871, 0.034730426100725),
    Complex(0.00732824693093313, 0.159368769948704),
    Complex(0.0316724575682239, 0.0272884109920191),
    Complex(-0.0188248574816404, 0),
    Complex(0.0316724575682239, -0.0272884109920191),
    Complex(0.00732824693093313, -0.159368769948704),
    Complex(-0.000439446526582871, -0.034730426100725),
    Complex(0.124898633160558, 0.00744624810841799),
    Complex(-0.060820913023267, -0.0265049129493382),
    Complex(0.0155097939889571, -0.0600964212256043),
    Complex(-0.00610788527865374, 0.0621390276637517)
  )

  val test5x5: DenseMatrix[Double] = DenseMatrix(
    (0.498364051982143, 0.959743958516081, 0.340385726666133, 0.585267750979777, 0.223811939491137),
    (0.751267059305653, 0.255095115459269, 0.505957051665142, 0.699076722656686, 0.890903252535799),
    (0.959291425205444, 0.547215529963803, 0.138624442828679, 0.149294005559057, 0.257508254123736),
    (0.840717255983663, 0.254282178971531, 0.814284826068816, 0.243524968724989, 0.929263623187228),
    (0.349983765984809, 0.196595250431208, 0.251083857976031, 0.616044676146639, 0.473288848902729)
  )

  val test5x5C: DenseMatrix[Complex] = DenseVector[Complex](
    Complex(0.498364051982143, 0),
    Complex(0.959743958516081, 0),
    Complex(0.340385726666133, 0),
    Complex(0.585267750979777, 0),
    Complex(0.223811939491137, 0),
    Complex(0.751267059305653, 0),
    Complex(0.255095115459269, 0),
    Complex(0.505957051665142, 0),
    Complex(0.699076722656686, 0),
    Complex(0.890903252535799, 0),
    Complex(0.959291425205444, 0),
    Complex(0.547215529963803, 0),
    Complex(0.138624442828679, 0),
    Complex(0.149294005559057, 0),
    Complex(0.257508254123736, 0),
    Complex(0.840717255983663, 0),
    Complex(0.254282178971531, 0),
    Complex(0.814284826068816, 0),
    Complex(0.243524968724989, 0),
    Complex(0.929263623187228, 0),
    Complex(0.349983765984809, 0),
    Complex(0.196595250431208, 0),
    Complex(0.251083857976031, 0),
    Complex(0.616044676146639, 0),
    Complex(0.473288848902729, 0)
  ).t.reshape(5, 5).t

  val test5x5fftC: DenseMatrix[Complex] = DenseVector[Complex](
    Complex(12.7308755393162, 0),
    Complex(1.42690914298293, 0.677101996312365),
    Complex(0.706711983513261, 0.0992583432580891),
    Complex(0.706711983513261, -0.0992583432580891),
    Complex(1.42690914298293, -0.677101996312365),
    Complex(-0.00414795799652948, -0.550321022506503),
    Complex(-1.07125334159435, -0.84510117240179),
    Complex(-0.882369741341135, -1.50250917640641),
    Complex(-0.278094191956766, 1.05327775860311),
    Complex(-0.851976472301687, -0.41204258211123),
    Complex(0.157643757426616, -1.69405765853058),
    Complex(1.2251658867134, -1.07981496098901),
    Complex(1.07941903837881, -0.328261611189008),
    Complex(-1.49121772761188, 0.542542959158191),
    Complex(-0.152677496093978, 1.94410294403697),
    Complex(0.157643757426616, 1.69405765853058),
    Complex(-0.152677496093978, -1.94410294403697),
    Complex(-1.49121772761188, -0.542542959158191),
    Complex(1.07941903837881, 0.328261611189008),
    Complex(1.2251658867134, 1.07981496098901),
    Complex(-0.00414795799652948, 0.550321022506503),
    Complex(-0.851976472301687, 0.41204258211123),
    Complex(-0.278094191956766, -1.05327775860311),
    Complex(-0.882369741341135, 1.50250917640641),
    Complex(-1.07125334159435, 0.84510117240179)
  ).t.reshape(5, 5).t

  val test5x5ifftC: DenseMatrix[Complex] = DenseVector[Complex](
    Complex(0.509235021572647, 0),
    Complex(0.057076365719317, -0.0270840798524946),
    Complex(0.0282684793405304, -0.00397033373032357),
    Complex(0.0282684793405304, 0.00397033373032357),
    Complex(0.057076365719317, 0.0270840798524946),
    Complex(-0.000165918319861191, 0.0220128409002601),
    Complex(-0.0428501336637738, 0.0338040468960716),
    Complex(-0.0352947896536454, 0.0601003670562562),
    Complex(-0.0111237676782706, -0.0421311103441244),
    Complex(-0.0340790588920675, 0.0164817032844492),
    Complex(0.00630575029706466, 0.0677623063412232),
    Complex(0.049006635468536, 0.0431925984395604),
    Complex(0.0431767615351525, 0.0131304644475603),
    Complex(-0.0596487091044752, -0.0217017183663277),
    Complex(-0.0061070998437591, -0.0777641177614789),
    Complex(0.00630575029706466, -0.0677623063412232),
    Complex(-0.0061070998437591, 0.0777641177614789),
    Complex(-0.0596487091044752, 0.0217017183663277),
    Complex(0.0431767615351525, -0.0131304644475603),
    Complex(0.049006635468536, -0.0431925984395604),
    Complex(-0.000165918319861191, -0.0220128409002601),
    Complex(-0.0340790588920675, -0.0164817032844492),
    Complex(-0.0111237676782706, 0.0421311103441244),
    Complex(-0.0352947896536454, -0.0601003670562562),
    Complex(-0.0428501336637738, -0.0338040468960716)
  ).t.reshape(5, 5).t
  // </editor-fold>

  test("fft 1D of DenseVector[Double], spanned") {

    // assert( norm( fourierTr(test16, 0 to 4) - test16fftC(0 to 4)  ) < testNormThreshold )

  }

  test("fourierShift/iFourierShift") {
    val dvOdd = DenseVector.tabulate(5)((i: Int) => i)
    val dvEven = DenseVector.tabulate(6)((i: Int) => i)
    assert(fourierShift(dvOdd) == DenseVector(3, 4, 0, 1, 2))
    assert(iFourierShift(dvOdd) == DenseVector(2, 3, 4, 0, 1))
    assert(fourierShift(dvEven) == DenseVector(3, 4, 5, 0, 1, 2))
    assert(iFourierShift(dvEven) == DenseVector(3, 4, 5, 0, 1, 2))
  }

  test("fourierFreq") {
    assert(fourierFreq(5, dt = 0.1) == DenseVector(0.0, 2.0, 4.0, -4.0, -2.0))
    assert(fourierFreq(4, dt = 0.1) == DenseVector(0.0, 2.5, -5.0, -2.5))
  }

//  test("#588 multithreading woes?") {
//    Array.tabulate(100)(i => DenseVector.rand(i + 1)).par.map { fourierTr(_) }
//  }

}
