package suzaku.ui

abstract class BaseRegistry[R, RC] {
  private var registrationId        = 1
  private var registrations         = Map.empty[Class[_ <: RC], Int]
  private var pendingRegistrations  = List.empty[R]
  private var notificationCallbacks = List.empty[() => Unit]

  def register(data: RC, dataClass: Class[_ <: RC]): Int = {
    this.synchronized {
      registrations.get(dataClass) match {
        case None =>
          val id = registrationId
          registrationId += 1
          pendingRegistrations ::= buildRegistryEntry(id, data, dataClass)
          registrations += dataClass -> id
          notificationCallbacks.foreach(cb => cb())
          id
        case Some(id) =>
          id
      }
    }
  }

  def buildRegistryEntry(id: Int, data: RC, dataClass: Class[_ <: RC]): R

  def hasRegistrations = pendingRegistrations.nonEmpty

  def dequeueRegistrations: List[R] = {
    this.synchronized {
      val regs = pendingRegistrations
      pendingRegistrations = Nil
      regs
    }
  }

  def onRegistration(callback: () => Unit): Unit = {
    notificationCallbacks ::= callback
  }

}
