package skinny.test

import skinny.controller.SkinnyServlet
import skinny.micro.context.SkinnyContext

/**
 * Mock of SkinnyServlet.
 */
trait MockServlet extends SkinnyServlet with MockControllerBase with MockWebPageControllerFeatures {

  // Work around for the following error 
  //  java.lang.NullPointerException
  //  at java.util.regex.Matcher.getTextLength(Matcher.java:1234)
  //  at java.util.regex.Matcher.reset(Matcher.java:308)
  //  at java.util.regex.Matcher.<init>(Matcher.java:228)
  //  at java.util.regex.Pattern.matcher(Pattern.java:1088)
  //  at scala.util.matching.Regex.findFirstIn(Regex.scala:388)
  //  at rl.UrlCodingUtils$class.isUrlEncoded(UrlCodingUtils.scala:24)
  //  at rl.UrlCodingUtils$.isUrlEncoded(UrlCodingUtils.scala:111)
  //  at rl.UrlCodingUtils$class.needsUrlEncoding(UrlCodingUtils.scala:32)
  //  at rl.UrlCodingUtils$.needsUrlEncoding(UrlCodingUtils.scala:111)
  //  at rl.UrlCodingUtils$class.ensureUrlEncoding(UrlCodingUtils.scala:35)
  //  at rl.UrlCodingUtils$.ensureUrlEncoding(UrlCodingUtils.scala:111)
  //  at org.scalatra.UriDecoder$.firstStep(ScalatraBase.scala:19)
  //  at org.scalatra.ScalatraServlet$.requestPath(ScalatraServlet.scala:30)
  //  at org.scalatra.ScalatraServlet$.org$scalatra$ScalatraServlet$$getRequestPath$1(ScalatraServlet.scala:19)
  //  at org.scalatra.ScalatraServlet$$anonfun$requestPath$3.apply(ScalatraServlet.scala:23)
  //  at org.scalatra.ScalatraServlet$$anonfun$requestPath$3.apply(ScalatraServlet.scala:22)
  //  at scala.Option.getOrElse(Option.scala:120)
  //  at org.scalatra.ScalatraServlet$.requestPath(ScalatraServlet.scala:22)
  //  at org.scalatra.ScalatraServlet.requestPath(ScalatraServlet.scala:68)
  //  at skinny.controller.feature.RequestScopeFeature$class.initializeRequestScopeAttributes(RequestScopeFeature.scala:98)
  //  at skinny.controller.SkinnyServlet.initializeRequestScopeAttributes(SkinnyServlet.scala:8)
  //  at skinny.test.MockControllerBase$class.$init$(MockControllerBase.scala:89)

  override def requestPath(implicit ctx: SkinnyContext = skinnyContext): String = {
    try {
      super.requestPath(ctx)
    } catch {
      case e: NullPointerException =>
        logger.debug("[work around] skipped NPE when resolving requestPath", e)
        "/"
    }
  }

}
