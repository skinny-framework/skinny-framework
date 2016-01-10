package skinny.bootstrap

import java.io.InputStream
import java.net.URL
import java.util.EventListener
import javax.servlet._
import javax.servlet.descriptor.JspConfigDescriptor

import scala.collection.JavaConverters._

/**
  * NOOP ServletContext mainly used for loading routes without a Servlet container.
  */
class NOOPServletContext extends ServletContext {

  val dummyServletRegistrationDynamic = new ServletRegistration.Dynamic {

    override def setServletSecurity(constraint: ServletSecurityElement): java.util.Set[String] = Set[String]().asJava

    override def setRunAsRole(roleName: String): Unit = {}

    override def setLoadOnStartup(loadOnStartup: Int): Unit = {}

    override def setMultipartConfig(multipartConfig: MultipartConfigElement): Unit = {}

    override def setAsyncSupported(isAsyncSupported: Boolean): Unit = {}

    override def getMappings: java.util.Collection[String] = Seq[String]().asJava

    override def getRunAsRole: String = null

    override def addMapping(urlPatterns: String*): java.util.Set[String] = Set[String]().asJava

    override def setInitParameters(initParameters: java.util.Map[String, String]): java.util.Set[String] = Set[String]().asJava

    override def getName: String = null

    override def getClassName: String = this.getClass.getCanonicalName

    override def setInitParameter(name: String, value: String): Boolean = true

    override def getInitParameters: java.util.Map[String, String] = Map[String, String]().asJava

    override def getInitParameter(name: String): String = null

  }

  val dummyFilterRegistrationDynamic = new FilterRegistration.Dynamic {

    override def getUrlPatternMappings: java.util.Collection[String] = Seq[String]().asJava

    override def addMappingForServletNames(dispatcherTypes: java.util.EnumSet[DispatcherType], isMatchAfter: Boolean, servletNames: String*): Unit = {}

    override def getServletNameMappings: java.util.Collection[String] = Seq[String]().asJava

    override def addMappingForUrlPatterns(dispatcherTypes: java.util.EnumSet[DispatcherType], isMatchAfter: Boolean, urlPatterns: String*): Unit = {}

    override def setAsyncSupported(isAsyncSupported: Boolean): Unit = {}

    override def setInitParameters(initParameters: java.util.Map[String, String]): java.util.Set[String] = Set[String]().asJava

    override def getName: String = null

    override def getClassName: String = this.getClass.getCanonicalName

    override def setInitParameter(name: String, value: String): Boolean = true

    override def getInitParameters: java.util.Map[String, String] = Map[String, String]().asJava

    override def getInitParameter(name: String): String = null

  }

  override def getRequestDispatcher(path: String): RequestDispatcher = null

  override def getJspConfigDescriptor: JspConfigDescriptor = null

  override def getNamedDispatcher(name: String): RequestDispatcher = null

  override def getRealPath(path: String): String = path

  override def getServletContextName: String = ""

  override def getEffectiveSessionTrackingModes: java.util.Set[SessionTrackingMode] = Set[SessionTrackingMode]().asJava

  override def getFilterRegistrations: java.util.Map[String, _ <: FilterRegistration] = Map[String, FilterRegistration]().asJava

  override def log(msg: String): Unit = {}

  override def log(exception: Exception, msg: String): Unit = {}

  override def log(message: String, throwable: Throwable): Unit = {}

  override def getAttribute(name: String): AnyRef = null

  override def removeAttribute(name: String): Unit = {}

  override def getContextPath: String = ""

  override def getSessionCookieConfig: SessionCookieConfig = null

  override def getEffectiveMinorVersion: Int = 0

  override def getServlets: java.util.Enumeration[Servlet] = Iterator[Servlet]().asJavaEnumeration

  override def setInitParameter(name: String, value: String): Boolean = true

  override def getServletRegistrations: java.util.Map[String, _ <: ServletRegistration] = Map[String, ServletRegistration]().asJava

  override def getDefaultSessionTrackingModes: java.util.Set[SessionTrackingMode] = Set[SessionTrackingMode]().asJava

  override def getResourceAsStream(path: String): InputStream = null

  override def getServletRegistration(servletName: String): ServletRegistration = null

  override def getContext(uripath: String): ServletContext = this

  override def declareRoles(roleNames: String*): Unit = {}

  override def getServletNames: java.util.Enumeration[String] = Iterator[String]().asJavaEnumeration

  override def createListener[T <: EventListener](clazz: Class[T]): T = clazz.newInstance()

  override def getEffectiveMajorVersion: Int = 3

  override def setAttribute(name: String, `object`: scala.Any): Unit = {}

  override def getClassLoader: ClassLoader = getClass.getClassLoader

  override def getAttributeNames: java.util.Enumeration[String] = Iterator[String]().asJavaEnumeration

  override def getResourcePaths(path: String): java.util.Set[String] = Set[String]().asJava

  override def getServlet(name: String): Servlet = null

  override def getServerInfo: String = null

  override def addListener(className: String): Unit = {}

  override def addListener[T <: EventListener](t: T): Unit = {}

  override def addListener(listenerClass: Class[_ <: EventListener]): Unit = {}

  override def addServlet(servletName: String, className: String): ServletRegistration.Dynamic = {
    dummyServletRegistrationDynamic
  }

  override def addServlet(servletName: String, servlet: Servlet): ServletRegistration.Dynamic = {
    dummyServletRegistrationDynamic
  }

  override def addServlet(servletName: String, servletClass: Class[_ <: Servlet]): ServletRegistration.Dynamic = {
    dummyServletRegistrationDynamic
  }

  override def getMinorVersion: Int = 0

  override def getInitParameterNames: java.util.Enumeration[String] = Iterator[String]().asJavaEnumeration

  override def setSessionTrackingModes(sessionTrackingModes: java.util.Set[SessionTrackingMode]): Unit = {}

  override def addFilter(filterName: String, className: String): FilterRegistration.Dynamic = {
    dummyFilterRegistrationDynamic
  }

  override def addFilter(filterName: String, filter: Filter): FilterRegistration.Dynamic = {
    dummyFilterRegistrationDynamic
  }

  override def addFilter(filterName: String, filterClass: Class[_ <: Filter]): FilterRegistration.Dynamic = {
    dummyFilterRegistrationDynamic
  }

  override def getFilterRegistration(filterName: String): FilterRegistration = null

  override def getMimeType(file: String): String = null

  override def getMajorVersion: Int = 3

  override def createServlet[T <: Servlet](clazz: Class[T]): T = clazz.newInstance()

  override def getInitParameter(name: String): String = null

  override def getResource(path: String): URL = new URL(path)

  override def createFilter[T <: Filter](clazz: Class[T]): T = clazz.newInstance()

}

object NOOPServletContext extends NOOPServletContext