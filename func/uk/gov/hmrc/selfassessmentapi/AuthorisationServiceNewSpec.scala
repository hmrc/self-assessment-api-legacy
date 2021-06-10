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

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status.{FORBIDDEN, INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSRequest
import uk.gov.hmrc.selfassessmentapi.resources.Jsons
import uk.gov.hmrc.stubs.{AuditStub, AuthStub, MtdIdLookupStub}
import uk.gov.hmrc.support.IntegrationBaseSpec
import uk.gov.hmrc.utils.Nino

class AuthorisationServiceNewSpec extends IntegrationBaseSpec {

  private trait Test {

    protected val nino: Nino = NinoGenerator().nextNino()

    val correlationId: String = "X-ID"

    def uri: String = s"/ni/${nino.nino}/self-employments"

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "authCheck" should {

    "return status code 200 " when {
      "user is authorised" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.authorised()
        }

        private val response = await(request.get)
        response.status shouldBe OK
      }

      "agent is authorised" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.authorisedAgent()
        }

        private val response = await(request.get)
        response.status shouldBe OK
      }

      "agent is authorised but no agent code received" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.authorisedAgentWithNoCode()
        }

        private val response = await(request.get)
        response.status shouldBe OK
      }
    }

    "return status code 403 " when {
      "not subscribed to MTD" in new Test {

        val expectedJson: JsValue = Json.parse(Jsons.Errors.clientNotSubscribed)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.notFound(nino.nino)
        }

        private val response = await(request.get)
        response.status shouldBe FORBIDDEN
        response.json shouldBe expectedJson
      }

      "not authorised to access the resource as a agent" in new Test {

        val expectedJson: JsValue = Json.parse(Jsons.Errors.agentNotSubscribed)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.insufficientEnrolments()
        }

        private val response = await(request.get)
        response.status shouldBe FORBIDDEN
        response.json shouldBe expectedJson
      }

      "bearer token is missing" in new Test {

        val expectedJson: JsValue = Json.parse(Jsons.Errors.unauthorised)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.unauthorisedNotLoggedIn()
        }

        private val response = await(request.get)
        response.status shouldBe FORBIDDEN
        response.json shouldBe expectedJson
      }

      "upstream 502 error is received" in new Test {

        val expectedJson: JsValue = Json.parse(Jsons.Errors.unauthorised)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.badGatewayError()
        }

        private val response = await(request.get)
        response.status shouldBe FORBIDDEN
        response.json shouldBe expectedJson
      }

      "an Individual without CL200 accessed the service" in new Test {

        val expectedJson: JsValue = Json.parse(Jsons.Errors.unauthorised)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.authorisedWithoutL200()
        }

        private val response = await(request.get)
        response.status shouldBe FORBIDDEN
      }
    }

    "return status code 500 " when {

      "MTD lookup service returns 500" in new Test {

        val expectedJson: JsValue = Json.parse(Jsons.Errors.internalServerError)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.internalServerError(nino.nino)
        }

        private val response = await(request.get)
        response.status shouldBe INTERNAL_SERVER_ERROR
        response.json shouldBe expectedJson
      }

      "MTD lookup service is unavailable" in new Test {

        val expectedJson: JsValue = Json.parse(Jsons.Errors.internalServerError)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.serviceUnavailableError(nino.nino)
        }

        private val response = await(request.get)
        response.status shouldBe INTERNAL_SERVER_ERROR
        response.json shouldBe expectedJson
      }

      "upstream 5xx error is received" in new Test {

        val expectedJson: JsValue = Json.parse(Jsons.Errors.internalServerError)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.serverError()
        }

        private val response = await(request.get)
        response.status shouldBe INTERNAL_SERVER_ERROR
        response.json shouldBe expectedJson
      }

      "upstream 4xx error is received" in new Test {

        val expectedJson: JsValue = Json.parse(Jsons.Errors.internalServerError)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.forbiddenError()
        }

        private val response = await(request.get)
        response.status shouldBe INTERNAL_SERVER_ERROR
        response.json shouldBe expectedJson
      }

      "upstream nonfatal error is received" in new Test {

        val expectedJson: JsValue = Json.parse(Jsons.Errors.internalServerError)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.nonFatalError()
        }

        private val response = await(request.get)
        response.status shouldBe INTERNAL_SERVER_ERROR
        response.json shouldBe expectedJson
      }
    }
  }
}
//
//  "if the user is authorised as a filing-only agent they" should {
//    "be able to make POST requests" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .userIsPartiallyAuthorisedForTheResource
//        .des()
//        .selfEmployment
//        .willBeCreatedFor(nino)
//        .when()
//        .post(Jsons.SelfEmployment())
//        .to(s"/ni/${nino.nino}/self-employments")
//        .thenAssertThat()
//        .statusIs(201)
//    }
//
//    "be able to make PUT requests" in {
//      val property = R2Jsons.Properties.otherPeriod(
//        fromDate = Some("2017-04-06"),
//        toDate = Some("2018-04-05"),
//        rentIncome = 500,
//        rentIncomeTaxDeducted = 250.55,
//        premiumsOfLeaseGrant = Some(200.22),
//        reversePremiums = 22.35,
//        premisesRunningCosts = Some(100.50),
//        repairsAndMaintenance = Some(100.50),
//        financialCosts = Some(100),
//        professionalFees = Some(100.50),
//        costOfServices = Some(100.50),
//        otherCost = Some(100.50),
//        residentialFinancialCost = Some(100.55)
//      )
//
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .userIsPartiallyAuthorisedForTheResource
//        .des()
//        .properties
//        .periodWillBeUpdatedFor(nino, PropertyType.OTHER)
//        .when()
//        .put(property)
//        .at(s"/r2/ni/${nino.nino}/uk-properties/${PropertyType.OTHER}/periods/2017-04-06_2018-04-05")
//        .thenAssertThat()
//        .statusIs(204)
//    }
//
//    "be forbidden from making GET requests" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .userIsPartiallyAuthorisedForTheResource
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments/abc")
//        .thenAssertThat()
//        .statusIs(403)
//        .bodyIsLike(Jsons.Errors.agentNotAuthorised)
//    }
//
//    "be able to make POST requests but don't have an agentCode" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .userIsPartiallyAuthorisedForTheResourceNoAgentCode
//        .des()
//        .selfEmployment
//        .willBeCreatedFor(nino)
//        .when()
//        .post(Jsons.SelfEmployment())
//        .to(s"/ni/${nino.nino}/self-employments")
//        .thenAssertThat()
//        .statusIs(201)
//    }
//  }
//
//}
