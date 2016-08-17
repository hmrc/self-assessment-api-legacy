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

import uk.gov.hmrc.selfassessmentapi.{SelfAssessmentSugar, UnitSpec}

class SavingsStartingRateCalculationSpec extends UnitSpec with SelfAssessmentSugar {

  "run" should {

    "return 5000 if nonSavingsIncomeReceived and deductions are equal to 0" in {

      savingsStartingRateFor(nonSavingsIncomeReceived = 0, totalDeductions = 0) shouldBe 5000
    }

    "return the difference between profit and startingRateLimit (5000) if deductions are 0" in {

      savingsStartingRateFor(nonSavingsIncomeReceived = 4500, totalDeductions = 0) shouldBe 500
    }

    "return 0 if profit is equal to startingRateLimit and deductions are 0" in {

      savingsStartingRateFor(nonSavingsIncomeReceived = 5000, totalDeductions = 0) shouldBe 0
    }

    "return 0 if profit is more than startingRateLimit and deductions are 0" in {

      savingsStartingRateFor(nonSavingsIncomeReceived = 6000, totalDeductions = 0) shouldBe 0
    }

    "return 5000 if nonSavingsIncomeReceived is less than deductions" in {

      savingsStartingRateFor(nonSavingsIncomeReceived = 5000, totalDeductions = 6000) shouldBe 5000
    }

    "return 5000 if nonSavingsIncomeReceived is equal to deductions" in {

      savingsStartingRateFor(nonSavingsIncomeReceived = 6000, totalDeductions = 6000) shouldBe 5000
    }

    "return the difference between profit after deductions and startingRateLimit (5000) if nonSavingsIncomeReceived is less than deductions + startingRateLimit" in {

      savingsStartingRateFor(nonSavingsIncomeReceived = 9000, totalDeductions = 6000) shouldBe 2000
    }

    "return 0 if nonSavingsIncomeReceived is equal to deductions + startingRateLimit" in {

      savingsStartingRateFor(nonSavingsIncomeReceived = 11000, totalDeductions = 6000) shouldBe 0
    }

    "return 0 if nonSavingsIncomeReceived is more than deductions + startingRateLimit" in {

      savingsStartingRateFor(nonSavingsIncomeReceived = 100000, totalDeductions = 6000) shouldBe 0
    }
  }

  private def savingsStartingRateFor(nonSavingsIncomeReceived: BigDecimal, totalDeductions: BigDecimal) = {
    val liability = aLiability().copy(
        nonSavingsIncomeReceived = Some(nonSavingsIncomeReceived),
        totalAllowancesAndReliefs = Some(totalDeductions)
    )
    SavingsStartingRateCalculation
      .run(SelfAssessment(), liability)
      .getLiabilityOrFail
      .allowancesAndReliefs
      .savingsStartingRate
      .get
  }
}
