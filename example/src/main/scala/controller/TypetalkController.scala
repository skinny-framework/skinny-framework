package controller

import skinny.controller.feature.TypetalkLoginFeature
import skinny.oauth2.client.OAuth2User
import skinny.oauth2.client.typetalk.TypetalkUser

class TypetalkController extends ApplicationController
    with TypetalkLoginFeature {

  override def redirectURI = "http://127.0.0.1:8080/example/typetalk/callback"

  override protected def saveAuthorizedUser(user: TypetalkUser): Unit = {
    session.setAttribute("user", user)
  }

  override protected def handleWhenLoginSucceeded(): Any = {
    redirect302(url(Controllers.typetalk.okUrl))
  }

  def ok = {
    set("user", session.getAs[OAuth2User]("user"))
    set("typetalk", session.getAs[TypetalkUser]("user"))
    render("/typetalk/ok")
  }

}

