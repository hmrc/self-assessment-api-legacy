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

import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.auth.microservice.connectors.ConfidenceLevel
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpResponse}
import uk.gov.hmrc.selfassessmentapi.UnitSpec

import scala.concurrent.Future

class AuthConnectorSpec extends UnitSpec with MockitoSugar with ScalaFutures {

  trait Setup {
    implicit val hc = HeaderCarrier()
    val authException = new RuntimeException

    val connector = new AuthConnector {
      override val http = mock[HttpGet]
      override val serviceUrl: String = "https://SERVICE_LOCATOR"
      override val handlerError: Throwable => Unit = mock[Function1[Throwable, Unit]]
    }
  }

  "saUtr" should {

    def authorityJson(confidenceLevel: ConfidenceLevel, utr: String): JsValue = {
      val json =
        s"""
           |{
           |    "accounts": {
           |        "sa": {
           |            "link": "/sa/individual/$utr",
           |            "utr": "$utr"
           |        }
           |    },
           |    "confidenceLevel": ${confidenceLevel.level}
           |}
      """.stripMargin

      Json.parse(json)
    }

    "return the SA UTR if confidence level is greater than the provided confidence level" in new Setup {
      val confidenceLevel = ConfidenceLevel.L100
      val utr = generateSaUtr()
      val response = HttpResponse(200, Some(authorityJson(confidenceLevel, utr.utr)))

      when(connector.http.GET(s"${connector.serviceUrl}/auth/authority")).thenReturn(Future.successful(response))

      connector.saUtr(ConfidenceLevel.L50).futureValue shouldBe Some(utr)
      verify(connector.handlerError, never).apply(authException)
    }

    "return the SA UTR if confidence level equals the provided confidence level" in new Setup {
      val confidenceLevel = ConfidenceLevel.L50
      val utr = generateSaUtr()
      val response = HttpResponse(200, Some(authorityJson(confidenceLevel, utr.utr)))

      when(connector.http.GET(s"${connector.serviceUrl}/auth/authority")).thenReturn(Future.successful(response))

      connector.saUtr(confidenceLevel).futureValue shouldBe Some(utr)
      verify(connector.handlerError, never).apply(authException)
    }

    "return None if confidence level is less than the provided confidence level" in new Setup {
      val confidenceLevel = ConfidenceLevel.L50
      val utr = generateSaUtr()
      val response = HttpResponse(200, Some(authorityJson(confidenceLevel, utr.utr)))

      when(connector.http.GET(s"${connector.serviceUrl}/auth/authority")).thenReturn(Future.successful(response))

      connector.saUtr(ConfidenceLevel.L200).futureValue shouldBe None
      verify(connector.handlerError, never).apply(authException)
    }


    "return None if there is no SA UTR in the accounts" in new Setup {

      val confidenceLevel = ConfidenceLevel.L50
      val json =
        s"""
           |{
           |    "accounts": {
           |    },
           |    "confidenceLevel": ${confidenceLevel.level}
           |}
      """.stripMargin

      val response = HttpResponse(200, Some(Json.parse(json)))

      when(connector.http.GET(s"${connector.serviceUrl}/auth/authority")).thenReturn(Future.successful(response))

      connector.saUtr(confidenceLevel).futureValue shouldBe None
      verify(connector.handlerError, never).apply(authException)
    }


    "return None if an error occurs in the authority request" in new Setup {

      when(connector.http.GET(s"${connector.serviceUrl}/auth/authority")).thenReturn(Future.failed(authException))

      connector.saUtr(ConfidenceLevel.L50).futureValue shouldBe None
      verify(connector.http).GET("https://SERVICE_LOCATOR/auth/authority")
      verify(connector.handlerError).apply(authException)
    }

  }
}
