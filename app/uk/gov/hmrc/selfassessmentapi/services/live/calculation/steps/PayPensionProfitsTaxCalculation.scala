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

import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBandSummary
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBand.{AdditionalHigherTaxBand, BasicTaxBand, HigherTaxBand}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{MongoLiability, TaxBand}

object PayPensionProfitsTaxCalculation extends CalculationStep {

  private val basicRate = HigherTaxBand.lowerBound
  private val extendedRate = AdditionalHigherTaxBand.lowerBound

  override def run(selfAssessment: SelfAssessment, liability: MongoLiability): MongoLiability = {

    val taxableAmount = liability.payPensionProfitsReceived.getOrElse(throw new IllegalStateException("PayPensionProfitsTaxCalculation cannot be performed " +
      "because the payPensionProfitsReceived value has not been computed"))

    val taxCalculationResults = Map[TaxBand, BigDecimal](
        BasicTaxBand -> (if (taxableAmount > basicRate) basicRate else taxableAmount),
        HigherTaxBand -> (if (taxableAmount > basicRate) taxableAmount.min(extendedRate) - basicRate else 0),
        AdditionalHigherTaxBand -> (if (taxableAmount > extendedRate) taxableAmount - extendedRate else 0)
      ) map {
        case (band, amount) => TaxBandSummary(amount, band)
      }

    liability.copy(payPensionsProfits = taxCalculationResults.toSeq)
  }
}
