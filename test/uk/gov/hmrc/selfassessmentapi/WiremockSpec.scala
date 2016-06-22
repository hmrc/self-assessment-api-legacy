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

package uk.gov.hmrc.selfassessmentapi

import java.util.concurrent.TimeUnit

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.client.{MappingBuilder, UrlMatchingStrategy, WireMock}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers}
import org.scalatestplus.play.OneServerPerSuite

import scala.concurrent.duration.FiniteDuration

trait WiremockSpec extends UnitSpec with Matchers with OneServerPerSuite with Eventually with ScalaFutures
  with BeforeAndAfterEach with IntegrationPatience with MockitoSugar with BeforeAndAfterAll with MongoEmbeddedDatabase {

  override implicit val defaultTimeout = FiniteDuration(100, TimeUnit.SECONDS)

  private val WIREMOCK_PORT = 21212
  private val stubHost = "localhost"

  protected val wiremockBaseUrl: String = s"http://localhost:$WIREMOCK_PORT"
  private val wireMockServer = new WireMockServer(wireMockConfig().port(WIREMOCK_PORT))

  override def beforeAll() = {
    mongoBeforeAll()
    wireMockServer.stop()
    wireMockServer.start()
    WireMock.configureFor(stubHost, WIREMOCK_PORT)
  }

  override def beforeEach() = {
    WireMock.reset()
  }

  def given() = new Givens()

  class Givens() {
    def get(strategy: UrlMatchingStrategy) = new Result(WireMock.get(strategy))

    class Result(mappingBuilder: MappingBuilder) {
      def returns(responseBody: String) = {
        stubFor(mappingBuilder.willReturn(aResponse()
          .withStatus(200)
          .withHeader("Content-Type", "application/json")
          .withBody(responseBody)))
      }

      def returns(statusCode: Int) = {
        stubFor(mappingBuilder.willReturn(aResponse()
          .withStatus(statusCode)))
      }

    }

  }
}
