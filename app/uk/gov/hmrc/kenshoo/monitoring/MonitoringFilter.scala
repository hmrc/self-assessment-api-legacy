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

import play.api.Logger
import play.api.mvc.{Filter, RequestHeader, Result}

import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

trait MonitoringFilter extends Filter with HttpAPIMonitor {

  val urlPatternToNameMapping: Map[String, String]

  def apiName(uri: String, method: String) = {
    urlPatternToNameMapping.find { keyValue => uri.matches(keyValue._1) } match {
      case Some(keyValue) => Some(s"API-${keyValue._2}-$method")
      case None => None
    }
  }

  override def apply(nextFilter: (RequestHeader) => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    implicit val hc = HeaderCarrierConverter.fromHeadersAndSession(requestHeader.headers)

    apiName(requestHeader.uri, requestHeader.method) match {
      case None =>
        Logger.debug(s"API-Not-Monitored: ${requestHeader.method}-${requestHeader.uri}")
        nextFilter(requestHeader)
      case Some(name) =>
        monitor(name) {
          nextFilter(requestHeader)
        }
    }
  }
}
