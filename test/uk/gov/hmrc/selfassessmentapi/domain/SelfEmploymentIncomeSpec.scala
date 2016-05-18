/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.selfassessmentapi.domain

import uk.gov.hmrc.selfassessmentapi.domain.SelfEmploymentIncomeType._
import play.api.libs.json.Json

class SelfEmploymentIncomeSpec extends JsonSpec {

  "format" should {

    "round trip valid SelfEmploymentIncome json" in {
      roundTripJson(SelfEmploymentIncome(`type` = Turnover, amount = BigDecimal(1000.99)))
    }
  }

  "validate" should {
    "reject amounts with more than 2 decimal values" in {
      Seq(BigDecimal(1000.123), BigDecimal(1000.1234), BigDecimal(1000.12345), BigDecimal(1000.123456789)).foreach { testAmount =>
        val seIncome = SelfEmploymentIncome(`type` = Turnover, amount = testAmount)
        assertValidationError[SelfEmploymentIncome](
          seIncome,
          Map(ErrorCode("INVALID_MONETARY_AMOUNT") -> "amount should be non-negative number up to 2 decimal values"),
          "Expected invalid self-employment-income with more than 2 decimal places")
      }
    }

    "reject invalid Income type" in {
      val json = Json.parse(
        """
          |{ "type": "FOO",
          |"amount" : 10000.45
          |}
        """.stripMargin)

      assertValidationError[SelfEmploymentIncome](
        json,
        Map(ErrorCode("NO_VALUE_FOUND") -> "Self Employment Income type is invalid"),
        "Expected income type not in { TURNOVER, OTHER }")
    }

    "reject negative amount" in {
      val seIncome = SelfEmploymentIncome(`type` = Turnover, amount = BigDecimal(-1000.12))
      assertValidationError[SelfEmploymentIncome](
        seIncome,
        Map(ErrorCode("INVALID_MONETARY_AMOUNT") -> "amount should be non-negative number up to 2 decimal values"),
        "should fail with INVALID_MONETARY_AMOUNT error")
    }
  }
}
