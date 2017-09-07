package suzaku.util

import java.util

sealed abstract class DenseIntMap[A <: AnyRef] {
  def apply(key: Int): A

  def get(key: Int): Option[A]

  def getOrElse(key: Int, other: => A): A = get(key).getOrElse(other)

  def updated(key: Int, value: A): DenseIntMap[A]

  @inline def updated(kv: (Int, A)): DenseIntMap[A] = updated(kv._1, kv._2)

  def updated(key: Int, value: A, join: (A, A) => A): DenseIntMap[A]

  @inline def updated(kv: (Int, A), join: (A, A) => A): DenseIntMap[A] = updated(kv._1, kv._2, join)

  def removed(key: Int): DenseIntMap[A]

  def contains(key: Int): Boolean

  def size: Int

  def join(other: DenseIntMap[A], f: (A, A) => A = (a: A, b: A) => b): DenseIntMap[A]

  def ++(other: DenseIntMap[A]): DenseIntMap[A]

  def duplicate: DenseIntMap[A]

  def isEmpty: Boolean = size == 0

  def nonEmpty: Boolean = !isEmpty

  def values: Seq[A]
}

object DenseIntMap {
  def empty[A <: AnyRef]: DenseIntMap[A] = EmptyMap.asInstanceOf[DenseIntMap[A]]

  private object EmptyMap extends DenseIntMap[AnyRef] {
    override def apply(key: Int): AnyRef = throw new NoSuchElementException

    override def get(key: Int): Option[AnyRef] = None

    override def getOrElse(key: Int, other: => AnyRef) = other

    override def updated(key: Int, value: AnyRef): DenseIntMap[AnyRef] = {
      if (key < 0) throw new IndexOutOfBoundsException("Key cannot be negative")
      Map1(key, value)
    }

    override def updated(key: Int, value: AnyRef, join: (AnyRef, AnyRef) => AnyRef): DenseIntMap[AnyRef] = {
      if (key < 0) throw new IndexOutOfBoundsException("Key cannot be negative")
      Map1(key, value)
    }

    override def removed(key: Int): DenseIntMap[AnyRef] = this

    override def contains(key: Int): Boolean = false

    override def size: Int = 0

    override def join(other: DenseIntMap[AnyRef], f: (AnyRef, AnyRef) => AnyRef): DenseIntMap[AnyRef] = other.duplicate

    override def ++(other: DenseIntMap[AnyRef]): DenseIntMap[AnyRef] = other.duplicate

    override def duplicate: DenseIntMap[AnyRef] = this

    override def values: Seq[AnyRef] = Nil

    override def toString = "DenseIntMap()"
  }

  case class Map1[A <: AnyRef](key1: Int, value1: A) extends DenseIntMap[A] {
    override def apply(key: Int): A =
      if (key == key1) value1 else throw new NoSuchElementException

    override def get(key: Int): Option[A] =
      if (key == key1) Some(value1) else None

    override def getOrElse(key: Int, other: => A) =
      if (key == key1) value1 else other

    override def updated(key: Int, value: A): DenseIntMap[A] = {
      if (key < 0) throw new IndexOutOfBoundsException("Key cannot be negative")
      if (key == key1) Map1(key, value) else Map2(key1, value1, key, value)
    }

    override def updated(key: Int, value: A, join: (A, A) => A): DenseIntMap[A] = {
      if (key < 0) throw new IndexOutOfBoundsException("Key cannot be negative")
      if (key == key1) Map1(key, join(value1, value)) else Map2(key1, value1, key, value)
    }

    override def removed(key: Int): DenseIntMap[A] =
      if (key == key1) EmptyMap.asInstanceOf[DenseIntMap[A]] else this

    override def contains(key: Int): Boolean =
      key == key1

    override def size: Int = 1

    override def join(other: DenseIntMap[A], f: (A, A) => A): DenseIntMap[A] = other match {
      case x if x.isInstanceOf[EmptyMap.type] => this
      case Map1(k1, v1)                       => updated(k1, v1, f)
      case Map2(k1, v1, k2, v2)               => updated(k1, v1, f).updated(k2, v2, f)
      case Map3(k1, v1, k2, v2, k3, v3)       => updated(k1, v1, f).updated(k2, v2, f).updated(k3, v3, f)
      case m: Map4Plus[A] =>
        val newMap = m.duplicate
        if (!newMap.contains(key1))
          newMap.updated(key1, value1, f)
        newMap
    }

    override def ++(other: DenseIntMap[A]): DenseIntMap[A] = join(other)

    override def duplicate: DenseIntMap[A] = this

    override def values: Seq[A] = value1 :: Nil

    override def toString = s"DenseIntMap($key1 -> $value1)"
  }

