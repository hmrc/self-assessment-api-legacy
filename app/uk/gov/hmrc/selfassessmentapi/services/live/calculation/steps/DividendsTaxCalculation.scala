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

import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBand._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{MongoLiability, TaxBand, TaxBandAllocation}


object DividendsTaxCalculation extends CalculationStep {

  private val dividendAllowance = 5000

  override def run(selfAssessment: SelfAssessment, liability: MongoLiability): MongoLiability = {

    val deductions = liability.deductionsRemaining.getOrElse(throw new IllegalStateException("DividendTaxCalculation cannot be run as deductionsRemaining is required"))

    val (basicBandAvailability, higherBandAvailabilityAdjustment) = adjustedAvailability(BasicTaxBand, liability)
    val (higherBandAvailability, _) = adjustedAvailability(HigherTaxBand, liability)

    val taxBands = Seq(
      TaxBandState(taxBand = DividendsNilTaxBand, available = dividendAllowance),
      TaxBandState(taxBand = DividendBasicTaxBand, available = basicBandAvailability),
      TaxBandState(taxBand = DividendHigherTaxBand, available = higherBandAvailability - higherBandAvailabilityAdjustment),
      TaxBandState(taxBand = DividendAdditionalHigherTaxBand, available = AdditionalHigherTaxBand.width)
    )

    val (taxableDividendsIncome, deductionsRemaining) = applyDeductions(liability.dividendsFromUKSources.map(_.totalDividend).sum, deductions)

    liability.copy(deductionsRemaining = Some(deductionsRemaining), dividendsIncome = allocateToTaxBands(taxableDividendsIncome, taxBands))
  }

  private def untaxedSavings(savingsIncome: Seq[TaxBandAllocation]) = {
    sum(savingsIncome.find(_.taxBand == SavingsNilTaxBand).map(_.amount),
      savingsIncome.find(_.taxBand == SavingsStartingTaxBand).map(_.amount))
  }

  private def available(taxBand: TaxBand, liability: MongoLiability) = {
    (for {
      fromSavings <- liability.savingsIncome.find(_.taxBand == taxBand)
      fromPpp <- liability.payPensionsProfitsIncome.find(_.taxBand == taxBand)
    } yield (fromPpp + fromSavings).available).headOption.getOrElse(taxBand.width)
  }

  private def adjustedAvailability(band: TaxBand, liability: MongoLiability): (BigDecimal, BigDecimal) = {

    val availabilityForBand = available(band, liability)

    if (band.width - availabilityForBand >= 0) {
      val adjustedAvailabilityForBand = availabilityForBand - untaxedSavings(liability.savingsIncome)
      if (adjustedAvailabilityForBand >= 0) (adjustedAvailabilityForBand, 0) else (0, adjustedAvailabilityForBand.abs)
    } else {
      (availabilityForBand, 0)
    }
  }

}
