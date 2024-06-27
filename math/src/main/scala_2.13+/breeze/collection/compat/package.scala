package breeze.collection

import scala.collection.compat.immutable.ArraySeq
import scala.reflect.ClassTag
import scala.collection.mutable.Builder

package object compat {
  def arraySeqBuilder[K: ClassTag]: Builder[K,ArraySeq[K]] = ArraySeq.newBuilder[K]
}
