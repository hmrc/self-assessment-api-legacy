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

class CreateSelfEmploymentsResourceISpec  extends IntegrationBaseSpec {

  private trait Test {

    protected val nino: Nino = NinoGenerator().nextNino()
    val correlationId: String = "X-ID"
    val id: String = "abc"
    val mtdId: String = "123"

    def uri: String = s"/ni/${nino.nino}/self-employments"

    def desUrl: String = s"/income-tax-self-assessment/nino/${nino.nino}/business"

    def desResponse: JsValue = Json.parse(DesJsons.SelfEmployment.createResponse(id, mtdId))

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "create self-employment resource" should {
    s"return status code 201 containing a location header" when {
      "a valid request is made" in new Test {

        val requestJson: JsValue = Jsons.SelfEmployment()

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.POST, desUrl, OK, desResponse)
        }

        private val response = await(request.post(requestJson))
        response.status shouldBe CREATED
        response.header("Location") shouldBe Some(s"/self-assessment/ni/${nino.nino}/self-employments/abc")

      }
    }

    s"return status code 400" when {
      "request payload contains invalid dates in the accountingPeriod" in new Test {

        val requestJson: JsValue = Jsons.SelfEmployment(accPeriodStart = "01-01-2017", accPeriodEnd = "02-01-2017")
        val expectedJson: JsValue = Json.parse(
          """
            |{
            |	"code": "INVALID_REQUEST",
            |	"message": "Invalid request",
            |	"errors": [{
            |		"code": "INVALID_DATE",
            |		"message": "please provide a date in ISO format (i.e. YYYY-MM-DD)",
            |		"path": "/accountingPeriod/end"
            |	}, {
            |		"code": "INVALID_DATE",
            |		"message": "please provide a date in ISO format (i.e. YYYY-MM-DD)",
            |		"path": "/accountingPeriod/start"
            |	}]
            |}
            |""".stripMargin)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        private val response = await(request.post(requestJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe expectedJson
      }

      "the request payload contains an invalid accounting type" in new Test {

        val requestJson: JsValue = Jsons.SelfEmployment(accountingType = "INVALID_ACC_TYPE")
        val expectedJson: JsValue = Json.parse(
          """
            |{
            |	"code": "INVALID_REQUEST",
            |	"message": "Invalid request",
            |	"errors": [{
            |		"code": "INVALID_VALUE",
            |		"message": "AccountingType should be one of: CASH, ACCRUAL",
            |		"path": "/accountingType"
            |	}]
            |}
            |""".stripMargin
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        private val response = await(request.post(requestJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe expectedJson
      }

      "the request payload fails DES validation" in new Test {

        val requestJson: JsValue = Jsons.SelfEmployment()
        val expectedJson: JsValue = Json.parse(Jsons.Errors.invalidRequest)

        override def desResponse: JsValue = Json.parse(DesJsons.Errors.invalidPayload)
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.POST, desUrl, BAD_REQUEST, desResponse)
        }

        private val response = await(request.post(requestJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe expectedJson
      }

      "the request payload fails DES nino validation" in new Test {

        val requestJson: JsValue = Jsons.SelfEmployment()
        val expectedJson: JsValue = Json.parse(Jsons.Errors.ninoInvalid)

        override def desResponse: JsValue = Json.parse(DesJsons.Errors.invalidNino)
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.POST, desUrl, BAD_REQUEST, desResponse)
        }

        private val response = await(request.post(requestJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe expectedJson
      }
    }

    s"return status code 403" when {
      "attempting to create more than one self-employment source" in new Test {

        val requestJson: JsValue = Jsons.SelfEmployment()

        override def desResponse: JsValue = Json.parse(DesJsons.Errors.tooManySources)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.POST, desUrl, INTERNAL_SERVER_ERROR, desResponse)
        }

        private val response = await(request.post(requestJson))
        response.status shouldBe INTERNAL_SERVER_ERROR
      }
    }

    s"return status code 500" when {
      "DES is experiencing issues" in new Test {

        val requestJson: JsValue = Jsons.SelfEmployment()

        override def desResponse: JsValue = Json.parse(DesJsons.Errors.serverError)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.POST, desUrl, INTERNAL_SERVER_ERROR, desResponse)
        }

        private val response = await(request.post(requestJson))
        response.status shouldBe INTERNAL_SERVER_ERROR
      }

      "DES is unavailable" in new Test {

        val requestJson: JsValue = Jsons.SelfEmployment()

        override def desResponse: JsValue = Json.parse(DesJsons.Errors.serviceUnavailable)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.POST, desUrl, SERVICE_UNAVAILABLE, desResponse)
        }

        private val response = await(request.post(requestJson))
        response.status shouldBe INTERNAL_SERVER_ERROR
      }

      "DES return a status code that we don't handle" in new Test {

        val requestJson: JsValue = Jsons.SelfEmployment()

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.POST, desUrl, IM_A_TEAPOT, Json.obj())
        }

        private val response = await(request.post(requestJson))
        response.status shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}

