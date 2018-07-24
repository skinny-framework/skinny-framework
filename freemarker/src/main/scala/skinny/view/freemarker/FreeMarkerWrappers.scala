/*
The Circumflex License
======================

Copyright (C) 2009-2010 Boris Okunskiy and The Circumflex Team <http://circumflex.ru>

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY AUTHOR AND CONTRIBUTORS ''AS IS'' AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED.  IN NO EVENT SHALL AUTHOR OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.
 */
package skinny.view.freemarker

import scala.language.existentials
import scala.language.reflectiveCalls

import org.slf4j._
import freemarker.template._
import scala.collection.JavaConverters._

/**
  * Scala object wrapper.
  */
class ScalaObjectWrapper extends ObjectWrapper {

  override def wrap(obj: Any): TemplateModel = obj match {
    case null                            => TemplateModel.NOTHING
    case opt: Option[_]                  => opt.map(obj => wrap(obj)).getOrElse(TemplateModel.NOTHING)
    case model: TemplateModel            => model
    case xml: scala.xml.NodeSeq          => new ScalaXmlWrapper(xml, this)
    case seq: Seq[_]                     => new ScalaSeqWrapper(seq, this)
    case map: scala.collection.Map[_, _] => new ScalaMapWrapper(map.map(p => (p._1.toString, p._2)), this)
    case it: Iterable[_]                 => new ScalaIterableWrapper(it, this)
    case it: Iterator[_]                 => new ScalaIteratorWrapper(it, this)
    case str: String                     => new SimpleScalar(str)
    case date: java.util.Date            => new ScalaJUDateWrapper(date, this)
    case num: Number                     => new SimpleNumber(num)
    case bool: Boolean                   => if (bool) TemplateBooleanModel.TRUE else TemplateBooleanModel.FALSE
    case o                               => new ScalaBaseWrapper(o, this)
  }
}

/**
  * Scala java.util.Date wrapper.
  *
  * @param date date
  * @param wrapper object wrapper
  */
class ScalaJUDateWrapper(val date: java.util.Date, wrapper: ObjectWrapper)
    extends ScalaBaseWrapper(date, wrapper)
    with TemplateDateModel {

  def getDateType = TemplateDateModel.UNKNOWN
  def getAsDate   = date
}

/**
  * Scala Seq wrapper.
  *
  * @param seq seq
  * @param wrapper object wrapper
  * @tparam T seq element type
  */
class ScalaSeqWrapper[T](val seq: Seq[T], wrapper: ObjectWrapper)
    extends ScalaBaseWrapper(seq, wrapper)
    with TemplateSequenceModel {

  def get(index: Int) = wrapper.wrap(seq(index))
  def size            = seq.size
}

/**
  * Scala Map wrapper.
  *
  * @param map Map object
  * @param wrapper object wrapper
  */
class ScalaMapWrapper(val map: collection.Map[String, _], wrapper: ObjectWrapper)
    extends ScalaBaseWrapper(map, wrapper)
    with TemplateHashModelEx {

  override def get(key: String): TemplateModel = wrapper.wrap(map.get(key).orElse(Option(super.get(key))))
  override def isEmpty                         = map.isEmpty

  def values = new ScalaIterableWrapper(map.values, wrapper)
  val keys   = new ScalaIterableWrapper(map.keys, wrapper)
  def size   = map.size
}

/**
  * Scala Iterable wrapper.
  *
  * @param it iterable
  * @param wrapper object wrapper
  * @tparam T iterable element type
  */
class ScalaIterableWrapper[T](val it: Iterable[T], wrapper: ObjectWrapper)
    extends ScalaBaseWrapper(it, wrapper)
    with TemplateCollectionModel {

  def iterator = new ScalaIteratorWrapper(it.iterator, wrapper)
}

/**
  * Scala Iterator wrapper.
  *
  * @param it iterator
  * @param wrapper object wrapper
  * @tparam T iterator element type
  */
class ScalaIteratorWrapper[T](val it: Iterator[T], wrapper: ObjectWrapper)
    extends ScalaBaseWrapper(it, wrapper)
    with TemplateModelIterator
    with TemplateCollectionModel {

  def next     = wrapper.wrap(it.next())
  def hasNext  = it.hasNext
  def iterator = this
}

/**
  * Scala method wrapper.
  *
  * @param target invocation target
  * @param methodName method name
  * @param wrapper object wrapper
  */
class ScalaMethodWrapper(
    val target: Any,
    val methodName: String,
    val wrapper: ObjectWrapper
) extends TemplateMethodModelEx {

  def exec(arguments: java.util.List[_]) = {
    try wrapper.wrap(org.apache.commons.beanutils.MethodUtils.invokeMethod(target, methodName, arguments.toArray))
    catch {
      case scala.util.control.NonFatal(e) =>
        val params = arguments.asScala
          .map { a =>
            a match {
              case v: java.util.List[_]   => v.asScala.asInstanceOf[Seq[_]]
              case v: SimpleCollection    => v.iterator
              case v: SimpleDate          => v.getAsDate
              case v: SimpleHash          => v.toMap
              case v: SimpleNumber        => v.getAsNumber.longValue
              case v: SimpleObjectWrapper => v
              case v: SimpleScalar        => v.getAsString
              case v: SimpleSequence => {
                // NOTE: @deprecated No replacement exists; not a reliable way of getting back the original list elemnts.
                v.toList
              }
              case v => v
            }
          }
          .map(_.asInstanceOf[Object])

        val paramTypes = params.map(_.getClass).map { clazz =>
          clazz match {
            case c if c == classOf[java.util.ArrayList[_]] => classOf[Seq[_]]
            case c if c == classOf[java.util.List[_]]      => classOf[Seq[_]]
            case c if c == classOf[java.lang.Boolean]      => classOf[scala.Boolean]
            case c if c == classOf[java.lang.Byte]         => classOf[scala.Byte]
            case c if c == classOf[java.lang.Character]    => classOf[scala.Char]
            case c if c == classOf[java.lang.Double]       => classOf[scala.Double]
            case c if c == classOf[java.lang.Float]        => classOf[scala.Float]
            case c if c == classOf[java.lang.Integer]      => classOf[scala.Int]
            case c if c == classOf[java.lang.Long]         => classOf[scala.Long]
            case c if c == classOf[java.lang.String]       => classOf[String]
            case c if c == classOf[java.lang.Short]        => classOf[scala.Short]
            case c if c == classOf[java.lang.Object]       => classOf[scala.Any]
            case c                                         => c
          }
        }
        val method = target.getClass.getDeclaredMethod(methodName, paramTypes.toIndexedSeq: _*)
        method.invoke(target, params.toIndexedSeq: _*)
    }
  }
}

