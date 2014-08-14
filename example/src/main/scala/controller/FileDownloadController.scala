package controller

class FileDownloadController extends ApplicationController {

  val message = "skinny-framework is great!\n" * 10000

  def small = withOutputStream { implicit s =>
    response.addHeader("Content-Type", "plain/text")
    writeChunk("OK".getBytes)
  }

  def index = withOutputStream { implicit s =>
    response.addHeader("Content-Type", "plain/text")
    (1 to 1000).foreach { _ =>
      writeChunk(message.getBytes("UTF-8"))
    }
  }

  def nullValue = withOutputStream { implicit s =>
    response.addHeader("Content-Type", "plain/text")
    writeChunk(null)
  }

  def error = withOutputStream { implicit s =>
    response.addHeader("Content-Type", "plain/text")
    throw new RuntimeException
  }

}
