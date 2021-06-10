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

import uk.gov.hmrc.support.IntegrationBaseSpec

class SelfAssessmentEndOfPeriodObligationsNewISpec extends IntegrationBaseSpec {

}

//  private trait Test {
//
//    protected val nino: Nino = NinoGenerator().nextNino()
//
//    val regime = "ITSB"
//    val correlationId: String = "X-ID"
//    val from = new LocalDate(2017, 1, 1)
//    val to = new LocalDate(2017, 12, 31)
//    val testRefNo = "abc"
//    val validSelfEmploymentId = "AABB12345678912"
//
//    def uri: String = s"/ni/${nino.nino}/self-employments/$validSelfEmploymentId/end-of-period-statements/obligations"
//
//    def desUrl: String = s"/enterprise/obligation-data/nino/${nino.nino}/ITSA"
//
//    val queryParams: Map[String, String] = Map("from" -> "2017-01-01", "to" -> "2017-12-31")
//
//    def desResponse(res: String): JsValue = Json.parse(res)
//
//    def setupStubs(): StubMapping
//
//    def request(): WSRequest = {
//      setupStubs()
//      buildRequest(uri)
//        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
//    }
//
//    def request(mtdQueryParams: Seq[(String, String)]): WSRequest = {
//      setupStubs()
//      buildRequest(uri)
//        .addQueryStringParameters(mtdQueryParams: _*)
//        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
//    }
//  }
//
//  "Retrieving end-of-period statement obligations" should {
//    "return status code 200 containing a set of canned obligations" when {
//      "a valid request is received" in new Test {
//
//        val expectedJson: JsValue = Jsons.Obligations.eops
//
//        def mtdQueryParams: Seq[(String, String)] =
//          Seq(
//            ("from", from.toString("yyyy-MM-dd")),
//            ("to", to.toString("yyyy-MM-dd"))
//          )
//
//        override def setupStubs(): StubMapping = {
//          AuditStub.audit()
//          AuthStub.authorised()
//          MtdIdLookupStub.ninoFound(nino)
//          DesStub.onSuccess(DesStub.GET, desUrl, queryParams, OK, desResponse(DesJsons.Obligations()))
//        }
//
//        private val response = await(request(mtdQueryParams).get)
//        response.status shouldBe OK
//        response.json shouldBe expectedJson
//        response.header("Content-Type") shouldBe Some("application/json")
//      }
//    }
//  }
//}
//
//  "Retrieving end-of-period statement obligations" should {
//
//    val from = new LocalDate(2017, 1, 1)
//    val to = new LocalDate(2017, 12, 31)
//
//    val testRefNo = "abc"
//    val validSelfEmploymentId = "AABB12345678912"
//
//    "return code 200 with a set of obligations" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des().obligations.returnEndOfPeriodObligationsFor(nino, validSelfEmploymentId)
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments/$validSelfEmploymentId/end-of-period-statements/obligations?from=$from&to=$to")
//        .thenAssertThat()
//        .statusIs(200)
//        .bodyIsLike(Jsons.Obligations.eops.toString)
//    }
//
//    "return code 400 when self-employment-id is invalid" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des().obligations.returnEndOfPeriodObligationsFor(nino, testRefNo)
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments/$testRefNo/end-of-period-statements/obligations?from=$from&to=$to")
//        .thenAssertThat()
//        .statusIs(400)
//        .bodyIsError("SELF_EMPLOYMENT_ID_INVALID")
//    }
//
//    "return code 404 when obligations with no 'identification' data is returned" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des().obligations.returnEopsObligationsWithNoIdentificationFor(nino)
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments/$validSelfEmploymentId/end-of-period-statements/obligations?from=$from&to=$to")
//        .thenAssertThat()
//        .statusIs(404)
//    }
//
//    "return code 400 when from date is invalid" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des().obligations.returnEndOfPeriodObligationsFor(nino, validSelfEmploymentId)
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments/$validSelfEmploymentId/end-of-period-statements/obligations?from=ABC&to=$to")
//        .thenAssertThat()
//        .statusIs(400)
//        .bodyIsLike(Json.toJson(Errors.InvalidDate).toString)
//    }
//
//
//    "return code 400 when to date is invalid" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des().obligations.returnEndOfPeriodObligationsFor(nino, validSelfEmploymentId)
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments/$validSelfEmploymentId/end-of-period-statements/obligations?from=$from&to=ABC")
//        .thenAssertThat()
//        .statusIs(400)
//        .bodyIsLike(Json.toJson(Errors.InvalidDate).toString)
//    }
//
//
//    "return code 400 when from and to date range is invalid" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des().obligations.returnEndOfPeriodObligationsFor(nino, validSelfEmploymentId)
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments/$validSelfEmploymentId/end-of-period-statements/obligations?from=$from&to=2016-12-31")
//        .thenAssertThat()
//        .statusIs(400)
//        .bodyIsLike(Json.toJson(Errors.InvalidDateRange_2).toString)
//    }
//
//    "return status 404 when and INVALID_BPKEY error is received" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des().obligations.returnEopsObligationsErrorFor(nino)(404, "INVALID_BPKEY")
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments/$validSelfEmploymentId/end-of-period-statements/obligations?from=$from&to=$to")
//        .thenAssertThat()
//        .statusIs(404)
//    }
//
//    def testErrorScenario(desStatus:Int, desCode:String)(expectedStatus: Int, expectedError: Errors.Error): Unit = {
//
//      s"return status $expectedStatus when a $desCode error is received" in {
//        given()
//          .userIsSubscribedToMtdFor(nino)
//          .clientIsFullyAuthorisedForTheResource
//          .des().obligations.returnEopsObligationsErrorFor(nino)(desStatus, desCode)
//          .when()
//          .get(s"/ni/${nino.nino}/self-employments/$validSelfEmploymentId/end-of-period-statements/obligations?from=$from&to=$to")
//          .thenAssertThat()
//          .statusIs(expectedStatus)
//          .bodyIsError(expectedError.code)
//      }
//    }
//
//    testErrorScenario(400, "INVALID_STATUS")(500, Errors.InternalServerError)
//    testErrorScenario(400, "INVALID_REGIME")(500, Errors.InternalServerError)
//    testErrorScenario(400, "INVALID_IDTYPE")(500, Errors.InternalServerError)
//    testErrorScenario(400, "INVALID_DATE_TO")(400, Errors.InvalidDate)
//    testErrorScenario(400, "INVALID_DATE_FROM")(400, Errors.InvalidDate)
//    testErrorScenario(400, "INVALID_DATE_RANGE")(400, Errors.InvalidDateRange_2)
//    testErrorScenario(400, "INVALID_IDNUMBER")(400, Errors.NinoInvalid)
//  }
//
//}
