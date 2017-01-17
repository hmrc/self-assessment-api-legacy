/*
 * Copyright 2017 HM Revenue & Customs
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

import reactivemongo.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.domain.PropertyPeriodOps
import uk.gov.hmrc.selfassessmentapi.domain.PropertyPeriodOps._
import uk.gov.hmrc.selfassessmentapi.repositories.PropertiesRepository
import uk.gov.hmrc.selfassessmentapi.resources.models.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.resources.models.Errors.Error
import uk.gov.hmrc.selfassessmentapi.resources.models._
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.{FHLPeriodicData, FHLProperties, OtherPeriodicData, OtherProperties}


trait PropertiesPeriodService[T <: Period, P <: PeriodicData] {
  val repository: PropertiesRepository

  val propertyOps: PropertyPeriodOps[T, P]

  def createPeriod(nino: Nino, period: T): Future[Either[Error, PeriodId]] = {
    val periodId = BSONObjectID.generate.stringify

    repository.retrieve(nino).flatMap {
      case Some(property) => propertyOps.validatePeriod(period, property) match {
        case Left(error) => Future.successful(Left(error))
        case Right(validResource) => repository.update(nino, propertyOps.setPeriodsTo(periodId, period, validResource)).flatMap {
          case true => Future.successful(Right(periodId))
          case false => Future.successful(Left(Error(INTERNAL_ERROR.toString, "", "")))
        }
      }
      case None => Future.successful(Left(Error(ErrorCode.NOT_FOUND.toString, s"Resource not found", "")))
    }
  }

  def updatePeriod(nino: Nino, periodId: PeriodId, period: P): Future[Boolean] = {
    repository.retrieve(nino).flatMap {
      case Some(property) if propertyOps.periodExists(periodId, property) =>
        repository.update(nino, propertyOps.update(periodId, period, property))
      case _ => Future.successful(false)
    }
  }

  def retrievePeriod(nino: Nino, periodId: PeriodId): Future[Option[T]] = {
    repository.retrieve(nino).map {
      case Some(properties) => propertyOps.period(periodId, properties)
      case None => None
    }
  }

  def retrieveAllPeriods(nino: Nino): Future[Option[Seq[PeriodSummary]]] = {
    repository.retrieve(nino).map {
      case Some(properties) => {
        val bucket = propertyOps.periods(properties)

        Some(bucket.map { case (k, v) => PeriodSummary(k, v.from, v.to) }.toSeq.sorted)
      }
      case None => None
    }
  }
}

object OtherPropertiesPeriodService extends PropertiesPeriodService[OtherProperties, OtherPeriodicData] {
  override val repository: PropertiesRepository = PropertiesRepository()
  override val propertyOps: PropertyPeriodOps[OtherProperties, OtherPeriodicData] =
    implicitly[PropertyPeriodOps[OtherProperties, OtherPeriodicData]]
}

object FHLPropertiesPeriodService extends PropertiesPeriodService[FHLProperties, FHLPeriodicData] {
  override val repository: PropertiesRepository = PropertiesRepository()
  override val propertyOps: PropertyPeriodOps[FHLProperties, FHLPeriodicData] =
    implicitly[PropertyPeriodOps[FHLProperties, FHLPeriodicData]]
}
