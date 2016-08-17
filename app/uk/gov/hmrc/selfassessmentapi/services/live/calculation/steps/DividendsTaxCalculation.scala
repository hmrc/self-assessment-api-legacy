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
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{LiabilityResult, MongoLiability, TaxBand}
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps.Math._


object DividendsTaxCalculation extends CalculationStep {

  private val dividendAllowance = 5000

  override def run(selfAssessment: SelfAssessment, liability: MongoLiability): LiabilityResult = {

    val deductions = liability.deductionsRemaining.getOrElse(throw PropertyNotComputedException("deductionsRemaining"))

    val availabilities = List((available(BasicTaxBand, liability), BigDecimal(0)),
                              (available(HigherTaxBand, liability), BigDecimal(0)))
    val taxExemptIncome = List((BigDecimal(0), getTaxExemptIncome(liability)))
    val adjustedAvailabilities = availabilities.foldLeft(taxExemptIncome)(calcAvailability).reverse.tail.unzip._1

    val taxBands = Seq(
      TaxBandState(taxBand = NilTaxBand, available = dividendAllowance),
      TaxBandState(taxBand = BasicTaxBand, available = adjustedAvailabilities(0)),
      TaxBandState(taxBand = HigherTaxBand, available = adjustedAvailabilities(1)),
      TaxBandState(taxBand = AdditionalHigherTaxBand, available = AdditionalHigherTaxBand.width)
    )

    val (taxableDividendsIncome, deductionsRemaining) = applyDeductions(liability.dividendsFromUKSources.map(_.totalDividend).sum, deductions)

    liability.copy(deductionsRemaining = Some(deductionsRemaining), dividendsIncome = allocateToTaxBands(taxableDividendsIncome, taxBands))
  }

  private def getTaxExemptIncome(liability: MongoLiability): BigDecimal = {
    sum(liability.savingsIncome.find(_.taxBand == NilTaxBand).map(_.amount),
      liability.savingsIncome.find(_.taxBand == SavingsStartingTaxBand).map(_.amount),
      Some(capAt(liability.dividendsFromUKSources.map(_.totalDividend).sum, dividendAllowance)))
  }

  private def available(taxBand: TaxBand, liability: MongoLiability) = {
    (for {
      fromSavings <- liability.savingsIncome.find(_.taxBand == taxBand)
      fromPpp <- liability.nonSavingsIncome.find(_.taxBand == taxBand)
    } yield (fromPpp + fromSavings).available).getOrElse(taxBand.width)
  }

  private def calcAvailability(input : List[(BigDecimal, BigDecimal)], elem: (BigDecimal, BigDecimal)) : List[(BigDecimal, BigDecimal)] = {
    val availability = elem._1 - input.head._2
    (if (availability >= 0) (availability, BigDecimal(0)) else (BigDecimal(0), availability.abs)) :: input
  }
}
