package breeze.linalg

import java.io.File
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Path, Paths}

/**
 * Created by Luca Puggini: lucapuggio@gmail.com on 19/02/16.
 */
class TextOperationsTest extends AnyFunSuite {
  test("csvread and String2File methods") {
    // A csv file can be read both using the java File function and the toFile method of the string class
    val url = getClass.getClassLoader.getResource("glass_data.txt")
    val file_path: Path = Paths.get(url.toURI)

    val csv1 = csvread(new File(file_path.toString))
    val csv2 = csvread(file_path.toFile)
    assert(csv1 == csv2)
  }
}
