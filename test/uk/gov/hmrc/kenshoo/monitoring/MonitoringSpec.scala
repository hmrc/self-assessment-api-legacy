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

package uk.gov.hmrc.kenshoo.monitoring

import io.restassured.RestAssured
import io.restassured.response.ValidatableResponse
import org.hamcrest.Matcher
import org.hamcrest.Matchers._
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.utils.Logging

import scala.concurrent.Future

trait MonitoringSpec extends UnitSpec with Logging {

  private class MetricBodyWrapper {
    var fieldName: String = ""

    def is[T](matcher: Matcher[T]): ValidatableResponse = {
      logger.debug(s"Metrics URL: ${RestAssured.baseURI}:${RestAssured.port}")
      logger.debug(RestAssured.get("/admin/metrics").`then`().assertThat().extract().body().asString())

      try {
        metrics.body(fieldName, matcher)
      } catch {
        case _: IllegalArgumentException => throw new RuntimeException(s"Cannot find '$fieldName'")
        case ex: Throwable => throw ex
      }
    }

    def field(s: String): MetricBodyWrapper = {
      this.fieldName = s
      this
    }
  }

  private def metricsBody(): MetricBodyWrapper = {
    new MetricBodyWrapper()
  }

  def kenshooMetricPort: Int

  RestAssured.baseURI = "http://localhost"
  RestAssured.port = kenshooMetricPort

  def assertResponseTimeRecordedFor(apiName: String): ValidatableResponse = {
    metricsBody().field(s"timers.Timer-$apiName.count").is(greaterThanOrEqualTo(1.asInstanceOf[Integer]))
  }

  def assertErrorCountRecordedFor(apiName: String, statusCode: Int, count: Int = 1): ValidatableResponse = {
    metricsBody().field(s"meters.Http${statusCode / 100}xxErrorCount-$apiName.count").is(greaterThanOrEqualTo(count.asInstanceOf[Integer]))
  }

  def verifyMonitoringFor(serviceName: String): Unit = {
    assertResponseTimeRecordedFor(serviceName)
    try {
      assertErrorCountRecordedFor(serviceName, 500)
    } catch {
      case ex: Throwable => assertErrorCountRecordedFor(serviceName, 400)
    }
  }

  def verifyHttpErrorRecorded[T](service: String, statusCode: Int = 500)(fn: => Future[T]): Any = {
    try {
      await(fn)
    } catch {
      case th: Throwable =>
    } finally {
      assertErrorCountRecordedFor(service, statusCode)
    }
  }

  def verifyResponseTime[T](service: String)(fn: => Future[T]): Any = {
    try {
      await(fn)
    } catch {
      case th: Throwable =>
    } finally {
      assertResponseTimeRecordedFor(service)
    }

  }

  def verifyMonitoring[T](service: String)(fn: => Future[T]): Any = {
    try {
      await(fn)
    } catch {
      case th: Throwable =>
    }
    finally {
      verifyMonitoringFor(service)
    }
  }

  private def metrics: ValidatableResponse = {
    RestAssured.get("/admin/metrics").`then`().assertThat()
  }


}
