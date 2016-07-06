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

import org.joda.time.DateTime
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.ExpenseType.ExpenseType
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.IncomeType.IncomeType
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.{Adjustments, Allowances, ExpenseType, IncomeType}
import uk.gov.hmrc.selfassessmentapi.domain.{SourceId, TaxYear}
import uk.gov.hmrc.selfassessmentapi.repositories.domain._

class SelfEmploymentProfitCalculationSpec extends UnitSpec {

  private val liability = MongoLiability.create(generateSaUtr(), taxYear)

  private val selfEmploymentId = "selfEmploymentId"

  "calculate profit for self employment" should {

    "not record any profit if there are no self employments" in {

      SelfEmploymentProfitCalculation.run(SelfAssessment(), liability) shouldBe liability
    }

    "add all incomes and adjustments to profit" in {

      val selfAssessment = SelfAssessment(selfEmployments = Seq(
        selfEmployment(selfEmploymentId,
          incomes = Seq(
            income(IncomeType.Turnover, 1200.01),
            income(IncomeType.Other, 799.99)
          ),
          adjustments = Some(Adjustments(
            basisAdjustment = Some(200),
            accountingAdjustment = Some(100),
            averagingAdjustment = Some(50)))
        )))

      SelfEmploymentProfitCalculation.run(selfAssessment, liability) shouldBe liability.copy(profitFromSelfEmployments = Seq(
        SelfEmploymentIncome(selfEmploymentId, taxableProfit = 2350, profit = 2350)
      ))
    }

    "add outstandingBusinessIncome to profit" in {

      val selfAssessment = SelfAssessment(selfEmployments = Seq(
        selfEmployment(selfEmploymentId,
          incomes = Seq(
            income(IncomeType.Turnover, 2000)
          ),
          adjustments = Some(Adjustments(
            outstandingBusinessIncome = Some(3000)
          ))
        )))

      SelfEmploymentProfitCalculation.run(selfAssessment, liability) shouldBe liability.copy(profitFromSelfEmployments = Seq(
        SelfEmploymentIncome(selfEmploymentId, taxableProfit = 5000, profit = 5000)
      ))
    }

    "subtract all expenses from profit" in {

      val selfAssessment = SelfAssessment(selfEmployments = Seq(
        selfEmployment(selfEmploymentId,
          incomes = Seq(
            income(IncomeType.Turnover, 2000)
          ),
          expenses = Seq(
            expense(ExpenseType.AdminCosts, 100),
            expense(ExpenseType.BadDebt, 50.01),
            expense(ExpenseType.CISPayments, 49.99)
          )
        )))

      SelfEmploymentProfitCalculation.run(selfAssessment, liability) shouldBe liability.copy(profitFromSelfEmployments = Seq(
        SelfEmploymentIncome(selfEmploymentId, taxableProfit = 1800, profit = 1800)
      ))
    }

    "subtract all allowances from profit" in {

      val selfAssessment = SelfAssessment(selfEmployments = Seq(
        selfEmployment(selfEmploymentId,
          incomes = Seq(
            income(IncomeType.Turnover, 2000)
          ),
          allowances = Some(Allowances(
            annualInvestmentAllowance = Some(50),
            capitalAllowanceMainPool = Some(10),
            capitalAllowanceSpecialRatePool = Some(10),
            restrictedCapitalAllowance = Some(10),
            businessPremisesRenovationAllowance = Some(10),
            enhancedCapitalAllowance = Some(4.99),
            allowancesOnSales = Some(5.01)
          ))
        )))

      SelfEmploymentProfitCalculation.run(selfAssessment, liability) shouldBe liability.copy(profitFromSelfEmployments = Seq(
        SelfEmploymentIncome(selfEmploymentId, taxableProfit = 1900, profit = 1900)
      ))
    }

    "round down profit to the nearest pound" in {

      val selfAssessment = SelfAssessment(selfEmployments = Seq(
        selfEmployment(selfEmploymentId,
          incomes = Seq(
            income(IncomeType.Turnover, 1299.01)
          ),
          allowances = Some(Allowances(
            annualInvestmentAllowance = Some(0.02)
          ))
        )))

      SelfEmploymentProfitCalculation.run(selfAssessment, liability) shouldBe liability.copy(profitFromSelfEmployments = Seq(
        SelfEmploymentIncome(selfEmploymentId, taxableProfit = 1298.99, profit = 1298)
      ))
    }

    "subtract certain adjustments from profit" in {

      val selfAssessment = SelfAssessment(selfEmployments = Seq(
        selfEmployment(selfEmploymentId,
          incomes = Seq(
            income(IncomeType.Turnover, 2000)
          ),
          adjustments = Some(Adjustments(
            includedNonTaxableProfits = Some(50),
            basisAdjustment = Some(-15),
            overlapReliefUsed = Some(10),
            averagingAdjustment = Some(-25)
          ))
        )))

      SelfEmploymentProfitCalculation.run(selfAssessment, liability) shouldBe liability.copy(profitFromSelfEmployments = Seq(
        SelfEmploymentIncome(selfEmploymentId, taxableProfit = 1900, profit = 1900)
      ))
    }

    "cap annualInvestmentAllowance at 200000" in {

      val selfAssessment = SelfAssessment(selfEmployments = Seq(
        selfEmployment(selfEmploymentId,
          incomes = Seq(
            income(IncomeType.Turnover, 230000)
          ),
          allowances = Some(Allowances(
            annualInvestmentAllowance = Some(230000)
          ))
        )))

      SelfEmploymentProfitCalculation.run(selfAssessment, liability) shouldBe liability.copy(profitFromSelfEmployments = Seq(
        SelfEmploymentIncome(selfEmploymentId, taxableProfit = 30000, profit = 30000)
      ))
    }

    "subtract lossBroughtForward from taxable profit, but not the profit" in {

      val selfAssessment = SelfAssessment(selfEmployments = Seq(
        selfEmployment(selfEmploymentId,
          incomes = Seq(
            income(IncomeType.Turnover, 2000)
          ),
          adjustments = Some(Adjustments(
            lossBroughtForward = Some(1000.49)
          ))
        )))

      SelfEmploymentProfitCalculation.run(selfAssessment, liability) shouldBe liability.copy(profitFromSelfEmployments = Seq(
        SelfEmploymentIncome(selfEmploymentId, taxableProfit = 999.51, profit = 2000)
      ))
    }

    "return zero as taxable profit if lossBroughtForward is greater than adjusted profit" in {

      val selfAssessment = SelfAssessment(selfEmployments = Seq(
        selfEmployment(selfEmploymentId,
          incomes = Seq(
            income(IncomeType.Turnover, 2000)
          ),
          adjustments = Some(Adjustments(
            lossBroughtForward = Some(3000)
          ))
        )))

      SelfEmploymentProfitCalculation.run(selfAssessment, liability) shouldBe liability.copy(profitFromSelfEmployments = Seq(
        SelfEmploymentIncome(selfEmploymentId, taxableProfit = 0, profit = 2000)
      ))
    }
  }

  private def selfEmployment(id: SourceId, incomes: Seq[MongoSelfEmploymentIncomeSummary] = Nil, expenses: Seq[MongoSelfEmploymentExpenseSummary] = Nil, allowances: Option[Allowances] = None, adjustments: Option[Adjustments] = None) = {
    MongoSelfEmployment(BSONObjectID.generate, id, generateSaUtr(), TaxYear("2016-17"), DateTime.now, DateTime.now, DateTime.now.toLocalDate, expenses = expenses, incomes = incomes, allowances = allowances, adjustments = adjustments)
  }

  private def income(`type`: IncomeType, amount: BigDecimal) = MongoSelfEmploymentIncomeSummary(BSONObjectID.generate.stringify, `type`, amount)

  private def expense(`type`: ExpenseType, amount: BigDecimal) = MongoSelfEmploymentExpenseSummary(BSONObjectID.generate.stringify, `type`, amount)
}
