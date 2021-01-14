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

import play.api.Logger
import play.api.mvc.Result
import uk.gov.hmrc.http._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait HttpErrorRateMeter extends KenshooMetric {
  def meterName[T](serviceName: String, statusCode: Int): String = {
    if (statusCode >= 500) s"Http5xxErrorCount-$serviceName" else s"Http4xxErrorCount-$serviceName"
  }

  def countErrors[T](serviceName: String)(future: Future[T])(implicit ec: ExecutionContext): Future[T] = {
    future.andThen {
      case Success(result: Result) if result.header.status >= 400 => record(meterName(serviceName, result.header.status))
      case Success(response: HttpResponse) if response.status >= 400 => record(meterName(serviceName, response.status))
      case Failure(e: UpstreamErrorResponse) if e.statusCode >= 500 && e.statusCode < 600 => record(meterName(serviceName, e.statusCode))
      case Failure(e: UpstreamErrorResponse) if e.statusCode >= 400 && e.statusCode < 500 => record(meterName(serviceName, e.statusCode))
      case Failure(e: HttpException) => record(meterName(serviceName, e.responseCode))
      case Failure(_: Throwable) => record(meterName(serviceName, 500))
    }
  }

  private def record[T](name: String): Unit = {
    kenshooRegistry.getMeters.getOrDefault(name, kenshooRegistry.meter(name)).mark()
    Logger.debug(s"kenshoo-event::meter::$name::recorded")
  }
}
