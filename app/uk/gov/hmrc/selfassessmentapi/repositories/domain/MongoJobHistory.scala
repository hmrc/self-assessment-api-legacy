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

package uk.gov.hmrc.selfassessmentapi.repositories.domain

import org.joda.time.DateTime
import play.api.libs.json._
import reactivemongo.bson.{BSON, BSONHandler, BSONString}
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.controllers.definition.EnumJson


object MongoJobStatus extends Enumeration {
  type MongoJobStatus = Value

  val InProgress = Value("InProgress")
  val Success = Value("Success")
  val Failed = Value("Failed")

  implicit val jobStatus = EnumJson.enumFormat(MongoJobStatus, Some("Job Status is invalid"))

  implicit object BSONEnumHandler extends BSONHandler[BSONString, MongoJobStatus] {
    def read(doc: BSONString) = MongoJobStatus.Value(doc.value)

    def write(stats: MongoJobStatus) = BSON.write(stats.toString)
  }

}

import uk.gov.hmrc.selfassessmentapi.repositories.domain.MongoJobStatus._

case class MongoJobHistory(jobNumber: Int, status: MongoJobStatus, startedAt: DateTime = DateTime.now,
                           finishedAt: Option[DateTime] = None, recordsDeleted: Int = 0) {
  val isInProgress = status == InProgress
  val hasFailed = status == Failed
  val hasFinished = status == Success
}

object MongoJobHistory {
  implicit val mongoFormats = Json.format[MongoJobHistory]
  implicit val dateTimeFormat = ReactiveMongoFormats.dateTimeFormats
}
