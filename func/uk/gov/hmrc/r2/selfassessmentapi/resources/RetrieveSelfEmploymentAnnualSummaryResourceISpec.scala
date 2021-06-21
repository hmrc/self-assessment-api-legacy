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

class RetrieveSelfEmploymentAnnualSummaryResourceISpec extends IntegrationBaseSpec {

  private trait Test {

    protected val nino: Nino = NinoGenerator().nextNino()
    val id: String = "abc"
    val correlationId: String = "X-ID"
    val taxYear: TaxYear = TaxYear("2017-18")

    def uri(): String = s"/r2/ni/${nino.nino}/self-employments/abc/$taxYear"

    def desUrl(): String = s"/income-store/nino/${nino.nino}/self-employments/$id/annual-summaries/${taxYear.toDesTaxYear}"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri())
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "retrieving annual summaries" should {
    s"return a 200 status code" when {
      "a valid request is made" in new Test {

        val expectedJson: JsValue = Jsons.SelfEmployment.annualSummary(
          annualInvestmentAllowance = 200,
          businessPremisesRenovationAllowance = 200,
          capitalAllowanceMainPool = 200,
          capitalAllowanceSpecialRatePool = 200,
          enhancedCapitalAllowance = 200,
          allowanceOnSales = 200,
          zeroEmissionGoodsVehicleAllowance = 200,
          tradingAllowance = 200,
          includedNonTaxableProfits = 200,
          basisAdjustment = 200,
          overlapReliefUsed = 200,
          accountingAdjustment = 200,
          averagingAdjustment = 200,
          lossBroughtForward = 200,
          outstandingBusinessIncome = 200,
          balancingChargeBPRA = 200,
          balancingChargeOther = 200,
          goodsAndServicesOwnUse = 200
        )

        val desResponse: JsValue = Json.parse(DesJsons.SelfEmployment.AnnualSummary())

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl(), OK, desResponse)
        }

        private val response = await(request().get)
        response.status shouldBe OK
        response.json shouldBe expectedJson
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "DES returns an empty object for a non-existent annual summary" in new Test {

        val expectedJson: JsValue = Json.obj()

        val desResponse: JsValue = Json.obj()

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl(), OK, desResponse)
        }

        private val response = await(request().get)
        response.status shouldBe OK
        response.json shouldBe expectedJson
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    s"return a 400 status code" when {
      "non MTD year is received" in new Test {

        override def uri(): String = s"/r2/ni/${nino.nino}/self-employments/abc/2015-16"

        val expectedJson: JsValue = Json.parse(
          """
            |{"code":"TAX_YEAR_INVALID","message":"Tax year invalid"}
            |""".stripMargin)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        private val response = await(request().get)
        response.status shouldBe BAD_REQUEST
        response.json shouldBe expectedJson
      }
    }

    s"return a 404 status code" when {
      "a valid request is made but self-employment business does not exist" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onError(DesStub.GET, desUrl(), NOT_FOUND, DesJsons.Errors.ninoNotFound)
        }

        private val response = await(request().get)
        response.status shouldBe NOT_FOUND
      }
    }

    s"return a 500 status code" when {

      "a valid request is made but received a status code from DES that we do not handle" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl(), IM_A_TEAPOT, Json.obj())
        }

        private val response = await(request().get)
        response.status shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
