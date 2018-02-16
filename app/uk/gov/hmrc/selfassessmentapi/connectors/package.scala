/*
 * Copyright 2018 HM Revenue & Customs
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

import play.api.Logger
import play.api.libs.json.Writes
import uk.gov.hmrc.play.http.logging.Authorization
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.selfassessmentapi.config.{AppContext, WSHttp}
import uk.gov.hmrc.selfassessmentapi.resources.GovTestScenarioHeader
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.Response

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

package object connectors {
  private val logger = Logger("connectors")

  private def withDesHeaders(hc: HeaderCarrier): HeaderCarrier = {
    val newHc = hc
      .copy(authorization = Some(Authorization(s"Bearer ${AppContext.desToken}")))
      .withExtraHeaders(
        "Environment" -> AppContext.desEnv,
        "Accept" -> "application/json",
        "Originator-Id" -> "DA_SDI"
      )

    // HACK: http-verbs removes all "otherHeaders" from HeaderCarrier on outgoing requests.
    //       We want to preserve the Gov-Test-Scenario header, so we copy it into "extraHeaders".
    //       and remove it from "otherHeaders" to prevent it from being removed again.
    hc.otherHeaders
      .find { case (name, _) => name == GovTestScenarioHeader }
      .map(newHc.withExtraHeaders(_))
      .map(headers => headers.copy(otherHeaders = headers.otherHeaders.filterNot(_._1 == GovTestScenarioHeader)))
      .getOrElse(newHc)
  }

  private def withAdditionalHeaders[R <: Response](url: String)(f: HeaderCarrier => Future[R])(
      implicit hc: HeaderCarrier): Future[R] = {
    val newHc = withDesHeaders(hc)
    logger.debug(s"URL:[$url] Headers:[${newHc.headers}]")
    f(newHc)
  }

  // http-verbs converts non-2xx statuses into exceptions. We don't want this, so here we define
  // our own reader that returns the raw response.
  private object NoExceptReads extends HttpReads[HttpResponse] {
    override def read(method: String, url: String, response: HttpResponse): HttpResponse = response
  }

  def httpGet[R <: Response](url: String, toResponse: HttpResponse => R)(implicit hc: HeaderCarrier): Future[R] =
    withAdditionalHeaders[R](url) {
      WSHttp.GET(url)(NoExceptReads, _) map toResponse
    }

  def httpPost[T: Writes, R <: Response](url: String, elem: T, toResponse: HttpResponse => R)(
      implicit hc: HeaderCarrier): Future[R] =
    withAdditionalHeaders[R](url) {
      WSHttp.POST(url, elem)(implicitly[Writes[T]], NoExceptReads, _) map toResponse
    }

  def httpEmptyPost[R <: Response](url: String, toResponse: HttpResponse => R)(implicit hc: HeaderCarrier): Future[R] =
    withAdditionalHeaders[R](url) {
      WSHttp.POSTEmpty(url)(NoExceptReads, _) map toResponse
    }

  def httpPut[T: Writes, R <: Response](url: String, elem: T, toResponse: HttpResponse => R)(
      implicit hc: HeaderCarrier): Future[R] =
    withAdditionalHeaders[R](url) {
      WSHttp.PUT(url, elem)(implicitly[Writes[T]], NoExceptReads, _) map toResponse
    }
}
