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

import uk.gov.hmrc.selfassessmentapi.domain.{DividendsFromUKSources, InterestFromUKBanksAndBuildingSocieties}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBand._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBandAllocation
import uk.gov.hmrc.selfassessmentapi.{SelfEmploymentSugar, UnitSpec}

class DividendsTaxCalculationSpec extends UnitSpec with SelfEmploymentSugar {

  "for income from dividends only" should {

    "calculate tax for dividend only income lesser than 5000" in {
      dividendTaxFor(Seq(DividendsFromUKSources(sourceId = "", totalDividend = 4000))) shouldBe Seq(
        aTaxBandAllocation(4000, DividendsNilTaxBand),
        aTaxBandAllocation(0, DividendBasicTaxBand),
        aTaxBandAllocation(0, DividendHigherTaxBand),
        aTaxBandAllocation(0, DividendAdditionalHigherTaxBand)
      )
    }

    "calculate tax for dividend only income between 5000 and 32000" in {
      dividendTaxFor(Seq(DividendsFromUKSources(sourceId = "", totalDividend = 30000)), remainingDeductions = 1000) shouldBe Seq(
        aTaxBandAllocation(5000, DividendsNilTaxBand),
        aTaxBandAllocation(24000, DividendBasicTaxBand),
        aTaxBandAllocation(0, DividendHigherTaxBand),
        aTaxBandAllocation(0, DividendAdditionalHigherTaxBand)
      )
    }

    "calculate tax for dividend only income between 37000 and 155000" in {
      dividendTaxFor(Seq(DividendsFromUKSources(sourceId = "", totalDividend = 100000)), remainingDeductions = 1000) shouldBe Seq(
        aTaxBandAllocation(5000, DividendsNilTaxBand),
        aTaxBandAllocation(32000, DividendBasicTaxBand),
        aTaxBandAllocation(62000, DividendHigherTaxBand),
        aTaxBandAllocation(0, DividendAdditionalHigherTaxBand)
      )
    }

    "calculate tax for dividend only income greater than 155000" in {
      dividendTaxFor(Seq(DividendsFromUKSources(sourceId = "", totalDividend = 500000),
        DividendsFromUKSources(sourceId = "", totalDividend = 500000)), remainingDeductions = 1000) shouldBe Seq(
        aTaxBandAllocation(5000, DividendsNilTaxBand),
        aTaxBandAllocation(32000, DividendBasicTaxBand),
        aTaxBandAllocation(118000, DividendHigherTaxBand),
        aTaxBandAllocation(844000, DividendAdditionalHigherTaxBand)
      )
    }

  }

