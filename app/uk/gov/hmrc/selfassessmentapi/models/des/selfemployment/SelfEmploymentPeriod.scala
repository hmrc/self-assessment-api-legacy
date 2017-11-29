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

package uk.gov.hmrc.selfassessmentapi.models.des.selfemployment

import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.models
import uk.gov.hmrc.selfassessmentapi.models.des.expense2Deduction

case class SelfEmploymentPeriod(id: Option[String], from: String, to: String, financials: Option[Financials])

object SelfEmploymentPeriod {
  implicit val format: Format[SelfEmploymentPeriod] = Json.format[SelfEmploymentPeriod]

  def from(apiSePeriod: models.selfemployment.SelfEmploymentPeriod): SelfEmploymentPeriod = {
    SelfEmploymentPeriod(id = None,
                         from = apiSePeriod.from.toString,
                         to = apiSePeriod.to.toString,
                         financials = Some(Financials.from(apiSePeriod)))
  }
}

case class Financials(incomes: Option[Incomes], deductions: Option[Deductions])

object Financials {

  implicit val format: Format[Financials] = Json.format[Financials]

  def from(apiSePeriod: models.selfemployment.SelfEmploymentPeriod): Financials = {

    Financials(incomes = apiSePeriod.incomes.map( inc =>
        Incomes(
          turnover = inc.turnover,
          other = inc.other
        )),
      deductions = apiSePeriod.expenses.map( exp =>
        Deductions(costOfGoods = exp.costOfGoodsBought.map(expense2Deduction),
          constructionIndustryScheme = exp.cisPaymentsToSubcontractors.map(expense2Deduction),
          staffCosts = exp.staffCosts.map(expense2Deduction),
          travelCosts = exp.travelCosts.map(expense2Deduction),
          premisesRunningCosts = exp.premisesRunningCosts.map(expense2Deduction),
          maintenanceCosts = exp.maintenanceCosts.map(expense2Deduction),
          adminCosts = exp.adminCosts.map(expense2Deduction),
          businessEntertainmentCosts = exp.businessEntertainmentCosts.map(expense2Deduction),
          advertisingCosts = exp.advertisingCosts.map(expense2Deduction),
          interest = exp.interest.map(expense2Deduction),
          financialCharges = exp.financialCharges.map(expense2Deduction),
          badDebt = exp.badDebt.map(expense2Deduction),
          professionalFees = exp.professionalFees.map(expense2Deduction),
          depreciation = exp.depreciation.map(expense2Deduction),
          other = exp.other.map(expense2Deduction))
      ).fold {
        apiSePeriod.consolidatedExpenses.map(se => Deductions(simplifiedExpenses = Option(se)))
      }(Option(_))) // FIXME if incomes and deductions are None we don't want to create Financials(None, None)
  }
  def from(sePeriodUpdate: models.selfemployment.SelfEmploymentPeriodUpdate): Financials =
    Financials(incomes = sePeriodUpdate.incomes.map( inc =>
        Incomes(
          turnover = inc.turnover,
          other = inc.other
        )),
      deductions = sePeriodUpdate.expenses.map( exp =>
        Deductions(costOfGoods = exp.costOfGoodsBought.map(expense2Deduction),
            constructionIndustryScheme = exp.cisPaymentsToSubcontractors.map(expense2Deduction),
            staffCosts = exp.staffCosts.map(expense2Deduction),
            travelCosts = exp.travelCosts.map(expense2Deduction),
            premisesRunningCosts = exp.premisesRunningCosts.map(expense2Deduction),
            maintenanceCosts = exp.maintenanceCosts.map(expense2Deduction),
            adminCosts = exp.adminCosts.map(expense2Deduction),
            businessEntertainmentCosts = exp.businessEntertainmentCosts.map(expense2Deduction),
            advertisingCosts = exp.advertisingCosts.map(expense2Deduction),
            interest = exp.interest.map(expense2Deduction),
            financialCharges = exp.financialCharges.map(expense2Deduction),
            badDebt = exp.badDebt.map(expense2Deduction),
            professionalFees = exp.professionalFees.map(expense2Deduction),
            depreciation = exp.depreciation.map(expense2Deduction),
            other = exp.other.map(expense2Deduction))
      ).fold {
         sePeriodUpdate.consolidatedExpenses.map(se => Deductions(simplifiedExpenses = Option(se)))
      }(Option(_)))
}

case class Incomes(turnover: Option[BigDecimal], other: Option[BigDecimal])

object Incomes {
  implicit val format: Format[Incomes] = Json.format[Incomes]
}





