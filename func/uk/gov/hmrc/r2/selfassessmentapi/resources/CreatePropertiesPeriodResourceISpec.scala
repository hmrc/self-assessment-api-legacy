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
import uk.gov.hmrc.r2.selfassessmentapi.models.properties.PropertyType
import uk.gov.hmrc.r2.selfassessmentapi.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}
import uk.gov.hmrc.support.IntegrationBaseSpec
import uk.gov.hmrc.utils.Nino

class CreatePropertiesPeriodResourceISpec extends IntegrationBaseSpec {

  private trait Test {

    protected val nino: Nino = NinoGenerator().nextNino()
    val periodKey: String = "A1A2"
    val correlationId: String = "X-ID"
    val id: String = "abc"

    def uri(propertyType: PropertyType): String = s"/r2/ni/${nino.nino}/uk-properties/$propertyType/periods"

    def desUrl(propertyType: PropertyType): String = s"/income-store/nino/${nino.nino}/uk-properties/$propertyType/periodic-summaries"

    def desResponse: JsValue = Json.parse(DesJsons.Properties.Period.createResponse())

    def setupStubs(): StubMapping

    def request(propertyType: PropertyType): WSRequest = {
      setupStubs()
      buildRequest(uri(propertyType))
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "createPeriod" should {
    for (propertyType <- Seq(PropertyType.OTHER, PropertyType.FHL)) {
      s"return status code 201 containing a location header" when {
        "a valid request is made" in new Test {

          val requestJson: JsValue = PropertiesFixture.period(propertyType)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onSuccess(DesStub.POST, desUrl(propertyType), OK, desResponse)
          }

          private val response = await(request(propertyType).post(requestJson))
          response.status shouldBe CREATED
          response.header("Location") shouldBe Some(s"/self-assessment/ni/${nino.nino}/self-employments/abc/periods/2017-04-06_2017-07-04")

        }

        "request payload period has bad debts expenses" in new Test {

          val requestJson: JsValue = Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"), badDebt = (-10.10, -10.10))

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onSuccess(DesStub.POST, desUrl(propertyType), OK, desResponse)
          }

          private val response = await(request(propertyType).post(requestJson))
          response.status shouldBe CREATED
          response.header("Location") shouldBe Some(s"/self-assessment/ni/${nino.nino}/self-employments/abc/periods/2017-04-06_2017-07-04")

        }

        "request payload period contains consolidated expenses" in new Test {

          val requestJson: JsValue = Jsons.SelfEmployment.periodWithSimplifiedExpenses(fromDate = Some("2017-04-06"),
            toDate = Some("2017-07-04"), consolidatedExpenses = Some(1234))

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onSuccess(DesStub.POST, desUrl(propertyType), OK, desResponse)
          }

          private val response = await(request(propertyType).post(requestJson))
          response.status shouldBe CREATED
          response.header("Location") shouldBe Some(s"/self-assessment/ni/${nino.nino}/self-employments/abc/periods/2017-04-06_2017-07-04")

        }
      }

