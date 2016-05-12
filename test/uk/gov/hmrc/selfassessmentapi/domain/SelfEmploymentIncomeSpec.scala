package uk.gov.hmrc.selfassessmentapi.domain

import uk.gov.hmrc.selfassessmentapi.domain.SelfEmploymentIncomeType._

class SelfEmploymentIncomeSpec extends JsonSpec {

  "format" should {
    "round trip SelfEmploymentIncome json when id present" in {
      roundTripJson(SelfEmploymentIncome(id = Some("id"), taxYear = "2016-17", incomeType = TURNOVER, amount = BigDecimal(1000.99)))
    }

    "round trip SelfEmployment json with no id" in {
      roundTripJson(SelfEmploymentIncome(id = None, taxYear = "2016-17", incomeType = TURNOVER, amount = BigDecimal(1000.99)))
    }
  }

  "validate" should {
    "reject amounts with more than 2 decimal values" in {
      Seq(BigDecimal(1000.123), BigDecimal(1000.1234), BigDecimal(1000.12345), BigDecimal(1000.123456789)).foreach { testAmount =>
        val seIncome = SelfEmploymentIncome(taxYear = "2016-17", incomeType = TURNOVER, amount = testAmount)
        assertValidationError[SelfEmploymentIncome](
          seIncome,
          Map(ErrorCode("AMOUNT_DECIMAL_LENGTH_EXCEEDED") -> "amount cannot have more than 2 decimal values"),
          "Expected invalid self-employment-income")
      }
    }

    "reject taxYear with invalid formats" in {
      Seq("2016-2017", "2016/17", "2016/2017", "2016 17", "20162017").foreach { testTaxYear =>
        val seIncome = SelfEmploymentIncome(taxYear = testTaxYear, incomeType = TURNOVER, amount = BigDecimal(1000.99))
        assertValidationError[SelfEmploymentIncome](
          seIncome,
          Map(ErrorCode("TAX_YEAR_INVALID") -> "tax year format is YYYY-YY (2016-17)"),
          "Expected invalid self-employment-income")
      }
    }
  }

}
