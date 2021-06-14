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
import uk.gov.hmrc.utils.{Nino, TaxYear}

class UpdatePropertiesAnnualSummaryResourceISpec extends IntegrationBaseSpec {

  private trait Test {

    protected val nino: Nino = NinoGenerator().nextNino()
    val periodKey: String = "A1A2"
    val correlationId: String = "X-ID"
    val id: String = "abc"
    val taxYear: TaxYear = TaxYear("2017-18")

    def uri(propertyType: PropertyType): String = s"/r2/ni/${nino.nino}/uk-properties/$propertyType/$taxYear"

    def desUrl(propertyType: PropertyType): String = s"/income-store/nino/${nino.nino}/uk-properties/$propertyType/annual-summaries/${taxYear.toDesTaxYear}"

    def desResponse: JsValue = Json.parse(DesJsons.Properties.AnnualSummary.response)

    def setupStubs(): StubMapping

    def request(propertyType: PropertyType): WSRequest = {
      setupStubs()
      buildRequest(uri(propertyType))
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "amending annual summaries" should {
    for (propertyType <- Seq(PropertyType.OTHER, PropertyType.FHL)) {
      s"return code 204 when updating an $propertyType period" when {
        "a valid request is made" in new Test {

          val requestJson: JsValue = PropertiesFixture.annualSummary(propertyType)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onSuccess(DesStub.PUT, desUrl(propertyType), OK, desResponse)
          }

          private val response = await(request(propertyType).put(requestJson))
          response.status shouldBe NO_CONTENT
        }

        "DES returns NO_CONTENT" in new Test {

          val requestJson: JsValue = PropertiesFixture.annualSummary(propertyType)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onSuccess(DesStub.PUT, desUrl(propertyType), NO_CONTENT, desResponse)
          }

          private val response = await(request(propertyType).put(requestJson))
          response.status shouldBe NO_CONTENT
        }
      }

      s"return status code 400 for property $propertyType" when {
        s"provided invalid data" in new Test {

          val requestJson: JsValue = PropertiesFixture.invalidAnnualSummary(propertyType)
          val expectedJson: JsValue = Json.parse(
            """
              |{
              |	"code": "INVALID_REQUEST",
              |	"message": "Invalid request",
              |	"errors": [{
              |		"code": "INVALID_MONETARY_AMOUNT",
              |		"message": "Amount should be a non-negative number less than 99999999999999.98 with up to 2 decimal places",
              |		"path": "/allowances/annualInvestmentAllowance"
              |	}, {
              |		"code": "INVALID_MONETARY_AMOUNT",
              |		"message": "Amount should be a non-negative number less than 99999999999999.98 with up to 2 decimal places",
              |		"path": "/adjustments/privateUseAdjustment"
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

        s"updating properties annual summary for a non MTD year" in new Test {

          val requestJson: JsValue = PropertiesFixture.annualSummary(propertyType)

          override def uri(propertyType: PropertyType): String = s"/r2/ni/${nino.nino}/uk-properties/$propertyType/2015-16"

          val expectedJson: JsValue = Json.parse(
            """
              |{"code":"TAX_YEAR_INVALID","message":"Tax year invalid"}
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
      }

      s"return status code 404 for property $propertyType" when {
        "properties business that does not exist" in new Test {

          val requestJson: JsValue = PropertiesFixture.period(propertyType)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onError(DesStub.PUT, desUrl(propertyType), NOT_FOUND, DesJsons.Errors.ninoNotFound)
          }

          private val response = await(request(propertyType).put(requestJson))
          response.status shouldBe NOT_FOUND
        }

        "DES returns a NOT_FOUND_PROPERTY error" in new Test {

          val requestJson: JsValue = PropertiesFixture.period(propertyType, overConsolidatedExpenses = true)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onError(DesStub.PUT, desUrl(propertyType), NOT_FOUND, DesJsons.Errors.notFoundProperty)
          }

          private val response = await(request(propertyType).put(requestJson))
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
            DesStub.onSuccess(DesStub.PUT, desUrl(propertyType), INTERNAL_SERVER_ERROR, desResponse)
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
            DesStub.onSuccess(DesStub.PUT, desUrl(propertyType), SERVICE_UNAVAILABLE, desResponse)
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
            DesStub.onSuccess(DesStub.PUT, desUrl(propertyType), IM_A_TEAPOT, Json.obj())
          }

          private val response = await(request(propertyType).put(period))
          response.status shouldBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }
}

