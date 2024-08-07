package breeze.math

import breeze.linalg._
import breeze.numerics.pow
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop

/**
 * breeze
 * 8/5/14
 * @author Gabriel Schubiner <gabeos@cs.washington.edu>
 *
 *
 */
trait OptimizationSpaceTest[M, V, S] extends TensorSpaceTestBase[V, Int, S] {
  implicit override val space: MutableOptimizationSpace[M, V, S]

  import space._

  def tolRefM(refs: M*): Double = refs.map(norm(_)).max

  implicit def genTripleM: Arbitrary[(M, M, M)]

  test("Addition is Associative - Matrix") {
    check(Prop.forAll { (trip: (M, M, M)) =>
      val (a, b, c) = trip
      closeM((a + b) + c, a + (b + c), TOL * tolRefM(a, b, c))
    })

    check(Prop.forAll { (trip: (M, M, M)) =>
      val (a, b, c) = trip
      val ab = a + b
      val bc = b + c
      ab += c
      bc += a
      closeM(ab, bc, TOL * tolRefM(a, b, c))
    })
  }

  test("Addition Commutes - Matrix") {
    check(Prop.forAll { (trip: (M, M, M)) =>
      val (a, b, _) = trip
      closeM(a + b, b + a, TOL * tolRefM(a, b))
    })

    check(Prop.forAll { (trip: (M, M, M)) =>
      val (a, b, _) = trip
      val ab = copyM(a)
      ab += b
      val ba = copyM(b)
      ba += a
      closeM(ab, ba, TOL * tolRefM(a, b))
    })
  }

  test("Zero is Zero - Matrix") {
    check(Prop.forAll { (trip: (M, M, M)) =>
      val (a, _, _) = trip
      val z = zeroLikeM(a)
      closeM(a +:+ z, a, TOL)
    })
    check(Prop.forAll { (trip: (M, M, M)) =>
      val (a, b, _) = trip
      val ab = copyM(a)
      val z = zeroLikeM(a)
      ab :+= z
      closeM(a, ab, TOL)
    })
  }

  test("a - a == 0 - Matrix") {
    check(Prop.forAll { (trip: (M, M, M)) =>
      val (a, b, c) = trip
      val z = zeroLikeM(a)
      val ama: M = a - a
      closeM(ama, z, TOL)
    })

    check(Prop.forAll { (trip: (M, M, M)) =>
      val (a, b, _) = trip
      val z = zeroLikeM(a)
      a -= a
      closeM(a, z, TOL)
    })

    check(Prop.forAll { (trip: (M, M, M)) =>
      val (a, b, _) = trip
      val z = zeroLikeM(a)
      a :-= a
      closeM(a, z, TOL)
    })

    check(Prop.forAll { (trip: (M, M, M)) =>
      val (a, b, _) = trip
      val z = zeroLikeM(a)
      val ab = a -:- b
      a -= b
      closeM(a, ab, TOL)
    })
  }

  test("Scalar mult distributes over vector addition - Matrix") {
    check(Prop.forAll { (trip: (M, M, M), s: S) =>
      val (a, b, _) = trip
      closeM((a + b) *:* s, (b *:* s) + (a *:* s), TOL * math.max(tolRefM(a, b), norm(s).abs))
    })

    //    check(Prop.forAll{ (trip: (M, M, M), s: S) =>
    //      val (a, b, _) = trip
    //      s == 0 || close( (a + b)/ s, (b / s +a / s), TOL)
    //    })

    check(Prop.forAll { (trip: (M, M, M), s: S) =>
      val (a, b, _) = trip
      val ab = copyM(a)
      ab += b
      ab *= s
      val ba = copyM(a) *:* s
      ba += (b *:* s)
      closeM(ab, ba, TOL * math.max(tolRefM(a, b), norm(s)).abs)
    })
  }

  test("daxpy is consistent - Matrix") {
    check(Prop.forAll { (trip: (M, M, M), s: S) =>
      val (a, b, _) = trip
      val ac = copyM(a)
      val prod = a + (b *:* s)
      breeze.linalg.axpy(s, b, ac)
      closeM(prod, ac, TOL * math.max(tolRefM(a, b), norm(s)).abs)
    })
  }

