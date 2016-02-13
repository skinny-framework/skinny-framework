package controller

import java.io.OutputStreamWriter
import model.Company
import skinny._
import skinny.validator._
import skinny.util.LoanPattern

class SampleApiController extends SkinnyApiController {

  def createForm = validation(
    Params(params),
    paramKey("name") is required & maxLength(60),
    paramKey("url") is maxLength(256)
  )
  val createStrongParams = Seq(
    "name" -> ParamType.String,
    "url" -> ParamType.String
  )

  def createCompany = {
    if (createForm.validate()) {
      val permittedParams = StrongParameters(params).permit(createStrongParams: _*)
      val id = Company.createWithPermittedAttributes(permittedParams)
      status = 201
      response.setHeader("Location", s"/companies/${id.value}")
    } else {
      status = 400
      toPrettyJSONString(createForm.errors)
    }
  }

  def companiesJson = toPrettyJSONString(Company.findAll())

  def responseToOutputStream = {
    LoanPattern.using(new OutputStreamWriter(response.getOutputStream, "MS932")) { writer =>
      writer.write("ABCDEGあいうえお")
    }
  }
}
