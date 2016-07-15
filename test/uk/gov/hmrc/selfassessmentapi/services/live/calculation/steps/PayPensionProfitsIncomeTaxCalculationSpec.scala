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

import uk.gov.hmrc.selfassessmentapi.domain.Deductions
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBand.{AdditionalHigherTaxBand, BasicTaxBand, HigherTaxBand}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBandAllocation
import uk.gov.hmrc.selfassessmentapi.{SelfEmploymentSugar, UnitSpec}

class PayPensionProfitsIncomeTaxCalculationSpec extends UnitSpec with SelfEmploymentSugar {

  "run with no deductions" should {

    "calculate tax for total pay pension and profit received lesser than 32000" in {
      payPensionProfitIncomeTaxFor(profit = 31999) shouldBe Seq(
        TaxBandAllocation(31999, BasicTaxBand),
        TaxBandAllocation(0, HigherTaxBand),
        TaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "calculate tax for total pay pension and profit received equal to 32000" in {
      payPensionProfitIncomeTaxFor(profit = 32000) shouldBe Seq(
        TaxBandAllocation(32000, BasicTaxBand),
        TaxBandAllocation(0, HigherTaxBand),
        TaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "calculate tax for total pay pension and profit received greater than 32000 but lesser than 150000" in {
      payPensionProfitIncomeTaxFor(profit = 60000) shouldBe Seq(
        TaxBandAllocation(32000, BasicTaxBand),
        TaxBandAllocation(28000, HigherTaxBand),
        TaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "calculate tax for total pay pension and profit received equal to 150000" in {
      payPensionProfitIncomeTaxFor(profit = 150000) shouldBe Seq(
        TaxBandAllocation(32000, BasicTaxBand),
        TaxBandAllocation(118000, HigherTaxBand),
        TaxBandAllocation(0, AdditionalHigherTaxBand)
      )
    }

    "calculate tax for total pay pension and profit received  greater than 150000" in {
      payPensionProfitIncomeTaxFor(profit = 300000) shouldBe Seq(
        TaxBandAllocation(32000, BasicTaxBand),
        TaxBandAllocation(118000, HigherTaxBand),
        TaxBandAllocation(150000, AdditionalHigherTaxBand)
      )
    }
  }

  "run with deductions" should {

    "calculate tax for total pay pension and profit received lesser than 32000" in {
      payPensionProfitIncomeTaxAndDeductionsFor(payPensionProfitsReceived = 33999, totalDeductions = 2000) shouldBe result(
        payPensionsProfitsIncome = Seq(
          TaxBandAllocation(31999, BasicTaxBand),
          TaxBandAllocation(0, HigherTaxBand),
          TaxBandAllocation(0, AdditionalHigherTaxBand)
        ),
        totalDeductionsRemaining = 0
      )
    }

    "calculate tax for total pay pension and profit received equal to 32000" in {
      payPensionProfitIncomeTaxAndDeductionsFor(payPensionProfitsReceived = 34000, totalDeductions = 2000) shouldBe result(
        payPensionsProfitsIncome = Seq(
          TaxBandAllocation(32000, BasicTaxBand),
          TaxBandAllocation(0, HigherTaxBand),
          TaxBandAllocation(0, AdditionalHigherTaxBand)
        ),
        totalDeductionsRemaining = 0
      )
    }

    "calculate tax for total pay pension and profit received greater than 32000 but lesser than 150000" in {
      payPensionProfitIncomeTaxAndDeductionsFor(payPensionProfitsReceived = 62000, totalDeductions = 2000) shouldBe result(
        payPensionsProfitsIncome = Seq(
          TaxBandAllocation(32000, BasicTaxBand),
          TaxBandAllocation(28000, HigherTaxBand),
          TaxBandAllocation(0, AdditionalHigherTaxBand)
        ),
        totalDeductionsRemaining = 0
      )
    }

    "calculate tax for total pay pension and profit received equal to 150000" in {
      payPensionProfitIncomeTaxAndDeductionsFor(payPensionProfitsReceived = 152000, totalDeductions = 2000) shouldBe result(
        payPensionsProfitsIncome = Seq(
          TaxBandAllocation(32000, BasicTaxBand),
          TaxBandAllocation(118000, HigherTaxBand),
          TaxBandAllocation(0, AdditionalHigherTaxBand)
        ),
        totalDeductionsRemaining = 0
      )
    }

    "calculate tax for total pay pension and profit received  greater than 150000" in {
      payPensionProfitIncomeTaxAndDeductionsFor(payPensionProfitsReceived = 302000, totalDeductions = 2000) shouldBe result(
        payPensionsProfitsIncome = Seq(
          TaxBandAllocation(32000, BasicTaxBand),
          TaxBandAllocation(118000, HigherTaxBand),
          TaxBandAllocation(150000, AdditionalHigherTaxBand)
        ),
        totalDeductionsRemaining = 0
      )
    }

    "calculate tax for total pay pension and profit received lesser than deductions" in {
      payPensionProfitIncomeTaxAndDeductionsFor(payPensionProfitsReceived = 1500, totalDeductions = 2000) shouldBe result(
        payPensionsProfitsIncome = Seq(
          TaxBandAllocation(0, BasicTaxBand),
          TaxBandAllocation(0, HigherTaxBand),
          TaxBandAllocation(0, AdditionalHigherTaxBand)
        ),
        totalDeductionsRemaining = 500
      )
    }

    "throw exception if payPensionProfitsReceived is None" in {
      intercept[IllegalStateException] {
        PayPensionProfitsIncomeTaxCalculation.run(SelfAssessment(), aLiability().copy(payPensionProfitsReceived = None))
      }
    }
  }

  private def payPensionProfitIncomeTaxFor(profit: BigDecimal) = {
    val liability = aLiability().copy(payPensionProfitsReceived = Some(profit))
    PayPensionProfitsIncomeTaxCalculation.run(SelfAssessment(), liability).payPensionsProfitsIncome
  }

  private def payPensionProfitIncomeTaxAndDeductionsFor(payPensionProfitsReceived: BigDecimal, totalDeductions: BigDecimal) = {
    val liability = aLiability().copy(
      payPensionProfitsReceived = Some(payPensionProfitsReceived),
      deductions = Some(Deductions(incomeTaxRelief = totalDeductions, totalDeductions = totalDeductions)),
      deductionsRemaining = Some(Deductions(incomeTaxRelief = totalDeductions, totalDeductions = totalDeductions))
    )
    val liabilityAfterCalculation = PayPensionProfitsIncomeTaxCalculation.run(SelfAssessment(), liability)

    result(liabilityAfterCalculation.payPensionsProfitsIncome, liabilityAfterCalculation.deductionsRemaining.get.totalDeductions)
  }

  case class result(payPensionsProfitsIncome: Seq[TaxBandAllocation], totalDeductionsRemaining: BigDecimal)
}
