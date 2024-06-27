package breeze.collection

import scala.collection.compat.immutable.ArraySeq
import scala.collection.mutable.Builder
import scala.reflect.ClassTag

package object compat {
  def arraySeqBuilder[K: ClassTag]: Builder[K, ArraySeq[K]] = ArraySeq.newBuilder[K]
}
