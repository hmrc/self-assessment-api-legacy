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

import uk.gov.hmrc.selfassessmentapi.{SelfEmploymentSugar, UnitSpec}

class PersonalAllowanceCalculationSpec extends UnitSpec with SelfEmploymentSugar {

  "run" should {

    "calculate personal allowance for total income lesser than 100000" in {
      val liability = aLiability().copy(totalTaxableIncome = Some(BigDecimal(23400)))
      val result = PersonalAllowanceCalculation.run(SelfAssessment(), liability)
      result.personalAllowance shouldBe Some(BigDecimal(11000))
    }

    "calculate personal allowance for total income equal to 100001.99" in {
      val liability = aLiability().copy(totalTaxableIncome = Some(BigDecimal(100001.99)))
      val result = PersonalAllowanceCalculation.run(SelfAssessment(), liability)
      result.personalAllowance shouldBe Some(BigDecimal(11000))
    }

    "calculate personal allowance for total income equal to 110000" in {
      val liability = aLiability().copy(totalTaxableIncome = Some(BigDecimal(110000)))
      val result = PersonalAllowanceCalculation.run(SelfAssessment(), liability)
      result.personalAllowance shouldBe Some(BigDecimal(6000))
    }

    "calculate personal allowance for total income equal to 121998.99" in {
      val liability = aLiability().copy(totalTaxableIncome = Some(BigDecimal(121998.99)))
      val result = PersonalAllowanceCalculation.run(SelfAssessment(), liability)
      result.personalAllowance shouldBe Some(BigDecimal(1))
    }

    "calculate personal allowance for total income equal to 122000" in {
      val liability = aLiability().copy(totalTaxableIncome = Some(BigDecimal(122000)))
      val result = PersonalAllowanceCalculation.run(SelfAssessment(), liability)
      result.personalAllowance shouldBe Some(BigDecimal(0))
    }

    "calculate personal allowance for total income equal to 322000" in {
      val liability = aLiability().copy(totalTaxableIncome = Some(BigDecimal(322000)))
      val result = PersonalAllowanceCalculation.run(SelfAssessment(), liability)
      result.personalAllowance shouldBe Some(BigDecimal(0))
    }

  }
}
