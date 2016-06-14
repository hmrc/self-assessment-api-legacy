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

import play.api.libs.json.Json.toJson
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.controllers.controllers._
import uk.gov.hmrc.selfassessmentapi.controllers.sandbox.SummaryHandler
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.repositories.SourceRepository
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

trait SourceHandler[T] {

  val repository : SourceRepository[T]
  implicit val reads: Reads[T]
  implicit val writes: Writes[T]
  val listName: String

  private def generateId: String = BSONObjectID.generate.stringify

  def create(saUtr: SaUtr, taxYear: TaxYear, jsValue: JsValue): Either[ErrorResult, Future[SourceId]] = {
    validate[T](jsValue) {
        repository.create(saUtr, taxYear, _)
    }
  }


  def findById(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Option[JsValue]] = {
    repository.findById(saUtr, taxYear, sourceId).map(_.map(toJson(_)))
  }

  def find(saUtr: SaUtr, taxYear: TaxYear): Future[Seq[SourceId]] = repository.listIds(saUtr, taxYear)

  def delete(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Boolean] = repository.delete(saUtr, taxYear,sourceId)

  def update(sourceId: SourceId, jsValue: JsValue): Future[Either[ErrorResult, SourceId]] =
    Future.successful (validate[T](sourceId, jsValue))

  def summaryHandler(summaryType: SummaryType): Option[SummaryHandler[_]]
}









