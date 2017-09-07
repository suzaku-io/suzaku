package suzaku.ui

import arteria.core.Protocol

trait WidgetProtocol extends Protocol {
  def widgetName: String = getClass.getName
}