  "for income from payment pension and profit and dividends" should {

    "calculate tax for self employment and dividend income" in {
      dividendTaxFor(dividendsFromUKSources = Seq(DividendsFromUKSources(sourceId = "", totalDividend = 2000)),
        basicTaxBandAllocated = 8000, remainingDeductions = 1000) shouldBe Seq(
        aTaxBandAllocation(1000, DividendsNilTaxBand),
        aTaxBandAllocation(0, DividendBasicTaxBand),
        aTaxBandAllocation(0, DividendHigherTaxBand),
        aTaxBandAllocation(0, DividendAdditionalHigherTaxBand)
      )
    }

    "calculate tax for self employment and dividend income greater than allowance" in {
      dividendTaxFor(dividendsFromUKSources = Seq(DividendsFromUKSources(sourceId = "", totalDividend = 6000)),
        basicTaxBandAllocated = 8000, remainingDeductions = 500) shouldBe Seq(
        aTaxBandAllocation(5000, DividendsNilTaxBand),
        aTaxBandAllocation(500, DividendBasicTaxBand),
        aTaxBandAllocation(0, DividendHigherTaxBand),
        aTaxBandAllocation(0, DividendAdditionalHigherTaxBand)
      )
    }

    "calculate tax for dividend income where the dividend income overflows from basic to higher bucket" in {
      dividendTaxFor(dividendsFromUKSources = Seq(DividendsFromUKSources(sourceId = "", totalDividend = 7000)),
        basicTaxBandAllocated = 31000) shouldBe Seq(
        aTaxBandAllocation(5000, DividendsNilTaxBand),
        aTaxBandAllocation(1000, DividendBasicTaxBand),
        aTaxBandAllocation(1000, DividendHigherTaxBand),
        aTaxBandAllocation(0, DividendAdditionalHigherTaxBand)
      )
    }

    "calculate tax for dividend income where the dividend income over the allowance goes entirely to higher bucket" in {
      dividendTaxFor(dividendsFromUKSources = Seq(DividendsFromUKSources(sourceId = "", totalDividend = 7000)),
        basicTaxBandAllocated = 32000) shouldBe Seq(
        aTaxBandAllocation(5000, DividendsNilTaxBand),
        aTaxBandAllocation(0, DividendBasicTaxBand),
        aTaxBandAllocation(2000, DividendHigherTaxBand),
        aTaxBandAllocation(0, DividendAdditionalHigherTaxBand)
      )
    }

    "calculate tax for dividend income where the dividend income overflows from higher to additional higher bucket" in {
      dividendTaxFor(dividendsFromUKSources = Seq(DividendsFromUKSources(sourceId = "", totalDividend = 7000)),
        basicTaxBandAllocated = 32000, higherTaxBandAllocated = 117000) shouldBe Seq(
        aTaxBandAllocation(5000, DividendsNilTaxBand),
        aTaxBandAllocation(0, DividendBasicTaxBand),
        aTaxBandAllocation(1000, DividendHigherTaxBand),
        aTaxBandAllocation(1000, DividendAdditionalHigherTaxBand)
      )
    }

    "calculate tax for dividend income where the dividend income over the allowance goes entirely to additional higher bucket" in {
      dividendTaxFor(dividendsFromUKSources = Seq(DividendsFromUKSources(sourceId = "", totalDividend = 7000)),
        basicTaxBandAllocated = 32000, higherTaxBandAllocated = 118000) shouldBe Seq(
        aTaxBandAllocation(5000, DividendsNilTaxBand),
        aTaxBandAllocation(0, DividendBasicTaxBand),
        aTaxBandAllocation(0, DividendHigherTaxBand),
        aTaxBandAllocation(2000, DividendAdditionalHigherTaxBand)
      )
    }

  }

  "for income from payment pension and profit, interest and dividends" should {

    "calculate tax for self employment interest and and dividend income" in {
      dividendTaxFor(dividendsFromUKSources = Seq(DividendsFromUKSources(sourceId = "", totalDividend = 2000)),
        basicTaxBandAllocated = 8000, basicTaxBandSavingsAllocated = 3000) shouldBe Seq(
        aTaxBandAllocation(2000, DividendsNilTaxBand),
        aTaxBandAllocation(0, DividendBasicTaxBand),
        aTaxBandAllocation(0, DividendHigherTaxBand),
        aTaxBandAllocation(0, DividendAdditionalHigherTaxBand)
      )
    }

    "calculate tax for self employment and dividend income greater than allowance" in {
      dividendTaxFor(dividendsFromUKSources = Seq(DividendsFromUKSources(sourceId = "", totalDividend = 6000)),
        basicTaxBandAllocated = 8000, basicTaxBandSavingsAllocated = 3000) shouldBe Seq(
        aTaxBandAllocation(5000, DividendsNilTaxBand),
        aTaxBandAllocation(1000, DividendBasicTaxBand),
        aTaxBandAllocation(0, DividendHigherTaxBand),
        aTaxBandAllocation(0, DividendAdditionalHigherTaxBand)
      )
    }

    "calculate tax for dividend income where the dividend income overflows from basic to higher bucket" in {
      dividendTaxFor(dividendsFromUKSources = Seq(DividendsFromUKSources(sourceId = "", totalDividend = 7000)),
        basicTaxBandAllocated = 20000, basicTaxBandSavingsAllocated = 11000) shouldBe Seq(
        aTaxBandAllocation(5000, DividendsNilTaxBand),
        aTaxBandAllocation(1000, DividendBasicTaxBand),
        aTaxBandAllocation(1000, DividendHigherTaxBand),
        aTaxBandAllocation(0, DividendAdditionalHigherTaxBand)
      )
    }

    "calculate tax for dividend income where the dividend income over the allowance goes entirely to higher bucket" in {
      dividendTaxFor(dividendsFromUKSources = Seq(DividendsFromUKSources(sourceId = "", totalDividend = 7000)),
        basicTaxBandAllocated = 20000, basicTaxBandSavingsAllocated = 12000) shouldBe Seq(
        aTaxBandAllocation(5000, DividendsNilTaxBand),
        aTaxBandAllocation(0, DividendBasicTaxBand),
        aTaxBandAllocation(2000, DividendHigherTaxBand),
        aTaxBandAllocation(0, DividendAdditionalHigherTaxBand)
      )
    }

    "calculate tax for dividend income where the dividend income overflows from higher to additional higher bucket" in {
      dividendTaxFor(dividendsFromUKSources = Seq(DividendsFromUKSources(sourceId = "", totalDividend = 7000)),
        basicTaxBandAllocated = 32000, higherTaxBandAllocated = 110000,
        higherTaxBandSavingsAllocated = 7000) shouldBe Seq(
        aTaxBandAllocation(5000, DividendsNilTaxBand),
        aTaxBandAllocation(0, DividendBasicTaxBand),
        aTaxBandAllocation(1000, DividendHigherTaxBand),
        aTaxBandAllocation(1000, DividendAdditionalHigherTaxBand)
      )
    }

    "calculate tax for dividend income where the dividend income over the allowance goes entirely to additional higher bucket" in {
      dividendTaxFor(dividendsFromUKSources = Seq(DividendsFromUKSources(sourceId = "", totalDividend = 7000)),
        basicTaxBandAllocated = 32000, higherTaxBandAllocated = 110000,
        higherTaxBandSavingsAllocated = 8000) shouldBe Seq(
        aTaxBandAllocation(5000, DividendsNilTaxBand),
        aTaxBandAllocation(0, DividendBasicTaxBand),
        aTaxBandAllocation(0, DividendHigherTaxBand),
        aTaxBandAllocation(2000, DividendAdditionalHigherTaxBand)
      )
    }
  }

