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

import uk.gov.hmrc.selfassessmentapi.domain.InterestFromUKBanksAndBuildingSocieties
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBand._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBandAllocation
import uk.gov.hmrc.selfassessmentapi.{SelfAssessmentSugar, UnitSpec}

class SavingsIncomeTaxCalculationSpec extends UnitSpec with SelfAssessmentSugar {

  "run" should {

    "not allocate tax to any of the tax bands if remaining deductions are bigger than savings income" in {

      savingsIncomeTaxFor(interestFromUKBanks = 6000, remainingDeductions = 7000, savingsStartingRate = 5000) shouldBe Seq(
        aTaxBandAllocation(0, SavingsStartingTaxBand),
        aTaxBandAllocation(0, NilTaxBand),
        aTaxBandAllocation(0, BasicTaxBand),
        aTaxBandAllocation(0, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "allocate tax to SavingsStartingTaxBand (savingsIncome < savingsStartingRate)" in {

      savingsIncomeTaxFor(interestFromUKBanks = 4500, savingsStartingRate = 5000) shouldBe Seq(
        aTaxBandAllocation(4500, SavingsStartingTaxBand),
        aTaxBandAllocation(0, NilTaxBand),
        aTaxBandAllocation(0, BasicTaxBand),
        aTaxBandAllocation(0, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "allocate tax to SavingsStartingTaxBand and NilTaxBand (savingsIncome < savingsStartingRate + personalSavingsAllowance)" in {

      savingsIncomeTaxFor(interestFromUKBanks = 5500, savingsStartingRate = 5000, personalSavingsAllowance = 1000) shouldBe Seq(
        aTaxBandAllocation(5000, SavingsStartingTaxBand),
        aTaxBandAllocation(500, NilTaxBand),
        aTaxBandAllocation(0, BasicTaxBand),
        aTaxBandAllocation(0, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "allocate tax to SavingsStartingTaxBand, NilTaxBand and BasicTaxBand" in {

      savingsIncomeTaxFor(interestFromUKBanks = 30000, savingsStartingRate = 5000, personalSavingsAllowance = 1000) shouldBe Seq(
        aTaxBandAllocation(5000, SavingsStartingTaxBand),
        aTaxBandAllocation(1000, NilTaxBand),
        aTaxBandAllocation(24000, BasicTaxBand),
        aTaxBandAllocation(0, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "allocate tax only to BasicTaxBand" in {

      savingsIncomeTaxFor(interestFromUKBanks = 32000, savingsStartingRate = 0, personalSavingsAllowance = 0) shouldBe Seq(
        aTaxBandAllocation(0, SavingsStartingTaxBand),
        aTaxBandAllocation(0, NilTaxBand),
        aTaxBandAllocation(32000, BasicTaxBand),
        aTaxBandAllocation(0, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "allocate tax to BasicTaxBand and HigherTaxBand (savingsStartingRate = 0, personalSavingsAllowance = 0)" in {

      savingsIncomeTaxFor(interestFromUKBanks = 70000, savingsStartingRate = 0, personalSavingsAllowance = 0) shouldBe Seq(
        aTaxBandAllocation(0, SavingsStartingTaxBand),
        aTaxBandAllocation(0, NilTaxBand),
        aTaxBandAllocation(32000, BasicTaxBand),
        aTaxBandAllocation(38000, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "allocate tax to BasicTaxBand, HigherTaxBand and AdditionalHigherTaxBand" in {

      savingsIncomeTaxFor(interestFromUKBanks = 155000, savingsStartingRate = 0, personalSavingsAllowance = 0) shouldBe Seq(
        aTaxBandAllocation(0, SavingsStartingTaxBand),
        aTaxBandAllocation(0, NilTaxBand),
        aTaxBandAllocation(32000, BasicTaxBand),
        aTaxBandAllocation(118000, HigherTaxBand),
        aTaxBandAllocation(5000, AdditionalHigherTaxBand)
      )
    }

    "allocate tax to SavingsStartingTaxBand, NilTaxBand, BasicTaxBand and HigherTaxBand" in {

      savingsIncomeTaxFor(interestFromUKBanks = 60000, savingsStartingRate = 1000, personalSavingsAllowance = 1000) shouldBe Seq(
        aTaxBandAllocation(1000, SavingsStartingTaxBand),
        aTaxBandAllocation(1000, NilTaxBand),
        aTaxBandAllocation(30000, BasicTaxBand),
        aTaxBandAllocation(28000, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "allocate tax to SavingsStartingTaxBand, NilTaxBand, BasicTaxBand, HigherTaxBand and AdditionalHigherTaxBand" in {

      savingsIncomeTaxFor(interestFromUKBanks = 170000, savingsStartingRate = 1000, personalSavingsAllowance = 1000) shouldBe Seq(
        aTaxBandAllocation(1000, SavingsStartingTaxBand),
        aTaxBandAllocation(1000, NilTaxBand),
        aTaxBandAllocation(30000, BasicTaxBand),
        aTaxBandAllocation(118000, HigherTaxBand),
        aTaxBandAllocation(20000, AdditionalHigherTaxBand)
      )
    }

    "allocate tax to SavingsStartingTaxBand, NilTaxBand and HigherTaxBand if BasicTaxBand is already fully allocated" in {

      savingsIncomeTaxFor(interestFromUKBanks = 50000, savingsStartingRate = 1000, personalSavingsAllowance = 1000, basicTaxBandAllocated = 32000) shouldBe Seq(
        aTaxBandAllocation(1000, SavingsStartingTaxBand),
        aTaxBandAllocation(1000, NilTaxBand),
        aTaxBandAllocation(0, BasicTaxBand),
        aTaxBandAllocation(48000, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "allocate tax to BasicTaxBand and HigherTaxBand if BasicTaxBand is partially allocated by nonSavingsProfit" in {

      savingsIncomeTaxFor(interestFromUKBanks = 50000, basicTaxBandAllocated = 20000) shouldBe Seq(
        aTaxBandAllocation(0, SavingsStartingTaxBand),
        aTaxBandAllocation(0, NilTaxBand),
        aTaxBandAllocation(12000, BasicTaxBand),
        aTaxBandAllocation(38000, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "allocate tax to BasicTaxBand, HigherTaxBand and AdditionalHigherTaxBand if BasicTaxBand is partially allocated by nonSavingsProfit" in {

      savingsIncomeTaxFor(interestFromUKBanks = 150000, basicTaxBandAllocated = 20000) shouldBe Seq(
        aTaxBandAllocation(0, SavingsStartingTaxBand),
        aTaxBandAllocation(0, NilTaxBand),
        aTaxBandAllocation(12000, BasicTaxBand),
        aTaxBandAllocation(118000, HigherTaxBand),
        aTaxBandAllocation(20000, AdditionalHigherTaxBand)
      )
    }

    "allocate tax to HigherTaxBand and AdditionalHigherTaxBand if BasicTaxBand is already fully allocated by nonSavingsProfit" in {

      savingsIncomeTaxFor(interestFromUKBanks = 150000, basicTaxBandAllocated = 32000) shouldBe Seq(
        aTaxBandAllocation(0, SavingsStartingTaxBand),
        aTaxBandAllocation(0, NilTaxBand),
        aTaxBandAllocation(0, BasicTaxBand),
        aTaxBandAllocation(118000, HigherTaxBand),
        aTaxBandAllocation(32000, AdditionalHigherTaxBand)
      )
    }

    "allocate tax to AdditionalHigherTaxBand if both BasicTaxBand and HigherTaxBand are already fully allocated by nonSavingsProfit" in {

      savingsIncomeTaxFor(interestFromUKBanks = 10000, basicTaxBandAllocated = 32000, higherTaxBandAllocated = 118000) shouldBe Seq(
        aTaxBandAllocation(0, SavingsStartingTaxBand),
        aTaxBandAllocation(0, NilTaxBand),
        aTaxBandAllocation(0, BasicTaxBand),
        aTaxBandAllocation(0, HigherTaxBand),
        aTaxBandAllocation(10000, AdditionalHigherTaxBand)
      )
    }

    "allocate tax to AdditionalHigherTaxBand if AdditionalHigherTaxBand is partially, BasicTaxBand and HigherTaxBand are already fully allocated by nonSavingsProfit" in {

      savingsIncomeTaxFor(interestFromUKBanks = 10000, basicTaxBandAllocated = 32000, higherTaxBandAllocated = 118000, additionalHigherTaxBandAllocated = 100000) shouldBe Seq(
        aTaxBandAllocation(0, SavingsStartingTaxBand),
        aTaxBandAllocation(0, NilTaxBand),
        aTaxBandAllocation(0, BasicTaxBand),
        aTaxBandAllocation(0, HigherTaxBand),
        aTaxBandAllocation(10000, AdditionalHigherTaxBand)
      )
    }
  }

  "reduce total remaining deductions to zero if interestFromUKBanks is greater than remaining deductions" in {

    val liability = liabilityFor(interestFromUkBanks = 4000, totalRemainingDeductions = 3000)

    liability.deductionsRemaining shouldBe Some(0)
  }

  "reduce total remaining deductions by the interestFromUKBanks if interestFromUKBanks is less than remaining deductions" in {

    val liability = liabilityFor(interestFromUkBanks = 1000, totalRemainingDeductions = 3000)

    liability.deductionsRemaining shouldBe Some(2000)
  }

  "not reduce total remaining deductions if interestFromUKBanks is zero" in {

    val liability = liabilityFor(interestFromUkBanks = 0, totalRemainingDeductions = 3000)

    liability.deductionsRemaining shouldBe Some(3000)
  }

  private def liabilityFor(interestFromUkBanks: BigDecimal, totalRemainingDeductions: BigDecimal) = {
    val liability = aLiability(
      interestFromUKBanksAndBuildingSocieties = Seq(InterestFromUKBanksAndBuildingSocieties("ue1", interestFromUkBanks)),
      deductionsRemaining = Some(totalRemainingDeductions),
      personalSavingsAllowance = Some(0),
      savingsStartingRate = Some(0)
    )

    SavingsIncomeTaxCalculation.run(SelfAssessment(), liability).getLiabilityOrFail
  }

  private def savingsIncomeTaxFor(interestFromUKBanks: BigDecimal, remainingDeductions: BigDecimal = 0, savingsStartingRate: BigDecimal = 0, personalSavingsAllowance: BigDecimal = 0,
                                  basicTaxBandAllocated: BigDecimal = 0, higherTaxBandAllocated: BigDecimal = 0, additionalHigherTaxBandAllocated: BigDecimal = 0) = {
    val liability = aLiability(
      interestFromUKBanksAndBuildingSocieties = Seq(InterestFromUKBanksAndBuildingSocieties("ue1", interestFromUKBanks)),
      deductionsRemaining = Some(remainingDeductions),
      personalSavingsAllowance = Some(personalSavingsAllowance),
      savingsStartingRate = Some(savingsStartingRate)
    ).copy(
      nonSavingsIncome = Seq(
        TaxBandAllocation(basicTaxBandAllocated, BasicTaxBand),
        TaxBandAllocation(higherTaxBandAllocated, HigherTaxBand),
        TaxBandAllocation(additionalHigherTaxBandAllocated, AdditionalHigherTaxBand)
      )
    )

    SavingsIncomeTaxCalculation.run(SelfAssessment(), liability).getLiabilityOrFail.savingsIncome
  }
}
