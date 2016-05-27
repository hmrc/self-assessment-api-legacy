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

package uk.gov.hmrc.selfassessmentapi.controllers

import play.api.libs.json.{JsString, _}
import play.api.mvc.Request
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.domain.ValidationErrors

import scala.util.{Success, Try}

trait BaseController
  extends uk.gov.hmrc.play.microservice.controller.BaseController with HalSupport {

  val context: String

  def hc(request: Request[Any]): HeaderCarrier =
    HeaderCarrier.fromHeadersAndSession(request.headers, None)

  def failedValidationJson(errors: ValidationErrors) = {
    JsArray(
      for {
        (path, errSeq) <- errors
        error <- errSeq
      } yield JsObject(
        Seq(
          "path" -> JsString(path.toString),
          "code" -> JsString(
            Try(error.args.filter(_.isInstanceOf[ErrorCode]).head.asInstanceOf[ErrorCode]) match {
              case Success(code) => code.toString
              case _ => "N/A"
            }
          ),
          "message" -> JsString(error.message))
      )
    )
  }
}
