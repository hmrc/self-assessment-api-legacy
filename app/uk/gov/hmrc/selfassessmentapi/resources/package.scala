/*
 * Copyright 2017 HM Revenue & Customs
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
import play.api.libs.json._
import play.api.mvc.Result
import play.api.mvc.Results.{BadRequest, InternalServerError}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.models.{ErrorResult, Errors, GenericErrorResult, ValidationErrorResult}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

package object resources {

  val GovTestScenarioHeader = "Gov-Test-Scenario"

  def unhandledResponse(status: Int, logger: Logger): Result = {
    logger.warn(s"Unhandled response from DES. Status code: $status. Returning 500 to client.")
    InternalServerError(Json.toJson(Errors.InternalServerError("An internal server error occurred")))
  }

  def handleValidationErrors(errorResult: ErrorResult): Result = {
    errorResult match {
      case GenericErrorResult(message) => BadRequest(Json.toJson(Errors.badRequest(message)))
      case ValidationErrorResult(errors) => BadRequest(Json.toJson(Errors.badRequest(errors)))
    }
  }

  def validate[T](id: String, jsValue: JsValue)(implicit reads: Reads[T]): Either[ErrorResult, String] = {
    Try(jsValue.validate[T]) match {
      case Success(JsSuccess(_, _)) => Right(id)
      case Success(JsError(errors)) => Left(ValidationErrorResult(errors))
      case Failure(e) => Left(GenericErrorResult(s"could not parse body due to ${e.getMessage}"))
    }
  }

  def validate[T, R](jsValue: JsValue)(f: T => Future[R])(implicit reads: Reads[T]): Either[ErrorResult, Future[R]] = {
    Try(jsValue.validate[T]) match {
      case Success(JsSuccess(payload, _)) => Right(f(payload))
      case Success(JsError(errors)) => Left(ValidationErrorResult(errors))
      case Failure(e) => Left(GenericErrorResult(s"could not parse body due to ${e.getMessage}"))
    }
  }

  implicit val desHeaderCarrier = HeaderCarrier(otherHeaders = Seq(
    "Authorization" -> AppContext.desToken,
    "Environment" -> AppContext.desEnv,
    "Accept" -> "application/vnd.hmrc.1.0+json"))
}
