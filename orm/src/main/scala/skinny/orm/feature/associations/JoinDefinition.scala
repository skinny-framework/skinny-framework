package skinny.orm.feature.associations

import skinny.orm._
import scalikejdbc.SQLInterpolation._
import skinny.orm.feature.AssociationsFeature
import org.slf4j.LoggerFactory

case class JoinDefinition[Entity](
    joinType: JoinType,
    thisMapper: AssociationsFeature[Entity],
    leftMapper: AssociationsFeature[Any],
    leftAlias: Alias[Any],
    rightMapper: AssociationsFeature[Any],
    rightAlias: Alias[Any],
    on: SQLSyntax,
    fk: Option[(Any) => Option[Long]] = None,
    var enabledEvenIfAssociated: Boolean = false,
    var enabledByDefault: Boolean = false) {

  val logger = LoggerFactory.getLogger(classOf[JoinDefinition[Entity]])

  def byDefaultEvenIfAssociated() = byDefault(true)

  def byDefault(enabledEvenIfAssociated: Boolean = true): JoinDefinition[Entity] = {
    val isDefaultAlias = thisMapper.defaultAlias == this.rightAlias
    val alreadyExistsYet = thisMapper.defaultJoinDefinitions.contains(this)
    val alreadySameNameExistsYetForOtherEntity = !alreadyExistsYet &&
      thisMapper.defaultJoinDefinitions.map(_.rightAlias.tableAliasName).contains(this.rightAlias.tableAliasName)

    if (isDefaultAlias) {
      logger.debug(s"Skipped this name '${this.rightAlias}' is the default alias of this mapper. (joinDef:${this})")
    } else if (alreadyExistsYet) {
      logger.debug(s"Skipped appending to the default join definitions because this join definition already exists. (joinDef:${this})")
    } else if (alreadySameNameExistsYetForOtherEntity) {
      logger.warn(s"Skipped because same name '${this.rightAlias}' is already used by another definition. You need to use different alias. (joinDef:${this})")
    } else {
      this.enabledByDefault = true
      this.enabledEvenIfAssociated = enabledEvenIfAssociated
      thisMapper.defaultJoinDefinitions.add(this)
    }
    this
  }

}
