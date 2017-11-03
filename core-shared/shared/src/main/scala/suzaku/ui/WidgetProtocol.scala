package suzaku.ui

import arteria.core.{MessageWitness, Protocol}

trait WidgetProtocol extends Protocol {
  def widgetName: String = getClass.getName

  def widgetExtWitness: MessageWitness[WidgetExtProtocol.WidgetMessage, this.type]
}
