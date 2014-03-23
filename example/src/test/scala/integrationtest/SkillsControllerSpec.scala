package integrationtest

import org.scalatra.test.scalatest._
import skinny.test._
import model._
import controller.SkillsController

class SkillsControllerSpec extends ScalatraFlatSpec with unit.SkinnyTesting {

  addFilter(SkillsController, "/*")

  def skill = Skill.findAllWithLimitOffset(1, 0).headOption.getOrElse {
    FactoryGirl(Skill).create()
  }

  it should "have correct url" in {
    SkillsController.urlSample should equal("/skills?page=1")
  }

  it should "show skills" in {
    get("/skills") {
      status should equal(200)
    }
    get("/skills/") {
      status should equal(200)
    }
    get("/skills.json") {
      logger.debug(body)
      status should equal(200)
    }
    get("/skills.xml") {
      logger.debug(body)
      status should equal(200)
    }
  }

  it should "show a skill in detail" in {
    get(s"/skills/${skill.id}") {
      status should equal(200)
    }
    get(s"/skills/${skill.id}.xml") {
      logger.debug(body)
      status should equal(200)
    }
    get(s"/skills/${skill.id}.json") {
      logger.debug(body)
      status should equal(200)
    }
  }

  it should "show new entry form" in {
    get(s"/skills/new") {
      status should equal(200)
    }
  }

  it should "create a skill" in {
    val newName = s"Created at ${System.currentTimeMillis}"
    post(s"/skills", "name" -> newName) {
      status should equal(403)
    }

    withSkinnySession("csrf-token" -> "12345") {
      post(s"/skills", "name" -> newName, "csrf-token" -> "12345") {
        status should equal(302)
        val id = header("Location").split("/").last.toLong
        Skill.findById(id).isDefined should equal(true)
      }
    }
  }

  it should "show the edit form" in {
    get(s"/skills/${skill.id}/edit") {
      status should equal(200)
    }
  }

  it should "update a skill" in {
    val newName = s"Updated at ${System.currentTimeMillis}"
    put(s"/skills/${skill.id}", "name" -> newName) {
      status should equal(403)
    }
    Skill.findById(skill.id).get.name should not equal (newName)

    withSkinnySession("csrf-token" -> "12345") {
      put(s"/skills/${skill.id}", "name" -> newName, "csrf-token" -> "12345") {
        status should equal(302)
      }
      put(s"/skills/${skill.id}", "csrf-token" -> "12345") {
        status should equal(400)
      }
    }
    Skill.findById(skill.id).get.name should equal(newName)
  }

  it should "delete a skill" in {
    val id = FactoryGirl(Skill).create().id

    delete(s"/skills/${id}") {
      status should equal(403)
    }
    withSkinnySession("csrf-token" -> "aaaaaa") {
      delete(s"/skills/${id}?csrf-token=aaaaaa") {
        status should equal(200)
      }
    }
  }

}
