package breeze.plot

import breeze.compat.Scala3Compat._

import java.awt.Color
import scala.language.implicitConversions

/**
 * Constructs a PaintScale for the given type T by examining a set of its
 * values.
 *
 * @author dramage
 */
trait PaintScaleFactory[T] extends (Iterable[T] => PaintScale[T])

/**
 * Creates a GradientPaintScale from the min and max of a set of data points.
 * bound are supplied.
 *
 * @author dramage
 */
case class GradientPaintScaleFactory[T](gradient: Array[Color] = PaintScale.WhiteToBlack)(implicit
  view: ConversionOrSubtype[T, Double]
) extends PaintScaleFactory[T] {
  override def apply(items: Iterable[T]): PaintScale[T] = {
    var min = items.head
    var max = items.head
    for (item <- items) {
      if (!view(item).isNaN) {
        if (item < min) min = item
        if (item > max) max = item
      }
    }
    GradientPaintScale(min, max, gradient)
  }
}

/**
 * Creates a categorical paint scale using the Category20 palette borrowed from
 * Protovis. http://vis.stanford.edu/protovis/docs/color.html
 *
 * Beware that category colors can be reused if the number of distinct items
 * is greater than 20.
 *
 * @author dramage
 */
case class CategoricalPaintScaleFactory[T]() extends PaintScaleFactory[T] {
  override def apply(items: Iterable[T]): PaintScale[T] = {
    val distinct = items.toList.distinct
    CategoricalPaintScale[T](Map() ++ distinct.zip(LazyList.continually(PaintScale.Category20.values.toList).flatten))
  }
}

object PaintScaleFactory {

  /**
   * Ignores incoming data, instead returns the provided PaintScale when
   * queried as a PaintScaleFactory.
   */
  implicit def singletonFactoryForPaintScale[S, T](
    paintScale: S
  )(implicit view: Conversion[S, PaintScale[T]]): PaintScaleFactory[T] = (items: Iterable[T]) => view(paintScale)
}
