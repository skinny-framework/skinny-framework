package skinny.session.jdbc

import skinny.orm._
import scalikejdbc._
import org.joda.time.DateTime
import java.io._
import skinny.logging.LoggerProvider

/**
 * SkinnySession JDBC implmenetation.
 */
case class SkinnySession(
    id: Long,
    createdAt: DateTime,
    expireAt: DateTime,
    servletSessions: Seq[ServletSession] = Nil,
    attributes: Seq[SkinnySessionAttribute] = Nil
) extends LoggerProvider {

  import SkinnySession._

  var consistentMode: Boolean = true

  case class OpsAndValue(ops: LastOperation, value: Any)
  private[this] val toBeStored = new scala.collection.concurrent.TrieMap[String, OpsAndValue]
  private[this] val alreadyStored = new scala.collection.concurrent.TrieMap[String, OpsAndValue]

  def getAttribute(name: String): Object = {
    val foundInToBeStored = toBeStored.find(_._1 == name).map { case (_, ov) => ov }
    val foundInAlreadyStored = alreadyStored.find(_._1 == name).map { case (_, ov) => ov }
    val foundInAttributes = attributes.find(_.name == name).map(attr => attributeToObject(attr.name, attr.value))
    val markedAsDeleted = foundInToBeStored.exists(_.ops == Remove) || foundInAlreadyStored.exists(_.ops == Remove)

    if (markedAsDeleted) null
    else foundInToBeStored.filter(_.ops == Set).map(_.value)
      .orElse(foundInAlreadyStored.filter(_.ops == Set).map(_.value))
      .orElse(foundInAttributes)
      .map(_.asInstanceOf[Object])
      .orNull[Object]
  }

  def setAttribute(name: String, value: Any) = {
    logger.debug("setAttribute: " + name + " -> " + value)
    if (consistentMode) {
      SkinnySession.setAttributeToDatabase(id, name, value)
      alreadyStored.update(name, OpsAndValue(Set, value))
    } else toBeStored.update(name, OpsAndValue(Set, value))
  }

  def removeAttribute(name: String) = {
    logger.debug("removeAttribute: " + name)
    if (consistentMode) {
      SkinnySession.removeAttributeFromDatabase(id, name)
      alreadyStored.update(name, OpsAndValue(Remove, None))
    } else toBeStored.update(name, OpsAndValue(Remove, None))
  }

  def attributeNames: Seq[String] = {
    attributes.map(_.name) ++
      alreadyStored.filter(_._2.ops == Set).map(_._1) ++
      toBeStored.filter(_._2.ops == Set).map(_._1)
  }

  def save(): Unit = {
    toBeStored.foreach {
      case (name, OpsAndValue(Set, value)) => SkinnySession.setAttributeToDatabase(id, name, value)
      case (name, OpsAndValue(Remove, value)) => SkinnySession.removeAttributeFromDatabase(id, name)
    }
  }

  private[this] def attributeToObject(k: String, v: Any): Any = v match {
    case null => null
    case None => null
    case some if some.isInstanceOf[Some[_]] =>
      attributeToObject(k, some.asInstanceOf[Some[_]].orNull[Any](null))
    case bytes: Array[Byte] =>
      try {
        using(new ByteArrayInputStream(bytes)) { b =>
          using(new ObjectInputStream(b)) { is =>
            attributeToObject(k, is.readObject)
          }
        }
      } catch {
        case e: StreamCorruptedException =>
          logger.info(s"Failed to load an attribute for $k because ${e.getMessage}")
          removeAttribute(k)
          null
        case e: InvalidClassException =>
          logger.info(s"Failed to load a serializable attribute for $k because ${e.getMessage}")
          removeAttribute(k)
          null
      }
    case v => v
  }

}

/**
 * SkinnySession JDBC implmenetation.
 */
object SkinnySession extends SkinnyCRUDMapper[SkinnySession] with LoggerProvider {

  sealed trait LastOperation
  case object Set extends LastOperation
  case object Remove extends LastOperation

  override def tableName = "skinny_sessions"
  override def defaultAlias = createAlias("ss")
  override def extract(rs: WrappedResultSet, n: ResultName[SkinnySession]) = new SkinnySession(
    id = rs.get(n.id),
    createdAt = rs.get(n.createdAt),
    expireAt = rs.get(n.expireAt)
  )

  val servletSessionsAlias = ServletSession.createAlias("svs")
  val servletSessionsRef = {
    hasMany[ServletSession](
      many = ServletSession -> servletSessionsAlias,
      on = (ss, svs) => sqls.eq(ss.id, svs.skinnySessionId),
      merge = (ss, svs) => ss.copy(servletSessions = svs)
    )
  }

  val attributesRef = {
    val attrs = SkinnySessionAttribute.createAlias("attrs")
    hasMany[SkinnySessionAttribute](
      many = SkinnySessionAttribute -> attrs,
      on = (s, a) => sqls.eq(s.id, a.skinnySessionId),
      merge = (s, as) => s.copy(attributes = as)
    ).byDefault
  }

