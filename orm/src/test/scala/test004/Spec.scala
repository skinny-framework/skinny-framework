package test004

import org.joda.time.DateTime
import org.scalatest._
import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback
import skinny.orm.{ SkinnyCRUDMapper, SkinnyRecord }
import skinny.orm.feature.TimestampsFeature

class Spec extends fixture.FunSpec with Matchers with Connection with CreateTables with AutoRollback {

  override def db(): DB = NamedDB("test004").toDB()

  // entities
  case class Ability(
      id: Long,
      name: String,
      abilityTypeId: Option[Long],
      createdAt: DateTime,
      updatedAt: Option[DateTime],
      abilityType: Option[AbilityType] = None
  ) extends SkinnyRecord[Ability] {

    override def skinnyCRUDMapper             = Ability
    override def excludedFieldNamesWhenSaving = Seq(Ability.createdAtFieldName, Ability.updatedAtFieldName)
  }
  case class AbilityType(
      id: Long,
      name: String
  )

  // mappers
  object Ability extends SkinnyCRUDMapper[Ability] with TimestampsFeature[Ability] {
    override val connectionPoolName                                     = "test004"
    override lazy val defaultAlias                                      = createAlias("a")
    lazy val abilityTypeRef                                             = belongsTo[AbilityType](AbilityType, (a, at) => a.copy(abilityType = at))
    override def extract(rs: WrappedResultSet, rn: ResultName[Ability]) = autoConstruct(rs, rn, "abilityType")
  }

  object AbilityType extends SkinnyCRUDMapper[AbilityType] {
    override val connectionPoolName                                         = "test004"
    override lazy val defaultAlias                                          = createAlias("at")
    override def extract(rs: WrappedResultSet, rn: ResultName[AbilityType]) = autoConstruct(rs, rn)
  }

  override def fixture(implicit session: DBSession): Unit = {}

  describe("SkinnyRecord") {
    it("should update timestamps") { implicit session =>
      val id              = Ability.createWithAttributes("name" -> "SCALA")
      val before: Ability = Ability.findById(id).get
      before.copy(name = "Scala").save()
      val after = Ability.findById(id).get
      after.createdAt should equal(before.createdAt)
      after.updatedAt should not equal (before.updatedAt)
    }

  }

}
