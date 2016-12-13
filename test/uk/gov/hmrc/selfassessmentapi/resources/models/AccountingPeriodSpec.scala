package uk.gov.hmrc.selfassessmentapi.resources.models

import org.joda.time.LocalDate
import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec

class AccountingPeriodSpec extends JsonSpec {
  "AccountingPeriod" should {
    "round trip" in {
      val accPeriod = AccountingPeriod(LocalDate.parse("2017-04-01"), LocalDate.parse("2017-04-02"))
      roundTripJson(accPeriod)
    }
  }

  "validate" should {
    "reject an accounting period if the `start` date is before 2017-04-01" in {
      val accPeriod = AccountingPeriod(LocalDate.parse("2017-03-31"), LocalDate.parse("2017-04-02"))
      assertValidationErrorWithCode(accPeriod, "/start", ErrorCode.DATE_NOT_IN_THE_PAST)
    }

    "reject an accounting period if the `end` date comes before the `start` date" in {
      val accPeriod = AccountingPeriod(LocalDate.parse("2017-04-02"), LocalDate.parse("2017-04-01"))
      assertValidationErrorWithCode(accPeriod, "", ErrorCode.INVALID_ACCOUNTING_PERIOD)
    }

    "reject an accounting period if any fields are missing" in {
      assertValidationErrorsWithMessage[AccountingPeriod](Json.obj(),
        Map(
          "/start" -> "error.path.missing",
          "/end" -> "error.path.missing"))
    }

    "accept an accounting period if the `start` date comes before the `end` date and the `start` date is after 2017-04-01" in {
      val accPeriod = AccountingPeriod(LocalDate.parse("2017-04-01"), LocalDate.parse("2018-03-31"))
      assertValidationPasses(accPeriod)
    }
  }
}