  /**
   * Returns expireAt value to be set for the ServletSession's maxInactiveInterval.
   */
  def getExpireAtFromMaxInactiveInterval(maxInactiveInterval: Int): DateTime = {
    if (maxInactiveInterval <= 0) DateTime.now.plusMonths(3) // 3 months alive is long enough
    else DateTime.now.plusSeconds(maxInactiveInterval)
  }

  /**
   * Finds a SkinnySession which specified JSESSIONID is attached from database.
   * If absent, create new record and attaches this JSESSIONID to the SkinnySession.
   */
  def findOrCreate(jsessionId: String, jsessionidToBeAttached: Option[String], expireAt: DateTime)(implicit s: DBSession = autoSession): SkinnySession = {
    findActiveByJsessionId(jsessionId).map { session =>
      jsessionidToBeAttached.foreach(jid => attachJsessionIdToSkinnySession(jid, session))
      ServletSession.narrowDownAttachedServletSessions(session, 10)
      postponeSkinnySessionTimeout(session, expireAt)
      session
    }.getOrElse {
      joins(attributesRef).findById(createSkinnySessionAndReturnId(expireAt)).map { session =>
        attachJsessionIdToSkinnySession(jsessionId, session)
        session
      }.get
    }
  }

  /**
   * Saves an attribute to the database.
   */
  def setAttributeToDatabase(id: Long, name: String, value: Any)(implicit s: DBSession = autoSession): Unit = {
    if (name != null) {
      val c = SkinnySessionAttribute.column
      val namedValues = Seq(
        c.skinnySessionId -> id,
        c.name -> name,
        c.value -> AsIsParameterBinder(toSerializable(value))
      )
      // easy-upsert
      try {
        val updated = withSQL {
          update(SkinnySessionAttribute).set(namedValues: _*)
            .where
            // compilation succeeded in Scala 2.10.0 but higher version on Java8 doesn't accept.
            // "applyDynamic does not support passing a vararg parameter"
            .eq(c.field("skinnySessionId"), id).and.eq(c.field("name"), name)
        }.update.apply()
        if (updated == 0) {
          insert.into(SkinnySessionAttribute).namedValues(namedValues: _*).toSQL.update.apply()
        }
      } catch {
        case scala.util.control.NonFatal(e) =>
          try update(SkinnySessionAttribute).set(namedValues: _*)
            .where
            // compilation succeeded in Scala 2.10.0 but higher version on Java8 doesn't accept.
            // "applyDynamic does not support passing a vararg parameter"
            .eq(c.field("skinnySessionId"), id).and.eq(c.field("name"), name)
            .toSQL.update.apply()
          catch {
            case scala.util.control.NonFatal(_) =>
              logger.info(s"Failed to set attribute ($name -> $value) for id: ${id}")
          }
      }
    }
  }

  /**
   * Removes an attribute from the database.
   */
  def removeAttributeFromDatabase(id: Long, name: String)(implicit s: DBSession = autoSession): Unit = {
    val c = SkinnySessionAttribute.column
    delete.from(SkinnySessionAttribute).where.eq(c.skinnySessionId, id).and.eq(c.name, name)
      .toSQL.update.apply()
  }

  /**
   * Invalidates this SkinnySession and all the JSESSIONID are detached too.
   */
  def invalidate(jsessionId: String)(implicit s: DBSession = autoSession): Unit = {
    val sv = servletSessionsAlias
    val idOpt = select(sv.skinnySessionId).from(ServletSession as sv)
      .where.eq(sv.jsessionId, jsessionId).toSQL.map(_.long(1)).single.apply()
    idOpt.foreach { id =>
      ServletSession.deleteBySkinnySessionId(id)
      SkinnySessionAttribute.deleteBySkinnySessionId(id)
      deleteById(id)
    }
  }

  private[this] def createSkinnySessionAndReturnId(expireAt: DateTime): Long = {
    createWithNamedValues(
      column.createdAt -> DateTime.now,
      column.expireAt -> expireAt
    )
  }
  private[this] def findActiveByJsessionId(jsessionId: String): Option[SkinnySession] = {
    joins(attributesRef, servletSessionsRef).findBy(
      sqls.eq(servletSessionsAlias.jsessionId, jsessionId).and.gt(defaultAlias.expireAt, DateTime.now)
    )
  }
  private[this] def attachJsessionIdToSkinnySession(jsessionId: String, session: SkinnySession): Unit = {
    ServletSession.findByJsessionId(jsessionId).map { servletSession =>
      servletSession.attachTo(session)
    }.getOrElse {
      try ServletSession.create(jsessionId, session)
      catch { case scala.util.control.NonFatal(e) => logger.info(s"Failed to create a ServletSession because ${e.getMessage}") }
    }
  }
  private[this] def postponeSkinnySessionTimeout(session: SkinnySession, expireAt: DateTime): Unit = {
    updateById(session.id).withNamedValues(column.expireAt -> expireAt)
  }

  private[this] def toSerializable(v: Any): Any = v match {
    case null => null
    case None => null
    case Some(v) => toSerializable(v)
    case v => {
      using(new ByteArrayOutputStream) { bytes =>
        using(new ObjectOutputStream(bytes)) { out =>
          out.writeObject(v)
          bytes.toByteArray
        }
      }
    }
  }

}
