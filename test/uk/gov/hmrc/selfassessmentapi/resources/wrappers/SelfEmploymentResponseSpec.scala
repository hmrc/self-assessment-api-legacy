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

package uk.gov.hmrc.selfassessmentapi.resources.wrappers

import play.api.libs.json.Json
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.UnitSpec

class SelfEmploymentResponseSpec extends UnitSpec {
  private val nino = generateNino

  "createLocation" should {
    "return a string containing the location with an ID extracted from some JSON" in {
      val json =
        Json.parse(
          """
            |{
            | "incomeSources": [
            |   {
            |     "incomeSourceId": "abc"
            |   }
            | ]
            |}
          """.stripMargin)

      val wrapper = SelfEmploymentResponse(HttpResponse(200, responseJson = Some(json)))

      wrapper.createLocationHeader(nino) shouldBe Some(s"/self-assessment/ni/$nino/self-employments/abc")
    }

    "return None for a json that does not contain an income source ID" in {
      val wrapper = SelfEmploymentResponse(HttpResponse(200, responseJson = None))

      wrapper.createLocationHeader(nino) shouldBe None
    }
  }
}
