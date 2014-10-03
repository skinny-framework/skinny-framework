package controller

import skinny.controller.feature.{ SkinnySessionOAuth2LoginFeature, GitHubLoginFeature }
import skinny.filter.SkinnySessionFilter
import skinny.oauth2.client.OAuth2User
import skinny.oauth2.client.github.GitHubUser

class GitHubController extends ApplicationController
    with GitHubLoginFeature
    with SkinnySessionFilter
    with SkinnySessionOAuth2LoginFeature[GitHubUser] {

  override def redirectURI = "http://localhost:8080/example/github/callback"

  override protected def saveAuthorizedUser(user: GitHubUser): Unit = {
    skinnySession.setAttribute("user", user)
  }

  override protected def handleWhenLoginSucceeded(): Any = {
    redirect302(url(Controllers.github.okUrl))
  }

  def ok = {
    set("user", skinnySession.getAs[OAuth2User]("user"))
    set("github", skinnySession.getAs[GitHubUser]("user"))
    render("/github/ok")
  }

}
