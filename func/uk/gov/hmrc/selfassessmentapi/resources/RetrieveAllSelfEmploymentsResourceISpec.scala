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

class RetrieveAllSelfEmploymentsResourceISpec  extends IntegrationBaseSpec {

  private trait Test {

    protected val nino: Nino = NinoGenerator().nextNino()
    val correlationId: String = "X-ID"
    val id: String = "abc"
    val mtdId: String = "123"

    def uri: String = s"/ni/${nino.nino}/self-employments"

    def desUrl: String = s".*/nino/${nino.nino}.*"

    def desResponse: JsValue = Json.parse(DesJsons.SelfEmployment(nino, mtdId, id))

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "RetrieveAll self-employment resource" should {
    s"return status code 200 with body" when {
      "a valid request is made" in new Test {

        val expectedJson: JsValue = Json.parse(
          s"""
             |[{
             |	"id": "abc",
             |	"accountingPeriod": {
             |		"start": "2017-04-06",
             |		"end": "2018-04-05"
             |	},
             |	"accountingType": "CASH",
             |	"commencementDate": "2017-01-01",
             |	"tradingName": "Acme Ltd",
             |	"businessAddressLineOne": "1 Acme Rd.",
             |	"businessAddressLineTwo": "London",
             |	"businessAddressLineThree": "Greater London",
             |	"businessAddressLineFour": "United Kingdom",
             |	"businessPostcode": "A9 9AA"
             |}]
         """.stripMargin)

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

    s"return status code 200 with empty body" when {
      "a valid request is made" in new Test {

        override def desResponse: JsValue = Json.parse(DesJsons.SelfEmployment.emptySelfEmployment(nino, mtdId))

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

