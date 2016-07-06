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

class TotalIncomeCalculationSpec extends UnitSpec with SelfEmploymentSugar {

  "run" should {

    "calculate total income" in {

      val liability = aLiability(profitFromSelfEmployments = Seq(
        SelfEmploymentIncome("se1", 0, 300),
        SelfEmploymentIncome("se2", 0, 200.50)
      ))

      TotalIncomeCalculation.run(SelfAssessment(), liability) shouldBe liability.copy(totalIncomeReceived = Some(500.50))
    }

    "calculate total income if there is no income from self employments" in {

      val liability = aLiability()

      TotalIncomeCalculation.run(SelfAssessment(), liability) shouldBe liability.copy(totalIncomeReceived = Some(0))
    }
  }
}
