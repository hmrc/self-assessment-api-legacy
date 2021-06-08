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

class UpdateSelfEmploymentPeriodResourceISpec extends IntegrationBaseSpec {

  private trait Test {

    protected val nino: Nino = NinoGenerator().nextNino()
    val periodKey: String = "A1A2"
    val correlationId: String = "X-ID"
    val id: String = "abc"

    def uri: String = s"/ni/${nino.nino}/self-employments/$id/periods/2017-04-05_2018-04-04"

    def desUrl: String = s"/income-store/nino/${nino.nino}/self-employments/$id/periodic-summaries"

    def desResponse: JsValue = Json.parse(DesJsons.SelfEmployment.Period.createResponse())

    val queryParams: Map[String, String] = Map("from" -> "2017-04-05", "to" -> "2018-04-04")

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "updatePeriod" should {
    s"return status code 201 containing a location header" when {
      "a valid request is made" in new Test {

        val requestJson: JsValue = Jsons.SelfEmployment.period(turnover = 200.25,
          otherIncome = 100.25,
          costOfGoodsBought = (200.25, 50.25),
          cisPaymentsToSubcontractors = (100.25, 55.25))

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.PUT, desUrl, queryParams, OK, desResponse)
        }

        private val response = await(request.put(requestJson))
        response.status shouldBe NO_CONTENT
      }

      "request payload period has bad debts expenses" in new Test {

        val requestJson: JsValue = Jsons.SelfEmployment.period(turnover = 200.25,
          otherIncome = 100.25,
          costOfGoodsBought = (200.25, 50.25),
          cisPaymentsToSubcontractors = (100.25, 55.25),
          badDebt = (-10.10, -10.10)
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.PUT, desUrl, queryParams, OK, desResponse)
        }

        private val response = await(request.put(requestJson))
        response.status shouldBe NO_CONTENT
      }
    }

    s"return status code 400" when {
      "the request payload contains both the 'expenses' and 'consolidatedExpenses" in new Test {

        val requestJson: JsValue = Jsons.SelfEmployment.period(turnover = 200.25,
          otherIncome = 100.25,
          costOfGoodsBought = (200.25, 50.25),
          cisPaymentsToSubcontractors = (100.25, 55.25),
          consolidatedExpenses = Some(12345))

        val expectedJson: JsValue = Json.parse(
          """
            |{
            |	"code": "INVALID_REQUEST",
            |	"message": "Invalid request",
            |	"errors": [{
            |		"code": "BOTH_EXPENSES_SUPPLIED",
            |		"message": "Both expenses and consolidatedExpenses elements cannot be present at the same time",
            |		"path": ""
            |	}]
            |}
            |""".stripMargin)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        private val response = await(request.put(requestJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe expectedJson
      }

      "the request payload has no incomes and expenses" in new Test {

        val requestJson: JsValue = Json.parse(
          s"""
             |{
             |  "incomes": {},
             |  "expenses": {}
             |}
         """.stripMargin)

        val expectedJson: JsValue = Json.parse(
          """
            |{
            |	"code": "INVALID_REQUEST",
            |	"message": "Invalid request",
            |	"errors": [{
            |		"code": "NO_INCOMES_AND_EXPENSES",
            |		"message": "No incomes and expenses are supplied",
            |		"path": ""
            |	}]
            |}
            |""".stripMargin)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        private val response = await(request.put(requestJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe expectedJson
      }
    }

    s"return status code 404" when {
      "period does not exist" in new Test {

        val period: JsValue = Jsons.SelfEmployment.period(
          fromDate = Some("2017-04-06"),
          toDate = Some("2017-07-04"),
          turnover = 100.25,
          otherIncome = 100.25,
          costOfGoodsBought = (100.25, 50.25),
          cisPaymentsToSubcontractors = (100.25, 50.25)
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.PUT, desUrl, NOT_FOUND, Json.parse(DesJsons.Errors.notFound))
        }

        private val response = await(request.put(period))
        response.status shouldBe NOT_FOUND
      }
    }

    s"return status code 500" when {
      "DES is experiencing issues" in new Test {

        val period: JsValue = Jsons.SelfEmployment.period(turnover = 200.25,
          otherIncome = 100.25,
          costOfGoodsBought = (200.25, 50.25),
          cisPaymentsToSubcontractors = (100.25, 55.25))

        override def desResponse: JsValue = Json.parse(DesJsons.Errors.serverError)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.PUT, desUrl, INTERNAL_SERVER_ERROR, desResponse)
        }

        private val response = await(request.put(period))
        response.status shouldBe INTERNAL_SERVER_ERROR
      }

      "DES is unavailable" in new Test {

        val period: JsValue = Jsons.SelfEmployment.period(turnover = 200.25,
          otherIncome = 100.25,
          costOfGoodsBought = (200.25, 50.25),
          cisPaymentsToSubcontractors = (100.25, 55.25))

        override def desResponse: JsValue = Json.parse(DesJsons.Errors.serviceUnavailable)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.PUT, desUrl, SERVICE_UNAVAILABLE, desResponse)
        }

        private val response = await(request.put(period))
        response.status shouldBe INTERNAL_SERVER_ERROR
      }

      "DES return a status code that we don't handle" in new Test {

        val period: JsValue = Jsons.SelfEmployment.period(turnover = 200.25,
          otherIncome = 100.25,
          costOfGoodsBought = (200.25, 50.25),
          cisPaymentsToSubcontractors = (100.25, 55.25))

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.PUT, desUrl, IM_A_TEAPOT, Json.obj())
        }

        private val response = await(request.put(period))
        response.status shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}

