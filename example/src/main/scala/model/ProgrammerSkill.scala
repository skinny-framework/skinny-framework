package model

import skinny.orm.SkinnyJoinTable

case class ProgrammerSkill(programmerId: Long, skillId: Long)

object ProgrammerSkill extends SkinnyJoinTable[ProgrammerSkill] {
  override val defaultAlias = createAlias("ps")
}
