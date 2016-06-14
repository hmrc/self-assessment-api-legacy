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

import play.api.libs.json.{JsError, JsSuccess, JsValue, Reads}

import scala.util.{Failure, Success, Try}

package object controllers {

  def validate[T](id: String, jsValue: JsValue)(implicit reads: Reads[T]): Either[ErrorResult, String] = {
    Try(jsValue.validate[T]) match {
      case Success(JsSuccess(payload, _)) => Right(id)
      case Success(JsError(errors)) =>
        Left(ErrorResult(validationErrors = Some(errors)))
      case Failure(e) =>
        Left(ErrorResult(message = Some(s"could not parse body due to ${e.getMessage}")))
    }
  }
}
