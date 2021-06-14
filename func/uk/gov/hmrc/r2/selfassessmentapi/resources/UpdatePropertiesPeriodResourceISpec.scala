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

class UpdatePropertiesPeriodResourceISpec extends IntegrationBaseSpec {

  private trait Test {

    protected val nino: Nino = NinoGenerator().nextNino()
    val periodKey: String = "A1A2"
    val correlationId: String = "X-ID"
    val id: String = "abc"

    def uri(propertyType: PropertyType): String = s"/r2/ni/${nino.nino}/uk-properties/$propertyType/periods/2017-04-06_2018-04-05"

    def desUrl(propertyType: PropertyType): String = s"/income-store/nino/${nino.nino}/uk-properties/$propertyType/periodic-summaries"

    def desResponse: JsValue = Json.obj()

    val queryParams: Map[String, String] = Map("from" -> "2017-04-06", "to" -> "2018-04-05")

    def setupStubs(): StubMapping

    def request(propertyType: PropertyType): WSRequest = {
      setupStubs()
      buildRequest(uri(propertyType))
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "updatePeriod" should {
    for (propertyType <- Seq(PropertyType.OTHER, PropertyType.FHL)) {
      s"return code 204 when updating an $propertyType period" when {
        "a valid request is made" in new Test {

          val requestJson: JsValue = PropertiesFixture.period(propertyType)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onSuccess(DesStub.PUT, desUrl(propertyType), queryParams, OK, desResponse)
          }

          private val response = await(request(propertyType).put(requestJson))
          response.status shouldBe NO_CONTENT
        }

        "request payload period has no expenses" in new Test {

          val requestJson: JsValue = PropertiesFixture.period(propertyType, noExpenses = true)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onSuccess(DesStub.PUT, desUrl(propertyType), queryParams, OK, desResponse)
          }

          private val response = await(request(propertyType).put(requestJson))
          response.status shouldBe NO_CONTENT
        }

        "request payload period contains only 'consolidatedExpenses'" in new Test {

          val requestJson: JsValue = PropertiesFixture.period(propertyType, onlyConsolidated = true)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onSuccess(DesStub.PUT, desUrl(propertyType), queryParams, OK, desResponse)
          }

          private val response = await(request(propertyType).put(requestJson))
          response.status shouldBe NO_CONTENT
        }

        "request payload period contains only 'residentialFinancialCost'" in new Test {

          val requestJson: JsValue = PropertiesFixture.period(propertyType, onlyResidential = true)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onSuccess(DesStub.PUT, desUrl(propertyType), queryParams, OK, desResponse)
          }

          private val response = await(request(propertyType).put(requestJson))
          response.status shouldBe NO_CONTENT
        }
      }

