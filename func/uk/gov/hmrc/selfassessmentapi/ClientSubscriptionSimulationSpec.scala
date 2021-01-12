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

package uk.gov.hmrc.selfassessmentapi

import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Configuration}
import uk.gov.hmrc.selfassessmentapi.resources.GovTestScenarioHeader
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode
import uk.gov.hmrc.support.BaseFunctionalSpec

class ClientSubscriptionSimulationSpec extends BaseFunctionalSpec {

  override lazy val app: Application = GuiceApplicationBuilder(configuration = Configuration.from(conf(true, true))).build()

  "Request for self-employments with Gov-Test-Scenario = CLIENT_NOT_SUBSCRIBED" should {
    "return HTTP 403 with error code informing client should be subscribed to MTD" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/self-employments")
        .withHeaders(GovTestScenarioHeader, "CLIENT_NOT_SUBSCRIBED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.CLIENT_NOT_SUBSCRIBED.toString)
    }
  }
}
