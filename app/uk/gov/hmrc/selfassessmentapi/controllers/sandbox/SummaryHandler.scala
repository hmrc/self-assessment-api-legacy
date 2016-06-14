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
import uk.gov.hmrc.selfassessmentapi.domain.{BaseDomain, _}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}



case class SummaryListItem(id: SummaryId, json: JsValue)

case class SummaryHandler[T](listName: String, domain: BaseDomain[T]) {

  implicit val reads: Reads[T] = domain.reads
  implicit val writes: Writes[T] = domain.writes
  def example(id: Option[SummaryId]): T = domain.example(id)

  private def generateId: String = BSONObjectID.generate.stringify

  def create(jsValue: JsValue): Future[Either[ErrorResult, SummaryId]] =
    Future.successful (validate[T](generateId, jsValue))

  def findById(summaryId: SummaryId): Future[Option[JsValue]] = {
    Future.successful(Some(toJson(example(Some(summaryId)))))
  }

  private def exampleJson(summaryId: SummaryId): JsValue =
    toJson(example(Some(summaryId)))

  def find: Future[Seq[SummaryListItem]] = {
    def createItem(summaryId: SummaryId) = SummaryListItem(summaryId, exampleJson(summaryId))
    Future.successful(
      Seq(
        createItem(generateId),
        createItem(generateId),
        createItem(generateId),
        createItem(generateId),
        createItem(generateId)
      )
    )
  }

  def delete(summaryId: SummaryId): Future[Boolean] =
    Future.successful(true)

  def update(summaryId: SummaryId, jsValue: JsValue): Future[Either[ErrorResult, SummaryId]] =
    Future.successful (validate[T](summaryId, jsValue))
}

