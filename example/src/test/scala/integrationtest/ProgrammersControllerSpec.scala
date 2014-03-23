package integrationtest

import org.scalatra.test.scalatest._
import skinny.test._
import model._
import controller.Controllers

class ProgrammersControllerSpec extends ScalatraFlatSpec with unit.SkinnyTesting {

  addFilter(Controllers.programmers, "/*")

  def skill = Skill.findAllWithLimitOffset(1, 0).headOption.getOrElse {
    FactoryGirl(Skill).create()
  }
  def company = Company.findAllWithLimitOffset(1, 0).headOption.getOrElse {
    FactoryGirl(Company).create()
  }
  def programmer = Programmer.findAllWithLimitOffset(1, 0).headOption.getOrElse {
    FactoryGirl(Programmer).create()
  }

  it should "show programmers" in {
    get("/programmers") {
      status should equal(200)
    }
    get("/programmers/") {
      status should equal(200)
    }
    get("/programmers.json") {
      logger.debug(body)
      status should equal(200)
    }
    get("/programmers.xml") {
      logger.debug(body)
      status should equal(200)
    }
  }

  it should "show a programmer in detail" in {
    get(s"/programmers/${programmer.id}") {
      status should equal(200)
    }
    get(s"/programmers/${programmer.id}.xml") {
      logger.debug(body)
      status should equal(200)
    }
    get(s"/programmers/${programmer.id}.json") {
      logger.debug(body)
      status should equal(200)
    }
  }

  it should "show new entry form" in {
    get(s"/programmers/new") {
      status should equal(200)
    }
  }

  it should "create a programmer" in {
    val newName = s"Created at ${System.currentTimeMillis}"
    post(s"/programmers", "name" -> newName) {
      status should equal(403)
    }

    withSession("csrf-token" -> "12345") {
      post(s"/programmers", "name" -> newName, "favoriteNumber" -> "123", "companyId" -> company.id.toString, "csrf-token" -> "12345") {
        status should equal(302)
        val id = header("Location").split("/").last.toLong
        Programmer.findById(id).isDefined should equal(true)
      }
    }
  }

  it should "show the edit form" in {
    get(s"/programmers/${programmer.id}/edit") {
      status should equal(200)
    }
  }

  it should "update a programmer" in {
    val newName = s"Updated at ${System.currentTimeMillis}"
    put(s"/programmers/${programmer.id}", "name" -> newName) {
      status should equal(403)
    }
    Programmer.findById(programmer.id).get.name should not equal (newName)

    withSession("csrf-token" -> "12345") {
      put(s"/programmers/${programmer.id}", "name" -> newName, "favoriteNumber" -> "123", "companyId" -> company.id.toString, "csrf-token" -> "12345") {
        status should equal(302)
      }
      put(s"/programmers/${programmer.id}", "csrf-token" -> "12345") {
        status should equal(400)
      }
    }
    Programmer.findById(programmer.id).get.name should equal(newName)
  }

  it should "delete a programmer" in {
    val id = Programmer.createWithAttributes('name -> "Unit Test Programmer", 'favoriteNumber -> 123)
    delete(s"/programmers/${id}") {
      status should equal(403)
    }
    withSession("csrf-token" -> "aaaaaa") {
      delete(s"/programmers/${id}?csrf-token=aaaaaa") {
        status should equal(200)
      }
    }
  }

  it should "add a programmer to a company" in {
    val id = Programmer.createWithAttributes('name -> "JoinCompany Test Programmer", 'favoriteNumber -> 123)
    try {
      withSession("csrf-token" -> "aaaaaa") {
        post(s"/programmers/${id}/company/${company.id}", "csrf-token" -> "aaaaaa") {
          status should equal(200)
        }
      }
    } finally {
      Programmer.deleteById(id)
    }
  }

  it should "remove a programmer from a company" in {
    val id = Programmer.createWithAttributes('name -> "LeaveCompany Test Programmer", 'favoriteNumber -> 123)
    try {
      withSession("csrf-token" -> "aaaaaa") {
        post(s"/programmers/${id}/company/${company.id}", "csrf-token" -> "aaaaaa") {
          status should equal(200)
        }
        delete(s"/programmers/${id}/company?csrf-token=aaaaaa") {
          status should equal(200)
        }
      }
    } finally {
      Programmer.deleteById(id)
    }
  }

  it should "add a skill to a programmer" in {
    val id = FactoryGirl(Programmer).create().id
    try {
      withSession("csrf-token" -> "aaaaaa") {
        post(s"/programmers/${id}/skills/${skill.id}", "csrf-token" -> "aaaaaa") {
          status should equal(200)
        }
        post(s"/programmers/${id}/skills/${skill.id}", "csrf-token" -> "aaaaaa") {
          status should equal(409)
        }
      }
    } finally {
      Programmer.deleteById(id)
    }
  }

  it should "remove a skill from a programmer" in {
    val id = FactoryGirl(Programmer).create().id
    try {
      withSession("csrf-token" -> "aaaaaa") {
        post(s"/programmers/${id}/skills/${skill.id}", "csrf-token" -> "aaaaaa") {
          status should equal(200)
        }
        delete(s"/programmers/${id}/skills/${skill.id}?csrf-token=aaaaaa") {
          status should equal(200)
        }
      }
    } finally {
      Programmer.deleteById(id)
    }
  }

}
