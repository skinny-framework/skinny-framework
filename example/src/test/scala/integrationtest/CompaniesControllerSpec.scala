package integrationtest

import skinny.test._
import skinny.json.JSONStringOps._
import model._
import controller.Controllers

class CompaniesControllerSpec extends SkinnyFlatSpec with unit.SkinnyTesting {

  addFilter(Controllers.companies, "/*")

  def company = Company.findAllWithLimitOffset(1, 0).headOption.getOrElse {
    FactoryGirl(Company).create()
  }

  it should "show companies" in {
    get("/companies") {
      status should equal(200)
      header("X-Content-Type-Options") should equal("nosniff")
      header("X-XSS-Protection") should equal("1; mode=block")
    }
    get("/companies/") {
      status should equal(200)
    }
    get("/companies.json") {
      logger.debug(body)
      status should equal(200)
      val acts = fromJSONString[List[Company]](body)
      acts should not equal (None)
      acts.get should not be (empty)
      atLeast(1, acts.get) should have('name (company.name))
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

    withSession("csrf-token" -> "12345") {
      post(s"/companies", "csrf-token" -> "12345") {
        status should equal(400)
      }
      post(s"/companies",
           "name"       -> newName,
           "url"        -> "http://www.example.com/",
           "updatedAt"  -> "2013-01-02 12:34:56",
           "csrf-token" -> "12345") {
        status should equal(302)
        val id = header("Location").split("/").last.toLong
        Company.findById(CompanyId(id)).isDefined should equal(true)
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

    withSession("csrf-token" -> "12345") {
      put(s"/companies/${company.id}",
          "name"       -> newName,
          "updatedAt"  -> "2013-01-02 12:34:56",
          "csrf-token" -> "12345") {
        status should equal(302)
      }
      put(s"/companies/${company.id}", "csrf-token" -> "12345") {
        status should equal(400)
      }
    }
    Company.findById(company.id).get.name should equal(newName)
  }

  it should "delete a company" in {
    val company = FactoryGirl(Company).create()
    delete(s"/companies/${company.id}") {
      status should equal(403)
    }
    withSession("csrf-token" -> "aaaaaa") {
      delete(s"/companies/${company.id}?csrf-token=aaaaaa") {
        status should equal(200)
      }
    }
  }

}
