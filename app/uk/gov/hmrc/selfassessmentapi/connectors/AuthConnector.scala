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

import play.api.Logger
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.play.auth.microservice.connectors.ConfidenceLevel
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet}
import uk.gov.hmrc.selfassessmentapi.config.{AppContext, WSHttp}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait AuthConnector {
  val serviceUrl: String
  val handlerError: Throwable => Unit
  val http: HttpGet

  def saUtr(confidenceLevel: ConfidenceLevel)(implicit hc: HeaderCarrier): Future[Option[SaUtr]] = {
    http.GET(s"$serviceUrl/auth/authority") map {
      resp =>
        val json = resp.json
        val cl = (json \ "confidenceLevel").as[Int]
        if (cl >= confidenceLevel.level) {
          val utr = (json \ "accounts" \ "sa" \ "utr").asOpt[String]
          utr.map(SaUtr(_))
        } else
          None
    } recover {
      case e: Throwable =>
        handlerError(e)
        None
    }
  }
}

object AuthConnector extends AuthConnector {
  override lazy val serviceUrl: String = AppContext.authUrl
  override val http: HttpGet = WSHttp
  override val handlerError: Throwable => Unit = e => Logger.error("Error in request to auth", e)
}
