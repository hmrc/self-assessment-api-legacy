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
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{MongoTaxDeducted, MongoLiability}

object TaxDeductedCalculation extends CalculationStep {
  override def run(selfAssessment: SelfAssessment, liability: MongoLiability): MongoLiability = {
    liability.copy(taxDeducted = Some(MongoTaxDeducted(
      interestFromUk = calculateInterestDeducted(selfAssessment),
      deductionFromUkProperties = taxDeductedForUkProperties(selfAssessment))))
  }

  private def calculateInterestDeducted(selfAssessment: SelfAssessment): BigDecimal = {
    val totalTaxedInterest = selfAssessment.unearnedIncomes.map(_.taxedSavingsInterest).sum
    val grossedUpInterest = roundDown(totalTaxedInterest * 100 / 80)

    roundUp(grossedUpInterest - totalTaxedInterest)
  }

  private def taxDeductedForUkProperties(selfAssessment: SelfAssessment): BigDecimal = {
    roundUp(selfAssessment.ukProperties.map(_.taxPaid).sum)
  }
}
