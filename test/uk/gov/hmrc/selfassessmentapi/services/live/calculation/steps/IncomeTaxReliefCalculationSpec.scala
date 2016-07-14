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

import uk.gov.hmrc.selfassessmentapi.repositories.domain.SelfEmploymentIncome
import uk.gov.hmrc.selfassessmentapi.{SelfEmploymentSugar, UnitSpec}

class IncomeTaxReliefCalculationSpec extends UnitSpec with SelfEmploymentSugar {

  "run" should {

    "calculate income tax relief if there is no income from self employments" in {

      incomeTaxReliefFor(profitFromSelfEmployments = Nil) shouldBe 0
    }

    "calculate income tax relief if there is income from one self employment" in {

      incomeTaxReliefFor(profitFromSelfEmployments = Seq(aSelfEmploymentIncome(lossBroughtForward = 100))) shouldBe 100
    }

    "calculate income tax relief if there is income from multiple self employments" in {

      incomeTaxReliefFor(profitFromSelfEmployments = Seq(
        aSelfEmploymentIncome(lossBroughtForward = 100),
        aSelfEmploymentIncome(lossBroughtForward = 199.99),
        aSelfEmploymentIncome(lossBroughtForward = 0.01)
      )) shouldBe 300
    }
  }

  private def incomeTaxReliefFor(profitFromSelfEmployments: Seq[SelfEmploymentIncome]) = {
    IncomeTaxReliefCalculation.run(
      selfAssessment = SelfAssessment(),
      liability = aLiability(profitFromSelfEmployments = profitFromSelfEmployments)
    ).incomeTaxRelief.get
  }
}
