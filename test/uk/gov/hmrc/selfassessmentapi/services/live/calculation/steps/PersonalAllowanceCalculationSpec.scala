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

import uk.gov.hmrc.selfassessmentapi.SelfAssessmentSugar._
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.repositories.domain.AllowancesAndReliefs

class PersonalAllowanceCalculationSpec extends UnitSpec {

  "run" should {

    "calculate personal allowance for (totalIncomeReceived < 100000)" in {

      personalAllowanceFor(totalIncomeReceived = 23400, incomeTaxRelief = 0) shouldBe 11000
    }

    "calculate personal allowance for (totalIncomeReceived = 100001)" in {

      personalAllowanceFor(totalIncomeReceived = 100001, incomeTaxRelief = 0) shouldBe 11000
    }

    "calculate personal allowance for (totalIncomeReceived = 110000)" in {

      personalAllowanceFor(totalIncomeReceived = 110000, incomeTaxRelief = 0) shouldBe 6000
    }

    "calculate personal allowance for (totalIncomeReceived = 121999)" in {

      personalAllowanceFor(totalIncomeReceived = 121999, incomeTaxRelief = 0) shouldBe 1
    }

    "calculate personal allowance for total (totalIncomeReceived = 122000)" in {

      personalAllowanceFor(totalIncomeReceived = 122000, incomeTaxRelief = 0) shouldBe 0
    }

    "calculate personal allowance for total (totalIncomeReceived = 322000)" in {

      personalAllowanceFor(totalIncomeReceived = 322000, incomeTaxRelief = 0) shouldBe 0
    }

    "calculate personal allowance for (totalIncomeReceived - incomeTaxRelief) < 0" in {

      personalAllowanceFor(totalIncomeReceived = 0, incomeTaxRelief = 210000) shouldBe 11000
    }

    "calculate personal allowance for (totalIncomeReceived - incomeTaxRelief) < 100000" in {

      personalAllowanceFor(totalIncomeReceived = 200000, incomeTaxRelief = 110000) shouldBe 11000
    }

    "calculate personal allowance for (totalIncomeReceived - incomeTaxRelief) = 100001" in {

      personalAllowanceFor(totalIncomeReceived = 200000, incomeTaxRelief = 99999) shouldBe 11000
    }

    "calculate personal allowance for (totalIncomeReceived - incomeTaxRelief) = 110000" in {

      personalAllowanceFor(totalIncomeReceived = 200000, incomeTaxRelief = 90000) shouldBe 6000
    }

    "calculate personal allowance for (totalIncomeReceived - incomeTaxRelief) > 120000" in {

      personalAllowanceFor(totalIncomeReceived = 200000, incomeTaxRelief = 70000) shouldBe 0
    }
  }

  private def personalAllowanceFor(totalIncomeReceived: BigDecimal, incomeTaxRelief: BigDecimal = 0) = {
    val liability = aLiability().copy(
        totalIncomeReceived = Some(totalIncomeReceived),
        allowancesAndReliefs = AllowancesAndReliefs(incomeTaxRelief = Some(incomeTaxRelief))
    )
    PersonalAllowanceCalculation
      .run(SelfAssessment(), liability)
      .getLiabilityOrFail
      .allowancesAndReliefs
      .personalAllowance
      .get
  }
}
