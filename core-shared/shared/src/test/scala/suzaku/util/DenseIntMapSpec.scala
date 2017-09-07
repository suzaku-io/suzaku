package suzaku.util

import suzaku.UnitSpec

case class Test(i: Int)
class DenseIntMapSpec extends UnitSpec {
  "An empty DenseIntMap" should {
    "work correctly" in {
      val m = DenseIntMap.empty[Test]
      m should have size 0

      m.size shouldBe 0
      m.get(0) shouldBe None
      m.removed(0) should ===(m)
      an[NoSuchElementException] should be thrownBy m.apply(0)

      m.updated(0, Test(42)) shouldBe a[DenseIntMap.Map1[_]]
    }

    "join with another" in {
      val m = DenseIntMap.empty[Test]

      m join DenseIntMap.empty[Test] should ===(m)
      m join DenseIntMap(0 -> Test(42)) shouldBe DenseIntMap.Map1(0, Test(42))
      m join DenseIntMap(0 -> Test(42), 45 -> Test(45)) shouldBe DenseIntMap.Map2(0, Test(42), 45, Test(45))
      m join DenseIntMap(0 -> Test(42), 45 -> Test(45), 3 -> Test(3)) shouldBe DenseIntMap.Map3(0, Test(42), 45, Test(45), 3, Test(3))
      val n = m join DenseIntMap(0 -> Test(42), 45 -> Test(45), 3 -> Test(3), 8 -> Test(8))
      n shouldBe a [DenseIntMap.Map4Plus[_]]
    }
  }

  "DenseIntMap with one value" should {
    "work correctly" in {
      val m = DenseIntMap.empty[Test].updated(42, Test(42))

      m.size shouldBe 1
      m.get(0) shouldBe None
      m.get(-1) shouldBe None
      m.get(1000) shouldBe None
      m.get(42) shouldBe Some(Test(42))
      m.removed(0) should ===(m)
      m.removed(42) should ===(DenseIntMap.empty[Test])

      an[NoSuchElementException] should be thrownBy m.apply(0)
      m.apply(42) shouldBe Test(42)

      m.updated(42, Test(43)) shouldBe a[DenseIntMap.Map1[_]]
      m.updated(43, Test(43)) shouldBe a[DenseIntMap.Map2[_]]
    }

    "join with another" in {
      val m = DenseIntMap(99 -> Test(99))

      m join DenseIntMap.empty[Test] shouldBe DenseIntMap.Map1(99, Test(99))
      m join DenseIntMap(0 -> Test(42)) shouldBe DenseIntMap.Map2(99, Test(99), 0, Test(42))
      m join DenseIntMap(0 -> Test(42), 45 -> Test(45)) shouldBe DenseIntMap.Map3(99, Test(99), 0, Test(42), 45, Test(45))
      val n = m join DenseIntMap(0 -> Test(42), 45 -> Test(45), 3 -> Test(3), 8 -> Test(8))
      n shouldBe a [DenseIntMap.Map4Plus[_]]
      n should have size 5
    }
  }

  "DenseIntMap with two values" should {
    "work correctly" in {
      val m = DenseIntMap.empty[Test].updated(42, Test(42)).updated(4, Test(4))

      m.size shouldBe 2
      m.get(0) shouldBe None
      m.get(-1) shouldBe None
      m.get(1000) shouldBe None
      m.get(42) shouldBe Some(Test(42))
      m.get(4) shouldBe Some(Test(4))
      m.removed(0) should ===(m)
      m.removed(42) shouldBe a[DenseIntMap.Map1[_]]
      m.removed(4) shouldBe a[DenseIntMap.Map1[_]]
      m.removed(42).removed(4) should ===(DenseIntMap.empty[Test])

      an[NoSuchElementException] should be thrownBy m.apply(0)
      m.apply(42) shouldBe Test(42)
      m.apply(4) shouldBe Test(4)

      m.updated(42, Test(4)) shouldBe a[DenseIntMap.Map2[_]]
      m.updated(4, Test(4)) shouldBe a[DenseIntMap.Map2[_]]
      m.updated(43, Test(43)) shouldBe a[DenseIntMap.Map3[_]]
    }
  }

