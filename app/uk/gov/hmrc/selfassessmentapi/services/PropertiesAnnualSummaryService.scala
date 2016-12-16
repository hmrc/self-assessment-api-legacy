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
import uk.gov.hmrc.selfassessmentapi.resources.models.TaxYear
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.PropertiesAnnualSummary

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait PropertiesAnnualSummaryService {

  val repository: PropertiesRepository

  def updateAnnualSummary(nino: Nino, taxYear: TaxYear, summary: PropertiesAnnualSummary): Future[Boolean] = {
    repository.retrieve(nino).flatMap {
      case Some(properties) =>
        repository.update(nino, properties.copy(annualSummaries = properties.annualSummaries.updated(taxYear, summary)))
      case None => Future.successful(false)
    }
  }


  def retrieveAnnualSummary(taxYear: TaxYear, nino: Nino): Future[Option[PropertiesAnnualSummary]] = {
    repository.retrieve(nino).map {
      case Some(resource) =>
        Some(resource.annualSummary(taxYear).getOrElse(PropertiesAnnualSummary(None, None, None, None, None)))
      case None => None
    }
  }
}

object PropertiesAnnualSummaryService extends PropertiesAnnualSummaryService {
  override val repository: PropertiesRepository = PropertiesRepository()
}
