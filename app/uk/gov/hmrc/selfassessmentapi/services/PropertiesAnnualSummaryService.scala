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

import play.api.Logger
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.repositories.PropertiesRepository
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.{FHLPropertiesAnnualSummary, OtherPropertiesAnnualSummary, PropertiesAnnualSummary, PropertyType}
import uk.gov.hmrc.selfassessmentapi.resources.models.TaxYear
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.PropertyType.PropertyType

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait PropertiesAnnualSummaryService {

  val logger: Logger
  val repository: PropertiesRepository

  def updateAnnualSummary(nino: Nino, taxYear: TaxYear, summary: PropertiesAnnualSummary): Future[Boolean] = {
    repository.retrieve(nino).flatMap {
      case Some(properties) => summary match {
        case o @ OtherPropertiesAnnualSummary(_, _) =>
          repository.update(nino, properties.copy(otherBucket = properties.otherBucket.copy(annualSummaries = properties.otherBucket.annualSummaries.updated(taxYear, o))))
        case o @ FHLPropertiesAnnualSummary(_, _) =>
          repository.update(nino, properties.copy(fhlBucket = properties.fhlBucket.copy(annualSummaries = properties.fhlBucket.annualSummaries.updated(taxYear, o))))
      }
      case None => Future.successful(false)
    }
  }


  def retrieveAnnualSummary(nino: Nino, propertyId: PropertyType, taxYear: TaxYear): Future[Option[PropertiesAnnualSummary]] = {
    repository.retrieve(nino).map {
      case Some(resource) =>
        Some(resource.annualSummary(propertyId, taxYear))
      case None => None
    }
  }
}

object PropertiesAnnualSummaryService extends PropertiesAnnualSummaryService {
  override val logger: Logger = Logger(classOf[PropertiesAnnualSummaryService])
  override val repository: PropertiesRepository = PropertiesRepository()
}
