/*
 * Copyright 2021 HM Revenue & Customs
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

class PropertiesBISSResourceSpec extends ResourceSpec {

  class Setup {
    val resource = new PropertiesBISSResource(
      mockAppContext,
      mockAuthorisationService,
      cc
    )
    mockAPIAction(SourceType.Properties)
  }

  "getSummary" should {
    "return a 410 for any request" when {
      "any data is supplied" in new Setup {

        val result: Future[Result] = resource.getSummary(nino, taxYear)(FakeRequest())
        status(result) shouldBe GONE
        contentType(result) shouldBe Some(JSON)
      }
    }
  }
}
