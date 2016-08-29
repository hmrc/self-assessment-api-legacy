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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.functional

import uk.gov.hmrc.selfassessmentapi.SelfEmploymentSugar._
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.Adjustments

class LossBroughtForwardSpec extends UnitSpec {

  private val selfEmploymentId = "selfEmploymentId"

  "Loss brought forward" should {
    "be equal self employment loss brought forward" in {
      val selfEmployment =
        aSelfEmployment(selfEmploymentId).copy(
          adjustments = Some(Adjustments(
            lossBroughtForward = Some(999)
          ))
        )

      Deductions.LossBroughtForward(selfEmployment) shouldBe 999
    }

    "be 0 if none is provided" in {
      val selfEmployment =
        aSelfEmployment(selfEmploymentId).copy(
          adjustments = None
        )

      Deductions.LossBroughtForward(selfEmployment) shouldBe 0
    }
  }

}
