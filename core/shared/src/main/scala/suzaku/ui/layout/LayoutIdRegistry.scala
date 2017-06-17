package suzaku.ui.layout

object LayoutIdRegistry {
  case class LayoutIdRegistration(id: Int, className: String)

  private var layoutId             = 1
  private var pendingRegistrations = List.empty[LayoutIdRegistration]

  def register(layoutClass: Class[_ <: LayoutId]): Int = {
    this.synchronized {
      val id = layoutId
      layoutId += 1
      pendingRegistrations ::= LayoutIdRegistration(id, layoutClass.getSimpleName)
      id
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
