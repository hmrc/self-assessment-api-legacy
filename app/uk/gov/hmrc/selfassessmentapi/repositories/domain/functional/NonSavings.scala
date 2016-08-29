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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.functional

import uk.gov.hmrc.selfassessmentapi.domain.TaxBandSummary
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBandAllocation
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps.SelfAssessment

object NonSavings {

  object TotalIncome {

    def apply(selfEmploymentProfits: BigDecimal, ukPropertyProfits: BigDecimal, employmentProfits: BigDecimal,
              furnishedHolidayLettingProfits: BigDecimal): BigDecimal = {
      selfEmploymentProfits + ukPropertyProfits + employmentProfits + furnishedHolidayLettingProfits
    }

    def apply(selfAssessment: SelfAssessment): BigDecimal = apply(SelfEmployment.TotalProfit(selfAssessment), UkProperty.TotalProfit(selfAssessment),
      Employment.TotalProfit(selfAssessment), FurnishedHolidayLetting.TotalProfit(selfAssessment))
  }

  object IncomeTaxBandSummary {
    def apply(selfAssessment: SelfAssessment): Seq[TaxBandSummary] = apply(SelfEmployment.TotalTaxableProfit(selfAssessment))

    def apply(totalTaxableProfitFromSelfEmployments: BigDecimal): Seq[TaxBandSummary] = {
      Seq(TaxBands.BasicTaxBand(), TaxBands.HigherTaxBand(), TaxBands.AdditionalHigherTaxBand()).map { taxBand =>
        TaxBandAllocation(taxBand.allocate2(totalTaxableProfitFromSelfEmployments), taxBand).toTaxBandSummary
      }
    }
  }

  object IncomeTax extends IncomeTax {
    def apply(selfAssessment: SelfAssessment): BigDecimal = apply(NonSavings.IncomeTaxBandSummary(selfAssessment))
    def apply(summaries: Seq[TaxBandSummary]): BigDecimal = incomeTax(summaries)
  }
}
