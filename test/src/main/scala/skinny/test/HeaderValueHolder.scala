package skinny.test

import scala.collection.JavaConverters._
import java.util._

object HeaderValueHolder {

  /**
    * Find a HeaderValueHolder by name, ignoring casing.
    */
  def getByName(headers: Map[String, HeaderValueHolder], name: String): Option[HeaderValueHolder] = {
    val found: Option[String] = headers.keySet.asScala.find { headerName =>
      headerName.equalsIgnoreCase(name)
    }
    found.map { n =>
      headers.get(n)
    }
  }

  def apply(values: Any*) = {
    val h = new HeaderValueHolder
    h.values.addAll(values.toSeq.asJava)
    h
  }

}

class HeaderValueHolder {

  val values = new LinkedList[Any]()

  def setValue(value: Any): Unit = {
    values.clear()
    values.add(value)
  }
  def addValue(value: Any): Unit               = values.add(value)
  def addValues(values: Collection[Any]): Unit = values.addAll(values)

  def getValues(): List[Any]   = Collections.unmodifiableList(values)
  def getValue(): Any          = if (!values.isEmpty) values.get(0) else null
  def getStringValue(): String = Option(getValue).map(_.toString).orNull[String]

  def getStringValues(): List[String] = {
    val stringList = new ArrayList[String](values.size())
    values.asScala.foreach { value =>
      stringList.add(value.toString)
    }
    Collections.unmodifiableList(stringList)
  }

}
