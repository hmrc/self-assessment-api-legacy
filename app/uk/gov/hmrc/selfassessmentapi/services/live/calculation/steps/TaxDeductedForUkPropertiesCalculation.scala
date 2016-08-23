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

import uk.gov.hmrc.selfassessmentapi.domain.TaxPaidForUkProperty
import uk.gov.hmrc.selfassessmentapi.repositories.domain._
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps.Math._

object TaxDeductedForUkPropertiesCalculation extends CalculationStep {

  override def run(selfAssessment: SelfAssessment, liability: MongoLiability): LiabilityResult = {
    val totalDeductionFromUkProperties = totalTaxDeductedForUkProperties(selfAssessment)
    val deductionsPerUkProperty = taxDeductedPerUkProperty(selfAssessment)

    liability.copy(taxDeducted = liability.taxDeducted match {
      case None => Some(MongoTaxDeducted(totalDeductionFromUkProperties = totalDeductionFromUkProperties, deductionFromUkProperties = deductionsPerUkProperty))
      case Some(mongoTaxDeducted) => Some(mongoTaxDeducted.copy(totalDeductionFromUkProperties = totalDeductionFromUkProperties, deductionFromUkProperties = deductionsPerUkProperty))
    })
  }

  private def totalTaxDeductedForUkProperties(selfAssessment: SelfAssessment): BigDecimal = {
    roundUpToPennies(selfAssessment.ukProperties.map(_.taxPaid).sum)
  }

  private def taxDeductedPerUkProperty(selfAssessment: SelfAssessment): Seq[TaxPaidForUkProperty] = {
    for {
      property <- selfAssessment.ukProperties
      taxPaid <- property.taxesPaid
    } yield TaxPaidForUkProperty(property.sourceId, taxPaid.amount)
  }
}
