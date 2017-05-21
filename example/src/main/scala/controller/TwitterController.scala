package controller

import skinny.controller.feature.{ SkinnySessionTwitterLoginFeature, TwitterLoginFeature }
import skinny.filter.SkinnySessionFilter
import twitter4j.User
import twitter4j.auth.AccessToken

class TwitterController extends ApplicationController with TwitterLoginFeature {
  //with SkinnySessionTwitterLoginFeature with SkinnySessionFilter {

  override protected def isLocalDebug = true

  override protected def saveAuthorizedUser(user: User): Unit = {
    session.setAttribute("user", user)
    //skinnySession.setAttribute("user", user)
  }

  override protected def handleWhenLoginSucceeded(): Any = {
    redirect302(url(Controllers.twitter.okUrl))
  }

  def ok = {
    logger.info("accessToken: " + currentAccessToken())
    logger.info("twitter4j: " + twitter)
    set("user", session.getAs[User]("user"))
    //set("user", skinnySession.getAs[User]("user"))
    render("/twitter/ok")
  }

}
