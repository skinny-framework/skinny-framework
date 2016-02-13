/*
 * Copyright 2011-2012 M3, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package skinny.http

import skinny.util.LoanPattern.using
import java.io.{ ByteArrayOutputStream, FileInputStream }

object FormData {

  def apply(name: String, bytes: Array[Byte]): FormData = {
    new FormData(name).bytes(bytes)
  }
  def apply(name: String, input: TextInput): FormData = {
    new FormData(name).text(input.textBody, input.charset)
  }
  def apply(name: String, input: FileInput): FormData = {
    new FormData(name).file(input.file).contentType(input.contentType)
  }

}

/**
 * Form data
 * @param name name
 * @param bytes body as a byte array
 * @param textInput body from a text value
 * @param fileInput body from a file
 */
case class FormData(
    var name: String,
    var bytes: Option[Array[Byte]] = None,
    var textInput: TextInput = NoTextInput,
    var fileInput: FileInput = NoFileInput
) {

  private[this] def returningThis(f: => Any): FormData = {
    f
    this
  }

  def name(name: String): FormData = returningThis(this.name = name)

  private[this] def toBytes(textInput: TextInput): Option[Array[Byte]] = {
    if (textInput != NoTextInput) {
      Some(textInput.textBody.getBytes(textInput.charset))
    } else None
  }

  private[this] def toBytes(fileInput: FileInput): Option[Array[Byte]] = {
    if (fileInput != NoFileInput) {
      using(new FileInputStream(fileInput.file)) { input =>
        using(new ByteArrayOutputStream) { out =>
          var c: Int = 0
          while ({ c = input.read(); c } != -1) { out.write(c) }
          Some(out.toByteArray)
        }
      }
    } else None
  }

  def asBytes: Array[Byte] = {
    bytes.orElse(toBytes(textInput)).orElse(toBytes(fileInput)).getOrElse(Array())
  }

  def bytes(body: Array[Byte]): FormData = returningThis(bytes = Some(body))

  def fileInput(input: FileInput): FormData = returningThis(fileInput = input)
  def file(file: java.io.File): FormData = returningThis {
    fileInput = FileInput(file, fileInput.contentType)
  }
  def filename: Option[String] = Option(fileInput.file).map(_.getName)
  def contentType: Option[String] = Option(fileInput.contentType)
  def contentType(contentType: String): FormData = returningThis {
    fileInput = FileInput(fileInput.file, contentType)
  }

  def textInput(input: TextInput): FormData = returningThis(textInput = input)
  def text(textBody: String, charset: String = HTTP.DEFAULT_CHARSET): FormData = returningThis {
    textInput = TextInput(textBody, charset)
  }
  def charset(charset: String): FormData = returningThis {
    textInput = TextInput(textInput.textBody, charset)
  }

}
