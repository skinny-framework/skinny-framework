package controller

import org.scalatra.test.scalatest._
import skinny.test._
import model._

class CompaniesControllerSpec extends ScalatraFlatSpec with unit.SkinnyTesting {

  addFilter(CompaniesController, "/*")

  def company = Company.findAllPaging(1, 0).head

  it should "show companies" in {
    get("/companies") {
      status should equal(200)
    }
    get("/companies/") {
      status should equal(200)
    }
    get("/companies.json") {
      logger.debug(body)
      status should equal(200)
    }
    get("/companies.xml") {
      logger.debug(body)
      status should equal(200)
      body should include("<companies><company>")
    }
  }

  it should "show a company in detail" in {
    get(s"/companies/${company.id}") {
      status should equal(200)
    }
    get(s"/companies/${company.id}.xml") {
      logger.debug(body)
      status should equal(200)
    }
    get(s"/companies/${company.id}.json") {
      logger.debug(body)
      status should equal(200)
    }
  }

  it should "show new entry form" in {
    get(s"/companies/new") {
      status should equal(200)
    }
  }

  it should "create a company" in {
    val newName = s"Created at ${System.currentTimeMillis}"
    post(s"/companies", "name" -> newName, "url" -> "http://www.example.com/") {
      status should equal(403)
    }

    withSession("csrfToken" -> "12345") {
      post(s"/companies", "name" -> newName, "url" -> "http://www.example.com/", "csrfToken" -> "12345") {
        status should equal(302)
        val id = header("Location").split("/").last.toLong
        Company.findById(id).isDefined should equal(true)
      }
    }
  }

  it should "show the edit form" in {
    get(s"/companies/${company.id}/edit") {
      status should equal(200)
    }
  }

  it should "update a company" in {
    val newName = s"Updated at ${System.currentTimeMillis}"
    put(s"/companies/${company.id}", "name" -> newName) {
      status should equal(403)
    }
    Company.findById(company.id).get.name should not equal (newName)

    withSession("csrfToken" -> "12345") {
      put(s"/companies/${company.id}", "name" -> newName, "csrfToken" -> "12345") {
        status should equal(200)
      }
    }
    Company.findById(company.id).get.name should equal(newName)
  }

  it should "delete a company" in {
    val company = FactoryGirl(Company).create()
    delete(s"/companies/${company.id}") {
      status should equal(403)
    }
    withSession("csrfToken" -> "aaaaaa") {
      delete(s"/companies/${company.id}?csrfToken=aaaaaa") {
        status should equal(200)
      }
    }
  }

}
