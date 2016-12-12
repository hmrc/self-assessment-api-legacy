package uk.gov.hmrc.selfassessmentapi.resources.models

import uk.gov.hmrc.selfassessmentapi.resources.models.properties.Properties
import uk.gov.hmrc.selfassessmentapi.resources.{JsonSpec, Jsons}

class AccountingTypeSpec extends JsonSpec {
  "Properties" should {
    "round trip" in {
      val properties = Properties(AccountingType.CASH)
      roundTripJson(properties)
    }
  }

  "validate" should {

    "return INVALID_VALUE when provided with an invalid accounting type" in {
      val json = Jsons.Properties(accountingType = "INVALID")

      assertValidationErrorsWithCode[Properties](json, Map("/accountingType" -> ErrorCode.INVALID_VALUE))
    }

    "pass when provided all valid accounting types" in {
      Seq(Jsons.Properties(accountingType = "CASH"), Jsons.Properties(accountingType = "ACCRUAL"))
        .foreach(assertValidationPasses(_))
    }
  }
}
