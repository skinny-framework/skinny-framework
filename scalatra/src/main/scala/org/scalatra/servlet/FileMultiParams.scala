package org.scalatra.servlet

class FileMultiParams(
  wrapped: Map[String, Seq[FileItem]] = Map.empty)
    extends Map[String, Seq[FileItem]] {

  def get(key: String): Option[Seq[FileItem]] = {
    (wrapped.get(key) orElse wrapped.get(key + "[]"))
  }

  def get(key: Symbol): Option[Seq[FileItem]] = get(key.name)

  def +[B1 >: Seq[FileItem]](kv: (String, B1)): FileMultiParams =
    new FileMultiParams(wrapped + kv.asInstanceOf[(String, Seq[FileItem])])

  def -(key: String): FileMultiParams = new FileMultiParams(wrapped - key)

  def iterator: Iterator[(String, Seq[FileItem])] = wrapped.iterator

  override def default(a: String): Seq[FileItem] = wrapped.default(a)
}

object FileMultiParams {

  def apply(): FileMultiParams = new FileMultiParams

  def apply[SeqType <: Seq[FileItem]](wrapped: Map[String, Seq[FileItem]]): FileMultiParams = {
    new FileMultiParams(wrapped)
  }

}
