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

package uk.gov.hmrc.selfassessmentapi.services

import play.api.libs.json.Format
import reactivemongo.api.DB
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.domain.PeriodContainer
import uk.gov.hmrc.selfassessmentapi.resources.models.{Period, PeriodicData}

import scala.concurrent.Future

abstract class NewSourceRepository[ID <: String, P <: Period : Format, PC <: PeriodContainer[P, PC, PD], PD <: PeriodicData : Format]
  (repoName: String, domainFormat: Format[PC])(implicit mongo: () => DB, manifest: Manifest[PC])
  extends ReactiveRepository[PC, BSONObjectID](
  repoName,
  mongo,
  domainFormat,
  idFormat = ReactiveMongoFormats.objectIdFormats) {

  def retrieve(id: ID, nino: Nino): Future[Option[PC]]
  def update(id: ID, nino: Nino, periodContainer: PC): Future[Boolean]
}
