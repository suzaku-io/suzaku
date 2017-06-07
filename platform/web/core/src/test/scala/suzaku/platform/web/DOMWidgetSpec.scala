package suzaku.platform.web

import suzaku.UnitSpec

class DOMWidgetSpec extends UnitSpec {

  "DOMWidget companion" should {
    import suzaku.ui.style._
    def test(prop: StyleBaseProperty)(propName: String, propValue: String) = {
      DOMWidget.extractStyle(prop) should be((propName, propValue))
    }

    "create correct CSS from basic styles" in {
      test(Color(0))("color", "rgb(0,0,0)")
      test(Color(RGBA(0x808080, 0.5)))("color", "rgba(128,128,128,0.5)")
    }

    "create correct font styles" in {
      test(FontFamily("Times Roman", "Arial", "serif"))("font-family", """"Times Roman","Arial","serif"""")
      test(FontSize(10.px))("font-size", "10px")
      test(FontSize(95.%%))("font-size", "95%")
      test(FontSize(xxsmall))("font-size", "xx-small")
      test(FontSize(xsmall))("font-size", "x-small")
      test(FontSize(small))("font-size", "small")
      test(FontSize(smaller))("font-size", "smaller")
      test(FontSize(xxlarge))("font-size", "xx-large")
      test(FontSize(xlarge))("font-size", "x-large")
      test(FontSize(large))("font-size", "large")
      test(FontSize(larger))("font-size", "larger")
      test(FontSize(medium))("font-size", "medium")
      test(FontWeight(90))("font-weight", "100")
      test(FontWeight(150))("font-weight", "200")
      test(FontWeight(400))("font-weight", "400")
      test(FontWeight(990))("font-weight", "900")
      test(FontItalics)("font-style", "italics")
    }

    "create correct dimension styles" in {
      test(Height(100.px))("height", "100px")
      test(Height(10.5.em))("height", "10.5em")
      test(Height(100.rem))("height", "100rem")
      test(Height(100.vw))("height", "100vw")
      test(Height(100.vh))("height", "100vh")
      test(Width(auto))("width", "auto")
      test(MaxWidth(auto))("max-width", "auto")
      test(MaxWidth(800.px))("max-width", "800px")
      test(MinWidth(auto))("min-width", "auto")
      test(MinWidth(800.px))("min-width", "800px")
      test(MaxHeight(auto))("max-height", "auto")
      test(MaxHeight(800.px))("max-height", "800px")
      test(MinHeight(auto))("min-height", "auto")
      test(MinHeight(800.px))("min-height", "800px")
      "Height(100)" shouldNot compile
    }

    "create correct layout styles" in {
      test(MarginTop(10.px))("margin-top", "10px")
      test(MarginLeft(auto))("margin-left", "auto")
      test(PaddingBottom(10.px))("padding-bottom", "10px")
      test(PaddingRight(auto))("padding-right", "auto")

      test(BorderWidthRight(1.px))("border-right-width", "1px")
      test(BorderWidthTop(0.em))("border-top-width", "0em")
      test(BorderStyleLeft(dotted))("border-left-style", "dotted")
      test(BorderStyleTop(none))("border-top-style", "none")
      test(BorderColorTop(0xFF00FF))("border-top-color", "rgb(255,0,255)")
    }
  }
}