  test("Scalar mult distributes over field addition - Matrix") {
    check(Prop.forAll { (trip: (M, M, M), s: S, t: S) =>
      val (a, _, _) = trip
      closeM(a *:* scalars.+(s, t), (a *:* s) + (a *:* t), TOLM * max(tolRefM(a), norm(s), norm(t)).abs)
    })

    check(Prop.forAll { (trip: (M, M, M), s: S, t: S) =>
      val (a, _, _) = trip
      val ab = copyM(a)
      ab *= s
      ab += (a *:* t)
      val ba = copyM(a)
      ba *= scalars.+(s, t)
      closeM(ab, ba, TOLM * max(tolRefM(a), norm(s), norm(t)).abs)
    })
  }

  test("Compatibility of scalar multiplication with field multiplication - Matrix") {
    check(Prop.forAll { (trip: (M, M, M), s: S, t: S) =>
      val (a, _, _) = trip
      closeM(a *:* scalars.*(s, t), a *:* s *:* t, TOL * math.max(tolRefM(a), norm(s)).abs)
    })

    check(Prop.forAll { (trip: (M, M, M), s: S, t: S) =>
      val (a, _, _) = trip
      val ab = copyM(a)
      ab *= s
      ab *= t
      val ba = copyM(a)
      ba *= scalars.*(s, t)
      closeM(ab, ba, TOL * max(tolRefM(a), norm(s).abs, norm(t).abs))
    })

    //     check(Prop.forAll{ (trip: (M, M, M), s: S, t: S) =>
    //       val (a, _, _) = trip
    //       s == scalars.zero || t == scalars.zero || {
    //       val ab = copy(a)
    //         ab /= s
    //         ab /= t
    //         val ba = copy(a)
    //         ba /= scalars.*(s, t)
    //         close(ab, ba, TOL)
    //       }
    //     })
  }

  // op set
  test("op set works - Matrix") {
    check(Prop.forAll { (trip: (M, M, M)) =>
      val (a, b, _) = trip
      val ab = copyM(a)
      ab := b
      a + b == (a + ab)
    })
  }

  test("1 is 1 - Matrix") {
    check(Prop.forAll { (trip: (M, M, M)) =>
      val (a, b, c) = trip
      closeM(a *:* scalars.one, a, TOL)
    })

    check(Prop.forAll { (trip: (M, M, M)) =>
      val (a, b, _) = trip
      val ab = copyM(a)
      ab *= scalars.one
      closeM(a, ab, TOL)
    })
  }

  // norm
  val TOLM = 1e-2
  test("norm positive homogeneity - Matrix") {
    check(Prop.forAll { (trip: (M, M, M), s: S) =>
      val (a, b, c) = trip
      norm(a * s) - norm(s) * norm(a) <= TOLM * norm(a * s)
    })
  }

  test("norm triangle inequality - Matrix") {
    check(Prop.forAll { (trip: (M, M, M)) =>
      val (a, b, c) = trip
      (1.0 - TOLM) * norm(a + b) <= norm(b) + norm(a)
    })
  }

  test("norm(v) == 0 iff v == 0 - Matrix") {
    check(Prop.forAll { (trip: (M, M, M)) =>
      val (a, b, c) = trip
      val z = zeroLikeM(a)
      norm(z) == 0.0 && (closeM(z, a, TOLM) || norm(a) != 0.0)
    })
  }

  // dot product distributes
  test("dot product distributes - Matrix") {
    check(Prop.forAll { (trip: (M, M, M)) =>
      val (a, b, c) = trip
      val res = scalars.close(scalars.+(a.dot(b), a.dot(c)), a.dot(b + c), TOLM * tolRefM(a, b, c))
      if (!res)
        println(s"${scalars.+(a.dot(b), a.dot(c))} ${a.dot(b + c)}")
      res
    })

    check(Prop.forAll { (trip: (M, M, M), s: S) =>
      val (a, b, c) = trip
      scalars.close(scalars.*(a.dot(b), s), a.dot(b *:* s))
      scalars.close(scalars.*(s, a.dot(b)), (a *:* s).dot(b))
    })
  }

  // zip map values
  test("zip map of + is the same as + - Matrix") {
    check(Prop.forAll { (trip: (M, M, M)) =>
      val (a, b, _) = trip
      zipMapValuesM.map(a, b, { scalars.+(_: S, _: S) }) == (a + b)
    })

  }

  test("Elementwise mult of vectors distributes over vector addition - Matrix") {
    check(Prop.forAll { (trip: (M, M, M)) =>
      val (a, b, c) = trip
      val ab = copyM(a)
      ab += b
      ab :*= c
      val ba = copyM(a) *:* c
      ba :+= (b *:* c)
      closeM(ab, ba, TOL)
    })
  }

