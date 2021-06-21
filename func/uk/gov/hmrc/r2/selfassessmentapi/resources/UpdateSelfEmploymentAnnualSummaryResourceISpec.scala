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
import uk.gov.hmrc.r2.selfassessmentapi.NinoGenerator
import uk.gov.hmrc.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}
import uk.gov.hmrc.support.IntegrationBaseSpec
import uk.gov.hmrc.utils.{Nino, TaxYear}

class UpdateSelfEmploymentAnnualSummaryResourceISpec extends IntegrationBaseSpec {

  private trait Test {

    protected val nino: Nino = NinoGenerator().nextNino()
    val correlationId: String = "X-ID"
    val id: String = "abc"
    val taxYear: TaxYear = TaxYear("2017-18")

    def uri: String = s"/r2/ni/${nino.nino}/self-employments/abc/$taxYear"

    def desUrl: String = s"/income-store/nino/${nino.nino}/self-employments/$id/annual-summaries/${taxYear.toDesTaxYear}"

    def desResponse: JsValue = Json.parse(DesJsons.SelfEmployment.AnnualSummary.response)

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "amending annual summaries" should {
    s"return code 204" when {
      "a valid request is made" in new Test {

        val requestJson: JsValue = Jsons.SelfEmployment.annualSummary()

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.PUT, desUrl, OK, desResponse)
        }

        private val response = await(request().put(requestJson))
        response.status shouldBe NO_CONTENT
      }

      "DES returns NO_CONTENT" in new Test {

        val requestJson: JsValue = Jsons.SelfEmployment.annualSummary()

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.PUT, desUrl, NO_CONTENT, desResponse)
        }

        private val response = await(request().put(requestJson))
        response.status shouldBe NO_CONTENT
      }
    }

    s"return status code 400" when {
      s"providing an invalid adjustment & allowance" in new Test {

        val requestJson: JsValue = Jsons.SelfEmployment.annualSummary(
          includedNonTaxableProfits = -100, overlapReliefUsed = -100,
          goodsAndServicesOwnUse = -100, capitalAllowanceMainPool = -100)
        val expectedJson: JsValue = Json.parse(
          """
            |{
            |	"code": "INVALID_REQUEST",
            |	"message": "Invalid request",
            |	"errors": [{
            |		"code": "INVALID_MONETARY_AMOUNT",
            |		"message": "Amount should be a non-negative number less than 99999999999999.98 with up to 2 decimal places",
            |		"path": "/allowances/capitalAllowanceMainPool"
            |	}, {
            |		"code": "INVALID_MONETARY_AMOUNT",
            |		"message": "Amount should be a non-negative number less than 99999999999999.98 with up to 2 decimal places",
            |		"path": "/adjustments/overlapReliefUsed"
            |	}, {
            |		"code": "INVALID_MONETARY_AMOUNT",
            |		"message": "Amount should be a non-negative number less than 99999999999999.98 with up to 2 decimal places",
            |		"path": "/adjustments/goodsAndServicesOwnUse"
            |	}, {
            |		"code": "INVALID_MONETARY_AMOUNT",
            |		"message": "Amount should be a non-negative number less than 99999999999999.98 with up to 2 decimal places",
            |		"path": "/adjustments/includedNonTaxableProfits"
            |	}]
            |}
            |""".stripMargin)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        private val response = await(request().put(requestJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe expectedJson
      }

      s"updating self-employment annual summary for a non MTD year" in new Test {

        val requestJson: JsValue = Jsons.SelfEmployment.annualSummary()

        override def uri: String = s"/r2/ni/${nino.nino}/self-employments/abc/2015-16"

        val expectedJson: JsValue = Json.parse(
          """
            |{"code":"TAX_YEAR_INVALID","message":"Tax year invalid"}
            |""".stripMargin)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        private val response = await(request().put(requestJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe expectedJson
      }
    }

    s"return status code 404 " when {
      "self-employment business that does not exist" in new Test {

        val requestJson: JsValue = Jsons.SelfEmployment.annualSummary()

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onError(DesStub.PUT, desUrl, NOT_FOUND, DesJsons.Errors.ninoNotFound)
        }

        private val response = await(request().put(requestJson))
        response.status shouldBe NOT_FOUND
      }
    }

    s"return status code 500" when {
      "DES is experiencing issues  for $" in new Test {

        val period: JsValue = Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"))

        override def desResponse: JsValue = Json.parse(DesJsons.Errors.serverError)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.PUT, desUrl, INTERNAL_SERVER_ERROR, desResponse)
        }

        private val response = await(request().put(period))
        response.status shouldBe INTERNAL_SERVER_ERROR
      }

      "DES is unavailable" in new Test {

        val period: JsValue = Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"))

        override def desResponse: JsValue = Json.parse(DesJsons.Errors.serviceUnavailable)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.PUT, desUrl, SERVICE_UNAVAILABLE, desResponse)
        }

        private val response = await(request().put(period))
        response.status shouldBe INTERNAL_SERVER_ERROR
      }

      "DES return a status code that we don't handle" in new Test {

        val period: JsValue = Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"))

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.PUT, desUrl, IM_A_TEAPOT, Json.obj())
        }

        private val response = await(request().put(period))
        response.status shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}

