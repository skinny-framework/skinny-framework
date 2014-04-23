package controller

import skinny.SkinnyServlet
import skinny.controller.feature.FileUploadFeature

class FileUploadController extends SkinnyServlet with FileUploadFeature {

  def form = render("/fileUpload/form")

  def submit = {
    fileParams.get("file") match {
      case Some(file) => println(new String(file.get()))
      case None => println("file not found")
    }
    redirect(url(Controllers.fileUpload.formUrl))
  }

}
