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
import play.api.libs.json._
import play.api.mvc.Result
import play.api.mvc.Results.{BadRequest, InternalServerError}
import uk.gov.hmrc.selfassessmentapi.models.{ErrorResult, Errors, GenericErrorResult, ValidationErrorResult}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

package object resources {

  val GovTestScenarioHeader = "Gov-Test-Scenario"

  def unhandledResponse(status: Int, logger: Logger): Result = {
    logger.error(s"Unhandled response from DES. Status code: $status. Returning 500 to client.")
    InternalServerError(Json.toJson(Errors.InternalServerError("An internal server error occurred")))
  }

  def handleValidationErrors(errorResult: ErrorResult): Result = {
    errorResult match {
      case GenericErrorResult(message) => BadRequest(Json.toJson(Errors.badRequest(message)))
      case ValidationErrorResult(errors) => BadRequest(Json.toJson(Errors.badRequest(errors)))
    }
  }

  def validate[T, R](jsValue: JsValue)(f: T => Future[R])(implicit reads: Reads[T]): Future[Either[ErrorResult, R]] =
    jsValue.validate[T] match {
      case JsSuccess(payload, _) => f(payload).map(Right(_))
      case JsError(errors) => Future.successful(Left(ValidationErrorResult(errors)))
    }
}
