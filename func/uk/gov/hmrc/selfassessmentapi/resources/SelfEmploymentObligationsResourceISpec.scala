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

import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentObligationsResourceISpec extends BaseFunctionalSpec {

  val regime = "ITSB"
  "retrieveObligations" should {

    "return code 200 with a set of obligations" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().obligations.returnObligationsFor(nino)
        .when()
        .get(s"/ni/${nino.nino}/self-employments/abc/obligations")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Obligations().toString)
      //        .when()
      //        .get("/admin/metrics")
      //        .thenAssertThat()
      //        .body(_ \ "timers" \ "Timer-API-SelfEmployments-obligations-GET" \ "count").is(1)
    }

    "forward the GovTestScenario header to DES" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().obligations.receivesObligationsTestHeader(nino, "ALL_MET")
        .when()
        .get(s"/ni/${nino.nino}/self-employments/abc/obligations").withHeaders(GovTestScenarioHeader, "ALL_MET")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Obligations().toString)
    }

    "return code 404 when self employment id does not exist" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().obligations.obligationNotFoundFor(nino)
        .when()
        .get(s"/ni/${nino.nino}/self-employments/abc/obligations")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 404 when obligations with no 'identification' data is returned" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().obligations.returnObligationsWithNoIdentificationFor(nino)
        .when()
        .get(s"/ni/${nino.nino}/self-employments/abc/obligations")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 400 when nino is invalid" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().invalidNinoFor(nino)
        .when()
        .get("/ni/abcd1234/self-employments/abc/obligations")
        .thenAssertThat()
        .statusIs(400)
    }

    "return code 400 when 'to' date is supplied with no 'from' date" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/${nino.nino}/self-employments/abc/obligations?to=2017-03-31")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsError("RULE_DATE_PARAMETER")
    }

    "return code 400 when 'from' date is supplied with no 'to' date" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/${nino.nino}/self-employments/abc/obligations?from=2017-03-31")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsError("RULE_DATE_PARAMETER")
    }

    "return code 400 when to is before from date" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/${nino.nino}/self-employments/abc/obligations?from=2017-12-01&to=2017-03-31")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsError("RANGE_TO_DATE_BEFORE_FROM_DATE")
    }

    "return code 400 when from and to date range is more than 366 days" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/${nino.nino}/self-employments/abc/obligations?from=2017-01-01&to=2018-01-02")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsError("RANGE_DATE_TOO_LONG")
    }
  }
}
