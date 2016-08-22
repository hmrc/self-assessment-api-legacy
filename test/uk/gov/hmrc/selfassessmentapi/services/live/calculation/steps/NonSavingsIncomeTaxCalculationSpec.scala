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
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBand.{AdditionalHigherTaxBand, BasicTaxBand, HigherTaxBand}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBandAllocation

class NonSavingsIncomeTaxCalculationSpec extends UnitSpec {

  "run with no deductions" should {

    "calculate tax for nonSavingsIncomeReceived lesser than 32000" in {
      nonSavingsIncomeTaxFor(nonSavingsIncomeReceived = 31999) shouldBe Seq(
          TaxBandAllocation(31999, BasicTaxBand),
          TaxBandAllocation(0, HigherTaxBand),
          TaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "calculate tax for nonSavingsIncomeReceived equal to 32000" in {
      nonSavingsIncomeTaxFor(nonSavingsIncomeReceived = 32000) shouldBe Seq(
          TaxBandAllocation(32000, BasicTaxBand),
          TaxBandAllocation(0, HigherTaxBand),
          TaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "calculate tax for nonSavingsIncomeReceived greater than 32000 but lesser than 150000" in {
      nonSavingsIncomeTaxFor(nonSavingsIncomeReceived = 60000) shouldBe Seq(
          TaxBandAllocation(32000, BasicTaxBand),
          TaxBandAllocation(28000, HigherTaxBand),
          TaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "calculate tax for nonSavingsIncomeReceived equal to 150000" in {
      nonSavingsIncomeTaxFor(nonSavingsIncomeReceived = 150000) shouldBe Seq(
          TaxBandAllocation(32000, BasicTaxBand),
          TaxBandAllocation(118000, HigherTaxBand),
          TaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "calculate tax for nonSavingsIncomeReceived  greater than 150000" in {
      nonSavingsIncomeTaxFor(nonSavingsIncomeReceived = 300000) shouldBe Seq(
          TaxBandAllocation(32000, BasicTaxBand),
          TaxBandAllocation(118000, HigherTaxBand),
          TaxBandAllocation(150000, AdditionalHigherTaxBand)
      )
    }
  }

  "run with deductions" should {

    "calculate tax for nonSavingsIncomeReceived lesser than 32000" in {
      nonSavingsIncomeTaxAndDeductionsFor(nonSavingsIncomeReceived = 33999, totalDeductions = 2000) shouldBe result(
          nonSavingsIncome = Seq(
              TaxBandAllocation(31999, BasicTaxBand),
              TaxBandAllocation(0, HigherTaxBand),
              TaxBandAllocation(0, AdditionalHigherTaxBand)
          ),
          deductionsRemaining = 0
      )
    }

    "calculate tax for nonSavingsIncomeReceived equal to 32000" in {
      nonSavingsIncomeTaxAndDeductionsFor(nonSavingsIncomeReceived = 34000, totalDeductions = 2000) shouldBe result(
          nonSavingsIncome = Seq(
              TaxBandAllocation(32000, BasicTaxBand),
              TaxBandAllocation(0, HigherTaxBand),
              TaxBandAllocation(0, AdditionalHigherTaxBand)
          ),
          deductionsRemaining = 0
      )
    }

    "calculate tax for nonSavingsIncomeReceived greater than 32000 but lesser than 150000" in {
      nonSavingsIncomeTaxAndDeductionsFor(nonSavingsIncomeReceived = 62000, totalDeductions = 2000) shouldBe result(
          nonSavingsIncome = Seq(
              TaxBandAllocation(32000, BasicTaxBand),
              TaxBandAllocation(28000, HigherTaxBand),
              TaxBandAllocation(0, AdditionalHigherTaxBand)
          ),
          deductionsRemaining = 0
      )
    }

    "calculate tax for nonSavingsIncomeReceived equal to 150000" in {
      nonSavingsIncomeTaxAndDeductionsFor(nonSavingsIncomeReceived = 152000, totalDeductions = 2000) shouldBe result(
          nonSavingsIncome = Seq(
              TaxBandAllocation(32000, BasicTaxBand),
              TaxBandAllocation(118000, HigherTaxBand),
              TaxBandAllocation(0, AdditionalHigherTaxBand)
          ),
          deductionsRemaining = 0
      )
    }

    "calculate tax for nonSavingsIncomeReceived greater than 150000" in {
      nonSavingsIncomeTaxAndDeductionsFor(nonSavingsIncomeReceived = 302000, totalDeductions = 2000) shouldBe result(
          nonSavingsIncome = Seq(
              TaxBandAllocation(32000, BasicTaxBand),
              TaxBandAllocation(118000, HigherTaxBand),
              TaxBandAllocation(150000, AdditionalHigherTaxBand)
          ),
          deductionsRemaining = 0
      )
    }

    "calculate tax for nonSavingsIncomeReceived lesser than deductions" in {
      nonSavingsIncomeTaxAndDeductionsFor(nonSavingsIncomeReceived = 1500, totalDeductions = 2000) shouldBe result(
          nonSavingsIncome = Seq(
              TaxBandAllocation(0, BasicTaxBand),
              TaxBandAllocation(0, HigherTaxBand),
              TaxBandAllocation(0, AdditionalHigherTaxBand)
          ),
          deductionsRemaining = 500
      )
    }

    "throw exception if nonSavingsIncomeReceived is None" in {
      intercept[IllegalStateException] {
        NonSavingsIncomeTaxCalculation.run(SelfAssessment(), aLiability().copy(nonSavingsIncomeReceived = None))
      }
    }
  }

  private def nonSavingsIncomeTaxFor(nonSavingsIncomeReceived: BigDecimal) = {
    val liability = aLiability().copy(nonSavingsIncomeReceived = Some(nonSavingsIncomeReceived))
    NonSavingsIncomeTaxCalculation.run(SelfAssessment(), liability).getLiabilityOrFail.nonSavingsIncome
  }

  private def nonSavingsIncomeTaxAndDeductionsFor(nonSavingsIncomeReceived: BigDecimal, totalDeductions: BigDecimal) = {
    val liability = aLiability().copy(
        nonSavingsIncomeReceived = Some(nonSavingsIncomeReceived),
        deductionsRemaining = Some(totalDeductions)
    )
    val liabilityAfterCalculation = NonSavingsIncomeTaxCalculation.run(SelfAssessment(), liability).getLiabilityOrFail

    result(liabilityAfterCalculation.nonSavingsIncome, liabilityAfterCalculation.deductionsRemaining.get)
  }

  case class result(nonSavingsIncome: Seq[TaxBandAllocation], deductionsRemaining: BigDecimal)
}
