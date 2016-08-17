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

package uk.gov.hmrc.selfassessmentapi.services.live.calculation

import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.IncomeType
import uk.gov.hmrc.selfassessmentapi.repositories.domain.MongoLiability
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBand._
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps.SelfAssessment
import uk.gov.hmrc.selfassessmentapi._

class LiabilityCalculatorSpec
    extends UnitSpec
    with EmploymentSugar
    with SelfEmploymentSugar
    with UnearnedIncomesSugar {

  "calculate" should {

    "calculate tax - only non savings income, which is allocated across all of the tax bands" in {

      val liability = liabilityCalculationFor(nonSavingsIncome = 180000)

      liability.nonSavingsIncome shouldBe Seq(
          aTaxBandAllocation(32000, BasicTaxBand),
          aTaxBandAllocation(118000, HigherTaxBand),
          aTaxBandAllocation(30000, AdditionalHigherTaxBand)
      )
      liability.savingsIncome shouldBe Seq(
          aTaxBandAllocation(0, SavingsStartingTaxBand),
          aTaxBandAllocation(0, NilTaxBand),
          aTaxBandAllocation(0, BasicTaxBand),
          aTaxBandAllocation(0, HigherTaxBand),
          aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "calculate tax - only non savings income, which is within basic tax band" in {

      val liability = liabilityCalculationFor(savingsIncome = 20000)

      liability.savingsIncome shouldBe Seq(
          aTaxBandAllocation(5000, SavingsStartingTaxBand),
          aTaxBandAllocation(1000, NilTaxBand),
          aTaxBandAllocation(3000, BasicTaxBand),
          aTaxBandAllocation(0, HigherTaxBand),
          aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "calculate tax - only dividends income, which is allocated only to nil tax band because of personal allowance" in {

      val liability = liabilityCalculationFor(dividendsIncome = 15000)

      liability.dividendsIncome shouldBe Seq(
          aTaxBandAllocation(4000, NilTaxBand),
          aTaxBandAllocation(0, BasicTaxBand),
          aTaxBandAllocation(0, HigherTaxBand),
          aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "calculate tax - only dividends income, which is allocated across all the tax bands" in {

      val liability = liabilityCalculationFor(dividendsIncome = 160000)

      liability.dividendsIncome shouldBe Seq(
          aTaxBandAllocation(5000, NilTaxBand),
          aTaxBandAllocation(27000, BasicTaxBand),
          aTaxBandAllocation(118000, HigherTaxBand),
          aTaxBandAllocation(10000, AdditionalHigherTaxBand)
      )
    }

    "calculate tax - savings and dividends income" in {

      val liability = liabilityCalculationFor(savingsIncome = 20000, dividendsIncome = 80000)

      liability.savingsIncome shouldBe Seq(
          aTaxBandAllocation(5000, SavingsStartingTaxBand),
          aTaxBandAllocation(500, NilTaxBand),
          aTaxBandAllocation(3500, BasicTaxBand),
          aTaxBandAllocation(0, HigherTaxBand),
          aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
      liability.dividendsIncome shouldBe Seq(
          aTaxBandAllocation(5000, NilTaxBand),
          aTaxBandAllocation(18000, BasicTaxBand),
          aTaxBandAllocation(57000, HigherTaxBand),
          aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "calculate tax - savings and dividends income, which is allocated across all the tax bands" in {

      val liability = liabilityCalculationFor(savingsIncome = 20000, dividendsIncome = 150000)

      liability.savingsIncome shouldBe Seq(
          aTaxBandAllocation(5000, SavingsStartingTaxBand),
          aTaxBandAllocation(0, NilTaxBand),
          aTaxBandAllocation(15000, BasicTaxBand),
          aTaxBandAllocation(0, HigherTaxBand),
          aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
      liability.dividendsIncome shouldBe Seq(
          aTaxBandAllocation(5000, NilTaxBand),
          aTaxBandAllocation(7000, BasicTaxBand),
          aTaxBandAllocation(118000, HigherTaxBand),
          aTaxBandAllocation(20000, AdditionalHigherTaxBand)
      )
    }

    "calculate tax - non savings and savings income - all within personal allowance and savings starting tax band" in {

      val liability = liabilityCalculationFor(nonSavingsIncome = 9000, savingsIncome = 6000)

      liability.nonSavingsIncome shouldBe Seq(
          aTaxBandAllocation(0, BasicTaxBand),
          aTaxBandAllocation(0, HigherTaxBand),
          aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
      liability.savingsIncome shouldBe Seq(
          aTaxBandAllocation(4000, SavingsStartingTaxBand),
          aTaxBandAllocation(0, NilTaxBand),
          aTaxBandAllocation(0, BasicTaxBand),
          aTaxBandAllocation(0, HigherTaxBand),
          aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "calculate tax - non savings and savings income - savings income allocated up to higher tax band" in {

      val liability = liabilityCalculationFor(nonSavingsIncome = 36000, savingsIncome = 12000)

      liability.nonSavingsIncome shouldBe Seq(
          aTaxBandAllocation(25000, BasicTaxBand),
          aTaxBandAllocation(0, HigherTaxBand),
          aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
      liability.savingsIncome shouldBe Seq(
          aTaxBandAllocation(0, SavingsStartingTaxBand),
          aTaxBandAllocation(500, NilTaxBand),
          aTaxBandAllocation(6500, BasicTaxBand),
          aTaxBandAllocation(5000, HigherTaxBand),
          aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "calculate tax - non savings and dividend income - dividends allocated only to higher and additional higher tax bands" in {

      val liability = liabilityCalculationFor(nonSavingsIncome = 50000, dividendsIncome = 120000)

      liability.nonSavingsIncome shouldBe Seq(
          aTaxBandAllocation(32000, BasicTaxBand),
          aTaxBandAllocation(18000, HigherTaxBand),
          aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
      liability.dividendsIncome shouldBe Seq(
          aTaxBandAllocation(5000, NilTaxBand),
          aTaxBandAllocation(0, BasicTaxBand),
          aTaxBandAllocation(95000, HigherTaxBand),
          aTaxBandAllocation(20000, AdditionalHigherTaxBand)
      )
    }

    "calculate tax - non savings, savings and dividends income" in {

      val liability = liabilityCalculationFor(nonSavingsIncome = 20000, savingsIncome = 20000, dividendsIncome = 10000)

      liability.nonSavingsIncome shouldBe Seq(
          aTaxBandAllocation(9000, BasicTaxBand),
          aTaxBandAllocation(0, HigherTaxBand),
          aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
      liability.savingsIncome shouldBe Seq(
          aTaxBandAllocation(0, SavingsStartingTaxBand),
          aTaxBandAllocation(500, NilTaxBand),
          aTaxBandAllocation(19500, BasicTaxBand),
          aTaxBandAllocation(0, HigherTaxBand),
          aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
      liability.dividendsIncome shouldBe Seq(
          aTaxBandAllocation(5000, NilTaxBand),
          aTaxBandAllocation(0, BasicTaxBand),
          aTaxBandAllocation(5000, HigherTaxBand),
          aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "run the liability calculation steps until a calculation error occurs" in {

      val ukTaxPaidSummary1 = anEmploymentUkTaxPaidSummary("ukTaxPaid1", -812.45)
      val ukTaxPaidSummary2 = anEmploymentUkTaxPaidSummary("ukTaxPaid2", 234.87)
      val employments = anEmployment().copy(ukTaxPaid = Seq(ukTaxPaidSummary1, ukTaxPaidSummary2))

      val selfAssessment =
        SelfAssessment(selfEmployments =
                         Seq(aSelfEmployment().copy(incomes = Seq(selfEmploymentIncome(IncomeType.Turnover, 20000)))),
                       employments = Seq(employments))

      val (_, errorLiabilities) =
        LiabilityCalculator().runSteps(selfAssessment, MongoLiability.create(generateSaUtr(), taxYear))

      errorLiabilities should not be empty
    }
  }

  private def liabilityCalculationFor(nonSavingsIncome: BigDecimal = 0,
                                      savingsIncome: BigDecimal = 0,
                                      dividendsIncome: BigDecimal = 0) = {

    val selfAssessment = SelfAssessment(
        selfEmployments = Seq(
            aSelfEmployment().copy(incomes = Seq(selfEmploymentIncome(IncomeType.Turnover, nonSavingsIncome)))
        ),
        unearnedIncomes = Seq(
            anUnearnedIncomes().copy(savings = Seq(anUnearnedInterestIncomeSummary(amount = savingsIncome))),
            anUnearnedIncomes().copy(dividends = Seq(anUnearnedDividendIncomeSummary(amount = dividendsIncome)))
        )
    )

    LiabilityCalculator().calculate(selfAssessment, MongoLiability.create(generateSaUtr(), taxYear)).getLiabilityOrFail
  }

}
