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

package uk.gov.hmrc.selfassessmentapi.resources.models.properties

import org.joda.time.LocalDate
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec
import uk.gov.hmrc.selfassessmentapi.resources.models.{ErrorCode, Income, SimpleExpense}

class PropertiesPeriodSpec extends JsonSpec {

  def propertiesPeriod(from: LocalDate = LocalDate.parse("2017-04-01"),
                       to: LocalDate = LocalDate.parse("2017-04-02"),
                      incomes: Map[IncomeType.IncomeType, Income] = Map(IncomeType.PremiumsOfLeaseGrant -> Income(1000, None)),
                      expenses: Map[ExpenseType.ExpenseType, SimpleExpense] = Map(ExpenseType.PremisesRunningCosts -> SimpleExpense(1000.50))) = {
    PropertiesPeriod(from = from, to = to,
      data = PropertiesPeriodicData(
        incomes = incomes,
        expenses = expenses))
  }

  "PropertiesPeriod" should {
    "round trip" in {
      roundTripJson(propertiesPeriod())
    }
  }

  "validate" should {
    "reject a PropertiesPeriod where the `to` date comes before the `from` date" in {
      val period = propertiesPeriod(from = LocalDate.parse("2017-04-02"), to = LocalDate.parse("2017-04-01"))
      assertValidationErrorWithCode(period, "", ErrorCode.INVALID_PERIOD)
    }
    
    "accept a PropertiesPeriod where the `from` and `to` dates are equal" in {
      val date = LocalDate.parse("2017-04-01")
      val period = propertiesPeriod(from = date, to = date)
      
      assertValidationPasses(period)
    }
  }
}
