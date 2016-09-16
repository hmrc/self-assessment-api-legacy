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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.builders

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.TestUtils._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{UKPropertiesTaxPaidSummary, _}

case class UKPropertyBuilder(rentARoomRelief: BigDecimal = 0, objectID: BSONObjectID = BSONObjectID.generate) {
  import uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty._

  private var ukProperties: UKProperties = UKProperties(objectID, objectID.stringify, generateSaUtr(), taxYear, now, now,
    Some(rentARoomRelief), Some(Allowances()), Some(Adjustments()))

  def withAllowances(annualInvestmentAllowance: BigDecimal, otherCapitalAllowance: BigDecimal,
                     wearAndTearAllowance: BigDecimal, businessPremisesRenovationAllowance: BigDecimal) = {
    ukProperties = ukProperties.copy(allowances = ukProperties.allowances.map(_.copy(
      otherCapitalAllowance = Some(otherCapitalAllowance), annualInvestmentAllowance = Some(annualInvestmentAllowance),
      wearAndTearAllowance = Some(wearAndTearAllowance), businessPremisesRenovationAllowance = Some(businessPremisesRenovationAllowance))))
    this
  }

  def lossBroughtForward(amount: BigDecimal) = {
    ukProperties = ukProperties.copy(adjustments = Some(Adjustments(lossBroughtForward = Some(amount))))
    this
  }

  def incomes(incomes: (IncomeType.IncomeType, BigDecimal)*) = {
    ukProperties = ukProperties.copy(incomes = incomes.map (income => UKPropertiesIncomeSummary("", income._1, income._2)))
    this
  }

  def expenses(expenses: (ExpenseType.ExpenseType, BigDecimal)*) = {
    ukProperties = ukProperties.copy(expenses = expenses.map (expense => UKPropertiesExpenseSummary("", expense._1, expense._2)))
    this
  }

  def balancingCharges(amounts: BigDecimal*) = {
    ukProperties = ukProperties.copy(balancingCharges = amounts.map (amount => UKPropertiesBalancingChargeSummary("", amount)))
    this
  }

  def privateUseAdjustment(amounts: BigDecimal*) = {
    ukProperties = ukProperties.copy(privateUseAdjustment = amounts.map (amount => UKPropertiesPrivateUseAdjustmentSummary("", amount)))
    this
  }

  def taxesPaid(amounts: BigDecimal*) = {
    ukProperties = ukProperties.copy(taxesPaid = amounts.map (amount => UKPropertiesTaxPaidSummary("", amount)))
    this
  }

  def create() = ukProperties
}
