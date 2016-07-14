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

import uk.gov.hmrc.selfassessmentapi.domain.{Deductions, InterestFromUKBanksAndBuildingSocieties}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBand._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBandAllocation
import uk.gov.hmrc.selfassessmentapi.{SelfEmploymentSugar, UnitSpec}

class SavingsIncomeTaxCalculationSpec extends UnitSpec with SelfEmploymentSugar {

  "run" should {

    "not allocate tax to any of the tax bands if remaining deductions are bigger than savings income" in {

      savingsIncomeTaxFor(interest = 6000, remainingDeductions = Deductions(0, 7000), savingsStartingRate = 5000) shouldBe Seq(
        aTaxBandAllocation(0, SavingsStartingTaxBand),
        aTaxBandAllocation(0, SavingsNilTaxBand),
        aTaxBandAllocation(0, BasicTaxBand),
        aTaxBandAllocation(0, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "allocate tax to SavingsStartingTaxBand (savingsIncome < savingsStartingRate)" in {

      savingsIncomeTaxFor(interest = 4500, savingsStartingRate = 5000) shouldBe Seq(
        aTaxBandAllocation(4500, SavingsStartingTaxBand),
        aTaxBandAllocation(0, SavingsNilTaxBand),
        aTaxBandAllocation(0, BasicTaxBand),
        aTaxBandAllocation(0, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "allocate tax to SavingsStartingTaxBand and SavingsNilTaxBand (savingsIncome < savingsStartingRate + personalSavingsAllowance)" in {

      savingsIncomeTaxFor(interest = 5500, savingsStartingRate = 5000, personalSavingsAllowance = 1000) shouldBe Seq(
        aTaxBandAllocation(5000, SavingsStartingTaxBand),
        aTaxBandAllocation(500, SavingsNilTaxBand),
        aTaxBandAllocation(0, BasicTaxBand),
        aTaxBandAllocation(0, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "allocate tax to SavingsStartingTaxBand, SavingsNilTaxBand and BasicTaxBand" in {

      savingsIncomeTaxFor(interest = 30000, savingsStartingRate = 5000, personalSavingsAllowance = 1000) shouldBe Seq(
        aTaxBandAllocation(5000, SavingsStartingTaxBand),
        aTaxBandAllocation(1000, SavingsNilTaxBand),
        aTaxBandAllocation(24000, BasicTaxBand),
        aTaxBandAllocation(0, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "allocate tax only to BasicTaxBand" in {

      savingsIncomeTaxFor(interest = 32000, savingsStartingRate = 0, personalSavingsAllowance = 0) shouldBe Seq(
        aTaxBandAllocation(0, SavingsStartingTaxBand),
        aTaxBandAllocation(0, SavingsNilTaxBand),
        aTaxBandAllocation(32000, BasicTaxBand),
        aTaxBandAllocation(0, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "allocate tax to BasicTaxBand and HigherTaxBand (savingsStartingRate = 0, personalSavingsAllowance = 0)" in {

      savingsIncomeTaxFor(interest = 70000, savingsStartingRate = 0, personalSavingsAllowance = 0) shouldBe Seq(
        aTaxBandAllocation(0, SavingsStartingTaxBand),
        aTaxBandAllocation(0, SavingsNilTaxBand),
        aTaxBandAllocation(32000, BasicTaxBand),
        aTaxBandAllocation(38000, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "allocate tax to BasicTaxBand, HigherTaxBand and AdditionalHigherTaxBand" in {

      savingsIncomeTaxFor(interest = 155000, savingsStartingRate = 0, personalSavingsAllowance = 0) shouldBe Seq(
        aTaxBandAllocation(0, SavingsStartingTaxBand),
        aTaxBandAllocation(0, SavingsNilTaxBand),
        aTaxBandAllocation(32000, BasicTaxBand),
        aTaxBandAllocation(118000, HigherTaxBand),
        aTaxBandAllocation(5000, AdditionalHigherTaxBand)
      )
    }

    "allocate tax to SavingsStartingTaxBand, SavingsNilTaxBand, BasicTaxBand, HigherTaxBand and AdditionalHigherTaxBand" in {

      savingsIncomeTaxFor(interest = 170000, savingsStartingRate = 1000, personalSavingsAllowance = 1000) shouldBe Seq(
        aTaxBandAllocation(1000, SavingsStartingTaxBand),
        aTaxBandAllocation(1000, SavingsNilTaxBand),
        aTaxBandAllocation(32000, BasicTaxBand),
        aTaxBandAllocation(118000, HigherTaxBand),
        aTaxBandAllocation(18000, AdditionalHigherTaxBand)
      )
    }

    "allocate tax to SavingsStartingTaxBand, SavingsNilTaxBand and HigherTaxBand if BasicTaxBand is already fully allocated" in {

      savingsIncomeTaxFor(interest = 50000, savingsStartingRate = 1000, personalSavingsAllowance = 1000, basicTaxBandAllocation = 32000) shouldBe Seq(
        aTaxBandAllocation(1000, SavingsStartingTaxBand),
        aTaxBandAllocation(1000, SavingsNilTaxBand),
        aTaxBandAllocation(0, BasicTaxBand),
        aTaxBandAllocation(48000, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "allocate tax to BasicTaxBand and HigherTaxBand if BasicTaxBand is partially allocated by payPensionProfit" in {

      savingsIncomeTaxFor(interest = 50000, basicTaxBandAllocation = 20000) shouldBe Seq(
        aTaxBandAllocation(0, SavingsStartingTaxBand),
        aTaxBandAllocation(0, SavingsNilTaxBand),
        aTaxBandAllocation(12000, BasicTaxBand),
        aTaxBandAllocation(38000, HigherTaxBand),
        aTaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "allocate tax to BasicTaxBand, HigherTaxBand and AdditionalHigherTaxBand if BasicTaxBand is partially allocated by payPensionProfit" in {

      savingsIncomeTaxFor(interest = 150000, basicTaxBandAllocation = 20000) shouldBe Seq(
        aTaxBandAllocation(0, SavingsStartingTaxBand),
        aTaxBandAllocation(0, SavingsNilTaxBand),
        aTaxBandAllocation(12000, BasicTaxBand),
        aTaxBandAllocation(118000, HigherTaxBand),
        aTaxBandAllocation(20000, AdditionalHigherTaxBand)
      )
    }

    "allocate tax to HigherTaxBand and AdditionalHigherTaxBand if BasicTaxBand is already fully allocated by payPensionProfit" in {

      savingsIncomeTaxFor(interest = 150000, basicTaxBandAllocation = 32000) shouldBe Seq(
        aTaxBandAllocation(0, SavingsStartingTaxBand),
        aTaxBandAllocation(0, SavingsNilTaxBand),
        aTaxBandAllocation(0, BasicTaxBand),
        aTaxBandAllocation(118000, HigherTaxBand),
        aTaxBandAllocation(32000, AdditionalHigherTaxBand)
      )
    }

    "allocate tax to AdditionalHigherTaxBand if both BasicTaxBand and HigherTaxBand are already fully allocated by payPensionProfit" in {

      savingsIncomeTaxFor(interest = 10000, basicTaxBandAllocation = 32000, higherTaxBandAllocation = 118000) shouldBe Seq(
        aTaxBandAllocation(0, SavingsStartingTaxBand),
        aTaxBandAllocation(0, SavingsNilTaxBand),
        aTaxBandAllocation(0, BasicTaxBand),
        aTaxBandAllocation(0, HigherTaxBand),
        aTaxBandAllocation(10000, AdditionalHigherTaxBand)
      )
    }

    "allocate tax to AdditionalHigherTaxBand if AdditionalHigherTaxBand is partially, BasicTaxBand and HigherTaxBand are already fully allocated by payPensionProfit" in {

      savingsIncomeTaxFor(interest = 10000, basicTaxBandAllocation = 32000, higherTaxBandAllocation = 118000, additionalHigherTaxBandAllocation = 100000) shouldBe Seq(
        aTaxBandAllocation(0, SavingsStartingTaxBand),
        aTaxBandAllocation(0, SavingsNilTaxBand),
        aTaxBandAllocation(0, BasicTaxBand),
        aTaxBandAllocation(0, HigherTaxBand),
        aTaxBandAllocation(10000, AdditionalHigherTaxBand)
      )
    }
  }

  "reduce total remaining deductions to zero if interest is greater than remaining deductions" in {

    val liability = liabilityFor(interest = 4000, totalRemainingDeductions = 3000)

    liability.deductionsRemaining shouldBe Some(Deductions(incomeTaxRelief = 0, totalDeductions = 0))
    liability.deductions shouldBe Some(Deductions(incomeTaxRelief = 0, totalDeductions = 3000))
  }

  "reduce total remaining deductions by the interest if interest is less than remaining deductions" in {

    val liability = liabilityFor(interest = 1000, totalRemainingDeductions = 3000)

    liability.deductionsRemaining shouldBe Some(Deductions(incomeTaxRelief = 0, totalDeductions = 2000))
    liability.deductions shouldBe Some(Deductions(incomeTaxRelief = 0, totalDeductions = 3000))
  }

  "not reduce total remaining deductions if interest is zero" in {

    val liability = liabilityFor(interest = 0, totalRemainingDeductions = 3000)

    liability.deductionsRemaining shouldBe Some(Deductions(incomeTaxRelief = 0, totalDeductions = 3000))
    liability.deductions shouldBe Some(Deductions(incomeTaxRelief = 0, totalDeductions = 3000))
  }

  private def liabilityFor(interest: BigDecimal, totalRemainingDeductions: BigDecimal) = {
    val liability = aLiability(
      interestFromUKBanksAndBuildingSocieties = Seq(InterestFromUKBanksAndBuildingSocieties("ue1", interest)),
      deductionsRemaining = Some(Deductions(incomeTaxRelief = 0, totalDeductions = totalRemainingDeductions)),
      deductions = Some(Deductions(incomeTaxRelief = 0, totalDeductions = totalRemainingDeductions)),
      personalSavingsAllowance = Some(0),
      savingsStartingRate = Some(0)
    )

    SavingsIncomeTaxCalculation.run(SelfAssessment(), liability)
  }

  private def savingsIncomeTaxFor(interest: BigDecimal, remainingDeductions: Deductions = Deductions(0, 0), savingsStartingRate: BigDecimal = 0, personalSavingsAllowance: BigDecimal = 0,
                                  basicTaxBandAllocation: BigDecimal = 0, higherTaxBandAllocation: BigDecimal = 0, additionalHigherTaxBandAllocation: BigDecimal = 0) = {
    val liability = aLiability(
      interestFromUKBanksAndBuildingSocieties = Seq(InterestFromUKBanksAndBuildingSocieties("ue1", interest)),
      deductionsRemaining = Some(remainingDeductions),
      deductions = Some(remainingDeductions),
      personalSavingsAllowance = Some(personalSavingsAllowance),
      savingsStartingRate = Some(savingsStartingRate)
    ).copy(
      payPensionsProfits = Seq(
        TaxBandAllocation(basicTaxBandAllocation, BasicTaxBand),
        TaxBandAllocation(higherTaxBandAllocation, HigherTaxBand),
        TaxBandAllocation(additionalHigherTaxBandAllocation, AdditionalHigherTaxBand)
      )
    )

    SavingsIncomeTaxCalculation.run(SelfAssessment(), liability).savingsIncome
  }
}
