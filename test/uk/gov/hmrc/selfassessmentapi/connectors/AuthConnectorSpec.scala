/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import org.mockito.Mockito
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.play.auth.microservice.connectors.ConfidenceLevel
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, Upstream5xxResponse}
import uk.gov.hmrc.selfassessmentapi.config.WSHttp
import uk.gov.hmrc.selfassessmentapi.{LoggingService, TestApplication, WiremockDSL}

class AuthConnectorSpec extends TestApplication with WiremockDSL {

  "saUtr" should {
    val utr = generateSaUtr()

    "return the SA UTR if confidence level is greater than the provided confidence level" in new TestAuthConnector(wiremockBaseUrl) {
      given().get(urlPathEqualTo("/auth/authority")).returns(authorityJson(ConfidenceLevel.L100, utr))

      await(saUtr(ConfidenceLevel.L50)) shouldBe Some(utr)
      
    }

    "return the SA UTR if confidence level equals the provided confidence level" in new TestAuthConnector(wiremockBaseUrl) {
      given().get(urlPathEqualTo("/auth/authority")).returns(authorityJson(ConfidenceLevel.L50, utr))

      await(saUtr(ConfidenceLevel.L50)) shouldBe Some(utr)

    }

    "return None if confidence level is less than the provided confidence level" in new TestAuthConnector(wiremockBaseUrl) {
      given().get(urlPathEqualTo("/auth/authority")).returns(authorityJson(ConfidenceLevel.L50, utr))

      await(saUtr(ConfidenceLevel.L200)) shouldBe None
    }


    "return None if there is no SA UTR in the accounts" in new TestAuthConnector(wiremockBaseUrl) {
      given().get(urlPathEqualTo("/auth/authority")).returns(authorityJson(ConfidenceLevel.L50))

      await(saUtr(ConfidenceLevel.L50)) shouldBe None
    }

    "return None if an error occurs in the authority request" in new TestAuthConnector(wiremockBaseUrl) {
      given().get(urlPathEqualTo("/auth/authority")).returns(500)

      await(saUtr(ConfidenceLevel.L50)) shouldBe None

      Mockito.verify(loggingService).error("Error in request to auth",
        new Upstream5xxResponse("GET of 'http://localhost:22222/auth/authority' returned 500. Response body: ''", 500, 502))
    }

  }
}

class TestAuthConnector(wiremockBaseUrl: String) extends AuthConnector with MockitoSugar {
  implicit val hc = HeaderCarrier()
  
  override val serviceUrl: String = wiremockBaseUrl
  override val http: HttpGet = WSHttp

  def authorityJson(confidenceLevel: ConfidenceLevel, utr: SaUtr) = {
    val json =
      s"""
         |{
         |    "accounts": {
         |        "sa": {
         |            "link": "/sa/individual/${utr.value}",
         |            "utr": "${utr.value}"
         |        }
         |    },
         |    "confidenceLevel": ${confidenceLevel.level}
         |}
      """.stripMargin

    json
  }

  def authorityJson(confidenceLevel: ConfidenceLevel) = {
    val json =
      s"""
         |{
         |    "accounts": {
         |    },
         |    "confidenceLevel": ${confidenceLevel.level}
         |}
      """.stripMargin

    json
  }

  override val loggingService: LoggingService = mock[LoggingService]
}