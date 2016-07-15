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
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{MongoLiability, TaxBand}

object SavingsIncomeTaxCalculation extends CalculationStep {

  override def run(selfAssessment: SelfAssessment, liability: MongoLiability): MongoLiability = {

    val personalSavingsAllowance = liability.allowancesAndReliefs.personalSavingsAllowance.getOrElse(throw PropertyNotComputedException("personalSavingsAllowance"))
    
    val savingsStartingRate = liability.allowancesAndReliefs.savingsStartingRate.getOrElse(throw PropertyNotComputedException("savingsStartingRate"))

    val deductionsAfterPayPensions = liability.deductionsRemaining.getOrElse(throw PropertyNotComputedException("deductionsRemaining"))

    val taxBands = Seq(
      TaxBandState(taxBand = SavingsStartingTaxBand, available = savingsStartingRate),
      TaxBandState(taxBand = SavingsNilTaxBand, available = personalSavingsAllowance),
      TaxBandState(taxBand = BasicTaxBand, available = positiveOrZero(availableAfterPayPensions(BasicTaxBand, liability) - savingsStartingRate - personalSavingsAllowance)),
      TaxBandState(taxBand = HigherTaxBand, available = availableAfterPayPensions(HigherTaxBand, liability)),
      TaxBandState(taxBand = AdditionalHigherTaxBand, available = availableAfterPayPensions(AdditionalHigherTaxBand, liability))
    )

    val (taxableSavingsIncome, deductionsRemaining) = applyDeductions(liability.totalSavingsIncome, deductionsAfterPayPensions)

    liability.copy(deductionsRemaining = Some(deductionsRemaining), savingsIncome = allocateToTaxBands(taxableSavingsIncome, taxBands))
  }
  
  private def availableAfterPayPensions(taxBand: TaxBand, liability: MongoLiability) =
    liability.payPensionsProfitsIncome.find(_.taxBand == taxBand).map(_.available).getOrElse(taxBand.width)
}
