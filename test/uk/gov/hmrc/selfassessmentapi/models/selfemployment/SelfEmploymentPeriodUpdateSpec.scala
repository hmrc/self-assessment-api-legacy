/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models.selfemployment

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.models.{ErrorCode, Expense, SimpleIncome}
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec

class SelfEmploymentPeriodUpdateSpec extends JsonSpec {

  val periodUpdate: SelfEmploymentPeriodUpdate = SelfEmploymentPeriodUpdate(
    Some(Incomes(Some(SimpleIncome(0)))),
    None,
    None
  )

  "SelfEmploymentPeriodUpdate" should {
    "round trip" in roundTripJson(periodUpdate)

    "return a NO_INCOMES_AND_EXPENSES error when incomes and expenses are not supplied" in {
      assertValidationErrorsWithCode[SelfEmploymentPeriodUpdate](
        Json.toJson(periodUpdate.copy(incomes = None)),
        Map("" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES))
      )
    }

    "return a BOTH_EXPENSES_SUPPLIED error when both consolidatedExpenses and expenses are supplied" in {
      assertValidationErrorsWithCode[SelfEmploymentPeriodUpdate](
        Json.toJson(periodUpdate.copy(
          expenses = Some(Expenses(staffCosts = Some(Expense(10.25, None)))),
          consolidatedExpenses = Some(10.25)
        )),
        Map("" -> Seq(ErrorCode.BOTH_EXPENSES_SUPPLIED))
      )
    }

    "return an INVALID_MONETARY_AMOUNT error" when {
      "consolidatedExpenses has a negative value" in {
        assertValidationErrorsWithCode[SelfEmploymentPeriodUpdate](
          Json.toJson(periodUpdate.copy(consolidatedExpenses = Some(-10.25))),
          Map("/consolidatedExpenses" -> Seq(ErrorCode.INVALID_MONETARY_AMOUNT))
        )
      }
      "consolidatedExpenses has greater than 3dp" in {
        assertValidationErrorsWithCode[SelfEmploymentPeriodUpdate](
          Json.toJson(periodUpdate.copy(consolidatedExpenses = Some(10.254))),
          Map("/consolidatedExpenses" -> Seq(ErrorCode.INVALID_MONETARY_AMOUNT))
        )
      }
      "consolidatedExpenses is larger than the max amount allowed" in {
        assertValidationErrorsWithCode[SelfEmploymentPeriodUpdate](
          Json.toJson(periodUpdate.copy(consolidatedExpenses = Some(BigDecimal("1000000000000000000")))),
          Map("/consolidatedExpenses" -> Seq(ErrorCode.INVALID_MONETARY_AMOUNT))
        )
      }
    }

  }
}