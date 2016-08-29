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

package uk.gov.hmrc.selfassessmentapi.repositories.live

import play.modules.reactivemongo.MongoDbConnection
import reactivemongo.api.DB
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.domain.TaxYear
import uk.gov.hmrc.selfassessmentapi.repositories.domain.functional.FLiabilityResult

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object LiabilityRepository extends MongoDbConnection {
  private lazy val repository = new LiabilityMongoRepository()
  def apply() = repository
}

class LiabilityMongoRepository(implicit mongo: () => DB)
    extends ReactiveRepository[FLiabilityResult, BSONObjectID]("liabilities",
                                                              mongo,
                                                              domainFormat = FLiabilityResult.liabilityResultFormat,
                                                              idFormat = ReactiveMongoFormats.objectIdFormats) {

  override def indexes: Seq[Index] =
    Seq(
        Index(Seq(("data.saUtr", Ascending), ("data.taxYear", Ascending)),
              name = Some("ui_utr_taxyear"),
              unique = true))

  def save[T <: FLiabilityResult](liabilityResult: T): Future[T] = {
    val selector = BSONDocument("data.saUtr" -> liabilityResult.saUtr.value, "data.taxYear" -> liabilityResult.taxYear.value)
    collection.update(selector, liabilityResult, upsert = true).map(_ => liabilityResult)
  }

  def findBy(saUtr: SaUtr, taxYear: TaxYear): Future[Option[FLiabilityResult]] = {
    find("data.saUtr" -> saUtr.value, "data.taxYear" -> taxYear.value).map(_.headOption)
  }
}
