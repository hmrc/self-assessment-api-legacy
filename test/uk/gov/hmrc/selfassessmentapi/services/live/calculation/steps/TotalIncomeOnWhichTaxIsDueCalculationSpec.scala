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

class TotalIncomeOnWhichTaxIsDueCalculationSpec extends UnitSpec with SelfAssessmentSugar {

  "run" should {

    "calculate total income on which tax is due" in {

      val liability = aLiability().copy(
          totalIncomeReceived = Some(100),
          totalAllowancesAndReliefs = Some(50)
      )

      TotalIncomeOnWhichTaxIsDueCalculation.run(SelfAssessment(), liability).getLiabilityOrFail shouldBe liability
        .copy(totalIncomeOnWhichTaxIsDue = Some(50))
    }

    "return zero if totalIncomeReceived is less than totalDeductions" in {

      val liability = aLiability().copy(
          totalIncomeReceived = Some(100),
          totalAllowancesAndReliefs = Some(200)
      )

      TotalIncomeOnWhichTaxIsDueCalculation.run(SelfAssessment(), liability).getLiabilityOrFail shouldBe liability
        .copy(totalIncomeOnWhichTaxIsDue = Some(0))
    }

    "throw exception if totalIncomeReceived is None" in {

      val liability = aLiability().copy(
          totalIncomeReceived = None,
          totalAllowancesAndReliefs = Some(200)
      )

      intercept[IllegalStateException] {
        TotalIncomeOnWhichTaxIsDueCalculation.run(SelfAssessment(), liability)
      }
    }

    "throw exception if deductions is None" in {

      val liability = aLiability().copy(
          totalIncomeReceived = Some(100),
          totalAllowancesAndReliefs = None
      )

      intercept[IllegalStateException] {
        TotalIncomeOnWhichTaxIsDueCalculation.run(SelfAssessment(), liability)
      }
    }
  }
}
