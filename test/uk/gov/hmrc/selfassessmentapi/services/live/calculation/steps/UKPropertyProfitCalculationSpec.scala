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

import uk.gov.hmrc.selfassessmentapi.domain.ukproperty.ExpenseType.ExpenseType
import uk.gov.hmrc.selfassessmentapi.domain.ukproperty.IncomeType.IncomeType
import uk.gov.hmrc.selfassessmentapi.{SelfEmploymentSugar, UkPropertySugar, UnitSpec}
import uk.gov.hmrc.selfassessmentapi.domain.ukproperty.{Adjustments, Allowances, ExpenseType, IncomeType}
import uk.gov.hmrc.selfassessmentapi.repositories.domain._

class UKPropertyProfitCalculationSpec extends UnitSpec with SelfEmploymentSugar with UkPropertySugar {
import uk.gov.hmrc.selfassessmentapi.{SelfEmploymentSugar, UnitSpec}
import uk.gov.hmrc.selfassessmentapi.domain.ukproperty.{Adjustments, Allowances, ExpenseType, IncomeType}
import uk.gov.hmrc.selfassessmentapi.repositories.domain._

class UKPropertyProfitCalculationSpec extends UnitSpec with SelfEmploymentSugar {

  "run" should {
    def income(incomeType: IncomeType, amount: BigDecimal) = MongoUKPropertiesIncomeSummary("", incomeType, amount)
    def privateUseAdjustment(amount: BigDecimal) = MongoUKPropertiesPrivateUseAdjustmentSummary("", amount)
    def balancingCharge(amount: BigDecimal) = MongoUKPropertiesBalancingChargeSummary("", amount)
    def expense(expenseType: ExpenseType, amount: BigDecimal) = MongoUKPropertiesExpenseSummary("", expenseType, amount)

    "compute adjusted profits for UK properties" in {
      val selfAssessment = SelfAssessment(ukProperties = Seq(aUkProperty("ukpropertyone").copy(
        incomes = Seq(income(IncomeType.RentIncome, 500), income(IncomeType.PremiumsOfLeaseGrant, 500),
          income(IncomeType.ReversePremiums, 500)),
        privateUseAdjustment = Seq(privateUseAdjustment(500)),
        balancingCharges = Seq(balancingCharge(500)),
        expenses = Seq(expense(ExpenseType.PremisesRunningCosts, 100), expense(ExpenseType.RepairsAndMaintenance, 100),
          expense(ExpenseType.FinancialCosts, 100), expense(ExpenseType.ProfessionalFees, 100),
          expense(ExpenseType.CostOfServices, 100), expense(ExpenseType.Other, 100)),
        allowances = Some(Allowances(Some(100), Some(100), Some(100), Some(100))),
        rentARoomRelief = Some(500))))

      UKPropertyProfitCalculation.run(selfAssessment, aLiability()).profitFromUkProperties should contain theSameElementsAs
        Seq(UkPropertyIncome("ukpropertyone", profit = 1000))
    }

    "compute taxableProfit profits for UK properties as adjustedProfit - lossBroughtForward" in {
      val selfAssessment = SelfAssessment(ukProperties = Seq(aUkProperty("ukpropertyone").copy(
        incomes = Seq(income(IncomeType.RentIncome, 500)),
        adjustments = Some(Adjustments(lossBroughtForward = Some(200))))))

      UKPropertyProfitCalculation.run(selfAssessment, aLiability()).profitFromUkProperties should contain theSameElementsAs
        Seq(UkPropertyIncome("ukpropertyone", profit = 500))
    }

    "ensure minimum value for the UK property profit is zero" in {
      val selfAssessment = SelfAssessment(ukProperties = Seq(aUkProperty("ukpropertyone").copy(
        incomes = Seq(income(IncomeType.RentIncome, 500)),
        expenses = Seq(expense(ExpenseType.PremisesRunningCosts, 1000)))))

      UKPropertyProfitCalculation.run(selfAssessment, aLiability()).profitFromUkProperties should contain theSameElementsAs
        Seq(UkPropertyIncome("ukpropertyone", profit = 0))
    }

    "computed profits should be rounded down to the nearest pound" in {
      val selfAssessment = SelfAssessment(ukProperties = Seq(aUkProperty("ukpropertyone").copy(
        incomes = Seq(income(IncomeType.RentIncome, 500.55), income(IncomeType.PremiumsOfLeaseGrant, 500.20)),
        expenses = Seq(expense(ExpenseType.PremisesRunningCosts, 100.11)),
        adjustments = Some(Adjustments(lossBroughtForward = Some(200.22))))))

      UKPropertyProfitCalculation.run(selfAssessment, aLiability()).profitFromUkProperties should contain theSameElementsAs
        Seq(UkPropertyIncome("ukpropertyone", profit = 900))
    }
  }
}
