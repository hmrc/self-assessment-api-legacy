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

class TotalAllowancesAndReliefsSpec extends UnitSpec with SelfEmploymentSugar {

  "run" should {

    """calculate total allowances and reliefs by summing income tax relief
      |across self-employment sources and adding the personal allowance""".stripMargin in {
      val selfEmploymentId1 = "someSeId1"
      val selfEmploymentId2 = "someSeId2"
      val personalAllowance = BigDecimal(10000.00)
      val lossBroughtForward1 = BigDecimal(550.00)
      val lossBroughtForward2 = BigDecimal(450.00)
      val liability =
        aLiability().copy(personalAllowance = Some(personalAllowance),
                          profitFromSelfEmployments =
                            Seq(SelfEmploymentIncome(sourceId = selfEmploymentId1,
                                                     taxableProfit = 2430,
                                                     profit = 2430,
                                                     lossBroughtForward = lossBroughtForward1),
                                SelfEmploymentIncome(sourceId = selfEmploymentId2,
                                                     taxableProfit = 2430,
                                                     profit = 2430,
                                                     lossBroughtForward = lossBroughtForward2)))
      TotalAllowancesAndReliefs
        .run(SelfAssessment(), liability)
        .totalAllowancesAndReliefs shouldBe Some(
          personalAllowance + lossBroughtForward1 + lossBroughtForward2)
    }
  }
}
