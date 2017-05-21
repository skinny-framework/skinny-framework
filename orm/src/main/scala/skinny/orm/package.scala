package skinny

/**
  * Skinny provides you Skinny-ORM as the default O/R mapper, which is built with ScalikeJDBC.
  */
package object orm {

  type Alias[A] = scalikejdbc.SyntaxProvider[A]

}
