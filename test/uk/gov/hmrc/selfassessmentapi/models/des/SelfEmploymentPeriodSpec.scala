/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models.des

import org.joda.time.LocalDate
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.models
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.{ExpenseType, IncomeType}

class SelfEmploymentPeriodSpec extends JsonSpec with MapperSpec {
  "from" should {

    val apiPeriod = models.selfemployment.SelfEmploymentPeriod(
      id = Some("abc"),
      from = LocalDate.parse("2017-04-06"),
      to = LocalDate.parse("2018-04-05"),
      data = models.selfemployment.SelfEmploymentPeriodicData(
        incomes =
          Map(IncomeType.Turnover -> Income(10.10, Some(10.10)), IncomeType.Other -> Income(10.10, Some(10.10))),
        expenses = Map(ExpenseType.CISPaymentsToSubcontractors -> Expense(10.10, Some(10.10)),
                       ExpenseType.Depreciation -> Expense(10.10, Some(10.10)),
                       ExpenseType.CostOfGoodsBought -> Expense(10.10, Some(10.10)),
                       ExpenseType.ProfessionalFees -> Expense(10.10, Some(10.10)),
                       ExpenseType.BadDebt -> Expense(10.10, Some(10.10)),
                       ExpenseType.AdminCosts -> Expense(10.10, Some(10.10)),
                       ExpenseType.AdvertisingCosts -> Expense(10.10, Some(10.10)),
                       ExpenseType.FinancialCharges -> Expense(10.10, Some(10.10)),
                       ExpenseType.Interest -> Expense(10.10, Some(10.10)),
                       ExpenseType.MaintenanceCosts -> Expense(10.10, Some(10.10)),
                       ExpenseType.PremisesRunningCosts -> Expense(10.10, Some(10.10)),
                       ExpenseType.StaffCosts -> Expense(10.10, Some(10.10)),
                       ExpenseType.TravelCosts -> Expense(10.10, Some(10.10)),
                       ExpenseType.Other -> Expense(10.10, Some(10.10)))))

    val desPeriod = Mapper[models.selfemployment.SelfEmploymentPeriod, des.SelfEmploymentPeriod].from(apiPeriod)

    "round trip" in {
      roundTrip(apiPeriod, desPeriod)
    }
  }
}
