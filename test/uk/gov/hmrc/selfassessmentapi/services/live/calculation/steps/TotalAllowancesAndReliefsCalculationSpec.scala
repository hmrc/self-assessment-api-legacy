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

import uk.gov.hmrc.selfassessmentapi.repositories.domain.AllowancesAndReliefs
import uk.gov.hmrc.selfassessmentapi.{SelfAssessmentSugar, UnitSpec}

class TotalAllowancesAndReliefsCalculationSpec extends UnitSpec with SelfAssessmentSugar {

  "run" should {

    "calculate total allowances and reliefs by summing income tax relief, personal allowance and retirement annuity contract" in {

      val liability = aLiability().copy(allowancesAndReliefs = AllowancesAndReliefs(personalAllowance = Some(5000), incomeTaxRelief = Some(1400), retirementAnnuityContract = Some(20000)))

      TotalAllowancesAndReliefsCalculation.run(SelfAssessment(), liability).getLiabilityOrFail.deductionsRemaining shouldBe Some(26400)
    }
  }
}