      s"return status code 400 for property $propertyType" when {
        s"provided with an invalid period" in new Test {

          val requestJson: JsValue = PropertiesFixture.invalidPeriod(propertyType)
          val expectedJson: JsValue = PropertiesFixture.expectedJsonforUpdatePeriod(propertyType)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          private val response = await(request(propertyType).put(requestJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe expectedJson
        }

        s"the request payload contains invalid period and no incomes and expenses" in new Test {

          val requestJson: JsValue = Json.parse(s"""
                                                   |{
                                                   |
                                                   |}""".stripMargin)

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

          private val response = await(request(propertyType).put(requestJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe expectedJson
        }

        s"the request payload contains both 'expenses' and 'consolidatedExpenses'" in new Test {

          val requestJson: JsValue = PropertiesFixture.bothExpenses(propertyType)
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

          private val response = await(request(propertyType).put(requestJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe expectedJson
        }

        s"the request payload contains an invalid 'costOfServices' field" in new Test {

          val requestJson: JsValue = PropertiesFixture.period(propertyType, costOfServices = Some(1234.567))
          val expectedJson: JsValue = Json.parse(
            """
              |{
              |	"code": "INVALID_REQUEST",
              |	"message": "Invalid request",
              |	"errors": [{
              |		"code": "INVALID_MONETARY_AMOUNT",
              |		"message": "Amount should be a non-negative number less than 99999999999999.98 with up to 2 decimal places",
              |		"path": "/expenses/costOfServices/amount"
              |	}]
              |}
              |""".stripMargin)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          private val response = await(request(propertyType).put(requestJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe expectedJson
        }

        s"DES returns a 409 BOTH_EXPENSES_SUPPLIED" in new Test {

          val requestJson: JsValue = PropertiesFixture.period(propertyType)
          val expectedJson: JsValue = Json.parse(
            """
              |{"code":"INVALID_REQUEST","message":"Invalid request","errors":[{"code":"BOTH_EXPENSES_SUPPLIED","message":"Elements: expenses and consolidatedElements cannot be both specified at the same time"}]}
              |""".stripMargin)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onError(DesStub.PUT, desUrl(propertyType), queryParams, CONFLICT, DesJsons.Errors.bothExpensesSupplied)
          }

          private val response = await(request(propertyType).put(requestJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe expectedJson
        }

        "DES return 409 INVALID_PERIOD error" in new Test {

          val period: JsValue = PropertiesFixture.period(propertyType)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onError(DesStub.PUT, desUrl(propertyType), queryParams, CONFLICT, DesJsons.Errors.invalidCreatePeriod)
          }

          private val response = await(request(propertyType).put(period))
          response.status shouldBe BAD_REQUEST
        }
      }

      s"return status code 403 for property $propertyType" when {
        "creating an overlapping period" in new Test {

          val requestJson: JsValue = PropertiesFixture.period(propertyType)

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
            DesStub.onError(DesStub.PUT, desUrl(propertyType), queryParams, CONFLICT, DesJsons.Errors.overlappingPeriod)
          }

          private val response = await(request(propertyType).put(requestJson))
          response.status shouldBe FORBIDDEN
          response.json shouldBe expectedJson
        }

        "the request payload period contains over 'consolidatedExpenses'" in new Test {

          val requestJson: JsValue = PropertiesFixture.period(propertyType, overConsolidatedExpenses = true)

          val expectedJson: JsValue = Json.parse(
            """
              |{
              |	"code": "BUSINESS_ERROR",
              |	"message": "Business validation error",
              |	"errors": [{
              |		"code": "NOT_ALLOWED_CONSOLIDATED_EXPENSES",
              |		"message": "The submission contains consolidated expenses but the accumulative turnover amount exceeds the threshold",
              |		"path": ""
              |	}]
              |}
              |""".stripMargin)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onError(DesStub.PUT, desUrl(propertyType), queryParams, CONFLICT, DesJsons.Errors.notAllowedConsolidatedExpenses)
          }

          private val response = await(request(propertyType).put(requestJson))
          response.status shouldBe FORBIDDEN
          response.json shouldBe expectedJson
        }

        "the request payload period is misaligned with the accounting period" in new Test {

          val misalignedPeriod: JsValue = PropertiesFixture.misalignedPeriod(propertyType)

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
            DesStub.onSuccess(DesStub.PUT, desUrl(propertyType), queryParams, CONFLICT, Json.parse(DesJsons.Errors.misalignedPeriod))
          }

          private val response = await(request(propertyType).put(misalignedPeriod))
          response.status shouldBe FORBIDDEN
          response.json shouldBe expectedJson
        }
      }

      s"return status code 404 for property $propertyType" when {
        "attempting to create a period that does not exist" in new Test {

          val period: JsValue = PropertiesFixture.period(propertyType)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onError(DesStub.PUT, desUrl(propertyType), queryParams, NOT_FOUND, DesJsons.Errors.notFound)
          }

          private val response = await(request(propertyType).put(period))
          response.status shouldBe NOT_FOUND
        }

        "property that does not exist and DES returns NOT_FOUND_INCOME_SOURCE error" in new Test {

          val period: JsValue = PropertiesFixture.period(propertyType)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onError(DesStub.PUT, desUrl(propertyType), queryParams, FORBIDDEN, DesJsons.Errors.notFoundIncomeSource)
          }

          private val response = await(request(propertyType).put(period))
          response.status shouldBe NOT_FOUND
        }
      }

      s"return status code 500 for $propertyType" when {
        "DES is experiencing issues" in new Test {

          val period: JsValue = Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"))

          override def desResponse: JsValue = Json.parse(DesJsons.Errors.serverError)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onSuccess(DesStub.PUT, desUrl(propertyType), queryParams, INTERNAL_SERVER_ERROR, desResponse)
          }

          private val response = await(request(propertyType).put(period))
          response.status shouldBe INTERNAL_SERVER_ERROR
        }

        "DES is unavailable" in new Test {

          val period: JsValue = Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"))

          override def desResponse: JsValue = Json.parse(DesJsons.Errors.serviceUnavailable)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onSuccess(DesStub.PUT, desUrl(propertyType), queryParams, SERVICE_UNAVAILABLE, desResponse)
          }

          private val response = await(request(propertyType).put(period))
          response.status shouldBe INTERNAL_SERVER_ERROR
        }

        "DES return a status code that we don't handle" in new Test {

          val period: JsValue = Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"))

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onSuccess(DesStub.PUT, desUrl(propertyType), queryParams, IM_A_TEAPOT, Json.obj())
          }

          private val response = await(request(propertyType).put(period))
          response.status shouldBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }
}

