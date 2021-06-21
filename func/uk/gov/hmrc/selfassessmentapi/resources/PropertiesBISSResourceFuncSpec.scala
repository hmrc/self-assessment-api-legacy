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
import play.api.http.Status.{OK, GONE}
import play.api.libs.json.Json
import play.api.libs.ws.WSRequest
import uk.gov.hmrc.selfassessmentapi.NinoGenerator
import uk.gov.hmrc.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}
import uk.gov.hmrc.support.IntegrationBaseSpec
import uk.gov.hmrc.utils.{Nino, TaxYear}

class PropertiesBISSResourceFuncSpec extends IntegrationBaseSpec {

  private trait Test {

    protected val nino: Nino = NinoGenerator().nextNino()
    val correlationId: String = "X-ID"
    val taxYear: TaxYear = TaxYear("2017-18")

    def uri: String = s"/ni/${nino.nino}/uk-properties/$taxYear/income-summary"

    def desUrl: String = s"/income-tax/income-sources/nino/${nino.nino}/uk-property/${taxYear.toDesTaxYear}/biss"

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "getSummary for Property BISS" should {
    s"return code 410 for any request" when {
      "a request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, OK, Json.parse(DesJsons.PropertiesBISS.summary))
        }

        private val response = await(request.get)
        response.status shouldBe GONE
        response.json shouldBe Json.parse(Jsons.Errors.resourceGone)
      }
    }
  }
}
