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

import play.modules.reactivemongo.MongoDbConnection
import reactivemongo.api.DB
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.domain.SelfEmploymentId
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SelfEmployment
import uk.gov.hmrc.selfassessmentapi.repositories.domain.MongoSelfEmployment

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait SelfEmploymentRepository {
  def create(se: SelfEmployment): Future[SelfEmploymentId]

  def findById(id: SelfEmploymentId): Future[Option[SelfEmployment]]
}

object SelfEmploymentRepository extends MongoDbConnection {
  private lazy val repository = new SelfEmploymentMongoRepository

  def apply() = repository
}

class SelfEmploymentMongoRepository(implicit mongo: () => DB)
  extends ReactiveRepository[MongoSelfEmployment, BSONObjectID](
    "selfEmployment",
    mongo,
    domainFormat = MongoSelfEmployment.formats,
    idFormat = ReactiveMongoFormats.objectIdFormats)
    with SelfEmploymentRepository {

  override def create(se: SelfEmployment): Future[SelfEmploymentId] = {
    val mongoSe = MongoSelfEmployment.from(se)
    insert(mongoSe).map(_ => mongoSe.id.stringify)
  }

  override def findById(id: SelfEmploymentId): Future[Option[SelfEmployment]] = {
    for (option <- findById(BSONObjectID(id))) yield option.map(_.toSelfEmployment)
  }
}
