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

package uk.gov.hmrc.r2.selfassessmentapi.connectors

import play.api.libs.json.Writes
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.r2.selfassessmentapi.config.AppContext
import uk.gov.hmrc.r2.selfassessmentapi.resources.wrappers.Response
import uk.gov.hmrc.utils.Logging

import scala.concurrent.{ExecutionContext, Future}

trait BaseConnector extends Logging {
  val http: DefaultHttpClient
  val appContext: AppContext

  def withDesHeaders(hc: HeaderCarrier, correlationId: String, additionalHeaders: Seq[String]): HeaderCarrier =
    HeaderCarrier(
      extraHeaders = hc.extraHeaders ++
        // Contract headers
        Seq(
          "Authorization" -> s"Bearer ${appContext.desToken}",
          "Environment" -> appContext.desEnv,
          "Originator-Id" -> "DA_SDI",
          "CorrelationId" -> correlationId
        ) ++
        // Other headers (i.e Gov-Test-Scenario, Content-Type)
        hc.headers(additionalHeaders ++ appContext.desEnvironmentHeaders.getOrElse(Seq.empty))
    )

  private def withAdditionalHeaders[R <: Response](url: String,
                                                   correlationId: String,
                                                   additionalHeaders: Seq[String] = Seq.empty)(f: HeaderCarrier => Future[R])(
    implicit hc: HeaderCarrier): Future[R] = {
    val newHc = withDesHeaders(hc, correlationId, additionalHeaders)
    f(newHc)
  }

  // http-verbs converts non-2xx statuses into exceptions. We don't want this, so here we define
  // our own reader that returns the raw response.
  private object NoExceptReads extends HttpReads[HttpResponse] {
    override def read(method: String, url: String, response: HttpResponse): HttpResponse = response
  }

  def httpGet[R <: Response](url: String, toResponse: HttpResponse => R)
                            (implicit hc: HeaderCarrier, ec: ExecutionContext, correlationId: String): Future[R] =
    withAdditionalHeaders[R](url, correlationId) { hcWithAdditionalHeaders =>
      http.GET(url)(NoExceptReads, hcWithAdditionalHeaders, ec) map toResponse
    }

  def httpGetWithNoId[R <: Response](url: String, toResponse: HttpResponse => R)
                            (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[R] =
    withAdditionalHeaders[R](url, correlationId = "") { hcWithAdditionalHeaders =>
      http.GET(url)(NoExceptReads, hcWithAdditionalHeaders, ec) map toResponse
    }

  def httpPost[T: Writes, R <: Response](url: String, elem: T, toResponse: HttpResponse => R)(
    implicit hc: HeaderCarrier, ec: ExecutionContext, correlationId: String): Future[R] =
    withAdditionalHeaders[R](url, correlationId, Seq("Content-Type")) { hcWithAdditionalHeaders =>
      http.POST(url, elem)(implicitly[Writes[T]], NoExceptReads, hcWithAdditionalHeaders, ec) map toResponse
    }

  def httpEmptyPost[R <: Response](url: String, toResponse: HttpResponse => R)
                                  (implicit hc: HeaderCarrier, ec: ExecutionContext, correlationId: String): Future[R] =
    withAdditionalHeaders[R](url, correlationId, Seq("Content-Type")) { hcWithAdditionalHeaders =>
      http.POSTEmpty(url)(NoExceptReads, hcWithAdditionalHeaders, ec) map toResponse
    }

  def httpPut[T: Writes, R <: Response](url: String, elem: T, toResponse: HttpResponse => R)(
    implicit hc: HeaderCarrier, ec: ExecutionContext, correlationId: String): Future[R] =
    withAdditionalHeaders[R](url, correlationId, Seq("Content-Type")) { hcWithAdditionalHeaders =>
      http.PUT(url, elem)(implicitly[Writes[T]], NoExceptReads, hcWithAdditionalHeaders, ec) map toResponse
    }
}