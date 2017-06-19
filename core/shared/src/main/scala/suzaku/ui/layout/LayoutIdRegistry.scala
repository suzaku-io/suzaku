package suzaku.ui.layout

object LayoutIdRegistry {
  case class LayoutIdRegistration(id: Int, className: String)

  private var layoutId             = 1
  private var registrations        = Map.empty[Class[_ <: LayoutId], Int]
  private var pendingRegistrations = List.empty[LayoutIdRegistration]

  def register(layoutClass: Class[_ <: LayoutId]): Int = {
    this.synchronized {
      registrations.get(layoutClass) match {
        case None =>
          val id = layoutId
          layoutId += 1
          pendingRegistrations ::= LayoutIdRegistration(id, layoutClass.getSimpleName)
          registrations += layoutClass -> id
          id
        case Some(id) =>
          id
      }
    }
  }

  def hasRegistrations = pendingRegistrations.nonEmpty

  def dequeueRegistrations: List[LayoutIdRegistration] = {
    this.synchronized {
      val regs = pendingRegistrations
      pendingRegistrations = Nil
      regs
    }
  }
}