  case class Map2[A <: AnyRef](key1: Int, value1: A, key2: Int, value2: A) extends DenseIntMap[A] {
    override def apply(key: Int): A =
      if (key == key1) value1
      else if (key == key2) value2
      else throw new NoSuchElementException

    override def get(key: Int): Option[A] =
      if (key == key1) Some(value1)
      else if (key == key2) Some(value2)
      else None

    override def getOrElse(key: Int, other: => A) =
      if (key == key1) value1
      else if (key == key2) value2
      else other

    override def updated(key: Int, value: A): DenseIntMap[A] = {
      if (key < 0) throw new IndexOutOfBoundsException("Key cannot be negative")
      if (key == key1) Map2(key1, value, key2, value2)
      else if (key == key2) Map2(key1, value1, key2, value)
      else Map3(key1, value1, key2, value2, key, value)
    }

    override def updated(key: Int, value: A, join: (A, A) => A): DenseIntMap[A] = {
      if (key < 0) throw new IndexOutOfBoundsException("Key cannot be negative")
      if (key == key1) Map2(key1, join(value1, value), key2, value2)
      else if (key == key2) Map2(key1, value1, key2, join(value2, value))
      else Map3(key1, value1, key2, value2, key, value)
    }

    override def removed(key: Int): DenseIntMap[A] =
      if (key == key1) Map1(key2, value2)
      else if (key == key2) Map1(key1, value1)
      else this

    override def contains(key: Int): Boolean =
      (key == key1) || (key == key2)

    override def size: Int = 2

    override def join(other: DenseIntMap[A], f: (A, A) => A): DenseIntMap[A] = other match {
      case x if x.isInstanceOf[EmptyMap.type] => this
      case Map1(k1, v1)                       => updated(k1, v1, f)
      case Map2(k1, v1, k2, v2)               => updated(k1, v1, f).updated(k2, v2, f)
      case Map3(k1, v1, k2, v2, k3, v3)       => updated(k1, v1, f).updated(k2, v2, f).updated(k3, v3, f)
      case m: Map4Plus[A] =>
        val newMap = m.duplicate
        if (!newMap.contains(key1))
          newMap.updated(key1, value1, f)
        if (!newMap.contains(key2))
          newMap.updated(key2, value2, f)
        newMap
    }

    override def ++(other: DenseIntMap[A]): DenseIntMap[A] = join(other)

    override def duplicate: DenseIntMap[A] = this

    override def values: Seq[A] = value1 :: value2 :: Nil

    override def toString = s"DenseIntMap($key1 -> $value1,$key2 -> $value2)"
  }

