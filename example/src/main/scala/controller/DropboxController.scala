package controller

import skinny.controller.feature.DropboxLoginFeature
import skinny.oauth2.client.OAuth2User
import skinny.oauth2.client.dropbox.DropboxUser

class DropboxController extends ApplicationController with DropboxLoginFeature {

  override def redirectURI = "http://localhost:8080/example/dropbox/callback"

  override protected def saveAuthorizedUser(user: DropboxUser): Unit = {
    session.setAttribute("user", user)
  }

  override protected def handleWhenLoginSucceeded(): Any = {
    redirect302(url(Controllers.dropbox.okUrl))
  }

  def ok = {
    set("user", session.getAs[OAuth2User]("user"))
    set("dropbox", session.getAs[DropboxUser]("user"))
    render("/dropbox/ok")
  }

}
