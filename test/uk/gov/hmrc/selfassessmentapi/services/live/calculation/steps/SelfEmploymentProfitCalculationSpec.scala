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

import uk.gov.hmrc.selfassessmentapi.SelfAssessmentSugar._
import uk.gov.hmrc.selfassessmentapi.SelfEmploymentSugar._
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment._
import uk.gov.hmrc.selfassessmentapi.repositories.domain._

class SelfEmploymentProfitCalculationSpec extends UnitSpec {

  private val liability = MongoLiability.create(generateSaUtr(), taxYear)

  private val selfEmploymentId = "selfEmploymentId"

  "calculate profit for self employment" should {

    "not record any profit if there are no self employments" in {

      SelfEmploymentProfitCalculation.run(SelfAssessment(), liability) shouldBe liability
    }

    "add all incomes, balancingCharges, goodsAndServices and adjustments to profit" in {

      val selfAssessment = SelfAssessment(
          selfEmployments = Seq(
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
              )))

      SelfEmploymentProfitCalculation.run(selfAssessment, liability).getLiabilityOrFail shouldBe liability.copy(
          profitFromSelfEmployments = Seq(
              SelfEmploymentIncome(selfEmploymentId, taxableProfit = 2430, profit = 2430)
          ))
    }

    "add outstandingBusinessIncome to profit" in {

      val selfAssessment = SelfAssessment(
          selfEmployments = Seq(
              aSelfEmployment(selfEmploymentId).copy(
                  incomes = Seq(
                      anIncome(IncomeType.Turnover, 2000)
                  ),
                  adjustments = Some(Adjustments(
                          outstandingBusinessIncome = Some(3000)
                      ))
              )))

      SelfEmploymentProfitCalculation.run(selfAssessment, liability).getLiabilityOrFail shouldBe liability.copy(
          profitFromSelfEmployments = Seq(
              SelfEmploymentIncome(selfEmploymentId, taxableProfit = 5000, profit = 5000)
          ))
    }

    "loss brought forward must be capped at adjusted profits" in {

      val selfAssessment = SelfAssessment(
          selfEmployments = Seq(
              aSelfEmployment(selfEmploymentId).copy(
                  adjustments = Some(Adjustments(
                          outstandingBusinessIncome = Some(5000.32),
                          lossBroughtForward = Some(20000)
                      ))
              )))

      val profitFromSelfEmployments =
        SelfEmploymentProfitCalculation.run(selfAssessment, liability).getLiabilityOrFail.profitFromSelfEmployments
      profitFromSelfEmployments.head.taxableProfit shouldBe 5000
      profitFromSelfEmployments.head.profit shouldBe 5000
    }

    "subtract all expenses apart from depreciation from profit" in {

      val selfAssessment = SelfAssessment(
          selfEmployments = Seq(
              aSelfEmployment(selfEmploymentId).copy(
                  incomes = Seq(
                      anIncome(IncomeType.Turnover, 2000)
                  ),
                  expenses = Seq(
                      anExpense(ExpenseType.AdminCosts, 100),
                      anExpense(ExpenseType.BadDebt, 50.01),
                      anExpense(ExpenseType.CISPayments, 49.99),
                      anExpense(ExpenseType.Depreciation, 1000000)
                  )
              )))

      SelfEmploymentProfitCalculation.run(selfAssessment, liability).getLiabilityOrFail shouldBe liability.copy(
          profitFromSelfEmployments = Seq(
              SelfEmploymentIncome(selfEmploymentId, taxableProfit = 1800, profit = 1800)
          ))
    }

    "subtract all allowances from profit" in {

      val selfAssessment = SelfAssessment(
          selfEmployments = Seq(
              aSelfEmployment(selfEmploymentId).copy(
                  incomes = Seq(
                      anIncome(IncomeType.Turnover, 2000)
                  ),
                  allowances = Some(
                      Allowances(
                          annualInvestmentAllowance = Some(50),
                          capitalAllowanceMainPool = Some(10),
                          capitalAllowanceSpecialRatePool = Some(10),
                          restrictedCapitalAllowance = Some(10),
                          businessPremisesRenovationAllowance = Some(10),
                          enhancedCapitalAllowance = Some(4.99),
                          allowancesOnSales = Some(5.01)
                      ))
              )))

      SelfEmploymentProfitCalculation.run(selfAssessment, liability).getLiabilityOrFail shouldBe liability.copy(
          profitFromSelfEmployments = Seq(
              SelfEmploymentIncome(selfEmploymentId, taxableProfit = 1900, profit = 1900)
          ))
    }

