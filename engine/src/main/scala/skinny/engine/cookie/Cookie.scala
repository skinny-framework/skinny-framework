package skinny.engine.cookie

import java.util.{ Date, Locale }
import skinny.engine.implicits.RicherStringImplicits

case class Cookie(name: String, value: String)(
    implicit cookieOptions: CookieOptions = CookieOptions()) {

  import Cookie._
  import RicherStringImplicits._

  val options: CookieOptions = cookieOptions

  def toCookieString: String = {
    val sb = new StringBuilder
    sb append name append "="
    sb append value

    if (cookieOptions.domain.nonBlank && cookieOptions.domain != "localhost")
      sb.append("; Domain=").append({
        if (!cookieOptions.domain.startsWith(".")) "." + cookieOptions.domain
        else cookieOptions.domain
      }.toLowerCase(Locale.ENGLISH))

    val pth = cookieOptions.path
    if (pth.nonBlank) {
      sb append "; Path=" append (if (!pth.startsWith("/")) "/" + pth else pth)
    }
    if (cookieOptions.comment.nonBlank) {
      sb append ("; Comment=") append cookieOptions.comment
    }

    appendMaxAge(sb, cookieOptions.maxAge, cookieOptions.version)

    if (cookieOptions.secure) sb append "; Secure"
    if (cookieOptions.httpOnly) sb append "; HttpOnly"
    sb.toString
  }

  private[this] def appendMaxAge(sb: StringBuilder, maxAge: Int, version: Int): StringBuilder = {
    val dateInMillis = maxAge match {
      case a if a < 0 => None // we don't do anything for max-age when it's < 0 then it becomes a session cookie
      case 0 => Some(0L) // Set the date to the min date for the system
      case a => Some(currentTimeMillis + a * 1000)
    }

    // This used to be Max-Age but IE is not always very happy with that
    // see: http://mrcoles.com/blog/cookies-max-age-vs-expires/
    // see Q1: http://blogs.msdn.com/b/ieinternals/archive/2009/08/20/wininet-ie-cookie-internals-faq.aspx
    val bOpt = dateInMillis map (ms => appendExpires(sb, new Date(ms)))
    val agedOpt = if (version > 0) bOpt map (_.append("; Max-Age=").append(maxAge)) else bOpt
    agedOpt getOrElse sb
  }

  private[this] def appendExpires(sb: StringBuilder, expires: Date): StringBuilder = {
    sb append "; Expires=" append formatExpires(expires)
  }

}

object Cookie {

  import java.text.SimpleDateFormat
  import java.util.{ Date, Locale, TimeZone }

  val SweetCookiesKey = "skinny.engine.SweetCookies"

  val CookieOptionsKey = "skinny.engine.CookieOptions"

  private object DateUtil {

    @volatile private[this] var _currentTimeMillis: Option[Long] = None

    def currentTimeMillis: Long = _currentTimeMillis getOrElse System.currentTimeMillis

    def currentTimeMillis_=(ct: Long): Unit = _currentTimeMillis = Some(ct)

    def freezeTime(): Unit = _currentTimeMillis = Some(System.currentTimeMillis())

    def unfreezeTime(): Unit = _currentTimeMillis = None

    def formatDate(
      date: Date,
      format: String,
      timeZone: TimeZone = TimeZone.getTimeZone("GMT"),
      locale: Locale = Locale.ENGLISH): String = {
      val df = new SimpleDateFormat(format, locale)
      df.setTimeZone(timeZone)
      df.format(date)
    }

  }

  @volatile private[this] var _currentTimeMillis: Option[Long] = None

  def currentTimeMillis: Long = _currentTimeMillis getOrElse System.currentTimeMillis

  def currentTimeMillis_=(ct: Long): Unit = _currentTimeMillis = Some(ct)

  def freezeTime(): Unit = _currentTimeMillis = Some(System.currentTimeMillis())

  def unfreezeTime(): Unit = _currentTimeMillis = None

  def formatExpires(date: Date): String = DateUtil.formatDate(date, "EEE, dd MMM yyyy HH:mm:ss zzz")

}
