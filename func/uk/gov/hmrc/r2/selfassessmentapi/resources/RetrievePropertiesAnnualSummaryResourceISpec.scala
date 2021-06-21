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

class RetrievePropertiesAnnualSummaryResourceISpec extends IntegrationBaseSpec {

  private trait Test {

    protected val nino: Nino = NinoGenerator().nextNino()
    val periodKey: String = "A1A2"
    val correlationId: String = "X-ID"
    val taxYear: TaxYear = TaxYear("2017-18")

    def uri(propertyType: PropertyType): String = s"/r2/ni/${nino.nino}/uk-properties/$propertyType/$taxYear"

    def desUrl(propertyType: PropertyType): String = s"/income-store/nino/${nino.nino}/uk-properties/$propertyType/annual-summaries/${taxYear.toDesTaxYear}"

    def setupStubs(): StubMapping

    def request(propertyType: PropertyType): WSRequest = {
      setupStubs()
      buildRequest(uri(propertyType))
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "retrieving annual summaries" should {
    for (propertyType <- Seq(PropertyType.OTHER, PropertyType.FHL)) {
      s"return a 200 status code with expected body for property type $propertyType" when {
        "a valid request is made" in new Test {

          val expectedJson: JsValue = PropertiesFixture.annualSummary(propertyType)
          val desResponse: JsValue = Json.parse(PropertiesFixture.desAnnualSummary(propertyType))

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onSuccess(DesStub.GET, desUrl(propertyType), OK, desResponse)
          }

          private val response = await(request(propertyType).get)
          response.status shouldBe OK
          response.json shouldBe expectedJson
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      s"return a 400 status code for property type $propertyType" when {
        "non MTD year is received" in new Test {

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

          private val response = await(request(propertyType).get)
          response.status shouldBe BAD_REQUEST
          response.json shouldBe expectedJson
        }
      }

      s"return a 404 status code for property type $propertyType" when {
        "no data found" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onError(DesStub.GET, desUrl(propertyType), NOT_FOUND, DesJsons.Errors.notFoundProperty)
          }

          private val response = await(request(propertyType).get)
          response.status shouldBe NOT_FOUND
        }

        "a valid request is made but property business does not exist" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onError(DesStub.GET, desUrl(propertyType), NOT_FOUND, DesJsons.Errors.ninoNotFound)
          }

          private val response = await(request(propertyType).get)
          response.status shouldBe NOT_FOUND
        }

        "a valid request is made but property period does not exist" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onError(DesStub.GET, desUrl(propertyType), NOT_FOUND, DesJsons.Errors.notFoundPeriod)
          }

          private val response = await(request(propertyType).get)
          response.status shouldBe NOT_FOUND
        }
      }

      s"return a 500 status code for property type $propertyType" when {
        "a valid request is made but received a status code from DES that we do not handle" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onSuccess(DesStub.GET, desUrl(propertyType), IM_A_TEAPOT, Json.obj())
          }

          private val response = await(request(propertyType).get)
          response.status shouldBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }
}