    "round down profit and taxableProfit to the nearest pound" in {

      val selfAssessment = SelfAssessment(
          selfEmployments = Seq(
              aSelfEmployment(selfEmploymentId).copy(
                  incomes = Seq(
                      anIncome(IncomeType.Turnover, 1299.01)
                  ),
                  allowances = Some(Allowances(
                          annualInvestmentAllowance = Some(0.02)
                      ))
              )))

      SelfEmploymentProfitCalculation.run(selfAssessment, liability).getLiabilityOrFail shouldBe liability.copy(
          profitFromSelfEmployments = Seq(
              SelfEmploymentIncome(selfEmploymentId, taxableProfit = 1298, profit = 1298)
          ))
    }

    "subtract adjustments from profit" in {

      val selfAssessment = SelfAssessment(
          selfEmployments = Seq(
              aSelfEmployment(selfEmploymentId).copy(
                  incomes = Seq(
                      anIncome(IncomeType.Turnover, 2000)
                  ),
                  adjustments = Some(
                      Adjustments(
                          includedNonTaxableProfits = Some(50),
                          basisAdjustment = Some(-15),
                          overlapReliefUsed = Some(10),
                          averagingAdjustment = Some(-25)
                      ))
              )))

      SelfEmploymentProfitCalculation.run(selfAssessment, liability).getLiabilityOrFail shouldBe liability.copy(
          profitFromSelfEmployments = Seq(
              SelfEmploymentIncome(selfEmploymentId, taxableProfit = 1900, profit = 1900)
          ))
    }

    "cap annualInvestmentAllowance at 200000" in {

      val selfAssessment = SelfAssessment(
          selfEmployments = Seq(
              aSelfEmployment(selfEmploymentId).copy(
                  incomes = Seq(
                      anIncome(IncomeType.Turnover, 230000)
                  ),
                  allowances = Some(Allowances(
                          annualInvestmentAllowance = Some(230000)
                      ))
              )))

      SelfEmploymentProfitCalculation.run(selfAssessment, liability).getLiabilityOrFail shouldBe liability.copy(
          profitFromSelfEmployments = Seq(
              SelfEmploymentIncome(selfEmploymentId, taxableProfit = 30000, profit = 30000)
          ))
    }

    "subtract lossBroughtForward from taxable profit, but not the profit" in {

      val selfAssessment = SelfAssessment(
          selfEmployments = Seq(
              aSelfEmployment(selfEmploymentId).copy(
                  incomes = Seq(
                      anIncome(IncomeType.Turnover, 2000)
                  ),
                  adjustments = Some(Adjustments(
                          lossBroughtForward = Some(1000.49)
                      ))
              )))

      SelfEmploymentProfitCalculation.run(selfAssessment, liability).getLiabilityOrFail shouldBe liability.copy(
          profitFromSelfEmployments = Seq(
              SelfEmploymentIncome(selfEmploymentId, taxableProfit = 999, profit = 2000)
          ))
    }

    "return zero as taxable profit if lossBroughtForward is greater than adjusted profit" in {

      val selfAssessment = SelfAssessment(
          selfEmployments = Seq(
              aSelfEmployment(selfEmploymentId).copy(
                  incomes = Seq(
                      anIncome(IncomeType.Turnover, 2000)
                  ),
                  adjustments = Some(Adjustments(
                          lossBroughtForward = Some(3000)
                      ))
              )))

      SelfEmploymentProfitCalculation.run(selfAssessment, liability).getLiabilityOrFail shouldBe liability.copy(
          profitFromSelfEmployments = Seq(
              SelfEmploymentIncome(selfEmploymentId, taxableProfit = 0, profit = 2000)
          ))
    }

    "return zero as profit and ignore lossBroughtForward if expenses are bigger than incomes (loss)" in {

      val selfAssessment = SelfAssessment(
          selfEmployments = Seq(
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
              )))

      SelfEmploymentProfitCalculation.run(selfAssessment, liability).getLiabilityOrFail shouldBe liability.copy(
          profitFromSelfEmployments = Seq(
              SelfEmploymentIncome(selfEmploymentId, taxableProfit = 0, profit = 0)
          ))
    }

    "calculate profit for multiple self employments" in {

      val selfAssessment = SelfAssessment(
          selfEmployments = Seq(
              aSelfEmployment("se1").copy(incomes = Seq(
                      anIncome(IncomeType.Turnover, 1200)
                  )),
              aSelfEmployment("se2").copy(incomes = Seq(
                      anIncome(IncomeType.Turnover, 800)
                  ))
          ))

      SelfEmploymentProfitCalculation.run(selfAssessment, liability).getLiabilityOrFail shouldBe liability.copy(
          profitFromSelfEmployments = Seq(
              SelfEmploymentIncome("se1", taxableProfit = 1200, profit = 1200),
              SelfEmploymentIncome("se2", taxableProfit = 800, profit = 800)
          ))
    }
  }
}
