package controller

import skinny._
import skinny.validator._
import model._

object CommentsController extends SkinnyResource with ApplicationController {
  //protectFromForgery()

  override def model = Comment
  override def resourcesName = "comments"
  override def resourceName = "comment"

  override def createForm = validation(createParams, paramKey("author") is required & maxLength(64))
  override def createFormStrongParameters = Seq("author" -> ParamType.String, "text" -> ParamType.String)

  override def updateForm = createForm
  override def updateFormStrongParameters = Seq("author" -> ParamType.String, "text" -> ParamType.String)

  post("/comments.json") {
    createResource()
    ""
  }
}
