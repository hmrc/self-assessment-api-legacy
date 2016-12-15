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

import org.joda.time.{DateTimeZone, LocalDate}
import reactivemongo.bson.BSONObjectID
import reactivemongo.core.errors.DatabaseException
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.domain.Properties
import uk.gov.hmrc.selfassessmentapi.repositories.PropertiesRepository
import uk.gov.hmrc.selfassessmentapi.resources.models._
import uk.gov.hmrc.selfassessmentapi.resources.models.Errors.Error

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PropertiesService {
  private val repository = PropertiesRepository()

  def update(nino: Nino, prop: properties.Properties): Future[Boolean] =
    repository.retrieve(nino).flatMap {
      case Some(persistedProperties) =>
        repository.update(nino, persistedProperties.copy(accountingType = prop.accountingType))
      case None => Future.successful(false)
    }

  def retrieve(nino: Nino): Future[Option[properties.Properties]] = {
    repository.retrieve(nino).map {
      case Some(properties) => Some(properties.toModel)
      case None => None
    }
  }

  def create(nino: Nino, props: properties.Properties): Future[Either[Error, Boolean]] = {
    val properties = Properties(BSONObjectID.generate, nino, props.accountingType)

    repository.create(properties).map(Right(_)) recover {
      case e: DatabaseException if e.code.contains(11000) => // i.e. Duplicate key exception.
        Left(
          Error(ErrorCode.ALREADY_EXISTS.toString, s"A property business already exists", ""))
    }
  }

}

object PropertiesService {
  def apply(): PropertiesService = new PropertiesService
}
