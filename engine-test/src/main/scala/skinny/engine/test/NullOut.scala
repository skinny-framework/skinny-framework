package skinny.engine.test

import java.io.OutputStream

object NullOut extends OutputStream {

  def write(b: Int) {}

}
