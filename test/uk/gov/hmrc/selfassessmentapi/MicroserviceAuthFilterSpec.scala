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

package uk.gov.hmrc.selfassessmentapi

import org.scalatest.{Matchers, WordSpecLike}
import uk.gov.hmrc.play.auth.controllers.AuthConfig
import uk.gov.hmrc.play.auth.microservice.connectors.ConfidenceLevel.L50
import uk.gov.hmrc.play.auth.microservice.connectors.{AuthRequestParameters, HttpVerb}
import uk.gov.hmrc.selfassessmentapi.config.MicroserviceAuthFilter

class MicroserviceAuthFilterSpec extends WordSpecLike with Matchers {

  val underTest = MicroserviceAuthFilter

  "MicroserviceAuthFilter" should {
    "extract resource that builds valid auth url" in {
      val resource = underTest.extractResource("/123456/employments", HttpVerb("GET"), AuthConfig(pattern = "/(\\w+)/.*".r, confidenceLevel = L50))
      resource.get.buildUrl("http://authhost.com/auth", AuthRequestParameters(L50)) shouldBe "http://authhost.com/auth/authorise/read/sa/123456?confidenceLevel=50"
    }
  }
}
