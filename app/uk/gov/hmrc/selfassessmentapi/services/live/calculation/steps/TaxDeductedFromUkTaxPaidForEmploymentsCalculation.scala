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

import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.domain.UkTaxPaidForEmployment
import uk.gov.hmrc.selfassessmentapi.repositories.domain._
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps.Math._

object TaxDeductedFromUkTaxPaidForEmploymentsCalculation extends CalculationStep {
  override def run(selfAssessment: SelfAssessment, liability: MongoLiability): LiabilityResult = {
    val (initialUkTaxesPaid, initialAccUkTaxPaid) = (Seq.empty[UkTaxPaidForEmployment], BigDecimal(0))

    val (ukTaxesPaidForEmployments, totalUkTaxesPaid) =
      selfAssessment.employments.foldLeft((initialUkTaxesPaid, initialAccUkTaxPaid)) {
        case ((ukTaxesPaid, accUkTaxPaid), employment) =>
          val ukTaxPaidForEmployment = employment.ukTaxPaid.map(_.amount).sum
          (ukTaxesPaid :+ UkTaxPaidForEmployment(employment.sourceId, ukTaxPaidForEmployment),
           accUkTaxPaid + ukTaxPaidForEmployment)
      }

    val isValidTaxPaid = ukTaxesPaidForEmployments.isEmpty || ukTaxesPaidForEmployments.exists(_.taxPaid >= 0)

    if (isValidTaxPaid) {
      val ukTaxPaid = if (totalUkTaxesPaid <= 0) BigDecimal(0) else roundUpToPennies(totalUkTaxesPaid)

      liability.copy(taxDeducted = liability.taxDeducted match {
        case None =>
          Some(MongoTaxDeducted(ukTaxPaid = ukTaxPaid, ukTaxesPaidForEmployments = ukTaxesPaidForEmployments))
        case Some(mongoTaxDeducted) =>
          Some(mongoTaxDeducted.copy(ukTaxPaid = ukTaxPaid, ukTaxesPaidForEmployments = ukTaxesPaidForEmployments))
      })
    } else {
      MongoLiabilityCalculationErrors.create(
          liability.saUtr,
          liability.taxYear,
          Seq(
              MongoLiabilityCalculationError(
                  INVALID_EMPLOYMENT_TAX_PAID,
                  s"The UK tax paid must be positive for at least one employment source")))
    }
  }
}