  case class Map3[A <: AnyRef](key1: Int, value1: A, key2: Int, value2: A, key3: Int, value3: A) extends DenseIntMap[A] {
    override def apply(key: Int): A =
      if (key == key1) value1
      else if (key == key2) value2
      else if (key == key3) value3
      else throw new NoSuchElementException

    override def get(key: Int): Option[A] =
      if (key == key1) Some(value1)
      else if (key == key2) Some(value2)
      else if (key == key3) Some(value3)
      else None

    override def getOrElse(key: Int, other: => A) =
      if (key == key1) value1
      else if (key == key2) value2
      else if (key == key3) value3
      else other

    override def updated(key: Int, value: A): DenseIntMap[A] = {
      if (key < 0) throw new IndexOutOfBoundsException("Key cannot be negative")
      if (key == key1) Map3(key1, value, key2, value2, key3, value3)
      else if (key == key2) Map3(key1, value1, key2, value, key3, value3)
      else if (key == key3) Map3(key1, value1, key2, value, key3, value)
      else Map4Plus(key1, value1, key2, value2, key3, value3, key, value)
    }

    override def updated(key: Int, value: A, join: (A, A) => A): DenseIntMap[A] = {
      if (key < 0) throw new IndexOutOfBoundsException("Key cannot be negative")
      if (key == key1) Map3(key1, join(value1, value), key2, value2, key3, value3)
      else if (key == key2) Map3(key1, value1, key2, join(value2, value), key3, value3)
      else if (key == key3) Map3(key1, value1, key2, value2, key3, join(value3, value))
      else Map4Plus(key1, value1, key2, value2, key3, value3, key, value)
    }

    override def removed(key: Int): DenseIntMap[A] =
      if (key == key1) Map2(key2, value2, key3, value3)
      else if (key == key2) Map2(key1, value1, key3, value3)
      else if (key == key3) Map2(key1, value1, key2, value2)
      else this

    override def contains(key: Int): Boolean =
      (key == key1) || (key == key2) || (key == key3)

    override def size: Int = 3

    override def join(other: DenseIntMap[A], f: (A, A) => A): DenseIntMap[A] = other match {
      case x if x.isInstanceOf[EmptyMap.type] => this
      case Map1(k1, v1)                       => updated(k1, v1, f)
      case Map2(k1, v1, k2, v2)               => updated(k1, v1, f).updated(k2, v2, f)
      case Map3(k1, v1, k2, v2, k3, v3)       => updated(k1, v1, f).updated(k2, v2, f).updated(k3, v3, f)
      case m: Map4Plus[A] =>
        val newMap = m.duplicate
        if (!newMap.contains(key1))
          newMap.updated(key1, value1, f)
        if (!newMap.contains(key2))
          newMap.updated(key2, value2, f)
        if (!newMap.contains(key3))
          newMap.updated(key3, value3, f)
        newMap
    }

    override def ++(other: DenseIntMap[A]): DenseIntMap[A] = join(other)

    override def duplicate: DenseIntMap[A] = this

    override def values: Seq[A] = value1 :: value2 :: value3 :: Nil

    override def toString = s"DenseIntMap($key1 -> $value1,$key2 -> $value2,$key3 -> $value3)"
  }

