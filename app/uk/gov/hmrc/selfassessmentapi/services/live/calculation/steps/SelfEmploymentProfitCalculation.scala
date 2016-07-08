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

package uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps

import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.ExpenseType.Depreciation
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{MongoLiability, MongoSelfEmployment, SelfEmploymentIncome}

object SelfEmploymentProfitCalculation extends CalculationStep {

  private val annualInvestmentAllowance = BigDecimal(200000)

  override def run(selfAssessment: SelfAssessment, liability: MongoLiability): MongoLiability = {

    val profitFromSelfEmployments = selfAssessment.selfEmployments.map { selfEmployment =>
      val adjustedProfit = positiveOrZero(profitIncreases(selfEmployment) - profitReductions(selfEmployment))
      val lossBroughtForward = valueOrZero(capAt(selfEmployment.adjustments.flatMap(_.lossBroughtForward), adjustedProfit))
      val outstandingBusinessIncome = valueOrZero(selfEmployment.adjustments.flatMap(_.outstandingBusinessIncome))
      val taxableProfit = adjustedProfit - lossBroughtForward + outstandingBusinessIncome
      val profit = roundDown(taxableProfit + lossBroughtForward)

      SelfEmploymentIncome(sourceId = selfEmployment.sourceId, taxableProfit = roundDown(taxableProfit), profit = profit, lossBroughtForward = lossBroughtForward)
    }

    liability.copy(profitFromSelfEmployments = profitFromSelfEmployments)
  }

  private def profitIncreases(selfEmployment: MongoSelfEmployment): BigDecimal = {
    val income = Some(selfEmployment.incomes.map(_.amount).sum)
    val balancingCharges = Some(selfEmployment.balancingCharges.map(_.amount).sum)
    val goodsAndServices = Some(selfEmployment.goodsAndServicesOwnUse.map(_.amount).sum)
    val adjustments = selfEmployment.adjustments.map { a =>
      sum(a.basisAdjustment, a.accountingAdjustment, a.averagingAdjustment)
    }

    sum(income, balancingCharges, goodsAndServices, adjustments)
  }

  private def profitReductions(selfEmployment: MongoSelfEmployment): BigDecimal = {
    val expenses = Some(selfEmployment.expenses.filterNot(_.`type` == Depreciation).map(_.amount).sum)
    val allowances = selfEmployment.allowances.map { a =>
      sum(capAt(a.annualInvestmentAllowance, annualInvestmentAllowance), a.capitalAllowanceMainPool, a.capitalAllowanceSpecialRatePool, a.restrictedCapitalAllowance, a.businessPremisesRenovationAllowance, a.enhancedCapitalAllowance, a.allowancesOnSales)
    }
    val adjustments = selfEmployment.adjustments.map { a =>
      sum(a.includedNonTaxableProfits, a.overlapReliefUsed)
    }

    sum(expenses, allowances, adjustments)
  }
}
