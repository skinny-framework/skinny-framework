package skinny.engine.multipart

import java.util.{ HashMap => JHashMap, Map => JMap }
import javax.servlet.http._

import skinny.engine.context.SkinnyEngineContext
import skinny.engine.data.MultiMapHeadView
import skinny.engine.SkinnyEngineBase

import scala.collection.JavaConverters._

/**
 * FileUploadSupport can be mixed into a [[skinny.engine.SkinnyEngineFilter]]
 * or [[skinny.engine.SkinnyEngineServlet]] to provide easy access to data
 * submitted as part of a multipart HTTP request.  Commonly this is used for
 * retrieving uploaded files.
 *
 * Once the trait has been mixed into your handler, you need to enable multipart
 * configuration in your ''web.xml'' or by using `@MultipartConfig` annotation. To
 * configure in ''web.xml'' add `<multipart-config />` to your `<servlet>` element. If you
 * prefer annotations instead, place `@MultipartConfig` to your handler. Both ways
 * provide some further configuration options, such as specifying the max total request size
 * and max size for invidual files in the request. You might want to set these to prevent
 * users from uploading too large files.
 *
 * When the configuration has been done, you can access any files using
 * `fileParams("myFile")` where ''myFile'' is the name
 * of the parameter used to upload the file being retrieved. If you are
 * expecting multiple files with the same name, you can use
 * `fileMultiParams("files[]")` to access them all.
 *
 * To handle any errors that are caused by multipart handling, you need
 * to configure an error handler to your handler class:
 *
 * {{{
 * import skinny.engine.servlet.SizeLimitExceededException
 * import skinny.engine.servlet.FileUploadSupport
 *
 * @MultipartConfig(maxFileSize=1024*1024)
 * class FileEaterServlet extends SkinnyEngineServlet with FileUploadSupport {
 *   error {
 *     case e: SizeConstrainttExceededException => "Oh, too much! Can't take it all."
 *     case e: IOException                      => "Server denied me my meal, thanks anyway."
 *   }
 *
 *   post("/eatfile") {
 *     "Thanks! You just provided me " + fileParams("lunch").size + " bytes for a lunch."
 *   }
 * }
 * }}}
 *
 * }}* @note Once any handler with FileUploadSupport has accessed the request, the
 *       fileParams returned by FileUploadSupport will remain fixed for the
 *       lifetime of the request.
 *
 * @note Will not work on Jetty versions prior to 8.1.3.  See
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=376324.  The old
 * scalatra-fileupload module still works for earlier versions
 * of Jetty.
 */
trait FileUploadSupport
    extends SkinnyEngineBase
    with HasMultipartConfig {

  import FileUploadSupport._

  /* Called for any exceptions thrown by handling file uploads
   * to detect whether it signifies a too large file being
   * uploaded or a too large request in general.
   *
   * This can be overriden for the container being used if it
   * doesn't throw `IllegalStateException` or if it throws
   * `IllegalStateException` for some other reason.
   */
  protected def isSizeConstraintException(e: Exception): Boolean = e match {
    case _: IllegalStateException => true
    case _ => false
  }

  override def handle(req: HttpServletRequest, res: HttpServletResponse): Unit = {
    val req2 = try {
      if (isMultipartRequest(req)) {
        val bodyParams = extractMultipartParams(req)
        val mergedFormParams = mergeFormParamsWithQueryString(req, bodyParams)

        wrapRequest(req, mergedFormParams)
      } else req
    } catch {
      case e: Exception => {
        req.setAttribute(SkinnyEngineBase.PrehandleExceptionKey, e)
        req
      }
    }

    super.handle(req2, res)
  }

  private def isMultipartRequest(req: HttpServletRequest): Boolean = {
    val isPostOrPut = Set("POST", "PUT", "PATCH").contains(req.getMethod)
    isPostOrPut && (req.contentType match {
      case Some(contentType) => contentType.startsWith("multipart/")
      case _ => false
    })
  }

  private def extractMultipartParams(req: HttpServletRequest): BodyParams = {
    req.get(BodyParamsKey).asInstanceOf[Option[BodyParams]] match {
      case Some(bodyParams) =>
        bodyParams

      case None => {
        val bodyParams = getParts(req).foldRight(BodyParams(FileMultiParams(), Map.empty)) {
          (part, params) =>
            val item = FileItem(part)

            if (!(item.isFormField)) {
              BodyParams(params.fileParams + ((
                item.getFieldName, item +: params.fileParams.getOrElse(item.getFieldName, List[FileItem]())
              )), params.formParams)
            } else {
              BodyParams(params.fileParams, params.formParams)
            }
        }

        req.setAttribute(BodyParamsKey, bodyParams)
        bodyParams
      }
    }
  }

  private def getParts(req: HttpServletRequest): Iterable[Part] = {
    try {
      if (isMultipartRequest(req)) req.getParts.asScala else Seq.empty[Part]
    } catch {
      case e: Exception if isSizeConstraintException(e) => throw new SizeConstraintExceededException("Too large request or file", e)
    }
  }

  private def fileItemToString(item: FileItem): String = {
    new String(item.get().map(_.toChar))
  }

  private def mergeFormParamsWithQueryString(req: HttpServletRequest, bodyParams: BodyParams): Map[String, List[String]] = {
    var mergedParams = bodyParams.formParams
    req.getParameterMap.asScala foreach {
      case (name, values) =>
        val formValues = mergedParams.getOrElse(name, List.empty)
        mergedParams += name -> (values.toList ++ formValues)
    }

    mergedParams
  }

  private def wrapRequest(req: HttpServletRequest, formMap: Map[String, Seq[String]]): HttpServletRequestWrapper = {
    val wrapped = new HttpServletRequestWrapper(req) {
      override def getParameter(name: String): String = formMap.get(name) map {
        _.head
      } getOrElse null

      override def getParameterNames: java.util.Enumeration[String] = formMap.keysIterator.asJavaEnumeration

      override def getParameterValues(name: String): Array[String] = formMap.get(name) map {
        _.toArray
      } getOrElse null

      override def getParameterMap: JMap[String, Array[String]] = {
        (new JHashMap[String, Array[String]].asScala ++ (formMap transform {
          (k, v) => v.toArray
        })).asJava
      }
    }
    wrapped
  }

  def fileMultiParams(implicit ctx: SkinnyEngineContext): FileMultiParams = {
    extractMultipartParams(ctx.request).fileParams
  }

  def fileMultiParams(key: String)(implicit ctx: SkinnyEngineContext): Seq[FileItem] = {
    fileMultiParams(ctx)(key)
  }

  /**
   * @return a Map, keyed on the names of multipart file upload parameters,
   *         of all multipart files submitted with the request
   */
  def fileParams(implicit ctx: SkinnyEngineContext): MultiMapHeadView[String, FileItem] = {
    new MultiMapHeadView[String, FileItem] {
      protected def multiMap = fileMultiParams(ctx)
    }
  }

  def fileParams(key: String)(implicit ctx: SkinnyEngineContext): FileItem = {
    fileParams(ctx)(key)
  }
}

object FileUploadSupport {

  private val BodyParamsKey = "skinny.engine.fileupload.bodyParams"

  case class BodyParams(
    fileParams: FileMultiParams,
    formParams: Map[String, List[String]])

}
