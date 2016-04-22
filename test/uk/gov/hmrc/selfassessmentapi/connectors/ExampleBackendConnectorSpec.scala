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

import scala.concurrent.Future

import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.selfassessmentapi.domain.Example

class ExampleBackendConnectorSpec extends UnitSpec with MockitoSugar with ScalaFutures {

  trait Setup {
    implicit val hc = HeaderCarrier()

    val connector = new ExampleBackendConnector {
      override val http = mock[HttpGet]
      override val desUrl = "https://DES_HOST"
    }
  }

  "fetch Example with utr '2234567890K'" should {
    "return an Example Response" in new Setup {

      val expectedResponse = Example("example",1.0)

      when(connector.http.GET[Example](Matchers.eq("https://DES_HOST/des-example-service/sa/2234567890K/example"))(any(), any())).
        thenReturn(Future.successful(expectedResponse))
      connector.fetchExample(SaUtr("2234567890K")).futureValue shouldBe expectedResponse
      verify(connector.http).GET[Example](Matchers.eq("https://DES_HOST/des-example-service/sa/2234567890K/example"))(any(), any())
    }
  }
}
