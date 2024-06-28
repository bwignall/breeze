package breeze.collection.mutable

import scala.collection.mutable._

/**
 * AutoUpdater wraps a Map such that any call to apply updates the map with an instance of the default value
 * @author dlwh
 */
class AutoUpdater[M, K, V](val theMap: M, default: => V)(implicit ev: M <:< Map[K, V]) extends Map[K, V] {
  override def apply(k: K): V = theMap.getOrElseUpdate(k, default)
  override def update(k: K, v: V): Unit = theMap.update(k, v)

  override def addOne(kv: (K, V)): this.type = { theMap += kv; this }

  def get(key: K): Option[V] = theMap.get(key)

  def iterator: Iterator[(K, V)] = theMap.iterator

  override def subtractOne(key: K): this.type = { theMap -= key; this }

  override def size: Int = theMap.size
}

object AutoUpdater {
  def apply[M, K, V](map: M, default: => V)(implicit ev: M <:< Map[K, V]): AutoUpdater[M, K, V] =
    new AutoUpdater[M, K, V](map, default)
  def apply[K, V](default: => V): AutoUpdater[Map[K, V], K, V] = apply(Map[K, V](), default)
  def ofKeys[K]: ofKeys[K] = new ofKeys[K]()
  class ofKeys[K]() extends {
    def andValues[V](v: => V): AutoUpdater[Map[K, V], K, V] = apply[K, V](v)
  }
}
