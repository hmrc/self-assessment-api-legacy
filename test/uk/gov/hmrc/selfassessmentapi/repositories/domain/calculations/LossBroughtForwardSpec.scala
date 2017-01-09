/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.calculations

import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.repositories.domain.builders.SelfEmploymentBuilder

class LossBroughtForwardSpec extends UnitSpec {

  "Loss brought forward" should {
    "be equal self employment loss brought forward" in {
      val selfEmployment = SelfEmploymentBuilder()
        .withAdjustments(
          lossBroughtForward = 999,
          outstandingBusinessIncome = 0,
          averagingAdjustment = 0,
          overlapReliefUsed = 0,
          basisAdjustment = 0,
          includedNonTaxableProfits = 0,
          accountingAdjustment = 0)
        .create()

      Deductions.LossBroughtForward(selfEmployment) shouldBe 999
    }

    "be 0 if none is provided" in {
      val selfEmployment = SelfEmploymentBuilder().create()

      Deductions.LossBroughtForward(selfEmployment) shouldBe 0
    }
  }

}
