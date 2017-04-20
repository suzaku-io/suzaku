package suzaku.ui

trait Blueprint {
  protected var _key: Option[Any] = None

  final def key: Option[Any] = _key

  final def withKey(key: Any): this.type = {
    _key = Some(key)
    this
  }
}

object EmptyBlueprint extends Blueprint

case class BlueprintSeq(blueprints: Seq[Blueprint]) extends Blueprint
