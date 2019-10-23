/*
 * Copyright 2019 HM Revenue & Customs
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

import akka.stream.Materializer
import com.codahale.metrics.MetricRegistry
import org.scalatest.Matchers
import play.api.http.HttpEntity
import play.api.mvc.request.RequestTarget
import play.api.mvc.{ResponseHeader, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class MonitoringFilterSpec extends UnitSpec {

  implicit val hc = HeaderCarrier()

  "monitoring filter" should {
    "monitor known incoming requests" in new MonitoringFilterTestImp {
      await(apply(_ => Future(Result(ResponseHeader(200), HttpEntity.NoEntity)))(
        FakeRequest().withTarget(RequestTarget("/ni/OM687829D/self-employments", "", Map.empty)).withMethod("GET")))
      assertRequestIsMonitoredAs("API-SelfEmployments-GET")
    }

    "do not monitor unknown incoming requests" in new MonitoringFilterTestImp {
      await(apply(_ => Future(Result(ResponseHeader(200), HttpEntity.NoEntity)))(
        FakeRequest().withTarget(RequestTarget("/foo-bar", "", Map.empty)).withMethod("GET")))
      assertRequestIsNotMonitored()
    }
  }
}

class MonitoringFilterTestImp extends MonitoringFilter with Matchers {
  override val urlPatternToNameMapping: Map[String, String] = Map("/ni/OM687829D/self-employments" -> "SelfEmployments")

  var serviceName : String = ""

  override def monitor[T](serviceName: String)(function: => Future[T])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[T] = {
    this.serviceName = serviceName
    function
  }

  def assertRequestIsMonitoredAs(expectedServiceName: String): Unit = {
    serviceName shouldBe expectedServiceName
  }

  def assertRequestIsNotMonitored(): Unit = {
    serviceName shouldBe ""
  }

  override def kenshooRegistry = new MetricRegistry

  override implicit def mat: Materializer = ???
}
