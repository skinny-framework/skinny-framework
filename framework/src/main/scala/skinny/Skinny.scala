package skinny

/**
 * Global object for accessing Skinny common APIs & request scope attributes in views.
 */
case class Skinny(requestScope: collection.mutable.Map[String, Any]) {

  import skinny.controller.feature.RequestScopeFeature._

  def getAs[A](key: String): Option[A] = requestScope.get(key).map(_.asInstanceOf[A])

  def set(key: String, value: Any): Unit = requestScope.update(key, value)

  def env: String = SkinnyEnv.get().orNull

  def params: Params = getAs[Params](ATTR_PARAMS).orNull
  def flash: Flash = getAs[Flash](ATTR_FLASH).orNull

  def errorMessages: Seq[String] = getAs[Seq[String]](ATTR_ERROR_MESSAGES).getOrElse(Nil)
  def keyAndErrorMessages: Map[String, Seq[String]] = getAs[Map[String, Seq[String]]](ATTR_KEY_AND_ERROR_MESSAGES).getOrElse(Map())

  def contextPath: String = getAs[String](ATTR_CONTEXT_PATH).orNull
  def requestPath: String = getAs[String](ATTR_REQUEST_PATH).orNull
  def requestPathWithQueryString: String = getAs[String](ATTR_REQUEST_PATH_WITH_QUERY_STRING).orNull

  def csrfKey: String = getAs[String](ATTR_CSRF_KEY).orNull
  def csrfToken: String = getAs[String](ATTR_CSRF_TOKEN).orNull
  def csrfMetaTag: String = s"""<meta content="${csrfToken}" name="${csrfKey}" />"""
  def csrfMetaTags: String = csrfMetaTag
  def csrfHiddenInputTag: String = s"""<input type="hidden" name="${csrfKey}" value="${csrfToken}"/>"""

  def i18n: I18n = getAs[I18n](ATTR_I18N).getOrElse(I18n())

}
