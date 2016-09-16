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

import uk.gov.hmrc.selfassessmentapi.{SelfEmploymentSugar, repositories}

/**
  * Created by Office on 13/09/2016.
  */
case class SelfEmploymentBuilder() {
  def create() = selfEmployment

  import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment._

  private var selfEmployment: repositories.domain.SelfEmployment = SelfEmploymentSugar.aSelfEmployment().copy(allowances = Some(Allowances()), adjustments = Some(Adjustments()))

  def withAllowances(allowancesOnSales: BigDecimal, enhancedCapitalAllowance: BigDecimal,
                     businessPremisesRenovationAllowance: BigDecimal, capitalAllowanceSpecialRatePool: BigDecimal,
                     capitalAllowanceMainPool: BigDecimal, annualInvestmentAllowance: BigDecimal) = {
    selfEmployment = selfEmployment.copy(allowances = selfEmployment.allowances.map(_.copy(
      allowancesOnSales = Some(allowancesOnSales),
      enhancedCapitalAllowance = Some(enhancedCapitalAllowance),
      businessPremisesRenovationAllowance = Some(businessPremisesRenovationAllowance),
      capitalAllowanceSpecialRatePool = Some(capitalAllowanceSpecialRatePool),
      capitalAllowanceMainPool = Some(capitalAllowanceMainPool),
      annualInvestmentAllowance = Some(annualInvestmentAllowance))))

    this
  }

  def withAdjustments(outstandingBusinessIncome: BigDecimal, lossBroughtForward: BigDecimal,
                      averagingAdjustment: BigDecimal, overlapReliefUsed: BigDecimal,
                      basisAdjustment: BigDecimal, includedNonTaxableProfits: BigDecimal) = {
    selfEmployment = selfEmployment.copy(adjustments = selfEmployment.adjustments.map(_.copy(
      outstandingBusinessIncome = Some(outstandingBusinessIncome),
      lossBroughtForward = Some(lossBroughtForward),
      averagingAdjustment = Some(averagingAdjustment),
      overlapReliefUsed = Some(overlapReliefUsed),
      basisAdjustment = Some(basisAdjustment),
      includedNonTaxableProfits = Some(includedNonTaxableProfits))))

    this
  }

  def incomes(incomes: (IncomeType.IncomeType, BigDecimal)*) = {
    selfEmployment = selfEmployment.copy(incomes = incomes.map (income => SelfEmploymentSugar.anIncome(income._1, income._2)))
    this
  }

  def expenses(expenses: (ExpenseType.ExpenseType, BigDecimal)*) = {
    selfEmployment = selfEmployment.copy(expenses = expenses.map (expense => SelfEmploymentSugar.anExpense(expense._1, expense._2)))
    this
  }

  def balancingCharges(balancingCharges: (BalancingChargeType.BalancingChargeType, BigDecimal)*) = {
    selfEmployment = selfEmployment.copy(balancingCharges = balancingCharges.map (balancingCharge => SelfEmploymentSugar.aBalancingCharge(balancingCharge._1, balancingCharge._2)))
    this
  }

  def goodsAndServicesOwnUse(amounts: BigDecimal*) = {
    selfEmployment = selfEmployment.copy(goodsAndServicesOwnUse = amounts.map(SelfEmploymentSugar.aGoodsAndServices))
    this
  }
}
