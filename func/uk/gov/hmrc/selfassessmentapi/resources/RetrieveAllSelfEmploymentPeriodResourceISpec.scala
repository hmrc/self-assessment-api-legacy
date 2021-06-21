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

class RetrieveAllSelfEmploymentPeriodResourceISpec extends IntegrationBaseSpec {

  private trait Test {

    protected val nino: Nino = NinoGenerator().nextNino()
    val periodKey: String = "A1A2"
    val correlationId: String = "X-ID"
    val id: String = "abc"

    def uri: String = s"/ni/${nino.nino}/self-employments/abc/periods"

    def desUrl: String = s"/income-store/nino/${nino.nino}/self-employments/$id/periodic-summaries"

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "retrieveAll periods" should {
    s"return status code 200 with body" when {
      "a valid request is made" in new Test {

        val expectedBody: JsValue = Json.parse(
          s"""
             |[
             |  {
             |    "id": "2017-04-06_2017-07-04",
             |    "from": "2017-04-06",
             |    "to": "2017-07-04"
             |  },
             |  {
             |    "id": "2017-07-05_2017-08-04",
             |    "from": "2017-07-05",
             |    "to": "2017-08-04"
             |  }
             |]
         """.stripMargin)

        def desResponse: JsValue = Json.parse(DesJsons.SelfEmployment.Period.periods)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, OK, desResponse)
        }

        private val response = await(request.get)
        response.status shouldBe OK
        response.json shouldBe expectedBody
      }

      "received payload with no periods" in new Test {

        def desResponse: JsValue = Json.parse(DesJsons.SelfEmployment.Period.emptyPeriods)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, OK, desResponse)
        }

        private val response = await(request.get)
        response.status shouldBe OK
        response.json shouldBe Json.arr()
      }
    }

    s"return status code 400" when {
      "DES fails nino validation" in new Test {

        val expectedJson: JsValue = Json.parse(Jsons.Errors.ninoInvalid)

        def desResponse: JsValue = Json.parse(DesJsons.Errors.invalidNino)

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
      "DES fails BusinessID validation" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, NOT_FOUND, Json.parse(DesJsons.Errors.invalidBusinessId))
        }

        private val response = await(request.get)
        response.status shouldBe NOT_FOUND
      }

      "self-employment source does not exist" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, NOT_FOUND, Json.parse(DesJsons.Errors.ninoNotFound))
        }

        private val response = await(request.get)
        response.status shouldBe NOT_FOUND
      }
    }

    s"return status code 500" when {
      "we receive an unexpected JSON from DES" in new Test {

        def desResponse: JsValue = Json.obj()

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, OK, desResponse)
        }

        private val response = await(request.get)
        response.status shouldBe INTERNAL_SERVER_ERROR
      }

      "DES is unavailable" in new Test {

        def desResponse: JsValue = Json.parse(DesJsons.Errors.serviceUnavailable)

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