  "DenseIntMap with three values" should {
    "work correctly" in {
      val m = DenseIntMap.empty[Test].updated(42, Test(42)).updated(4, Test(4)).updated(400, Test(400))

      m.size shouldBe 3
      m.get(0) shouldBe None
      m.get(-1) shouldBe None
      m.get(1000) shouldBe None
      m.get(42) shouldBe Some(Test(42))
      m.get(4) shouldBe Some(Test(4))
      m.get(400) shouldBe Some(Test(400))
      m.removed(0) should ===(m)
      m.removed(42) shouldBe a[DenseIntMap.Map2[_]]
      m.removed(4) shouldBe a[DenseIntMap.Map2[_]]
      m.removed(42).removed(4) shouldBe a[DenseIntMap.Map1[_]]
      m.removed(42).removed(4).removed(400) should ===(DenseIntMap.empty[Test])

      an[NoSuchElementException] should be thrownBy m.apply(0)
      m.apply(42) shouldBe Test(42)
      m.apply(4) shouldBe Test(4)
      m.apply(400) shouldBe Test(400)

      m.updated(42, Test(43)) shouldBe a[DenseIntMap.Map3[_]]
      m.updated(4, Test(3)) shouldBe a[DenseIntMap.Map3[_]]
      m.updated(400, Test(43)) shouldBe a[DenseIntMap.Map3[_]]
      m.updated(43, Test(43)) shouldBe a[DenseIntMap.Map4Plus[_]]
    }
  }

  "DenseIntMap with more than three values" should {
    "work correctly" in {
      val m = DenseIntMap.empty[Test].updated(42, Test(42)).updated(4, Test(4)).updated(40, Test(40)).updated(49, Test(49))

      m shouldBe a[DenseIntMap.Map4Plus[_]]

      m.size shouldBe 4
      m.get(0) shouldBe None
      m.get(-1) shouldBe None
      m.get(1000) shouldBe None
      m.get(42) shouldBe Some(Test(42))
      m.get(4) shouldBe Some(Test(4))
      m.get(40) shouldBe Some(Test(40))
      m.get(49) shouldBe Some(Test(49))

      an[NoSuchElementException] should be thrownBy m.apply(0)
      an[NoSuchElementException] should be thrownBy m.apply(1)
      an[NoSuchElementException] should be thrownBy m.apply(-1)
      m.apply(42) shouldBe Test(42)
      m.apply(4) shouldBe Test(4)
      m.apply(40) shouldBe Test(40)
      m.apply(49) shouldBe Test(49)

      val m1 = m.updated(43, Test(43))
      m1 should ===(m)

      m.removed(0) should ===(m)
      m.size shouldBe 5
      m.removed(42) should ===(m)
      m.size shouldBe 4
      m.removed(4) should ===(m)
      m.size shouldBe 3
    }

    "join with another" in {
      val m1 = DenseIntMap(2 -> Test(42), 45 -> Test(45), 3 -> Test(3), 8 -> Test(8))
      val m2 = DenseIntMap(0 -> Test(420), 4 -> Test(4), 3 -> Test(30), 82 -> Test(82))
      val m3 = DenseIntMap(88 -> Test(88))

      val t1 = m1 ++ m2
      val t2 = m2 ++ m1
      t1(0) shouldBe Test(420)
      t2(0) shouldBe Test(420)
      t1(3) shouldBe Test(30)
      t2(3) shouldBe Test(3)
      t1(82) shouldBe Test(82)
      t2(82) shouldBe Test(82)

      val t3 = m1 ++ m3
      t3(2) shouldBe Test(42)
      t3(8) shouldBe Test(8)
      t3(88) shouldBe Test(88)

      m1 join m2
      m2 should have size 4
      m1 should have size 7
      m1 join m3
      m1 should have size 8
      m1(88) shouldBe Test(88)
      m1(0) shouldBe Test(420)
    }
  }
}
