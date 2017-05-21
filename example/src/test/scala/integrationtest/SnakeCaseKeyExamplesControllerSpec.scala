package integrationtest

import _root_.controller.SnakeCaseKeyExamplesController
import _root_.model._
import skinny._, skinny.test._

class SnakeCaseKeyExamplesControllerSpec extends SkinnyFlatSpec with SkinnyTestSupport with DBSettings {
  addFilter(SnakeCaseKeyExamplesController, "/*")

  def snakeCaseKeyExample = FactoryGirl(SnakeCaseKeyExample).create()

  it should "show snake_case_key_examples" in {
    get("/snake_case_key_examples") {
      status should equal(200)
    }
    get("/snake_case_key_examples/") {
      status should equal(200)
    }
    get("/snake_case_key_examples.json") {
      status should equal(200)
    }
    get("/snake_case_key_examples.xml") {
      status should equal(200)
    }
  }

  it should "show a snake_case_key_example in detail" in {
    get(s"/snake_case_key_examples/${snakeCaseKeyExample.id}") {
      status should equal(200)
    }
    get(s"/snake_case_key_examples/${snakeCaseKeyExample.id}.xml") {
      status should equal(200)
    }
    get(s"/snake_case_key_examples/${snakeCaseKeyExample.id}.json") {
      status should equal(200)
    }
  }

  it should "show new entry form" in {
    get(s"/snake_case_key_examples/new") {
      status should equal(200)
    }
  }

  it should "create a snake_case_key_example" in {
    post(s"/snake_case_key_examples", "first_name" -> "dummy", "luckey_number" -> "123") {
      status should equal(403)
    }

    withSession("csrf-token" -> "12345") {
      post(s"/snake_case_key_examples", "first_name" -> "dummy", "luckey_number" -> "123", "csrf-token" -> "12345") {
        status should equal(302)
        val id = header("Location").split("/").last.toLong
        SnakeCaseKeyExample.findById(id).isDefined should equal(true)
      }
    }
  }

  it should "show the edit form" in {
    get(s"/snake_case_key_examples/${snakeCaseKeyExample.id}/edit") {
      status should equal(200)
    }
  }

  it should "update a snake_case_key_example" in {
    put(s"/snake_case_key_examples/${snakeCaseKeyExample.id}", "first_name" -> "dummy", "luckey_number" -> "123") {
      status should equal(403)
    }

    withSession("csrf-token" -> "12345") {
      put(s"/snake_case_key_examples/${snakeCaseKeyExample.id}",
          "first_name"    -> "dummy",
          "luckey_number" -> "123",
          "csrf-token"    -> "12345") {
        status should equal(302)
      }
    }
  }

  it should "delete a snake_case_key_example" in {
    val snakeCaseKeyExample = FactoryGirl(SnakeCaseKeyExample).create()
    delete(s"/snake_case_key_examples/${snakeCaseKeyExample.id}") {
      status should equal(403)
    }
    withSession("csrf-token" -> "aaaaaa") {
      delete(s"/snake_case_key_examples/${snakeCaseKeyExample.id}?csrf-token=aaaaaa") {
        status should equal(200)
      }
    }
  }

}
