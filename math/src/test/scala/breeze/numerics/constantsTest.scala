package breeze.numerics

import breeze.numerics.constants._
import org.scalatest.funsuite.AnyFunSuite

class constantsTest extends AnyFunSuite {

  test("constants test") {
    assert(Database.unit("atomic mass constant energy equivalent") == "J")
    assert(Database.unit(""".*Planck.*""".r).size == 12)
  }

}
