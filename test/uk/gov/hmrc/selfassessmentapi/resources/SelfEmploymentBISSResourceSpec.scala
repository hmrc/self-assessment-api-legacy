/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.resources

import play.api.mvc.Result
import play.api.test.FakeRequest
import uk.gov.hmrc.selfassessmentapi.models.SourceType

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SelfEmploymentBISSResourceSpec extends ResourceSpec {

  class Setup {
    val resource = new SelfEmploymentBISSResource(
      mockAppContext,
      mockAuthorisationService,
      cc
    )
    mockAPIAction(SourceType.SelfEmployments)
  }

  "getSummary" should {
    "return a 410 for any request" when {
      "any data is supplied" in new Setup {

        val result: Future[Result] = resource.getSummary(nino, taxYear, "12345678")(FakeRequest())
        status(result) shouldBe GONE
        contentType(result) shouldBe Some(JSON)
      }
    }
  }
}
