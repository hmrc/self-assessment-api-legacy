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
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.repositories.SelfEmploymentsRepository
import uk.gov.hmrc.selfassessmentapi.resources.models.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.resources.models.Errors.Error
import uk.gov.hmrc.selfassessmentapi.resources.models._
import uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment.{SelfEmploymentPeriod, SelfEmploymentPeriodicData}
import uk.gov.hmrc.selfassessmentapi.resources.models.{PeriodSummary, SourceId}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait SelfEmploymentPeriodService {

  val repository: SelfEmploymentsRepository

  def createPeriod(nino: Nino, id: SourceId, period: SelfEmploymentPeriod): Future[Either[Error, PeriodId]] = {
    val periodId = BSONObjectID.generate.stringify

    repository.retrieve(id, nino).flatMap {
      case Some(selfEmployment) =>
        selfEmployment.validatePeriod(period).map(err => Future.successful(Left(err))).getOrElse {
          repository.update(id, nino, selfEmployment.setPeriodsTo(periodId, period)).flatMap {
            case true => Future.successful(Right(periodId))
            case false => Future.successful(Left(Error(INTERNAL_ERROR.toString, "", "")))
          }
        }
      case None => Future.successful(Left(Error(NOT_FOUND.toString, s"Resource not found for id: $id", "")))
    }
  }

  def updatePeriod(nino: Nino, id: SourceId, periodId: PeriodId, periodicData: SelfEmploymentPeriodicData): Future[Boolean] = {
    repository.retrieve(id, nino).flatMap {
      case Some(selfEmployment) if selfEmployment.periodExists(periodId) =>
        repository.update(id, nino, selfEmployment.update(periodId, periodicData))
      case _ => Future.successful(false)
    }
  }

  def retrievePeriod(nino: Nino, id: SourceId, periodId: PeriodId): Future[Option[SelfEmploymentPeriod]] = {
    repository.retrieve(id, nino).map {
      case Some(selfEmployment) => selfEmployment.period(periodId)
      case None => None
    }
  }

  def retrieveAllPeriods(nino: Nino, id: SourceId): Future[Option[Seq[PeriodSummary]]] = {
    repository.retrieve(id, nino).map {
      case Some(selfEmployment) => Some(selfEmployment.periods.map {
        case (k, v) => PeriodSummary(k, v.from, v.to)
      }.toSeq.sorted)
      case None => None
    }
  }
}


object SelfEmploymentPeriodService extends SelfEmploymentPeriodService {
  override val repository: SelfEmploymentsRepository = SelfEmploymentsRepository()
}
