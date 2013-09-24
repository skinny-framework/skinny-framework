package skinny.validator

trait Error {

  def name: String
  def messageParams: Seq[Any] = Nil

  override def toString(): String = {
    "Error(name = " + name + ", messageParams = " + messageParams + ")"
  }
}

