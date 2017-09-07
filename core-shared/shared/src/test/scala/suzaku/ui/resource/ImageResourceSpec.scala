package suzaku.ui.resource

import suzaku.UnitSpec

class ImageResourceSpec extends UnitSpec {
  "Image resource" should {
    "pickle as resource reference or URI" in {
      import boopickle.DefaultBasic._

      val svg: ImageResource    = SVGImageResource("some svg", (0, 0, 20, 20))
      val base64: ImageResource = Base64ImageResource("base64data", 16, 16)
      val uri: ImageResource    = URIImageResource("some uri")

      val img1 = Unpickle[ImageResource].fromBytes(Pickle.intoBytes(svg))
      val img2 = Unpickle[ImageResource].fromBytes(Pickle.intoBytes(base64))
      val img3 = Unpickle[ImageResource].fromBytes(Pickle.intoBytes(uri))

      img1 shouldBe an[ReferenceEmbeddedImageResource]
      img2 shouldBe an[ReferenceEmbeddedImageResource]
      img3 shouldBe an[URIImageResource]
    }
  }
}
