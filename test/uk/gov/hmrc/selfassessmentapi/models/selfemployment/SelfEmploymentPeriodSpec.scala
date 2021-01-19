/*
 * Copyright 2021 HM Revenue & Customs
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

import org.joda.time.LocalDate
import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.models.{ErrorCode, Expense, SimpleIncome}
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec

class SelfEmploymentPeriodSpec extends JsonSpec {

  val period: SelfEmploymentPeriod = SelfEmploymentPeriod(id = None,
    from = LocalDate.parse("2017-04-01"),
    to = LocalDate.parse("2017-04-02"),
    incomes = Some(Incomes(turnover = Some(SimpleIncome(0)))),
    expenses = Some(Expenses(staffCosts = Some(Expense(10.25, None)))),
    consolidatedExpenses = None)

  "SelfEmploymentPeriod" should {
    "round trip" when {
      "passed a SelfEmploymentPeriod with incomes" in {
        roundTripJson(period)
      }
      "passed a SelfEmploymentPeriod with only expenses" in {
        roundTripJson(period.copy(incomes = None))
      }
    }

    "return a INVALID_PERIOD error when using a period with a 'from' date before the 'to' date" in {
      assertValidationErrorsWithCode[SelfEmploymentPeriod](
        Json.toJson(period.copy(to = period.from.minusDays(1))),
        Map("" -> Seq(ErrorCode.INVALID_PERIOD))
      )
    }

    "return a NO_INCOMES_AND_EXPENSES error when incomes and expenses are not supplied" in {
      assertValidationErrorsWithCode[SelfEmploymentPeriod](
        Json.toJson(period.copy(incomes = None, expenses = None)),
        Map("" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES)))
    }

    "return a INVALID_PERIOD and NO_INCOMES_AND_EXPENSES errors when the from and to dates are invalid and incomes and expenses are not supplied" in {
      assertValidationErrorsWithCode[SelfEmploymentPeriod](
        Json.toJson(period.copy(to = period.from.minusDays(1), incomes = None, expenses = None)),
        Map("" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES, ErrorCode.INVALID_PERIOD))
      )
    }

    "return an error when provided with an empty json body" in
      assertValidationErrorsWithMessage[SelfEmploymentPeriod](Json.parse("{}"),
                                                              Map("/from" -> Seq("error.path.missing"),
                                                                  "/to" -> Seq("error.path.missing")))

    "pass if the from date is equal to the end date" in {
      val period = SelfEmploymentPeriod(id = None,
                                        from = LocalDate.parse("2017-04-01"),
                                        to = LocalDate.parse("2017-04-01"),
                                        incomes = Some(Incomes(turnover = Some(SimpleIncome(0)))),
                                        expenses = None,
                                        consolidatedExpenses = None)
      assertValidationPasses(period)
    }
  }

}
