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

package uk.gov.hmrc.r2.selfassessmentapi.resources

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSRequest
import uk.gov.hmrc.selfassessmentapi.NinoGenerator
import uk.gov.hmrc.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}
import uk.gov.hmrc.support.IntegrationBaseSpec
import uk.gov.hmrc.utils.Nino

class PropertiesResourceNewISpec extends IntegrationBaseSpec {

  private trait Test {

    protected val nino: Nino = NinoGenerator().nextNino()

    val correlationId: String = "X-ID"

    def uri: String = s"/ni/${nino.nino}/uk-properties"

    def desUrl: String = s"/income-tax-self-assessment/nino/${nino.nino}/properties"

    def desResponse(res: String): JsValue = Json.parse(res)

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "creating a property business" should {
    "return status code 201 with location header" when {
      "a valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.POST, desUrl, OK, desResponse(DesJsons.Properties.createResponse))
        }

        private val response = await(request().post(Jsons.Properties()))
        response.status shouldBe CREATED
        response.header("Location") shouldBe Some(s"/self-assessment/ni/${nino.nino}/uk-properties")
      }
    }

    "return status code 409 with location header" when {
      "attempting to create the same property business more than once" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.POST, desUrl, FORBIDDEN, desResponse(DesJsons.Errors.propertyConflict))
        }

        private val response = await(request().post(Jsons.Properties()))
        response.status shouldBe CONFLICT
        response.header("Location") shouldBe Some(s"/self-assessment/ni/${nino.nino}/uk-properties")
      }
    }

    "return status code 400" when {
      "attempting to create a property business with invalid information" in new Test {

        val expectedJson: JsValue = Json.parse(Jsons.Errors.invalidRequest)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.POST, desUrl, BAD_REQUEST, desResponse(DesJsons.Errors.invalidPayload))
        }

        private val response = await(request().post(Jsons.Properties()))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe expectedJson
      }
    }

    "return status code 400 with NINO_INVALID error" when {
      "attempting to create a property business that fails DES nino validation" in new Test {

        val expectedJson: JsValue = Json.parse(Jsons.Errors.ninoInvalid)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.POST, desUrl, BAD_REQUEST, desResponse(DesJsons.Errors.invalidNino))
        }

        private val response = await(request().post(Jsons.Properties()))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe expectedJson
      }
    }

    "return status code 500 with INTERNAL_SERVER_ERROR" when {
      "DES service throw server error" in new Test {

        val expectedJson: JsValue = Json.parse(Jsons.Errors.internalServerError)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.POST, desUrl, INTERNAL_SERVER_ERROR, desResponse(DesJsons.Errors.serverError))
        }

        private val response = await(request().post(Jsons.Properties()))
        response.status shouldBe INTERNAL_SERVER_ERROR
        response.json shouldBe expectedJson
      }

      "DES service is unavailable or down" in new Test {

        val expectedJson: JsValue = Json.parse(Jsons.Errors.internalServerError)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.POST, desUrl, SERVICE_UNAVAILABLE, desResponse(DesJsons.Errors.serviceUnavailable))
        }

        private val response = await(request().post(Jsons.Properties()))
        response.status shouldBe INTERNAL_SERVER_ERROR
        response.json shouldBe expectedJson
      }

      "we receive a status code from DES that we do not handle" in new Test {

        val expectedJson: JsValue = Json.parse(Jsons.Errors.internalServerError)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.POST, desUrl, IM_A_TEAPOT, Json.obj())
        }

        private val response = await(request().post(Jsons.Properties()))
        response.status shouldBe INTERNAL_SERVER_ERROR
        response.json shouldBe expectedJson
      }
    }
  }
}


