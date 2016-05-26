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

package uk.gov.hmrc.selfassessmentapi.controllers.sandbox

import play.api.libs.json.Json._
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.domain._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

case class ErrorResult(message: Option[String] = None, validationErrors: Option[ValidationErrors] = None)

trait SummaryHandler[T] {

  implicit val reads: Reads[T]
  implicit val writes: Writes[T]
  def example(id: SummaryId): T
  val listName: String

  private def generateId: String = BSONObjectID.generate.stringify

  def create(jsValue: JsValue): Future[Either[ErrorResult, SummaryId]] =
    Future.successful (
      Try(jsValue.validate[T]) match {
        case Success(JsSuccess(payload, _)) => Right(generateId)
        case Success(JsError(errors)) =>
          Left(ErrorResult(validationErrors = Some(errors)))
        case Failure(e) =>
          Left(ErrorResult(message = Some(s"could not parse body due to ${e.getMessage}")))
      }
    )

  def findById(summaryId: SummaryId): Future[Option[JsValue]] = {
    Future.successful(Some(toJson(example(summaryId))))
  }

  def find: Future[Seq[SummaryId]] =
    Future.successful(
      Seq(
        generateId,
        generateId,
        generateId,
        generateId,
        generateId
      )
    )

  def delete(summaryId: SummaryId): Future[Boolean] =
    Future.successful(true)

  def update(summaryId: SummaryId, jsValue: JsValue): Future[Either[ErrorResult, SummaryId]] =
    Future.successful (
      Try(jsValue.validate[T]) match {
        case Success(JsSuccess(payload, _)) => Right(summaryId)
        case Success(JsError(errors)) =>
          Left(ErrorResult(validationErrors = Some(errors)))
        case Failure(e) =>
          Left(ErrorResult(message = Some(s"could not parse body due to ${e.getMessage}")))
      }
    )
}


