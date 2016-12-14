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

package uk.gov.hmrc.selfassessmentapi.repositories

import org.joda.time.{DateTimeZone, LocalDate}
import play.api.libs.json.JsObject
import play.modules.reactivemongo.MongoDbConnection
import reactivemongo.api.DB
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.domain.Properties
import uk.gov.hmrc.selfassessmentapi.resources.models.PropertyId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PropertiesRepository(implicit mongo: () => DB)
  extends ReactiveRepository[Properties, BSONObjectID](
    "properties",
    mongo,
    Properties.mongoFormats,
    idFormat = ReactiveMongoFormats.objectIdFormats) {

  override def indexes: Seq[Index] = Seq(
    Index(Seq(("nino", Ascending)), name = Some("properties_nino"), unique = true),
    Index(Seq(("lastModifiedDateTime", Ascending)), name = Some("properties_lastmodified"), unique = false)
  )

  def create(properties: Properties): Future[Boolean] = insert(properties).map(_.ok)

  def retrieve(nino: Nino): Future[Option[Properties]] = find("nino" -> nino.nino).map(_.headOption)

  def update(nino: Nino, properties: Properties): Future[Boolean] = {
    domainFormatImplicit.writes(properties.copy(lastModifiedDateTime = LocalDate.now(DateTimeZone.UTC))) match {
      case d @ JsObject(_) =>
        collection.update(
          BSONDocument("nino" -> nino.nino),
          d
        ).map { res =>
          if (res.hasErrors) logger.error(s"Database error occurred. Error: ${res.errmsg} Code: ${res.code}")
          res.ok && res.nModified > 0
        }
      case _ => Future.successful(false)
    }
  }
}

object PropertiesRepository extends MongoDbConnection {
  lazy private val repository = new PropertiesRepository()

  def apply(): PropertiesRepository = repository
}
