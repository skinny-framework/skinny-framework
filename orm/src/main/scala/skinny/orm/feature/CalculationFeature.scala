package skinny.orm.feature

import scalikejdbc._
import skinny.orm.SkinnyMapperBase

/**
 * Calculation feature.
 */
trait CalculationFeature[Entity] extends SkinnyMapperBase[Entity] {

  /**
   * Calculates rows.
   */
  def calculate(sql: SQLSyntax)(implicit s: DBSession = autoSession): BigDecimal = {
    withSQL {
      select(sql).from(as(defaultAlias)).where(defaultScopeWithDefaultAlias)
    }.map(_.bigDecimal(1)).single.apply().map(_.toScalaBigDecimal).getOrElse(BigDecimal(0))
  }

  /**
   * Count only.
   */
  def count(fieldName: Symbol = Symbol(""), distinct: Boolean = false)(
    implicit s: DBSession = autoSession): Long = {
    if (fieldName == Symbol("")) {
      withSQL {
        select(sqls.count).from(as(syntax))
      }.map(_.long(1)).single.apply().getOrElse(0L)
    } else {
      calculate {
        if (distinct) sqls.count(sqls.distinct(defaultAlias.field(fieldName.name)))
        else sqls.count(defaultAlias.field(fieldName.name))
      }.toLong
    }
  }

  /**
   * Counts distinct rows.
   */
  def distinctCount(fieldName: Symbol = Symbol(primaryKeyFieldName))(implicit s: DBSession = autoSession): Long = count(fieldName, true)

  /**
   * Calculates sum of a column.
   */
  def sum(fieldName: Symbol)(implicit s: DBSession = autoSession): BigDecimal = calculate(sqls.sum(defaultAlias.field(fieldName.name)))

  /**
   * Calculates average of a column.
   */
  def average(fieldName: Symbol, decimals: Option[Int] = None)(implicit s: DBSession = autoSession): BigDecimal = {
    calculate(decimals match {
      case Some(dcml) =>
        val decimalsValue = dcml match {
          case 1 => sqls"1"
          case 2 => sqls"2"
          case 3 => sqls"3"
          case 4 => sqls"4"
          case 5 => sqls"5"
          case 6 => sqls"6"
          case 7 => sqls"7"
          case 8 => sqls"8"
          case 9 => sqls"9"
          case _ => sqls"10"
        }
        sqls"round(${sqls.avg(defaultAlias.field(fieldName.name))}, ${decimalsValue})"
      case _ =>
        sqls.avg(defaultAlias.field(fieldName.name))
    })
  }

  def avg(fieldName: Symbol, decimals: Option[Int] = None)(implicit s: DBSession = autoSession): BigDecimal = average(fieldName, decimals)

  /**
   * Calculates minimum value of a column.
   */
  def minimum(fieldName: Symbol)(implicit s: DBSession = autoSession): BigDecimal = calculate(sqls.min(defaultAlias.field(fieldName.name)))

  def min(fieldName: Symbol)(implicit s: DBSession = autoSession): BigDecimal = minimum(fieldName)

  /**
   * Calculates minimum value of a column.
   */
  def maximum(fieldName: Symbol)(implicit s: DBSession = autoSession): BigDecimal = calculate(sqls.max(defaultAlias.field(fieldName.name)))

  def max(fieldName: Symbol)(implicit s: DBSession = autoSession): BigDecimal = maximum(fieldName)

}
