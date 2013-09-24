package skinny

package object orm {

  type Alias[A] = scalikejdbc.SQLInterpolation.SyntaxProvider[A]

}
