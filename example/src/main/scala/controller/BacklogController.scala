package controller

import skinny.controller.feature.BacklogLoginFeature
import skinny.oauth2.client.OAuth2User
import skinny.oauth2.client.backlog.BacklogUser

class BacklogController extends ApplicationController with BacklogLoginFeature {

  override def spaceID     = "seratch"
  override def redirectURI = "http://localhost:8080/example/backlog/callback"

  override protected def saveAuthorizedUser(user: BacklogUser): Unit = {
    session.setAttribute("user", user)
  }

  override protected def handleWhenLoginSucceeded(): Any = {
    redirect302(url(Controllers.backlog.okUrl))
  }

  def ok = {
    set("user", session.getAs[OAuth2User]("user"))
    set("backlog", session.getAs[BacklogUser]("user"))
    render("/backlog/ok")
  }

}
