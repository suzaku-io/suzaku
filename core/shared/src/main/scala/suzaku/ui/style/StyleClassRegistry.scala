package suzaku.ui.style

import suzaku.ui.BaseRegistry

case class StyleClassRegistration(id: Int, className: String, styleProps: List[StyleProperty])

object StyleClassRegistry extends BaseRegistry[StyleClassRegistration, StyleClass] {
  def buildRegistryEntry(id: Int, style: StyleClass, styleClass: Class[_ <: StyleClass]): StyleClassRegistration = {
    val styles = style.styleDefs.flatMap {
      case StyleSeq(seq) =>
        seq
      case s: StyleProperty =>
        s :: Nil
    }
    StyleClassRegistration(id, styleClass.getSimpleName, styles)
  }
}
