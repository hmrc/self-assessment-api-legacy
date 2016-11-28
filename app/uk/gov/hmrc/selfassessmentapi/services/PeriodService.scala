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
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.resources.Errors.Error
import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.controllers.api.PeriodId
import uk.gov.hmrc.selfassessmentapi.domain.PeriodContainer
import uk.gov.hmrc.selfassessmentapi.resources.models.periods.{Period, PeriodSummary}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class PeriodService[ID <: String, P <: Period : Format, PC <: PeriodContainer[P, PC]] {

  val periodRepository : PeriodRepository[ID, P, PC]

  def createPeriod(nino: Nino, id: ID, period: P): Future[Either[Error, PeriodId]] = {
    val periodId = BSONObjectID.generate.stringify

    periodRepository.retrieve(id, nino).flatMap {
      case Some(selfEmployment) if selfEmployment.containsOverlappingPeriod(period) =>
        Future.successful(Left(Error(OVERLAPPING_PERIOD.toString, "Periods should not overlap", "")))
      case Some(selfEmployment) if selfEmployment.containsGap(period) =>
        Future.successful(Left(Error(GAP_PERIOD.toString, "Periods should not contain gaps between each other", "")))
      case Some(selfEmployment) if selfEmployment.containsMisalignedPeriod(period) =>
        Future.successful(Left(Error(MISALIGNED_PERIOD.toString, "Periods must fall on or within the start and end dates of the self-employment accounting period", "")))
      case Some(selfEmployment) =>
        periodRepository.update(id, nino, selfEmployment.setPeriodsTo(periodId, period)).flatMap {
          case true => Future.successful(Right(periodId))
          case false => Future.successful(Left(Error(INTERNAL_ERROR.toString, "", "")))
        }
      case None => Future.successful(Left(Error(NOT_FOUND.toString, s"Self-employment not found for id: $id", "")))
    }
  }

  def updatePeriod(nino: Nino, id: ID, periodId: PeriodId, period: P): Future[Boolean] = {
    periodRepository.retrieve(id, nino).flatMap {
      case Some(selfEmployment) if selfEmployment.periodExists(periodId) =>
        periodRepository.update(id, nino, selfEmployment.setPeriodsTo(periodId, period))
      case _ => Future.successful(false)
    }
  }

  def retrievePeriod(nino: Nino, id: ID, periodId: PeriodId): Future[Option[P]] = {
    periodRepository.retrieve(id, nino).map {
      case Some(selfEmployment) => selfEmployment.period(periodId)
      case None => None
    }
  }

  def retrieveAllPeriods(nino: Nino, id: ID): Future[Seq[PeriodSummary]] = {
    periodRepository.retrieve(id, nino).map {
      case Some(selfEmployment) => selfEmployment.periods.map {
        case (k, v) => PeriodSummary(k, v.from, v.to)
      }.toSeq.sorted
      case _ => Seq.empty
    }
  }
}
