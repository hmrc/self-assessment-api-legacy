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

import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.Adjustments
import uk.gov.hmrc.selfassessmentapi.domain.ukproperty.IncomeType
import uk.gov.hmrc.selfassessmentapi.repositories.domain._
import uk.gov.hmrc.selfassessmentapi.{SelfEmploymentSugar, UnitSpec, domain}

class IncomeTaxReliefCalculationSpec extends UnitSpec with SelfEmploymentSugar {

  "run" should {

    "round up the sum of loss brought forward for self employments." in {
      val selfEmploymentOne = aSelfEmployment().copy(adjustments = Some(Adjustments(lossBroughtForward = Some(100.14))))
      val selfEmploymentTwo = aSelfEmployment().copy(adjustments = Some(Adjustments(lossBroughtForward = Some(200.59))))

      incomeTaxReliefFor(selfEmployments = Seq(selfEmploymentOne, selfEmploymentTwo), ukProperties = Seq.empty) shouldBe 301
    }

    "round up the sum of loss brought forward and cap it at the total adjusted profit for UK properties" in {
      val ukPropertyOne = aUkProperty().copy(incomes = Seq(MongoUKPropertiesIncomeSummary("", IncomeType.RentIncome, 250)),
        adjustments = Some(domain.ukproperty.Adjustments(lossBroughtForward = Some(100.12))))
      val ukPropertyTwo = aUkProperty().copy(incomes = Seq(MongoUKPropertiesIncomeSummary("", IncomeType.RentIncome, 250)),
        adjustments = Some(domain.ukproperty.Adjustments(lossBroughtForward = Some(450.45))))

      incomeTaxReliefFor(Seq.empty, Seq(ukPropertyOne, ukPropertyTwo)) shouldBe 500
    }

    "income tax relief should be the total of loss brought forward for self employments and capped UK properties" in {
      val selfEmploymentOne = aSelfEmployment().copy(adjustments = Some(Adjustments(lossBroughtForward = Some(100.14))))
      val selfEmploymentTwo = aSelfEmployment().copy(adjustments = Some(Adjustments(lossBroughtForward = Some(200.59))))
      val ukPropertyOne = aUkProperty().copy(incomes = Seq(MongoUKPropertiesIncomeSummary("", IncomeType.RentIncome, 250)),
        adjustments = Some(domain.ukproperty.Adjustments(lossBroughtForward = Some(100.12))))
      val ukPropertyTwo = aUkProperty().copy(incomes = Seq(MongoUKPropertiesIncomeSummary("", IncomeType.RentIncome, 250)),
        adjustments = Some(domain.ukproperty.Adjustments(lossBroughtForward = Some(450.45))))

      incomeTaxReliefFor(selfEmployments = Seq(selfEmploymentOne, selfEmploymentTwo), ukProperties = Seq(ukPropertyOne, ukPropertyTwo)) shouldBe 801
    }

    "income tax relief is 0 if there is no loss brought forward" in {
      incomeTaxReliefFor(selfEmployments = Seq.empty, ukProperties = Seq.empty) shouldBe 0
    }
  }

  private def incomeTaxReliefFor(selfEmployments: Seq[MongoSelfEmployment], ukProperties: Seq[MongoUKProperties]): BigDecimal = {
    IncomeTaxReliefCalculation.run(
      selfAssessment = SelfAssessment(selfEmployments = selfEmployments, ukProperties = ukProperties),
      liability = aLiability()
    ).allowancesAndReliefs.incomeTaxRelief.get
  }
}
