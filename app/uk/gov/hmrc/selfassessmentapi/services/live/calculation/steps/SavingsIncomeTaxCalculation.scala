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

object SavingsIncomeTaxCalculation extends CalculationStep {

  override def run(selfAssessment: SelfAssessment, liability: MongoLiability): LiabilityResult = {

    val personalSavingsAllowance = liability.allowancesAndReliefs.personalSavingsAllowance.getOrElse(throw PropertyNotComputedException("personalSavingsAllowance"))
    
    val savingsStartingRate = liability.allowancesAndReliefs.savingsStartingRate.getOrElse(throw PropertyNotComputedException("savingsStartingRate"))

    val deductionsAfterNonSavings = liability.deductionsRemaining.getOrElse(throw PropertyNotComputedException("deductionsRemaining"))

    val taxBands = Seq(
      TaxBandState(taxBand = SavingsStartingTaxBand, available = savingsStartingRate),
      TaxBandState(taxBand = NilTaxBand, available = personalSavingsAllowance),
      TaxBandState(taxBand = BasicTaxBand, available = positiveOrZero(availableAfterNonSavings(BasicTaxBand, liability) - savingsStartingRate - personalSavingsAllowance)),
      TaxBandState(taxBand = HigherTaxBand, available = availableAfterNonSavings(HigherTaxBand, liability)),
      TaxBandState(taxBand = AdditionalHigherTaxBand, available = availableAfterNonSavings(AdditionalHigherTaxBand, liability))
    )

    val (taxableSavingsIncome, deductionsRemaining) = applyDeductions(liability.totalSavingsIncome, deductionsAfterNonSavings)

    liability.copy(deductionsRemaining = Some(deductionsRemaining), savingsIncome = allocateToTaxBands(taxableSavingsIncome, taxBands))
  }
  
  private def availableAfterNonSavings(taxBand: TaxBand, liability: MongoLiability) =
    liability.nonSavingsIncome.find(_.taxBand == taxBand).map(_.available).getOrElse(taxBand.width)
}
