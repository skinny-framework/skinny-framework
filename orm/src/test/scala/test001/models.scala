package test001

import skinny.orm._
import scalikejdbc._, SQLInterpolation._

/* with defaultAliases
SELECT t1_default.id   AS i_on_t1_default,
       t1_default.name AS n_on_t1_default,
       t1t2.test1_id   AS ti1_on_t1t2,
       t1t2.test2_id   AS ti2_on_t1t2,
       t2_default.id   AS i_on_t2_default,
       t2_default.name AS n_on_t2_default,
       t1_default.id   AS i_on_t1_default,
       t1_default.name AS n_on_t1_default
FROM   test1 t1_default
       LEFT JOIN test1_test2 t1t2
              ON t1_default.id = t1t2.test1_id
       LEFT JOIN test2 t2_default
              ON t1t2.test2_id = t2_default.id
       LEFT JOIN test1 t1_default
              ON t1t2.test1_id = t1_default.id
WHERE  t1_default.name = ?
 */

/* using hasManyThrough.byDefault each other (impossible)

SELECT t1_default.id       AS i_on_t1_default,
       t1_default.name     AS n_on_t1_default,
       t1t2_in_t1.test1_id AS ti1_on_t1t2_in_t1,
       t1t2_in_t1.test2_id AS ti2_on_t1t2_in_t1,
       t2_in_t1.id         AS i_on_t2_in_t1,
       t2_in_t1.name       AS n_on_t2_in_t1,
       t1t2_in_t2.test1_id AS ti1_on_t1t2_in_t2,
       t1t2_in_t2.test2_id AS ti2_on_t1t2_in_t2,
       t1_in_t2.id         AS i_on_t1_in_t2,
       t1_in_t2.name       AS n_on_t1_in_t2
FROM   test1 t1_default
       LEFT JOIN test1_test2 t1t2_in_t1
              ON t1_default.id = t1t2_in_t1.test1_id
       LEFT JOIN test2 t2_in_t1
              ON t1t2_in_t1.test2_id = t2_in_t1.id
       LEFT JOIN test1_test2 t1t2_in_t2
              ON t2_default.id = t1t2_in_t2.test2_id
       LEFT JOIN test1 t1_in_t2
              ON t1t2_in_t2.test1_id = t1_in_t2.id
WHERE  t1_default.name = ?
 */

case class Test1(id: Long, name: String, test2: Seq[Test2] = Nil)
object Test1 extends SkinnyCRUDMapper[Test1] {
  override def connectionPoolName = 'test001
  override def defaultAlias = createAlias("t1_default")
  override def extract(rs: WrappedResultSet, n: ResultName[Test1]) = new Test1(id = rs.get(n.id), name = rs.get(n.name))

  //hasManyThrough[Test2](Test1Test2, Test2, (t1, t2) => t1.copy(test2 = t2)).byDefault
  val test2Ref = hasManyThrough[Test1Test2, Test2](
    through = Test1Test2 -> Test1Test2.createAlias("t1t2_in_t1"),
    throughOn = (t1: Alias[Test1], t1t2: Alias[Test1Test2]) => sqls.eq(t1.id, t1t2.test1Id),
    many = Test2 -> Test2.createAlias("t2_in_t1"),
    on = (t1t2: Alias[Test1Test2], t2: Alias[Test2]) => sqls.eq(t1t2.test2Id, t2.id),
    merge = (t1, t2) => t1.copy(test2 = t2)
  ).byDefault
}

case class Test2(id: Long, name: String, test1: Seq[Test1] = Nil)
object Test2 extends SkinnyCRUDMapper[Test2] {
  override def connectionPoolName = 'test001
  override def defaultAlias = createAlias("t2_default")
  override def extract(rs: WrappedResultSet, n: ResultName[Test2]) = new Test2(id = rs.get(n.id), name = rs.get(n.name))

  //hasManyThrough[Test1](Test1Test2, Test1, (t2, t1) => t2.copy(test1 = t1)).byDefault
  val test1Ref = hasManyThrough[Test1Test2, Test1](
    through = Test1Test2 -> Test1Test2.createAlias("t1t2_in_t2"),
    throughOn = (t2: Alias[Test2], t1t2: Alias[Test1Test2]) => sqls.eq(t2.id, t1t2.test2Id),
    many = Test1 -> Test1.createAlias("t1_in_t2"),
    on = (t1t2: Alias[Test1Test2], t1: Alias[Test1]) => sqls.eq(t1t2.test1Id, t1.id),
    merge = (t2, t1) => t2.copy(test1 = t1)
  ) //.byDefault
  // NOTICE: using byDefault each other (for hasManyThrough) is impossible!

}

case class Test1Test2(test1Id: Long, test2Id: Long)
object Test1Test2 extends SkinnyJoinTable[Test1Test2] {
  override def connectionPoolName = 'test001
  override def defaultAlias = createAlias("t1t2")
}

