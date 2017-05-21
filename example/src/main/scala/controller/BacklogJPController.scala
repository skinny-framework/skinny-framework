package controller

import skinny.controller.feature.BacklogJPLoginFeature
import skinny.oauth2.client.OAuth2User
import skinny.oauth2.client.backlog.BacklogUser

class BacklogJPController extends ApplicationController with BacklogJPLoginFeature {

  override def spaceID     = "seratch"
  override def redirectURI = "http://localhost:8080/example/backlogjp/callback"

  override protected def saveAuthorizedUser(user: BacklogUser): Unit = {
    session.setAttribute("user", user)
  }

  override protected def handleWhenLoginSucceeded(): Any = {
    redirect302(url(Controllers.backlogJp.okUrl))
  }

  def ok = {
    set("user", session.getAs[OAuth2User]("user"))
    set("backlog", session.getAs[BacklogUser]("user"))
    render("/backlog/ok")
  }

}
