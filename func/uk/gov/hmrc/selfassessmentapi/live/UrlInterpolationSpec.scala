package uk.gov.hmrc.selfassessmentapi.live

import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.support.UrlInterpolation

import scala.collection.mutable

class UrlInterpolationSpec extends UnitSpec {

  "source and summary Ids" should {
    "be inserted in the URL" in new UrlInterpolation {
      val urlPathVariableValues = mutable.Map("sourceId" -> "577bceeb1600001600e94259", "summaryId" -> "577bceec1600004a00e9425b")
      private val interpolatedUrl = interpolated("/self-assessment/9000038562/2016-17/self-employments/%sourceId%/incomes/%summaryId%")(urlPathVariableValues)
      interpolatedUrl shouldBe "/self-assessment/9000038562/2016-17/self-employments/577bceeb1600001600e94259/incomes/577bceec1600004a00e9425b"
    }
  }
}
