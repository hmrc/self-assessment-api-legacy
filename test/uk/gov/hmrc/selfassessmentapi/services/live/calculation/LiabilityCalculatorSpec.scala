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
import uk.gov.hmrc.selfassessmentapi.{SelfEmploymentSugar, UnitSpec}

class LiabilityCalculatorSpec extends UnitSpec with SelfEmploymentSugar {

  "calculate" should {

    "calculate tax (nonSavingsIncome = 150000)" in {

      val liability = liabilityCalculationFor(nonSavingsIncome = 180000)

      liability.payPensionsProfitsIncome shouldBe Seq(
        aTaxBandAllocation(32000, BasicTaxBand),
        aTaxBandAllocation(118000, HigherTaxBand),
        aTaxBandAllocation(30000, AdditionalHigherTaxBand)
      )
      liability.savingsIncome shouldBe Seq(
        aTaxBandAllocation(0, SavingsStartingTaxBand),
        aTaxBandAllocation(0, SavingsNilTaxBand),
        aTaxBandAllocation(0, BasicTaxBand),
        aTaxBandAllocation(0, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "calculate tax (savingsIncome = 20000)" in {

      val liability = liabilityCalculationFor(savingsIncome = 20000)

      liability.savingsIncome shouldBe Seq(
        aTaxBandAllocation(5000, SavingsStartingTaxBand),
        aTaxBandAllocation(1000, SavingsNilTaxBand),
        aTaxBandAllocation(3000, BasicTaxBand),
        aTaxBandAllocation(0, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "calculate tax (dividendsIncome = 15000)" in {

      val liability = liabilityCalculationFor(dividendsIncome = 15000)

      liability.dividendsIncome shouldBe Seq(
        aTaxBandAllocation(4000, DividendsNilTaxBand),
        aTaxBandAllocation(0, DividendBasicTaxBand),
        aTaxBandAllocation(0, DividendHigherTaxBand),
        aTaxBandAllocation(0, DividendAdditionalHigherTaxBand)
      )
    }

    "calculate tax (dividendsIncome = 160000)" in {

      val liability = liabilityCalculationFor(dividendsIncome = 160000)

      liability.dividendsIncome shouldBe Seq(
        aTaxBandAllocation(5000, DividendsNilTaxBand),
        aTaxBandAllocation(32000, DividendBasicTaxBand),
        aTaxBandAllocation(118000, DividendHigherTaxBand),
        aTaxBandAllocation(5000, DividendAdditionalHigherTaxBand)
      )
    }

    //TODO ignored until bug with dividends is fixed
    "calculate tax (savingsIncome = 20000, dividendsIncome = 80000)" ignore {

      val liability = liabilityCalculationFor(savingsIncome = 20000, dividendsIncome = 80000)

      liability.savingsIncome shouldBe Seq(
        aTaxBandAllocation(5000, SavingsStartingTaxBand),
        aTaxBandAllocation(500, SavingsNilTaxBand),
        aTaxBandAllocation(3500, BasicTaxBand),
        aTaxBandAllocation(0, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
      liability.dividendsIncome shouldBe Seq(
        aTaxBandAllocation(5000, DividendsNilTaxBand),
        aTaxBandAllocation(18000, DividendBasicTaxBand),
        aTaxBandAllocation(57000, DividendHigherTaxBand),
        aTaxBandAllocation(0, DividendAdditionalHigherTaxBand)
      )
    }

    //TODO ignored until bug with dividends is fixed
    "calculate tax (savingsIncome = 20000, dividendsIncome = 130000)" ignore {

      val liability = liabilityCalculationFor(savingsIncome = 20000, dividendsIncome = 130000)

      liability.savingsIncome shouldBe Seq(
        aTaxBandAllocation(5000, SavingsStartingTaxBand),
        aTaxBandAllocation(500, SavingsNilTaxBand),
        aTaxBandAllocation(14500, BasicTaxBand),
        aTaxBandAllocation(0, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
      liability.dividendsIncome shouldBe Seq(
        aTaxBandAllocation(5000, DividendsNilTaxBand),
        aTaxBandAllocation(7000, DividendBasicTaxBand),
        aTaxBandAllocation(118000, DividendHigherTaxBand),
        aTaxBandAllocation(20000, DividendAdditionalHigherTaxBand)
      )
    }

    "calculate tax (nonSavingsIncome = 9000, savingsIncome = 6000)" in {

      val liability = liabilityCalculationFor(nonSavingsIncome = 9000, savingsIncome = 6000)

      liability.payPensionsProfitsIncome shouldBe Seq(
        aTaxBandAllocation(0, BasicTaxBand),
        aTaxBandAllocation(0, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
      liability.savingsIncome shouldBe Seq(
        aTaxBandAllocation(4000, SavingsStartingTaxBand),
        aTaxBandAllocation(0, SavingsNilTaxBand),
        aTaxBandAllocation(0, BasicTaxBand),
        aTaxBandAllocation(0, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "calculate tax (nonSavingsIncome = 36000, savingsIncome = 12000)" in {

      val liability = liabilityCalculationFor(nonSavingsIncome = 36000, savingsIncome = 12000)

      liability.payPensionsProfitsIncome shouldBe Seq(
        aTaxBandAllocation(25000, BasicTaxBand),
        aTaxBandAllocation(0, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
      liability.savingsIncome shouldBe Seq(
        aTaxBandAllocation(0, SavingsStartingTaxBand),
        aTaxBandAllocation(500, SavingsNilTaxBand),
        aTaxBandAllocation(6500, BasicTaxBand),
        aTaxBandAllocation(5000, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "calculate tax (nonSavingsIncome = 50000, dividendsIncome = 120000)" in {

      val liability = liabilityCalculationFor(nonSavingsIncome = 50000, dividendsIncome = 120000)

      liability.payPensionsProfitsIncome shouldBe Seq(
        aTaxBandAllocation(32000, BasicTaxBand),
        aTaxBandAllocation(18000, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
      liability.dividendsIncome shouldBe Seq(
        aTaxBandAllocation(5000, DividendsNilTaxBand),
        aTaxBandAllocation(0, DividendBasicTaxBand),
        aTaxBandAllocation(100000, DividendHigherTaxBand),
        aTaxBandAllocation(15000, DividendAdditionalHigherTaxBand)
      )
    }

    "calculate tax (nonSavingsIncome = 9000, savingsIncome = 6000, dividendsIncome = 10000)" in {

      val liability = liabilityCalculationFor(nonSavingsIncome = 20000, savingsIncome = 20000, dividendsIncome = 10000)

      liability.payPensionsProfitsIncome shouldBe Seq(
        aTaxBandAllocation(9000, BasicTaxBand),
        aTaxBandAllocation(0, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
      liability.savingsIncome shouldBe Seq(
        aTaxBandAllocation(0, SavingsStartingTaxBand),
        aTaxBandAllocation(500, SavingsNilTaxBand),
        aTaxBandAllocation(19500, BasicTaxBand),
        aTaxBandAllocation(0, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
      liability.dividendsIncome shouldBe Seq(
        aTaxBandAllocation(5000, DividendsNilTaxBand),
        aTaxBandAllocation(3000, DividendBasicTaxBand),
        aTaxBandAllocation(2000, DividendHigherTaxBand),
        aTaxBandAllocation(0, DividendAdditionalHigherTaxBand)
      )
    }
  }

  private def liabilityCalculationFor(nonSavingsIncome: BigDecimal = 0, savingsIncome: BigDecimal = 0, dividendsIncome: BigDecimal = 0) = {

    val selfAssessment = SelfAssessment(
      selfEmployments = Seq(
        aSelfEmployment().copy(incomes = Seq(income(IncomeType.Turnover, nonSavingsIncome)))
      ),
      unearnedIncomes = Seq(
        anUnearnedIncomes().copy(savings = Seq(anUnearnedInterestIncomeSummary(amount = savingsIncome))),
        anUnearnedIncomes().copy(dividends = Seq(anUnearnedDividendIncomeSummary(amount = dividendsIncome)))
      )
    )

    LiabilityCalculator().calculate(selfAssessment, MongoLiability.create(generateSaUtr(), taxYear))
  }
}
