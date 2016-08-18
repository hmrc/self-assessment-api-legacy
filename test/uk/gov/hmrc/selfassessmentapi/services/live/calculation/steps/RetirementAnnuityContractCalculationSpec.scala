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

import uk.gov.hmrc.selfassessmentapi.domain.pensioncontribution.PensionContribution
import uk.gov.hmrc.selfassessmentapi.{SelfEmploymentSugar, UnitSpec}

class RetirementAnnuityContractCalculationSpec extends UnitSpec with SelfEmploymentSugar {
  "run" should {
    "compute the sum of the retirement annuity contributions, overseas pensions and employer pension contributions" in {
      val selfAssessment = SelfAssessment(taxYearProperties = Some(aTaxYearProperty.copy(pensionContributions =
        Some(PensionContribution(retirementAnnuity = Some(500), overseasPension = Some(500), employerScheme = Some(500)))).toTaxYearProperties))

      RetirementAnnuityContractCalculation.run(selfAssessment, aLiability()).allowancesAndReliefs.retirementAnnuityContract shouldBe Some(1500)
    }

    "return 0 when are no pension contributions" in {
      RetirementAnnuityContractCalculation.run(SelfAssessment(), aLiability()).allowancesAndReliefs.retirementAnnuityContract shouldBe Some(0)
    }
  }
}
