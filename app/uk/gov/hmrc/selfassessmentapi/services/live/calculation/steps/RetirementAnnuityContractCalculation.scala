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
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{LiabilityResult, MongoLiability}
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps.Math._

object RetirementAnnuityContractCalculation extends CalculationStep {
  override def run(selfAssessment: SelfAssessment, liability: MongoLiability): LiabilityResult = {
    val allowancesAndReliefs =
      liability.allowancesAndReliefs.copy(retirementAnnuityContract = Some(calculateAnnuityContract(selfAssessment)))

    liability.copy(allowancesAndReliefs = allowancesAndReliefs)
  }

  private def calculateAnnuityContract(selfAssessment: SelfAssessment): BigDecimal = {
    val annuityContract = for {
      taxProperties <- selfAssessment.taxYearProperties
    } yield roundUp(taxProperties.retirementAnnuityContract)

    annuityContract.sum
  }
}
