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

import uk.gov.hmrc.selfassessmentapi.domain.Deductions
import uk.gov.hmrc.selfassessmentapi.{SelfEmploymentSugar, UnitSpec}

class SavingsStartingRateCalculationSpec extends UnitSpec with SelfEmploymentSugar {

  "run" should {

    "return 5000 if payPensionProfitsReceived is less than deductions" in {

      savingsStartingRateFor(payPensionProfitsReceived = 5000, totalDeductions = 6000) shouldBe 5000
    }

    "return 5000 if payPensionProfitsReceived is equal to deductions" in {

      savingsStartingRateFor(payPensionProfitsReceived = 6000, totalDeductions = 6000) shouldBe 5000
    }

    "return the difference between profit after deductions and startingRateLimit (5000) if payPensionProfitsReceived is less than deductions + startingRateLimit" in {

      savingsStartingRateFor(payPensionProfitsReceived = 9000, totalDeductions = 6000) shouldBe 2000
    }

    "return 0 if payPensionProfitsReceived is equal to deductions + startingRateLimit" in {

      savingsStartingRateFor(payPensionProfitsReceived = 11000, totalDeductions = 6000) shouldBe 0
    }

    "return 0 if payPensionProfitsReceived is more than deductions + startingRateLimit" in {

      savingsStartingRateFor(payPensionProfitsReceived = 100000, totalDeductions = 6000) shouldBe 0
    }
  }

  private def savingsStartingRateFor(payPensionProfitsReceived: BigDecimal, totalDeductions: BigDecimal) = {
    val liability = aLiability().copy(
      payPensionProfitsReceived = Some(payPensionProfitsReceived),
      deductions = Some(Deductions(0, totalDeductions))
    )
    SavingsStartingRateCalculation.run(SelfAssessment(), liability).allowancesAndReliefs.savingsStartingRate.get
  }
}
