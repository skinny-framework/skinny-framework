package controller

import skinny.controller.feature.GoogleLoginFeature
import skinny.oauth2.client.OAuth2User
import skinny.oauth2.client.google.GoogleUser

class GoogleController extends ApplicationController with GoogleLoginFeature {

  override def redirectURI = "http://localhost:8080/example/google/callback"

  override protected def saveAuthorizedUser(user: GoogleUser): Unit = {
    session.setAttribute("user", user)
  }

  override protected def handleWhenLoginSucceeded(): Any = {
    redirect302(url(Controllers.google.okUrl))
  }

  def ok = {
    set("user", session.getAs[OAuth2User]("user"))
    set("google", session.getAs[GoogleUser]("user"))
    render("/google/ok")
  }

}
