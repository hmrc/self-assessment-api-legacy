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

import reactivemongo.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.repositories.PropertiesRepository
import uk.gov.hmrc.selfassessmentapi.resources.models.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.resources.models.Errors.Error
import uk.gov.hmrc.selfassessmentapi.resources.models._
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.{PropertiesPeriod, PropertiesPeriodicData}


trait PropertiesPeriodService {
  val repository: PropertiesRepository

  def createPeriod(nino: Nino, period: PropertiesPeriod): Future[Either[Error, PeriodId]] = {
    val periodId = BSONObjectID.generate.stringify

    repository.retrieve(nino).flatMap {
      case Some(property) => property.validatePeriod(period) match {
        case Left(error) => Future.successful(Left(error))
        case Right(validResource) => repository.update(nino, validResource.setPeriodsTo(periodId, period)).flatMap {
          case true => Future.successful(Right(periodId))
          case false => Future.successful(Left(Error(INTERNAL_ERROR.toString, "", "")))
        }
      }
      case None => Future.successful(Left(Error(ErrorCode.NOT_FOUND.toString, s"Resource not found", "")))
    }
  }

  def updatePeriod(nino: Nino, periodId: PeriodId, periodicData: PropertiesPeriodicData): Future[Boolean] = {
    repository.retrieve(nino).flatMap {
      case Some(selfEmployment) if selfEmployment.periodExists(periodId) =>
        repository.update(nino, selfEmployment.update(periodId, periodicData))
      case _ => Future.successful(false)
    }
  }

  def retrievePeriod(nino: Nino, id: PeriodId): Future[Option[PropertiesPeriod]] = {
    repository.retrieve(nino).map {
      case Some(properties) => properties.period(id)
      case None => None
    }
  }

  def retrieveAllPeriods(nino: Nino): Future[Seq[PeriodSummary]] = {
    repository.retrieve(nino).map {
      case Some(properties) => properties.periods.map {
        case (k, v) => PeriodSummary(k, v.from, v.to)
      }.toSeq.sorted
      case _ => Seq.empty
    }
  }
}

object PropertiesPeriodService extends PropertiesPeriodService {
  override val repository: PropertiesRepository = PropertiesRepository()
}
