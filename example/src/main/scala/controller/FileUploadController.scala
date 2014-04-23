package controller

import org.scalatra.servlet.FileUploadSupport
import skinny.SkinnyServlet

class FileUploadController extends SkinnyServlet with FileUploadSupport {

  def form = render("/fileUpload/form")

  def submit = {
    fileParams.get("file") match {
      case Some(file) => println(new String(file.get()))
      case None => println("file not found")
    }
    redirect(url(Controllers.fileUpload.formUrl))
  }

}
