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

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSRequest
import uk.gov.hmrc.selfassessmentapi.NinoGenerator
import uk.gov.hmrc.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}
import uk.gov.hmrc.support.IntegrationBaseSpec
import uk.gov.hmrc.utils.Nino

class RetrieveSelfEmploymentsResourceISpec  extends IntegrationBaseSpec {

  private trait Test {

    protected val nino: Nino = NinoGenerator().nextNino()
    val correlationId: String = "X-ID"
    val id: String = "abc"
    val mtdId: String = "123"

    def uri: String = s"/ni/${nino.nino}/self-employments/abc"

    def desUrl: String = s"/registration/business-details/nino/${nino.nino}"

    def desResponse: JsValue = Json.parse(DesJsons.SelfEmployment(nino, mtdId, id))

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "Retrieve self-employment resource" should {
    s"return status code 200 containing a location header" when {
      "a valid request is made" in new Test {

        val expectedJson: JsValue = Jsons.SelfEmployment(cessationDate = None, businessDescription = None)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, OK, desResponse)
        }

        private val response = await(request.get)
        response.status shouldBe OK
        response.json shouldBe expectedJson
      }
    }

    s"return status code 400" when {
      "the request payload fails DES nino validation" in new Test {

        val expectedJson: JsValue = Json.parse(Jsons.Errors.ninoInvalid)

        override def desResponse: JsValue = Json.parse(DesJsons.Errors.invalidNino)
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, BAD_REQUEST, desResponse)
        }

        private val response = await(request.get)
        response.status shouldBe BAD_REQUEST
        response.json shouldBe expectedJson
      }
    }

    s"return status code 404" when {
      "resource does not exist" in new Test {

        override def uri: String = s"/ni/${nino.nino}/self-employments/invalidSourceId"
        override def desResponse: JsValue = Json.parse(DesJsons.SelfEmployment.emptySelfEmployment(nino, mtdId))

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, OK, desResponse)
        }

        private val response = await(request.get)
        response.status shouldBe NOT_FOUND
      }

      "income id is not matched" in new Test {

        override def uri: String = s"/ni/${nino.nino}/self-employments/$id"
        override def desResponse: JsValue = Json.parse(DesJsons.SelfEmployment(nino, mtdId, id = "inexistent"))

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, OK, desResponse)
        }

        private val response = await(request.get)
        response.status shouldBe NOT_FOUND
      }
    }

    s"return status code 500" when {
      "DES returns invalid Json" in new Test {

        override def desResponse: JsValue = Json.parse("""{ "businessData": 1 }""")

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, OK, desResponse)
        }

        private val response = await(request.get)
        response.status shouldBe INTERNAL_SERVER_ERROR
      }

      "DES throws 500 error" in new Test {

        override def desResponse: JsValue = Json.parse(DesJsons.Errors.serverError)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, INTERNAL_SERVER_ERROR, desResponse)
        }

        private val response = await(request.get)
        response.status shouldBe INTERNAL_SERVER_ERROR
      }

      "DES is unavailable" in new Test {

        override def desResponse: JsValue = Json.parse(DesJsons.Errors.serviceUnavailable)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, SERVICE_UNAVAILABLE, desResponse)
        }

        private val response = await(request.get)
        response.status shouldBe INTERNAL_SERVER_ERROR
      }

      "DES return a status code that we don't handle" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, IM_A_TEAPOT, Json.obj())
        }

        private val response = await(request.get)
        response.status shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
//  "retrieveAll" should {
//    "return code 200 when retrieving self-employments that exist" in {
//
//      val expectedBody =
//        s"""
//           |[
//           |  ${Jsons.SelfEmployment(cessationDate = None, businessDescription = None).toString()}
//           |]
//         """.stripMargin
//
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des().selfEmployment.willBeReturnedFor(nino)
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments")
//        .thenAssertThat()
//        .statusIs(200)
//        .contentTypeIsJson()
//        .bodyIsLike(expectedBody)
//        .selectFields(_ \\ "id").isLength(1).matches("\\w+".r)
//    }
//
//    "return code 200 with an empty body when the user has no self-employment sources" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des().selfEmployment.noneFor(nino)
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments")
//        .thenAssertThat()
//        .statusIs(200)
//        .jsonBodyIsEmptyArray()
//    }
//
//    "return code 400 when attempting to retrieve self-employments that fails DES nino validation" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des().invalidNinoFor(nino)
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments")
//        .thenAssertThat()
//        .statusIs(400)
//        .bodyIsLike(Jsons.Errors.ninoInvalid)
//    }
//
//    "return code 404 when attempting to retrieve self-employments for a nino that does not exist" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des().ninoNotFoundFor(nino)
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments")
//        .thenAssertThat()
//        .statusIs(404)
//    }
//
//    "return code 500 when DES returns invalid Json" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des().selfEmployment.invalidJson(nino)
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments")
//        .thenAssertThat()
//        .statusIs(500)
//        .bodyIsLike(Jsons.Errors.internalServerError)
//    }
//
//    "return code 500 when we receive a status code from DES that we do not handle" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des().isATeapotFor(nino)
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments")
//        .thenAssertThat()
//        .statusIs(500)
//    }
//  }
//
//}
