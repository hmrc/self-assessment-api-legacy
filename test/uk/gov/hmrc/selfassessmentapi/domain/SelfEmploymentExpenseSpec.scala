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

import SelfEmploymentExpenseType._
import play.api.libs.json.Json
import ErrorCode._
class SelfEmploymentExpenseSpec extends JsonSpec {

  "format" should {
    "round trip SelfEmploymentExpense json" in {
      SelfEmploymentExpenseType.values.foreach {
        cat => roundTripJson(SelfEmploymentExpense(`type` = cat, amount = BigDecimal(1000.99)))
      }
    }
  }

  "validate" should {
    "reject amounts with more than 2 decimal values" in {
      Seq(BigDecimal(1000.123), BigDecimal(1000.1234), BigDecimal(1000.12345), BigDecimal(1000.123456789)).foreach { testAmount =>
        val seExpense = SelfEmploymentExpense(`type` = CISPayments, amount = testAmount)
        assertValidationError[SelfEmploymentExpense](
          seExpense,
          Map(("/amount", INVALID_MONETARY_AMOUNT) -> "amount should be non-negative number up to 2 decimal values"),
          "Expected invalid self-employment-income")
      }
    }

    "reject negative monetary amounts" in {
      Seq(BigDecimal(-1000.12), BigDecimal(-10.12)).foreach { testAmount =>
        val seExpense = SelfEmploymentExpense(`type` = CISPayments, amount = testAmount)
        assertValidationError[SelfEmploymentExpense](
          seExpense,
          Map(("/amount", INVALID_MONETARY_AMOUNT) -> "amount should be non-negative number up to 2 decimal values"),
          "Expected invalid self-employment-income")
      }
    }

    "reject negative amount" in {
      val seExpense = SelfEmploymentExpense(`type` = CISPayments, amount = BigDecimal(-1000.12))
      assertValidationError[SelfEmploymentExpense](
        seExpense,
        Map(("/amount", INVALID_MONETARY_AMOUNT) -> "amount should be non-negative number up to 2 decimal values"),
        "Expected negative self-employment expense")
    }

    "reject invalid Expense category" in {
      val json = Json.parse(
        """
          |{ "type": "BAZ",
          |"amount" : 10000.45
          |}
        """.
          stripMargin)

      assertValidationError[SelfEmploymentExpense](
        json,
        Map(("/type", NO_VALUE_FOUND) -> "Self Employment Expense type is invalid"),
        "Expected expense type not in { CoGBought, CISPayments, StaffCosts, TravelCosts, PremisesRunningCosts, MaintenanceCosts, AdminCosts,  AdvertisingCosts, Internet, FinancialCharges, BadDept, ProfessionalFees, Deprecation, Other }")
    }
  }
}
