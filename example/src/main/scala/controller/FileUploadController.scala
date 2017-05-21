package controller

import skinny.SkinnyServlet
import skinny.controller.feature.FileUploadFeature

class FileUploadController extends SkinnyServlet with FileUploadFeature {

  def form = render("/fileUpload/form")

  def submit = {
    if (params.get("name").isEmpty) {
      throw new RuntimeException
    }
    logger.info(params.get("name"))
    fileParams.get("file") match {
      case Some(file) => logger.info(new String(file.get()))
      case None       => logger.info("file not found")
    }
    redirect(url(Controllers.fileUpload.formUrl))
  }

}
