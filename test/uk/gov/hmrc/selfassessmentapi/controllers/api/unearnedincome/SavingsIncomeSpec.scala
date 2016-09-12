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

package uk.gov.hmrc.selfassessmentapi.controllers.api.unearnedincome

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode
import ErrorCode._
import uk.gov.hmrc.selfassessmentapi.controllers.api.JsonSpec
import uk.gov.hmrc.selfassessmentapi.controllers.api.unearnedincome.SavingsIncomeType._

class SavingsIncomeSpec extends JsonSpec {

  "format" should {

    "round trip valid SavingsIncome json" in {
      roundTripJson(SavingsIncome(`type` = InterestFromBanksTaxed, amount = BigDecimal(1000.99)))
    }
  }

  "validate" should {
    "reject amounts with more than 2 decimal values" in {
      Seq(BigDecimal(1000.123), BigDecimal(1000.1234), BigDecimal(1000.12345), BigDecimal(1000.123456789)).foreach { testAmount =>
        assertValidationError[SavingsIncome](
          SavingsIncome(`type` = InterestFromBanksTaxed, amount = testAmount),
          Map("/amount" -> INVALID_MONETARY_AMOUNT), "Expected invalid savings-income with more than 2 decimal places")
      }
    }

    "reject invalid SavingsIncome type" in {
      val json = Json.parse(
        """
          |{ "type": "FOO",
          |"amount" : 10000.45
          |}
        """.stripMargin)

      assertValidationError[SavingsIncome](
        json, Map("/type" -> NO_VALUE_FOUND), "should fail with invalid type")
    }

    "reject negative amount" in {
      val seIncome = SavingsIncome(`type` = InterestFromBanksTaxed, amount = BigDecimal(-1000.12))
      assertValidationError[SavingsIncome](
        seIncome, Map("/amount" -> INVALID_MONETARY_AMOUNT), "should fail with INVALID_MONETARY_AMOUNT error")
    }
  }
}
