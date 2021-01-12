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

import play.api.{Application, Configuration}
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.support.BaseFunctionalSpec

class PropertiesBISSResourceFuncSpec extends BaseFunctionalSpec {

  override lazy val app: Application = GuiceApplicationBuilder(configuration = Configuration.from(conf(true))).build()

    "getSummary for Property BISS" should {
      "return code 410 for any request" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().PropertiesBISS.getSummary(nino, taxYear)
          .when()
          .get(s"/ni/$nino/uk-properties/$taxYear/income-summary")
          .thenAssertThat()
          .statusIs(410)
          .bodyIsLike(Jsons.Errors.resourceGone.toString)
      }
    }
}
