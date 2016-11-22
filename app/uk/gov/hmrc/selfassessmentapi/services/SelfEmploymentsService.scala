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
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi._
import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode.ErrorCode
import uk.gov.hmrc.selfassessmentapi.controllers.api.{ErrorCode, PeriodId, SourceId, TaxYear}
import uk.gov.hmrc.selfassessmentapi.repositories.SelfEmploymentsRepository
import uk.gov.hmrc.selfassessmentapi.resources.models.periods.{PeriodSummary, SelfEmploymentPeriod}
import uk.gov.hmrc.selfassessmentapi.resources.models.{SelfEmployment, SelfEmploymentAnnualSummary}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait SelfEmploymentsService {

  def create(nino: Nino, selfEmployment: SelfEmployment): Future[Option[SourceId]]
  def update(nino: Nino, selfEmployment: SelfEmployment, id: SourceId): Future[Boolean]
  def retrieve(nino: Nino, id: SourceId): Future[Option[SelfEmployment]]
  def retrieveAll(nino: Nino): Future[Seq[SelfEmployment]]
  def updateAnnualSummary(nino: Nino, id: SourceId, taxYear: TaxYear, summary: SelfEmploymentAnnualSummary): Future[Boolean]
  def retrieveAnnualSummary(id: SourceId, taxYear: TaxYear, nino: Nino): Future[Option[SelfEmploymentAnnualSummary]]
  def createPeriod(nino: Nino, id: SourceId, period: SelfEmploymentPeriod): Future[Either[ErrorCode, PeriodId]]
  def updatePeriod(nino: Nino, id: SourceId, periodId: PeriodId, period: SelfEmploymentPeriod): Future[Boolean]
  def retrievePeriod(nino: Nino, id: SourceId, periodId: PeriodId): Future[Option[SelfEmploymentPeriod]]
  def retrieveAllPeriods(nino: Nino, id: SourceId): Future[Seq[PeriodSummary]]
}

object SelfEmploymentsService {
  def apply(): SelfEmploymentsService = new SelfEmploymentsMongoService(SelfEmploymentsRepository())
  def apply(repository: SelfEmploymentsRepository): SelfEmploymentsService =
    new SelfEmploymentsMongoService(repository)
}

class SelfEmploymentsMongoService(mongoRepository: SelfEmploymentsRepository) extends SelfEmploymentsService {

  override def create(nino: Nino, selfEmployment: SelfEmployment): Future[Option[SourceId]] = {
    val id = BSONObjectID.generate
    val newSelfEmployment = domain.SelfEmployment(id, id.stringify, nino, LocalDate.now(DateTimeZone.UTC), selfEmployment.commencementDate)
    mongoRepository.create(newSelfEmployment).map {
      case true => Some(newSelfEmployment.sourceId)
      case false => None
    }
  }

  override def update(nino: Nino, selfEmployment: SelfEmployment, id: SourceId): Future[Boolean] = {
    mongoRepository.retrieve(id, nino).flatMap {
      case Some(oldSelfEmployment) =>
        mongoRepository.update(id, nino, oldSelfEmployment.copy(commencementDate = selfEmployment.commencementDate))
      case None => Future.successful(false)
    }
  }

  override def retrieve(nino: Nino, id: SourceId): Future[Option[SelfEmployment]] =
    mongoRepository.retrieve(id, nino).map(_.map(_.toModel))

  override def retrieveAll(nino: Nino): Future[Seq[SelfEmployment]] =
    mongoRepository.retrieveAll(nino).map(_.map(_.toModel))

  override def updateAnnualSummary(nino: Nino, id: SourceId, taxYear: TaxYear, summary: SelfEmploymentAnnualSummary): Future[Boolean] = {
    mongoRepository.retrieve(id, nino).flatMap {
      case Some(selfEmployment) =>
        mongoRepository.update(id, nino, selfEmployment.copy(annualSummaries = selfEmployment.annualSummaries.updated(taxYear, summary)))
      case None => Future.successful(false)
    }
  }

  override def retrieveAnnualSummary(id: SourceId, taxYear: TaxYear, nino: Nino): Future[Option[SelfEmploymentAnnualSummary]] = {
    mongoRepository.retrieve(id, nino).map {
      case Some(selfEmployment) => selfEmployment.annualSummary(taxYear)
      case None => None
    }
  }

  override def createPeriod(nino: Nino, id: SourceId, period: SelfEmploymentPeriod): Future[Either[ErrorCode, PeriodId]] = {
    val periodId = BSONObjectID.generate.stringify

    mongoRepository.retrieve(id, nino).flatMap {
      case Some(selfEmployment) if selfEmployment.containsPeriod(period) => Future.successful(Left(ErrorCode.DUPLICATE_PERIOD))
      case Some(selfEmployment) =>
        mongoRepository.update(id, nino, selfEmployment.copy(periods = selfEmployment.periods.updated(periodId, period))).flatMap {
          case true => Future.successful(Right(periodId))
          case false => Future.successful(Left(ErrorCode.INTERNAL_ERROR))
        }
      case None => Future.successful(Left(ErrorCode.NOT_FOUND))
    }
  }

  override def updatePeriod(nino: Nino, id: SourceId, periodId: PeriodId, period: SelfEmploymentPeriod): Future[Boolean] = {
    mongoRepository.retrieve(id, nino).flatMap {
      case Some(selfEmployment) if selfEmployment.periodExists(periodId) =>
        mongoRepository.update(id, nino, selfEmployment.copy(periods = selfEmployment.periods.updated(periodId, period)))
      case _ => Future.successful(false)
    }
  }

  override def retrievePeriod(nino: Nino, id: SourceId, periodId: PeriodId): Future[Option[SelfEmploymentPeriod]] = {
    mongoRepository.retrieve(id, nino).map {
      case Some(selfEmployment) => selfEmployment.period(periodId)
      case None => None
    }
  }

  override def retrieveAllPeriods(nino: Nino, id: SourceId): Future[Seq[PeriodSummary]] = {
    mongoRepository.retrieve(id, nino).map {
      case Some(selfEmployment) => selfEmployment.periods.map {
        case (k, v) => PeriodSummary(k, v.from, v.to)
      }.toSeq.sorted
      case _ => Seq.empty
    }
  }
}
