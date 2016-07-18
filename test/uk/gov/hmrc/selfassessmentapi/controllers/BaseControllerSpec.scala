package uk.gov.hmrc.selfassessmentapi.controllers

import org.scalatest.{Matchers, WordSpecLike}
import play.api.data.validation.ValidationError
import play.api.libs.json.JsPath
import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode

class BaseControllerSpec extends WordSpecLike with Matchers {

  val controller = new BaseController {
    override val context = ""
  }

  "invalid parts" should {
    "transform validation errors into matching sequence of invalid parts" in {

      val errors = Seq((JsPath \ "commencementDate", List(ValidationError("error.expected.jodadate.format", Seq()))))
      val request = controller.invalidRequest(errors)

      request.errors.head shouldBe InvalidPart(ErrorCode.INVALID_FIELD, "error.expected.jodadate.format", "/commencementDate")
    }
  }

}
