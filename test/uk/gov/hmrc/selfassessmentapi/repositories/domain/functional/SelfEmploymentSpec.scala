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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.functional

import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.SelfEmploymentSugar._
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.SelfEmploymentIncome
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps.SelfAssessment

class SelfEmploymentSpec extends UnitSpec {

  private val selfEmploymentId = "selfEmploymentId"

  "TotalTaxableProfit" should {
    "be TotalProfit - TotalDeduction" in {
      SelfEmployment.TotalTaxableProfit(totalProfit = 10000, totalDeduction = 5000) shouldBe 5000
    }

    "be 0 if TotalProfit is less than TotalDeductions" in {
      SelfEmployment.TotalTaxableProfit(totalProfit = 1000, totalDeduction = 1001) shouldBe 0
    }
  }

  "TotalProfit" should {
    "be sum of profits from across all self employments" in {
      val selfEmploymentOne =
          aSelfEmployment(selfEmploymentId).copy(incomes = Seq(anIncome(IncomeType.Turnover, 2000)))
      val selfEmploymentTwo =
        aSelfEmployment(selfEmploymentId).copy(incomes = Seq(anIncome(IncomeType.Turnover, 3000)))

        SelfEmployment.TotalProfit(SelfAssessment(selfEmployments = Seq(selfEmploymentOne, selfEmploymentTwo))) shouldBe BigDecimal(5000)
    }
  }


  "Profit from self employment" should {
    "be equal to the sum of all incomes, balancingCharges, goodsAndServices and basisAdjustment, accountingAdjustment and " +
      "averagingAdjustment" in {

      val selfEmployment =
        aSelfEmployment(selfEmploymentId).copy(
          incomes = Seq(
            anIncome(IncomeType.Turnover, 1200.01),
            anIncome(IncomeType.Other, 799.99)
          ),
          balancingCharges = Seq(
            aBalancingCharge(BalancingChargeType.BPRA, 10),
            aBalancingCharge(BalancingChargeType.Other, 20)
          ),
          goodsAndServicesOwnUse = Seq(
            aGoodsAndServices(50)
          ),
          adjustments = Some(Adjustments(
            basisAdjustment = Some(200),
            accountingAdjustment = Some(100),
            averagingAdjustment = Some(50)
          ))
        )

      SelfEmployment.Profit(selfEmployment) shouldBe BigDecimal(2430)

    }

    "be equal to incomes and outstandingBusinessIncome" in {

      val selfEmployment =
        aSelfEmployment(selfEmploymentId).copy(
          incomes = Seq(
            anIncome(IncomeType.Turnover, 10000.98)
          ),
          adjustments = Some(Adjustments(
            outstandingBusinessIncome = Some(5000.32),
            lossBroughtForward = Some(20000.50)
          ))
        )

      SelfEmployment.Profit(selfEmployment) shouldBe BigDecimal(15001)

    }

    "not contain any expenses apart from depreciation" in {

      val selfEmployment =
        aSelfEmployment(selfEmploymentId).copy(
          incomes = Seq(
            anIncome(IncomeType.Turnover, 2000)
          ),
          expenses = Seq(
            anExpense(ExpenseType.AdminCosts, 100),
            anExpense(ExpenseType.BadDebt, 50.01),
            anExpense(ExpenseType.CISPaymentsToSubcontractors, 49.99),
            anExpense(ExpenseType.Depreciation, 1000000)
          )
        )

      SelfEmployment.Profit(selfEmployment) shouldBe BigDecimal(1800)
    }

    "subtract all allowances from profit" in {

      val selfEmployment =
        aSelfEmployment(selfEmploymentId).copy(
          incomes = Seq(
            anIncome(IncomeType.Turnover, 2000)
          ),
          allowances = Some(Allowances(
            annualInvestmentAllowance = Some(50),
            capitalAllowanceMainPool = Some(10),
            capitalAllowanceSpecialRatePool = Some(10),
            businessPremisesRenovationAllowance = Some(10),
            enhancedCapitalAllowance = Some(4.99),
            allowancesOnSales = Some(5.01)
          ))
        )

      SelfEmployment.Profit(selfEmployment) shouldBe BigDecimal(1910)

    }

    "be rounded down to the nearest pound" in {

      val selfEmployment =
        aSelfEmployment(selfEmploymentId).copy(
          incomes = Seq(
            anIncome(IncomeType.Turnover, 1.99)
          ),
          allowances = Some(Allowances(
            annualInvestmentAllowance = Some(0.02)
          ))
        )

      SelfEmployment.Profit(selfEmployment) shouldBe BigDecimal(1)
    }

    "subtract adjustments from profit" in {

      val selfEmployment =
        aSelfEmployment(selfEmploymentId).copy(
          incomes = Seq(
            anIncome(IncomeType.Turnover, 2000)
          ),
          adjustments = Some(Adjustments(
            includedNonTaxableProfits = Some(50),
            basisAdjustment = Some(-15),
            overlapReliefUsed = Some(10),
            averagingAdjustment = Some(-25)
          ))
        )

      SelfEmployment.Profit(selfEmployment) shouldBe BigDecimal(1900)

    }

    "reduce cap annualInvestmentAllowance at 200000" in {

      val selfEmployment =
        aSelfEmployment(selfEmploymentId).copy(
          incomes = Seq(
            anIncome(IncomeType.Turnover, 230000)
          ),
          allowances = Some(Allowances(
            annualInvestmentAllowance = Some(230000)
          ))
        )

      SelfEmployment.Profit(selfEmployment) shouldBe BigDecimal(30000)

    }

    "be sum of adjusted profits and outstandingBusinessIncome" in {

      val selfEmployment =
        aSelfEmployment(selfEmploymentId).copy(
          incomes = Seq(
            anIncome(IncomeType.Turnover, 2000)
          ),
          adjustments = Some(Adjustments(
            outstandingBusinessIncome = Some(1000)
          ))
        )

      SelfEmployment.Profit(selfEmployment) shouldBe BigDecimal(3000)
    }

    "be zero if expenses are bigger than incomes (loss)" in {

      val selfEmployment =
        aSelfEmployment(selfEmploymentId).copy(
          incomes = Seq(
            anIncome(IncomeType.Turnover, 2000)
          ),
          expenses = Seq(
            anExpense(ExpenseType.AdminCosts, 4000)
          ),
          adjustments = Some(Adjustments(
            lossBroughtForward = Some(1000)
          ))
        )

      SelfEmployment.Profit(selfEmployment) shouldBe BigDecimal(0)
    }

  }