      s"return status code 400" when {
        "the 'from' and 'to' dates are in the incorrect order" in new Test {

          val requestJson: JsValue = Jsons.SelfEmployment.period(fromDate = Some("2017-04-01"), toDate = Some("2017-03-31"))
          val expectedJson: JsValue = Json.parse(
            """
              |{
              |  "code":"INVALID_REQUEST",
              |  "message":"Invalid request",
              |  "errors":[
              |     {
              |        "code":"INVALID_PERIOD",
              |        "message":"The period 'to' date is before the period 'from' date or the submission period already exists.","path":""
              |      }
              |   ]
              |}
              |""".stripMargin)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          private val response = await(request(propertyType).post(requestJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe expectedJson
        }

        "the request payload contains both the 'expenses' and 'consolidatedExpenses" in new Test {

          val requestJson: JsValue = Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"), consolidatedExpenses = Some(12345))
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

          private val response = await(request(propertyType).post(requestJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe expectedJson
        }

        "the request payload has no incomes and expenses" in new Test {

          val requestJson: JsValue = Json.parse(
            s"""
               |{
               |  "from": "2017-03-31",
               |  "to": "2017-04-01"
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

          private val response = await(request(propertyType).post(requestJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe expectedJson
        }
      }

      s"return status code 400 with multiple validation errors" when {
        "the request payload has no incomes and expenses and with invalid from and to dates" in new Test {

          val requestJson: JsValue = Json.parse(
            s"""
               |{
               |  "to": "2017-03-31",
               |  "from": "2017-04-01"
               |}""".stripMargin)

          val expectedJson: JsValue = Json.parse(
            """
              |{
              |	"code": "INVALID_REQUEST",
              |	"message": "Invalid request",
              |	"errors": [{
              |		"code": "INVALID_PERIOD",
              |		"message": "The period 'to' date is before the period 'from' date or the submission period already exists.",
              |		"path": ""
              |	}, {
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

          private val response = await(request(propertyType).post(requestJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe expectedJson
        }
      }

      s"return status code 403" when {
        "the request payload period is not contiguous with existing periods" in new Test {

          val nonContiguousPeriod: JsValue = Jsons.SelfEmployment.period(fromDate = Some("2017-08-04"), toDate = Some("2017-09-04"))

          val expectedJson: JsValue = Json.parse(
            """
              |{
              |	"code": "BUSINESS_ERROR",
              |	"message": "Business validation error",
              |	"errors": [{
              |		"code": "NOT_CONTIGUOUS_PERIOD",
              |		"message": "Periods should be contiguous.",
              |		"path": ""
              |	}]
              |}
              |""".stripMargin)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onSuccess(DesStub.POST, desUrl(propertyType), CONFLICT, Json.parse(DesJsons.Errors.nonContiguousPeriod))
          }

          private val response = await(request(propertyType).post(nonContiguousPeriod))
          response.status shouldBe FORBIDDEN
          response.json shouldBe expectedJson
        }

        "the request payload period date range overlaps with another period" in new Test {

          val overlappingPeriod: JsValue = Jsons.SelfEmployment.period(fromDate = Some("2017-08-04"), toDate = Some("2017-09-04"))

          val expectedJson: JsValue = Json.parse(
            """
              |{
              |	"code": "BUSINESS_ERROR",
              |	"message": "Business validation error",
              |	"errors": [{
              |		"code": "OVERLAPPING_PERIOD",
              |		"message": "Period overlaps with existing periods.",
              |		"path": ""
              |	}]
              |}
              |""".stripMargin)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onSuccess(DesStub.POST, desUrl(propertyType), CONFLICT, Json.parse(DesJsons.Errors.overlappingPeriod))
          }

          private val response = await(request(propertyType).post(overlappingPeriod))
          response.status shouldBe FORBIDDEN
          response.json shouldBe expectedJson
        }

        "the request payload period is misaligned with the accounting period" in new Test {

          val misalignedPeriod: JsValue = Jsons.SelfEmployment.period(fromDate = Some("2017-08-04"), toDate = Some("2017-09-04"))

          val expectedJson: JsValue = Json.parse(
            """
              |{
              |	"code": "BUSINESS_ERROR",
              |	"message": "Business validation error",
              |	"errors": [{
              |		"code": "MISALIGNED_PERIOD",
              |		"message": "Period is not within the accounting period.",
              |		"path": ""
              |	}]
              |}
              |""".stripMargin)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onSuccess(DesStub.POST, desUrl(propertyType), CONFLICT, Json.parse(DesJsons.Errors.misalignedPeriod))
          }

          private val response = await(request(propertyType).post(misalignedPeriod))
          response.status shouldBe FORBIDDEN
          response.json shouldBe expectedJson
        }
      }

      s"return status code 404" when {
        "attempting to create a period for a self-employment that does not exist" in new Test {

          val period: JsValue = Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"))

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onSuccess(DesStub.POST, desUrl(propertyType), NOT_FOUND, Json.parse(DesJsons.Errors.notFound))
          }

          private val response = await(request(propertyType).post(period))
          response.status shouldBe NOT_FOUND
        }
      }

      s"return status code 500" when {
        "DES is experiencing issues" in new Test {

          val period: JsValue = Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"))

          override def desResponse: JsValue = Json.parse(DesJsons.Errors.serverError)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onSuccess(DesStub.POST, desUrl(propertyType), INTERNAL_SERVER_ERROR, desResponse)
          }

          private val response = await(request(propertyType).post(period))
          response.status shouldBe INTERNAL_SERVER_ERROR
        }

        "DES is unavailable" in new Test {

          val period: JsValue = Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"))

          override def desResponse: JsValue = Json.parse(DesJsons.Errors.serviceUnavailable)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onSuccess(DesStub.POST, desUrl(propertyType), SERVICE_UNAVAILABLE, desResponse)
          }

          private val response = await(request(propertyType).post(period))
          response.status shouldBe INTERNAL_SERVER_ERROR
        }

        "DES return a status code that we don't handle" in new Test {

          val period: JsValue = Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"))

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onSuccess(DesStub.POST, desUrl(propertyType), IM_A_TEAPOT, Json.obj())
          }

          private val response = await(request(propertyType).post(period))
          response.status shouldBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }
}