  class Map4Plus[A <: AnyRef](var offset: Int, var maxIdx: Int, var valueArray: Array[A], var _size: Int)
      extends DenseIntMap[A] {
    private def ensureCapacity(key: Int): Unit = {
      if (key < offset) {
        val newOffset = key
        val newValues = Array.ofDim[AnyRef](valueArray.length + offset - newOffset)
        System.arraycopy(valueArray, 0, newValues, offset - newOffset, valueArray.length)
        offset = newOffset
        valueArray = newValues.asInstanceOf[Array[A]]
      } else if (key > maxIdx) {
        val newSize   = (key - offset) * 3 / 2
        val newValues = Array.ofDim[AnyRef](newSize)
        System.arraycopy(valueArray, 0, newValues, 0, valueArray.length)
        maxIdx = newSize + offset - 1
        valueArray = newValues.asInstanceOf[Array[A]]
      }
    }

    override def apply(key: Int): A = {
      if (key < offset || key > maxIdx) throw new NoSuchElementException
      val a = valueArray(key - offset)
      if (a == null) throw new NoSuchElementException
      a
    }

    override def get(key: Int): Option[A] =
      if (key < offset || key > maxIdx) None else Option(valueArray(key - offset))

    override def getOrElse(key: Int, other: => A) =
      if (key < offset || key > maxIdx || valueArray(key - offset) == null) other else valueArray(key - offset)

    override def updated(key: Int, value: A): DenseIntMap[A] = {
      if (key < 0) throw new IndexOutOfBoundsException("Key cannot be negative")
      ensureCapacity(key)
      if (valueArray(key - offset) == null) _size += 1
      valueArray(key - offset) = value.asInstanceOf[A]
      this
    }

    override def updated(key: Int, value: A, join: (A, A) => A): DenseIntMap[A] = {
      if (key < 0) throw new IndexOutOfBoundsException("Key cannot be negative")
      ensureCapacity(key)
      if (valueArray(key - offset) == null) {
        _size += 1
        valueArray(key - offset) = value
      } else {
        valueArray(key - offset) = join(valueArray(key - offset), value)
      }
      this
    }

    override def removed(key: Int): DenseIntMap[A] = {
      if (key >= offset && key <= maxIdx) {
        if (valueArray(key - offset) != null) _size -= 1
        valueArray(key - offset) = null.asInstanceOf[A]
      }
      this
    }

    override def contains(key: Int): Boolean =
      if (key < offset || key > maxIdx) false else valueArray(key - offset) == null

    override def size: Int = _size

    override def join(other: DenseIntMap[A], f: (A, A) => A): DenseIntMap[A] = other match {
      case x if x.isInstanceOf[EmptyMap.type] => this
      case Map1(k1, v1)                       => updated(k1, v1, f)
      case Map2(k1, v1, k2, v2)               => updated(k1, v1, f).updated(k2, v2, f)
      case Map3(k1, v1, k2, v2, k3, v3)       => updated(k1, v1, f).updated(k2, v2, f).updated(k3, v3, f)
      case m: Map4Plus[A] =>
        val newOffset = offset min m.offset
        val newMaxIdx = maxIdx max m.maxIdx
        valueArray = if (newOffset < offset || newMaxIdx > maxIdx) {
          // reallocate space
          val newArray = Array.ofDim[AnyRef](newMaxIdx - newOffset + 1).asInstanceOf[Array[A]]
          System.arraycopy(valueArray, 0, newArray, offset - newOffset, valueArray.length)
          newArray
        } else valueArray
        // copy values from the other map
        for (i <- m.valueArray.indices) {
          val a = m.valueArray(i)
          if (a != null) {
            if (valueArray(i + m.offset - newOffset) == null) {
              _size += 1
              valueArray(i + m.offset - newOffset) = a
            } else {
              valueArray(i + m.offset - newOffset) = f(valueArray(i + m.offset - newOffset), a)
            }
          }
        }
        offset = newOffset
        maxIdx = newMaxIdx
        this
    }

    override def ++(other: DenseIntMap[A]): DenseIntMap[A] = duplicate.join(other)

    override def duplicate: DenseIntMap[A] =
      new Map4Plus[A](offset, maxIdx, util.Arrays.copyOf[A](valueArray, valueArray.length), _size)

    override def values: Seq[A] = valueArray.filter(_ != null)

    override def toString =
      s"DenseIntMap(${valueArray.indices.collect { case i if valueArray(i) != null => s"$i -> ${valueArray(i)}" }.mkString(",")})"
  }

  object Map4Plus {
    private[DenseIntMap] def apply[A <: AnyRef](key1: Int,
                                                value1: A,
                                                key2: Int,
                                                value2: A,
                                                key3: Int,
                                                value3: A,
                                                key4: Int,
                                                value4: A): Map4Plus[A] = {
      val offset = key1 min key2 min key3 min key4
      val maxIdx = key1 max key2 max key3 max key4
      val values = Array.ofDim[AnyRef](maxIdx - offset + 1).asInstanceOf[Array[A]]
      values(key1 - offset) = value1
      values(key2 - offset) = value2
      values(key3 - offset) = value3
      values(key4 - offset) = value4
      new Map4Plus[A](offset, maxIdx, values, 4)
    }
  }

  def apply[A <: AnyRef](kv1: (Int, A)): DenseIntMap[A] =
    Map1(kv1._1, kv1._2)
  def apply[A <: AnyRef](kv1: (Int, A), kv2: (Int, A)): DenseIntMap[A] =
    Map1(kv1._1, kv1._2).updated(kv2._1, kv2._2)
  def apply[A <: AnyRef](kv1: (Int, A), kv2: (Int, A), kv3: (Int, A)): DenseIntMap[A] =
    Map1(kv1._1, kv1._2).updated(kv2._1, kv2._2).updated(kv3._1, kv3._2)
  def apply[A <: AnyRef](kv1: (Int, A), kv2: (Int, A), kv3: (Int, A), kv4: (Int, A), kvRest: (Int, A)*): DenseIntMap[A] = {
    val map = Map1(kv1._1, kv1._2).updated(kv2._1, kv2._2).updated(kv3._1, kv3._2).updated(kv4._1, kv4._2)
    kvRest.foreach { case (k, v) => map.updated(k, v) }
    map
  }
}