  test("Vector element-wise mult distributes over vector addition - Matrix") {
    check(Prop.forAll { (trip: (M, M, M)) =>
      val (a, b, c) = trip
      closeM((a + b) *:* c, (b *:* c) + (a *:* c), TOL)
    })

    //    check(Prop.forAll{ (trip: (M, M, M), s: S) =>
    //      val (a, b, _) = trip
    //      s == 0 || close( (a + b)/ s, (b / s +a / s), TOL)
    //    })

    check(Prop.forAll { (trip: (M, M, M)) =>
      val (a, b, c) = trip
      val ab = copyM(a)
      ab += b
      ab :*= c
      val ba = copyM(a) *:* c
      ba += (b *:* c)
      closeM(ab, ba, TOL)
    })
  }
}

class DenseOptimizationSpaceTest_Double
    extends DenseVectorPropertyTestBase[Double]
    with OptimizationSpaceTest[DenseMatrix[Double], DenseVector[Double], Double] {
  implicit override val space: MutableOptimizationSpace[DenseMatrix[Double], DenseVector[Double], Double] =
    MutableOptimizationSpace.DenseDoubleOptimizationSpace.denseDoubleOptSpace

  val myReasonable: Arbitrary[Double] = RandomInstanceSupport.reasonableDouble(1e-4, 10)

  val N = 5
  implicit override def genTripleM: Arbitrary[(DenseMatrix[Double], DenseMatrix[Double], DenseMatrix[Double])] = {
    Arbitrary {
      for {
        x <- RandomInstanceSupport.genDenseMatrix[Double](N, N, myReasonable.arbitrary)
        y <- RandomInstanceSupport.genDenseMatrix[Double](N, N, myReasonable.arbitrary)
        z <- RandomInstanceSupport.genDenseMatrix[Double](N, N, myReasonable.arbitrary)
      } yield {
        (x, y, z)
      }
    }
  }

  def genScalar: Arbitrary[Double] = RandomInstanceSupport.genReasonableDouble
}

class SparseOptimizationSpaceTest_Double
    extends SparseVectorPropertyTestBase[Double]
    with OptimizationSpaceTest[CSCMatrix[Double], SparseVector[Double], Double] {
  implicit override val space: MutableOptimizationSpace[CSCMatrix[Double], SparseVector[Double], Double] =
    MutableOptimizationSpace.SparseDoubleOptimizationSpace.sparseDoubleOptSpace

  // TODO: generate arbitrarily dimensioned matrices
  val N = 30
  val M = 30

  override val TOLM = 1e-2

  def genScalar: Arbitrary[Double] = RandomInstanceSupport.genReasonableDouble

  val arbColIndex: Arbitrary[Int] = Arbitrary(Gen.choose[Int](0, N - 1))
  val arbRowIndex: Arbitrary[Int] = Arbitrary(Gen.choose[Int](0, M - 1))
  val genAS: Gen[Int] = Gen.chooseNum(0, pow(N, 2))
  implicit val arbEntry: Arbitrary[(Int, Int, Double)] =
    Arbitrary.arbTuple3[Int, Int, Double](arbRowIndex, arbColIndex, genScalar)
  implicit val arbVals: Arbitrary[List[(Int, Int, Double)]] = Arbitrary(
    genAS.flatMap(activeSize => Gen.listOfN[(Int, Int, Double)](activeSize, Arbitrary.arbitrary[(Int, Int, Double)]))
  )
  def addToBuilder(bldr: CSCMatrix.Builder[Double], v: (Int, Int, Double)): Unit = bldr.add(v._1, v._2, v._3)
  implicit override def genTripleM: Arbitrary[(CSCMatrix[Double], CSCMatrix[Double], CSCMatrix[Double])] = {
    Arbitrary {
      for {
        xvs <- Arbitrary.arbitrary[List[(Int, Int, Double)]]
        yvs <- Arbitrary.arbitrary[List[(Int, Int, Double)]]
        zvs <- Arbitrary.arbitrary[List[(Int, Int, Double)]]
      } yield {
        val xb = new CSCMatrix.Builder[Double](N, N)
        val yb = new CSCMatrix.Builder[Double](N, N)
        val zb = new CSCMatrix.Builder[Double](N, N)
        ({
           xvs.foreach(v => addToBuilder(xb, v))
           xb.result()
         }, {
           yvs.foreach(v => addToBuilder(yb, v))
           yb.result()
         }, {
           zvs.foreach(v => addToBuilder(zb, v))
           zb.result()
         }
        )
      }

    }
  }
}
