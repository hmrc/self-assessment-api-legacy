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

import play.api.libs.json.{Json, Reads, Writes}
import uk.gov.hmrc.selfassessmentapi.models
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.{ExpenseType, IncomeType, SelfEmploymentPeriodicData}

case class SelfEmploymentPeriod(id: Option[String], from: String, to: String, financials: Option[Financials])

object SelfEmploymentPeriod {
  implicit val writes: Writes[SelfEmploymentPeriod] = Json.writes[SelfEmploymentPeriod]
  implicit val reads: Reads[SelfEmploymentPeriod] = Json.reads[SelfEmploymentPeriod]

  def from(apiSePeriod: models.selfemployment.SelfEmploymentPeriod): SelfEmploymentPeriod = {
    SelfEmploymentPeriod(
      id = None,
      from = apiSePeriod.from.toString,
      to = apiSePeriod.to.toString,
      financials = Some(
        Financials(
          incomes = Some(Incomes(
            turnover = apiSePeriod.data.incomes.get(IncomeType.Turnover).map(_.amount),
            other = apiSePeriod.data.incomes.get(IncomeType.Other).map(_.amount)
          )),
          deductions = Some(Deductions(
            costOfGoods = apiSePeriod.data.expenses.get(ExpenseType.CostOfGoodsBought).map(expense2Deduction),
            constructionIndustryScheme = apiSePeriod.data.expenses.get(ExpenseType.CISPaymentsToSubcontractors).map(expense2Deduction),
            staffCosts = apiSePeriod.data.expenses.get(ExpenseType.StaffCosts).map(expense2Deduction),
            travelCosts = apiSePeriod.data.expenses.get(ExpenseType.TravelCosts).map(expense2Deduction),
            premisesRunningCosts = apiSePeriod.data.expenses.get(ExpenseType.PremisesRunningCosts).map(expense2Deduction),
            maintenanceCosts = apiSePeriod.data.expenses.get(ExpenseType.MaintenanceCosts).map(expense2Deduction),
            adminCosts = apiSePeriod.data.expenses.get(ExpenseType.AdminCosts).map(expense2Deduction),
            advertisingCosts = apiSePeriod.data.expenses.get(ExpenseType.AdvertisingCosts).map(expense2Deduction),
            interest = apiSePeriod.data.expenses.get(ExpenseType.Interest).map(expense2Deduction),
            financialCharges = apiSePeriod.data.expenses.get(ExpenseType.FinancialCharges).map(expense2Deduction),
            badDebt = apiSePeriod.data.expenses.get(ExpenseType.BadDebt).map(expense2Deduction),
            professionalFees = apiSePeriod.data.expenses.get(ExpenseType.ProfessionalFees).map(expense2Deduction),
            depreciation = apiSePeriod.data.expenses.get(ExpenseType.Depreciation).map(expense2Deduction),
            other = apiSePeriod.data.expenses.get(ExpenseType.Other).map(expense2Deduction)
          ))
        )
      )
    )
  }
}

case class Financials(incomes: Option[Incomes], deductions: Option[Deductions])

object Financials {
  implicit val writes: Writes[Financials] = Json.writes[Financials]
  implicit val reads: Reads[Financials] = Json.reads[Financials]

  def from(apiPeriodData: SelfEmploymentPeriodicData): Financials = {
    Financials(
      incomes = Some(Incomes(
        turnover = apiPeriodData.incomes.get(IncomeType.Turnover).map(_.amount),
        other = apiPeriodData.incomes.get(IncomeType.Other).map(_.amount)
      )),
      deductions = Some(Deductions(
        costOfGoods = apiPeriodData.expenses.get(ExpenseType.CostOfGoodsBought).map(expense2Deduction),
        constructionIndustryScheme = apiPeriodData.expenses.get(ExpenseType.CISPaymentsToSubcontractors).map(expense2Deduction),
        staffCosts = apiPeriodData.expenses.get(ExpenseType.StaffCosts).map(expense2Deduction),
        travelCosts = apiPeriodData.expenses.get(ExpenseType.TravelCosts).map(expense2Deduction),
        premisesRunningCosts = apiPeriodData.expenses.get(ExpenseType.PremisesRunningCosts).map(expense2Deduction),
        maintenanceCosts = apiPeriodData.expenses.get(ExpenseType.MaintenanceCosts).map(expense2Deduction),
        adminCosts = apiPeriodData.expenses.get(ExpenseType.AdminCosts).map(expense2Deduction),
        advertisingCosts = apiPeriodData.expenses.get(ExpenseType.AdvertisingCosts).map(expense2Deduction),
        interest = apiPeriodData.expenses.get(ExpenseType.Interest).map(expense2Deduction),
        financialCharges = apiPeriodData.expenses.get(ExpenseType.FinancialCharges).map(expense2Deduction),
        badDebt = apiPeriodData.expenses.get(ExpenseType.BadDebt).map(expense2Deduction),
        professionalFees = apiPeriodData.expenses.get(ExpenseType.ProfessionalFees).map(expense2Deduction),
        depreciation = apiPeriodData.expenses.get(ExpenseType.Depreciation).map(expense2Deduction),
        other = apiPeriodData.expenses.get(ExpenseType.Other).map(expense2Deduction)
      ))
    )
  }
}

case class Incomes(turnover: Option[BigDecimal], other: Option[BigDecimal])

object Incomes {
  implicit val writes: Writes[Incomes] = Json.writes[Incomes]
  implicit val reads: Reads[Incomes] = Json.reads[Incomes]
}

case class Deductions(costOfGoods: Option[Deduction],
                      constructionIndustryScheme: Option[Deduction],
                      staffCosts: Option[Deduction],
                      travelCosts: Option[Deduction],
                      premisesRunningCosts: Option[Deduction],
                      maintenanceCosts: Option[Deduction],
                      adminCosts: Option[Deduction],
                      advertisingCosts: Option[Deduction],
                      interest: Option[Deduction],
                      financialCharges: Option[Deduction],
                      badDebt: Option[Deduction],
                      professionalFees: Option[Deduction],
                      depreciation: Option[Deduction],
                      other: Option[Deduction])

object Deductions {
  implicit val writes: Writes[Deductions] = Json.writes[Deductions]
  implicit val reads: Reads[Deductions] = Json.reads[Deductions]
}

case class Deduction(amount: BigDecimal, disallowableAmount: Option[BigDecimal])

object Deduction {
  implicit val writes: Writes[Deduction] = Json.writes[Deduction]
  implicit val reads: Reads[Deduction] = Json.reads[Deduction]
}
