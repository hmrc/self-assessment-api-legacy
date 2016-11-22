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

package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.{JsValue, Json, Writes}
import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode.ErrorCode
import uk.gov.hmrc.selfassessmentapi.controllers.api.ValidationErrors

object Errors {
  implicit val errorDescWrites = Json.writes[Error]
  implicit val badRequestWrites = new Writes[BadRequest] {
    override def writes(req: BadRequest): JsValue = {
      Json.obj("code" -> req.code, "message" -> req.message, "errors" -> req.errors)
    }
  }

  case class Error(code: String, message: String, path: String)

  case class BadRequest(errors: Seq[Error], message: String) {
    val code = "INVALID_REQUEST"
  }

  def badRequest(validationErrors: ValidationErrors) = BadRequest(flattenValidationErrors(validationErrors), "Invalid request")
  def badRequest(message: String) = BadRequest(Seq.empty, message)

  private def flattenValidationErrors(validationErrors: ValidationErrors): Seq[Error] = {
    validationErrors.flatMap { validationError =>
      val (path, errors) = validationError
      errors.map { err =>
        Error(err.args(0).asInstanceOf[ErrorCode].toString, err.message, path.toString())
      }
    }
  }
}