/**
  * Scala XML wrapper.
  *
  * @param nodes mxl node seq
  * @param wrapper object wrapper
  */
class ScalaXmlWrapper(val nodes: scala.xml.NodeSeq, val wrapper: ObjectWrapper)
    extends TemplateNodeModel
    with TemplateHashModel
    with TemplateSequenceModel
    with TemplateScalarModel {

  import scala.xml._

  def children: Seq[Node] = nodes match {
    case node: Elem =>
      node.child.flatMap {
        case e: Elem                        => Option(e)
        case a: Attribute                   => Option(a)
        case t: Text if (t.text.trim != "") => Option(t)
        case _                              => None
      }
    case _ => Nil
  }

  def getNodeNamespace: String = nodes match {
    case e: Elem => e.namespace
    case _       => ""
  }

  def getNodeType: String = nodes match {
    case e: Elem      => "element"
    case t: Text      => "text"
    case a: Attribute => "attribute"
    case _            => null
  }

  def getNodeName: String = nodes match {
    case e: Elem => e.label
    case _       => null
  }

  def getChildNodes: TemplateSequenceModel = new ScalaSeqWrapper[Node](children, wrapper)

  // due to immutability of Scala XML API, nodes are unaware of their parents.
  def getParentNode: TemplateNodeModel = new ScalaXmlWrapper(null, wrapper)

  // as hash
  def isEmpty: Boolean = nodes.size == 0

  def get(key: String): TemplateModel = {
    val children = nodes \ key
    if (children.size == 0) wrapper.wrap(None)
    if (children.size == 1) wrapper.wrap(children(0))
    else wrapper.wrap(children)
  }

  // as sequence
  def size: Int = nodes.size

  def get(index: Int): TemplateModel = new ScalaXmlWrapper(nodes(index), wrapper)

  // as scalar
  def getAsString: String = nodes.text
}

/**
  * Scala basic wrapper.
  *
  * @param obj object
  * @param wrapper object wrapper
  */
class ScalaBaseWrapper(val obj: Any, val wrapper: ObjectWrapper) extends TemplateHashModel with TemplateScalarModel {

  private[this] val logger: Logger = LoggerFactory.getLogger(classOf[ScalaBaseWrapper])

  import java.lang.reflect.{ Field, Method, Modifier }

  // TODO
  val resolveFields     = true
  val resolveMethods    = true
  val delegateToDefault = false

  val objectClass = obj.asInstanceOf[Object].getClass

  private def findMethod(cl: Class[_], name: String): Option[Method] =
    cl.getMethods.find { m =>
      m.getName.equals(name) && Modifier.isPublic(m.getModifiers)
    } match {
      case None if cl != classOf[Object] => findMethod(cl.getSuperclass, name)
      case other                         => other
    }

  private def findField(cl: Class[_], name: String): Option[Field] =
    cl.getFields.find { f =>
      f.getName.equals(name) && Modifier.isPublic(f.getModifiers)
    } match {
      case None if cl != classOf[Object] => findField(cl.getSuperclass, name)
      case other                         => other
    }

  def get(key: String): TemplateModel = {
    if (obj.isInstanceOf[Dynamic]) {
      try {
        val selectDynamic = obj.getClass.getDeclaredMethod("selectDynamic", classOf[String])
        if (selectDynamic != null) {
          return wrapper.wrap(selectDynamic.invoke(obj, key))
        }
      } catch {
        case scala.util.control.NonFatal(e) =>
          logger.debug(s"Failed to invoke #selectDynamic because ${e.getMessage}", e)
      }
      try {
        val applyDynamic = obj.getClass.getDeclaredMethod("applyDynamic", classOf[String])
        if (applyDynamic != null) {
          return wrapper.wrap(applyDynamic.invoke(obj, key))
        }
      } catch {
        case scala.util.control.NonFatal(e) =>
          logger.debug(s"Failed to invoke #applyDynamic because ${e.getMessage}", e)
      }
    } else {
      val o = obj.asInstanceOf[Object]
      if (resolveFields) {
        findField(objectClass, key) match {
          case Some(field) => return wrapper.wrap(field.get(o))
          case _           =>
        }
      }
      if (resolveMethods) {
        findMethod(objectClass, key) match {
          case Some(method) if (method.getParameterTypes.length == 0) =>
            return wrapper.wrap(method.invoke(obj))
          case Some(method) =>
            return new ScalaMethodWrapper(obj, method.getName, wrapper)
          case _ =>
        }
      }
    }
    // nothing found
    if (delegateToDefault)
      new DefaultObjectWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS).build().wrap(obj)
    else wrapper.wrap(null)
  }

  def isEmpty = false

  def getAsString = obj.toString

}
