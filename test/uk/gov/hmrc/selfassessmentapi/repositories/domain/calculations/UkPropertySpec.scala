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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.calculations

import uk.gov.hmrc.selfassessmentapi.SelfAssessmentSugar._
import uk.gov.hmrc.selfassessmentapi.UKPropertySugar._
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.controllers.api.SelfAssessment
import uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty.ExpenseType.{apply => _, _}
import uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty.IncomeType._
import uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty.{Adjustments, Allowances, ExpenseType, IncomeType}
import uk.gov.hmrc.selfassessmentapi.controllers.api.UkPropertyIncome
import uk.gov.hmrc.selfassessmentapi.repositories.domain._

class UKPropertySpec extends UnitSpec {

  def income(incomeType: IncomeType, amount: BigDecimal) = UKPropertiesIncomeSummary("", incomeType, amount)
  def privateUseAdjustment(amount: BigDecimal) = UKPropertiesPrivateUseAdjustmentSummary("", amount)
  def balancingCharge(amount: BigDecimal) = UKPropertiesBalancingChargeSummary("", amount)
  def expense(expenseType: ExpenseType, amount: BigDecimal) = UKPropertiesExpenseSummary("", expenseType, amount)

  "UK property incomes" should {
    "compute adjusted profits for UK properties" in {
      val selfAssessment =
        SelfAssessment(
          ukProperties =
            Seq(
              aUkProperty("ukpropertyone").copy(incomes = Seq(income(IncomeType.RentIncome, 500),
                income(IncomeType.PremiumsOfLeaseGrant, 500),
                income(IncomeType.ReversePremiums, 500)),
                privateUseAdjustment = Seq(privateUseAdjustment(500)),
                balancingCharges = Seq(balancingCharge(500)),
                expenses = Seq(expense(ExpenseType.PremisesRunningCosts, 100),
                  expense(ExpenseType.RepairsAndMaintenance, 100),
                  expense(ExpenseType.FinancialCosts, 100),
                  expense(ExpenseType.ProfessionalFees, 100),
                  expense(ExpenseType.CostOfServices, 100),
                  expense(ExpenseType.Other, 100)),
                allowances =
                  Some(Allowances(Some(100), Some(100), Some(100), Some(100))),
                rentARoomRelief = Some(500))))

      UKProperty.Incomes(selfAssessment) should contain theSameElementsAs
        Seq(UkPropertyIncome("ukpropertyone", profit = 1000))
    }

    "ensure minimum value for the UK property profit is zero" in {
      val selfAssessment = SelfAssessment(
        ukProperties = Seq(
          aUkProperty("ukpropertyone").copy(incomes = Seq(income(IncomeType.RentIncome, 500)),
            expenses = Seq(expense(ExpenseType.PremisesRunningCosts, 1000)))))

      UKProperty.Incomes(selfAssessment) should contain theSameElementsAs
        Seq(UkPropertyIncome("ukpropertyone", profit = 0))
    }

    "compute adjusted profits for each UK property" in {
      val selfAssessment = SelfAssessment(
        ukProperties = Seq(
          aUkProperty("ukpropertyone").copy(incomes = Seq(income(IncomeType.RentIncome, 1000)),
            expenses = Seq(expense(ExpenseType.PremisesRunningCosts, 500))),
        aUkProperty("ukpropertytwo").copy(incomes = Seq(income(IncomeType.RentIncome, 1000)),
          expenses = Seq(expense(ExpenseType.PremisesRunningCosts, 200)))
        )
      )

      UKProperty.Incomes(selfAssessment) should contain theSameElementsAs
        Seq(UkPropertyIncome("ukpropertyone", profit = 500), UkPropertyIncome("ukpropertytwo", profit = 800))
    }

    "computed profits should be rounded down to the nearest pound" in {
      val selfAssessment =
        SelfAssessment(
          ukProperties =
            Seq(
              aUkProperty("ukpropertyone").copy(incomes = Seq(income(IncomeType.RentIncome, 500.55),
                income(IncomeType.PremiumsOfLeaseGrant, 500.20)),
                expenses = Seq(expense(ExpenseType.PremisesRunningCosts, 100.11)),
                adjustments = Some(Adjustments(lossBroughtForward = Some(200.22))))))

      UKProperty.Incomes(selfAssessment) should contain theSameElementsAs
        Seq(UkPropertyIncome("ukpropertyone", profit = 900))
    }
  }

  "TotalLossBroughtForward" should {
    "sum of all loss brought forward capped at total adjusted profits" in {
      val selfAssessment =
        SelfAssessment(
          ukProperties = Seq(
              aUkProperty("ukpropertyone").copy(
                incomes = Seq(income(IncomeType.RentIncome, 5000.99)),
                adjustments = Some(Adjustments(lossBroughtForward = Some(6000.34)))),
            aUkProperty("ukpropertytwo").copy(
              incomes = Seq(income(IncomeType.RentIncome, 3000.45)),
              adjustments = Some(Adjustments(lossBroughtForward = Some(2100.34))))
          )
        )

      UKProperty.CappedTotalLossBroughtForward(selfAssessment) shouldBe 8002
    }
  }

  "UkProperty.TotalTaxPaid" should {

    "be equal to sum of all uk property tax paid" in {
      UKProperty.TotalTaxPaid(aSelfAssessment(
        ukProperties = Seq(
          aUkProperty(id = "property-1").copy(taxesPaid = Seq(aTaxPaidSummary("", 500), aTaxPaidSummary("", 600))),
          aUkProperty(id = "property2").copy(taxesPaid = Seq(aTaxPaidSummary("", 400), aTaxPaidSummary("", 300)))
        )
      )) shouldBe 1800
    }

    "be equal to rounded up sum of all uk property tax paid" in {
      UKProperty.TotalTaxPaid(aSelfAssessment(
        ukProperties = Seq(
          aUkProperty(id = "property-1").copy(taxesPaid = Seq(aTaxPaidSummary("", 500.09), aTaxPaidSummary("", 600.86))),
          aUkProperty(id = "property2").copy(taxesPaid = Seq(aTaxPaidSummary("", 400.67), aTaxPaidSummary("", 300.34)))
        )
      )) shouldBe 1801.96
    }
  }

}