  private def liabilityFor(interestFromUkBanks: BigDecimal, totalRemainingDeductions: BigDecimal) = {
    val liability = aLiability(
      interestFromUKBanksAndBuildingSocieties = Seq(InterestFromUKBanksAndBuildingSocieties("ue1", interestFromUkBanks)),
      deductionsRemaining = Some(totalRemainingDeductions),
      personalSavingsAllowance = Some(0),
      savingsStartingRate = Some(0)
    )

    SavingsIncomeTaxCalculation.run(SelfAssessment(), liability)
  }

  private def dividendTaxFor(dividendsFromUKSources: Seq[DividendsFromUKSources] = Nil,
                             remainingDeductions: BigDecimal = 0,
                             basicTaxBandAllocated: BigDecimal = 0,
                             higherTaxBandAllocated: BigDecimal = 0,
                             additionalHigherTaxBandAllocated: BigDecimal = 0,
                             savingsStartingRateAllocated: BigDecimal = 0,
                             savingsNilRateAllocated: BigDecimal = 0,
                             basicTaxBandSavingsAllocated: BigDecimal = 0,
                             higherTaxBandSavingsAllocated: BigDecimal = 0,
                             additionalHigherTaxBandSavingsAllocated: BigDecimal = 0) = {
    val liability = aLiability(
      dividendsFromUKSources = dividendsFromUKSources,
      deductionsRemaining = Some(remainingDeductions)
    ).copy(
      payPensionsProfitsIncome = Seq(
        TaxBandAllocation(basicTaxBandAllocated, BasicTaxBand),
        TaxBandAllocation(higherTaxBandAllocated, HigherTaxBand),
        TaxBandAllocation(additionalHigherTaxBandAllocated, AdditionalHigherTaxBand)
      ),
      savingsIncome = Seq(
        TaxBandAllocation(savingsStartingRateAllocated, SavingsStartingTaxBand),
        TaxBandAllocation(savingsNilRateAllocated, SavingsNilTaxBand),
        TaxBandAllocation(basicTaxBandSavingsAllocated, BasicTaxBand),
        TaxBandAllocation(higherTaxBandSavingsAllocated, HigherTaxBand),
        TaxBandAllocation(additionalHigherTaxBandSavingsAllocated, AdditionalHigherTaxBand)
      )
    )

    DividendsTaxCalculation.run(SelfAssessment(), liability).dividendsIncome
  }
}
