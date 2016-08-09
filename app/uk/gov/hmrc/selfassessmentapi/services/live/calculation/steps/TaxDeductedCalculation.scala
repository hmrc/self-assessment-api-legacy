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

import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.SavingsIncomeType._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{CalculationError, MongoLiability, MongoTaxDeducted, MongoUkTaxPaidForEmployment}
import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode._

object TaxDeductedCalculation extends CalculationStep {
  override def run(selfAssessment: SelfAssessment, liability: MongoLiability): MongoLiability = {
    val totalTaxedInterest = selfAssessment.unearnedIncomes.map { unearnedIncome =>
      unearnedIncome.savings.filter(_.`type` == InterestFromBanksTaxed).map(_.amount).sum
    }.sum

    val grossedUpInterest = roundDown(totalTaxedInterest * 100 / 80)
    val interestFromUk = roundUp(grossedUpInterest - totalTaxedInterest)

    val (initialUkTaxesPaid, initialAccUkTaxPaid) = (Seq[MongoUkTaxPaidForEmployment](), BigDecimal(0))

    val (ukTaxesPaidForEmployments, totalUkTaxesPaid) =
      selfAssessment.employments.foldLeft((initialUkTaxesPaid, initialAccUkTaxPaid)) {
        case ((ukTaxesPaid, accUkTaxPaid), employment) =>
          val ukTaxPaidForEmployment = employment.ukTaxPaid.map(_.amount).sum
          (ukTaxesPaid :+ MongoUkTaxPaidForEmployment(employment.sourceId, ukTaxPaidForEmployment),
           accUkTaxPaid + ukTaxPaidForEmployment)
      }

    val isValidTaxPaid = ukTaxesPaidForEmployments.isEmpty || ukTaxesPaidForEmployments.exists(_.ukTaxPaid >= 0)

    liability.copy(taxDeducted =
                     if (isValidTaxPaid)
                       Some(
                           MongoTaxDeducted(interestFromUk = interestFromUk,
                                            ukTaxPAid = if (totalUkTaxesPaid <= 0) 0 else roundUp(totalUkTaxesPaid),
                                            ukTaxesPaidForEmployments = ukTaxesPaidForEmployments))
                     else None,
                   calculationError =
                     if (isValidTaxPaid) None
                     else
                       Some(
                           CalculationError(INVALID_EMPLOYMENT_TAX_PAID,
                                            "Tax Paid from your Employment(s) should not be negative")))
  }
}