  "SelfEmployment.TaxableProfit" should {
    "be RoundDown(Profit - CappedAt(LossBroughtForward, AdjustedProfit))" in {
      val selfEmployment =
        aSelfEmployment(selfEmploymentId).copy(
          incomes = Seq(
            anIncome(IncomeType.Turnover, 10000.98)
          ),
          adjustments = Some(Adjustments(
            outstandingBusinessIncome = Some(5000.32),
            lossBroughtForward = Some(20000.50)
          ))
        )

      SelfEmployment.TaxableProfit(selfEmployment) shouldBe BigDecimal(5000.00)
    }
  }

  "Incomes" should {
    "be profit per self employment source" in {
      val selfEmploymentOne =
        aSelfEmployment("selfEmploymentIdOne").copy(incomes = Seq(anIncome(IncomeType.Turnover, 2000)))
      val selfEmploymentTwo =
        aSelfEmployment("selfEmploymentIdTwo").copy(incomes = Seq(anIncome(IncomeType.Turnover, 3000)))

      SelfEmployment.Incomes(SelfAssessment(selfEmployments = Seq(selfEmploymentOne, selfEmploymentTwo))) should contain theSameElementsAs
        Seq(SelfEmploymentIncome("selfEmploymentIdOne", profit = 2000, taxableProfit = 2000),
          SelfEmploymentIncome("selfEmploymentIdTwo", profit = 3000, taxableProfit = 3000))
    }
  }

  "TotalLossBroughtForward" should {
    "be the Rounded up sum of all loss brought forward" in {
      val selfEmploymentOne =
        aSelfEmployment("selfEmploymentIdOne").copy(
          incomes = Seq(anIncome(IncomeType.Turnover, 2000)),
          adjustments = Some(Adjustments(lossBroughtForward = Some(100.45))))

      val selfEmploymentTwo =
        aSelfEmployment("selfEmploymentIdOne").copy(
          incomes = Seq(anIncome(IncomeType.Turnover, 2000)),
          adjustments = Some(Adjustments(lossBroughtForward = Some(200.13))))


      SelfEmployment.TotalLossBroughtForward(SelfAssessment(selfEmployments = Seq(selfEmploymentOne, selfEmploymentTwo))) shouldBe 301
    }

    "be the Rounded up sum of all loss brought forward capped at adjusted profits" in {
      val selfEmploymentOne =
        aSelfEmployment("selfEmploymentIdOne").copy(
          incomes = Seq(anIncome(IncomeType.Turnover, 2000.45)),
          adjustments = Some(Adjustments(lossBroughtForward = Some(2100))))

      val selfEmploymentTwo =
        aSelfEmployment("selfEmploymentIdOne").copy(
          incomes = Seq(anIncome(IncomeType.Turnover, 2000)),
          adjustments = Some(Adjustments(lossBroughtForward = Some(1000.45))))


      SelfEmployment.TotalLossBroughtForward(SelfAssessment(selfEmployments = Seq(selfEmploymentOne, selfEmploymentTwo))) shouldBe 3001
    }
  }

}
