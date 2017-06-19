package suzaku.ui.style

object StyleClassRegistry {
  case class StyleClassRegistration(id: Int, className: String, styleProps: List[StyleProperty])

  private var styleClassId         = 1
  private var registrations        = Map.empty[Class[_ <: StyleClass], Int]
  private var pendingRegistrations = List.empty[StyleClassRegistration]

  def register(styleClass: Class[_ <: StyleClass], style: List[StyleDef]): Int = {
    val styles = style.flatMap {
      case StyleSeq(seq) =>
        seq
      case s: StyleProperty =>
        s :: Nil
    }
    this.synchronized {
      registrations.get(styleClass) match {
        case None =>
          val id = styleClassId
          styleClassId += 1
          pendingRegistrations ::= StyleClassRegistration(id, styleClass.getSimpleName, styles)
          registrations += styleClass -> id
          id
        case Some(id) =>
          id
      }
    }
  }

  def hasRegistrations = pendingRegistrations.nonEmpty

  def dequeueRegistrations: List[StyleClassRegistration] = {
    this.synchronized {
      val regs = pendingRegistrations
      pendingRegistrations = Nil
      regs
    }
  }
}
