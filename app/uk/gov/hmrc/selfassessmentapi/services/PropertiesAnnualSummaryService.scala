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

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.repositories.PropertiesRepository
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.PropertyType
import uk.gov.hmrc.selfassessmentapi.resources.models.{AnnualSummary, TaxYear}
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.PropertyType.PropertyType

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait PropertiesAnnualSummaryService {

  val repository: PropertiesRepository

  def updateAnnualSummary(nino: Nino, propertyId: PropertyType, taxYear: TaxYear, summary: AnnualSummary): Future[Boolean] = {
    repository.retrieve(nino).flatMap {
      case Some(properties) => propertyId match {
        case PropertyType.OTHER => ??? // repository.update(nino, properties.copy(annualSummaries = properties.annualSummaries.updated(taxYear, summary)))
        case PropertyType.FHL => ???
      }
      case None => Future.successful(false)
    }
  }


  def retrieveAnnualSummary(nino: Nino, propertyId: PropertyType, taxYear: TaxYear): Future[Option[AnnualSummary]] = {
    repository.retrieve(nino).map {
      case Some(resource) =>
        Some(resource.annualSummary(propertyId, taxYear))
      case None => None
    }
  }
}

object PropertiesAnnualSummaryService extends PropertiesAnnualSummaryService {
  override val repository: PropertiesRepository = PropertiesRepository()
}
