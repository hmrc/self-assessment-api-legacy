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
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.SavingsIncomeType._
import uk.gov.hmrc.selfassessmentapi.repositories.domain._
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps.Math._

object TaxDeductedCalculation extends CalculationStep {
  override def run(selfAssessment: SelfAssessment, liability: MongoLiability): LiabilityResult = {
    calculateInterestFromUk(selfAssessment, liability)
      .fold(identity, calculateUkTaxPaidForEmployments(selfAssessment, _))
  }

  private[calculation] def calculateInterestFromUk(selfAssessment: SelfAssessment,
                                                   liability: MongoLiability): LiabilityResult = {
    val totalTaxedInterest = selfAssessment.unearnedIncomes.map { unearnedIncome =>
      unearnedIncome.savings.filter(_.`type` == InterestFromBanksTaxed).map(_.amount).sum
    }.sum

    val grossedUpInterest = roundDown(totalTaxedInterest * 100 / 80)
    val interestFromUk = roundUp(grossedUpInterest - totalTaxedInterest)

    liability.copy(taxDeducted = liability.taxDeducted match {
      case None => Some(MongoTaxDeducted(interestFromUk = interestFromUk))
      case Some(mongoTaxDeducted) => Some(mongoTaxDeducted.copy(interestFromUk = interestFromUk))
    })
  }

  private[calculation] def calculateUkTaxPaidForEmployments(selfAssessment: SelfAssessment,
                                                            liability: MongoLiability): LiabilityResult = {
    val (initialUkTaxesPaid, initialAccUkTaxPaid) = (Seq.empty[MongoUkTaxPaidForEmployment], BigDecimal(0))

    val (ukTaxesPaidForEmployments, totalUkTaxesPaid) =
      selfAssessment.employments.foldLeft((initialUkTaxesPaid, initialAccUkTaxPaid)) {
        case ((ukTaxesPaid, accUkTaxPaid), employment) =>
          val ukTaxPaidForEmployment = employment.ukTaxPaid.map(_.amount).sum
          (ukTaxesPaid :+ MongoUkTaxPaidForEmployment(employment.sourceId, ukTaxPaidForEmployment),
           accUkTaxPaid + ukTaxPaidForEmployment)
      }

    val isValidTaxPaid = ukTaxesPaidForEmployments.isEmpty || ukTaxesPaidForEmployments.exists(_.ukTaxPaid >= 0)

    if (isValidTaxPaid) {
      val ukTaxPaid = if (totalUkTaxesPaid <= 0) BigDecimal(0) else roundUp(totalUkTaxesPaid)

      liability.copy(taxDeducted = liability.taxDeducted match {
        case None =>
          Some(MongoTaxDeducted(ukTaxPAid = ukTaxPaid, ukTaxesPaidForEmployments = ukTaxesPaidForEmployments))
        case Some(mongoTaxDeducted) =>
          Some(mongoTaxDeducted.copy(ukTaxPAid = ukTaxPaid, ukTaxesPaidForEmployments = ukTaxesPaidForEmployments))
      })
    } else {
      val invalidEmploymentsErrors = ukTaxesPaidForEmployments
        .filter(_.ukTaxPaid < 0)
        .map(mongoUkTaxPaidForEmployment =>
              Error(INVALID_EMPLOYMENT_TAX_PAID,
                    s"The UK tax paid for employment with source id ${mongoUkTaxPaidForEmployment.sourceId} should not be negative"))

      CalculationError.create(liability.saUtr, liability.taxYear, invalidEmploymentsErrors)
    }
  }
}
