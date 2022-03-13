package controller

import skinny._
import skinny.filter.TxPerRequestFilter
import model.Company
import org.joda.time.DateTime

object ErrorController extends ApplicationController with TxPerRequestFilter with Routes {

  def runtime = {
    throw new RuntimeException
  }

  val runtimeUrl = get("/error/runtime")(runtime).as("errorPage")

  get("/error/rollback") {
    Company.createWithAttributes("name" -> "Typesafe", "createdAt" -> DateTime.now)

    rollbackTxPerRequest
    logger.info("Transaction should be rolled back.")

  }.as("rollbackPage")

}
