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

import play.api.data.validation.ValidationError
import play.api.hal.{Hal, HalLink, HalResource}
import play.api.libs.json.{JsString, _}
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait BaseController
  extends uk.gov.hmrc.play.microservice.controller.BaseController {

  val context: String

  def hc(request: Request[Any]): HeaderCarrier =
    HeaderCarrier.fromHeadersAndSession(request.headers, None)

  def halResource(jsValue: JsValue, links: Seq[HalLink]): HalResource = {
    val halState = Hal.state(jsValue)
    links.foldLeft(halState)((res, link) => res ++ link)
  }

  def halResourceList(name: String, value: JsValue, self: String) = {
    halResource(
      JsObject(
        Seq(
          "_embedded" -> JsObject(
            Seq(name -> value))
        )
      ),
      Seq(HalLink("self", self)))
  }

  override protected def withJsonBody[T](f: (T) => Future[Result])(
    implicit request: Request[JsValue], m: Manifest[T], reads: Reads[T]) =
    Try(request.body.validate[T]) match {
      case Success(JsSuccess(payload, _)) => f(payload)
      case Success(JsError(errors)) =>
        Future.successful(BadRequest(failedValidationJson(errors)))
      case Failure(e) =>
        Future.successful(BadRequest(s"could not parse body due to ${e.getMessage}"))
    }


  private def failedValidationJson(errors: Seq[(JsPath, Seq[ValidationError])]): JsObject = {
    JsObject(
      Seq(
        "code" -> JsString("VALIDATION_FAILED"),
        "message" -> JsString("Validation failed"),
        "errors" -> JsArray(
          for {
            (path, errSeq) <- errors
            error <- errSeq
          } yield JsObject(
            Seq(
              "path" -> JsString(path.toString),
              "code" -> JsString(
                Try(error.args.filter(_.isInstanceOf[ErrorCode]).head.asInstanceOf[ErrorCode]) match {
                  case Success(code) => code.value
                  case _ => "N/A"
                }
              ),
              "message" -> JsString(error.message))))))
  }
}
