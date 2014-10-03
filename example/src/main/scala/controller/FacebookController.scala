package controller

import skinny.controller.feature.FacebookLoginFeature
import skinny.oauth2.client.OAuth2User
import skinny.oauth2.client.facebook.FacebookUser

class FacebookController extends ApplicationController with FacebookLoginFeature {

  override def redirectURI = "http://localhost:8080/example/facebook/callback"

  override protected def saveAuthorizedUser(user: FacebookUser): Unit = {
    session.setAttribute("user", user)
  }

  override protected def handleWhenLoginSucceeded(): Any = {
    redirect302(url(Controllers.facebook.okUrl))
  }

  def ok = {
    set("user", session.getAs[OAuth2User]("user"))
    set("facebook", session.getAs[FacebookUser]("user"))
    render("/facebook/ok")
  }

}
