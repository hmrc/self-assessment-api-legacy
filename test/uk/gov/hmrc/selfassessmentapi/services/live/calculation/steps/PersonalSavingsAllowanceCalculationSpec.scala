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

import org.scalatest.prop.GeneratorDrivenPropertyChecks
import uk.gov.hmrc.selfassessmentapi.{SelfAssessmentSugar, UnitSpec}
import uk.gov.hmrc.selfassessmentapi.Generators._

class PersonalSavingsAllowanceCalculationSpec
    extends UnitSpec
    with SelfAssessmentSugar
    with GeneratorDrivenPropertyChecks {

  "run" should {

    "calculate the personal savings allowance as zero when the total income on which tax is due is zero" in {
      val liability = aLiability().copy(totalIncomeOnWhichTaxIsDue = Some(0))
      val result = PersonalSavingsAllowanceCalculation.run(SelfAssessment(), liability)

      result.getLiabilityOrFail.allowancesAndReliefs.personalSavingsAllowance shouldEqual Some(BigDecimal(0))
    }

    "calculate the personal savings allowance for the basic tax band" in forAll(basicTaxBandAmountGen) { amount =>
      val liability = aLiability().copy(totalIncomeOnWhichTaxIsDue = Some(amount))
      val result = PersonalSavingsAllowanceCalculation.run(SelfAssessment(), liability)

      result.getLiabilityOrFail.allowancesAndReliefs.personalSavingsAllowance shouldEqual Some(BigDecimal(1000))
    }

    "calculate the personal savings allowance for the higher tax band" in forAll(higherTaxBandAmountGen) { amount =>
      val liability = aLiability().copy(totalIncomeOnWhichTaxIsDue = Some(amount))
      val result = PersonalSavingsAllowanceCalculation.run(SelfAssessment(), liability)

      result.getLiabilityOrFail.allowancesAndReliefs.personalSavingsAllowance shouldEqual Some(BigDecimal(500))
    }

    "calculate the personal savings allowance for the additional higher tax band" in forAll(
        additionalHigherTaxBandAmountGen) { amount =>
      val liability = aLiability().copy(totalIncomeOnWhichTaxIsDue = Some(amount))
      val result = PersonalSavingsAllowanceCalculation.run(SelfAssessment(), liability)

      result.getLiabilityOrFail.allowancesAndReliefs.personalSavingsAllowance shouldEqual Some(BigDecimal(0))
    }
  }

}
