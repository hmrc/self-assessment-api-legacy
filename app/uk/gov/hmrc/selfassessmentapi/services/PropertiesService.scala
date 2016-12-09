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
import uk.gov.hmrc.selfassessmentapi.controllers.api.{Location, PeriodId}
import uk.gov.hmrc.selfassessmentapi.domain.Properties
import uk.gov.hmrc.selfassessmentapi.repositories.PropertiesRepository
import uk.gov.hmrc.selfassessmentapi.resources.models.Errors.Error
import uk.gov.hmrc.selfassessmentapi.resources.models.TaxYear
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.{PropertiesAnnualSummary, PropertiesPeriod, PropertiesPeriodicData}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PropertiesService extends PeriodService[Location, PropertiesPeriod, Properties, PropertiesPeriodicData]
  with AnnualSummaryService[PropertiesAnnualSummary, Properties] {

  override val periodRepository = PropertiesRepository()
  override val annualSummaryRepository = periodRepository

  private def create(nino: Nino, location: Location): Future[Option[Properties]] = {
    val properties = Properties(BSONObjectID.generate, LocalDate.now(DateTimeZone.UTC), nino, location, Map.empty, Map.empty)
    periodRepository.create(properties).map {
      case true => Some(properties)
      case false => None
    }
  }

  override def createPeriod(nino: Nino, location: Location, period: PropertiesPeriod): Future[Either[Error, PeriodId]] = {
    periodRepository.retrieve(location, nino).flatMap { opt =>
      if (opt.isEmpty) create(nino, location) else Future.successful(opt)
    }.flatMap {
      case Some(_) => super.createPeriod(nino, location, period)
      case None => throw new RuntimeException("Could not persist Properties to the database. Is the database available?")
    }
  }

  def updateAnnualSummary(nino: Nino, location: Location, taxYear: TaxYear, summary: PropertiesAnnualSummary) =
    periodRepository.retrieve(location, nino).flatMap { opt =>
      if (opt.isEmpty) create(nino, location) else Future.successful(opt)
    }.flatMap {
      case Some(properties) => periodRepository.update(location, nino, properties.copy(annualSummaries = properties.annualSummaries.updated(taxYear, summary)))
      case None => throw new RuntimeException("Could not persist Properties to the database. Is the database available?")
    }
}

object PropertiesService {
  def apply(): PropertiesService = new PropertiesService

}
