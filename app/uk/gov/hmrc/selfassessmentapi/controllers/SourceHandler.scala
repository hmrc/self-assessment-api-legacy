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
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.controllers._
import uk.gov.hmrc.selfassessmentapi.repositories.SourceRepository

import scala.concurrent.ExecutionContext.Implicits.global

abstract class SourceHandler[T](jsonMarshaller: JsonMarshaller[T], val listName: String) {

  val repository: SourceRepository[T]
  implicit val reads = jsonMarshaller.reads
  implicit val writes = jsonMarshaller.writes

  def create(nino: Nino, taxYear: TaxYear, jsValue: JsValue) = {
    validate[T, String](jsValue) {
      repository.create(nino, taxYear, _)
    }
  }

  def update(nino: Nino, taxYear: TaxYear, sourceId: SourceId, jsValue: JsValue) = {
    validate[T, Boolean](jsValue) {
      repository.update(nino, taxYear, sourceId, _)
    }
  }

  def findById(nino: Nino, taxYear: TaxYear, sourceId: SourceId) = {
    repository.findById(nino, taxYear, sourceId).map(_.map(toJson(_)))
  }

  def find(nino: Nino, taxYear: TaxYear) = repository.listAsJsonItem(nino, taxYear)

  def delete(nino: Nino, taxYear: TaxYear, sourceId: SourceId) = repository.delete(nino, taxYear, sourceId)

  def summaryHandler(summaryType: SummaryType): Option[SummaryHandler[_]]
}









