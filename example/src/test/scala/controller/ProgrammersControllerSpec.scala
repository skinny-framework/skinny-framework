package controller

import org.scalatra.test.scalatest._
import skinny.test._
import model._

class ProgrammersControllerSpec extends ScalatraFlatSpec with unit.SkinnyTesting {

  addFilter(Controllers.programmers, "/*")

  def skill = Skill.findAll(1, 0).head
  def company = Company.findAll(1, 0).head
  def programmer = Programmer.findAll(1, 0).head

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

    withSession("csrfToken" -> "12345") {
      post(s"/programmers", "name" -> newName, "companyId" -> company.id.toString, "csrfToken" -> "12345") {
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

    withSession("csrfToken" -> "12345") {
      put(s"/programmers/${programmer.id}", "name" -> newName, "companyId" -> company.id.toString, "csrfToken" -> "12345") {
        status should equal(200)
      }
    }
    Programmer.findById(programmer.id).get.name should equal(newName)
  }

  it should "delete a programmer" in {
    val c = Programmer.column
    val id = Programmer.createWithNamedValues(c.name -> "Unit Test Programmer")
    delete(s"/programmers/${id}") {
      status should equal(403)
    }
    withSession("csrfToken" -> "aaaaaa") {
      delete(s"/programmers/${id}?csrfToken=aaaaaa") {
        status should equal(200)
      }
    }
  }

  it should "add a programmer to a company" in {
    val c = Programmer.column
    val id = Programmer.createWithNamedValues(c.name -> "JoinCompany Test Programmer")
    try {
      withSession("csrfToken" -> "aaaaaa") {
        post(s"/programmers/${id}/company/${company.id}", "csrfToken" -> "aaaaaa") {
          status should equal(200)
        }
      }
    } finally {
      Programmer.deleteById(id)
    }
  }

  it should "remove a programmer from a company" in {
    val c = Programmer.column
    val id = Programmer.createWithNamedValues(c.name -> "LeaveCompany Test Programmer")
    try {
      withSession("csrfToken" -> "aaaaaa") {
        post(s"/programmers/${id}/company/${company.id}", "csrfToken" -> "aaaaaa") {
          status should equal(200)
        }
        delete(s"/programmers/${id}/company?csrfToken=aaaaaa") {
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
      withSession("csrfToken" -> "aaaaaa") {
        post(s"/programmers/${id}/skills/${skill.id}", "csrfToken" -> "aaaaaa") {
          status should equal(200)
        }
        post(s"/programmers/${id}/skills/${skill.id}", "csrfToken" -> "aaaaaa") {
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
      withSession("csrfToken" -> "aaaaaa") {
        post(s"/programmers/${id}/skills/${skill.id}", "csrfToken" -> "aaaaaa") {
          status should equal(200)
        }
        delete(s"/programmers/${id}/skills/${skill.id}?csrfToken=aaaaaa") {
          status should equal(200)
        }
      }
    } finally {
      Programmer.deleteById(id)
    }
  }

}
