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
import uk.gov.hmrc.selfassessmentapi.repositories.SelfEmploymentsRepository
import uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment.SelfEmploymentAnnualSummary
import uk.gov.hmrc.selfassessmentapi.resources.models.{SourceId, TaxYear}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait SelfEmploymentAnnualSummaryService {

  val repository: SelfEmploymentsRepository

  def updateAnnualSummary(nino: Nino, id: SourceId, taxYear: TaxYear, annualSummary: SelfEmploymentAnnualSummary): Future[Boolean] = {
    repository.retrieve(id, nino).flatMap {
      case Some(selfEmployment) =>
        repository.update(id, nino, selfEmployment.copy(annualSummaries = selfEmployment.annualSummaries.updated(taxYear, annualSummary)))
      case None => Future.successful(false)
    }
  }

  def retrieveAnnualSummary(id: SourceId, taxYear: TaxYear, nino: Nino): Future[Option[SelfEmploymentAnnualSummary]] = {
    repository.retrieve(id, nino).map {
      case Some(resource) => resource.annualSummary(taxYear)
      case None => None
    }
  }
}

object SelfEmploymentAnnualSummaryService extends SelfEmploymentAnnualSummaryService {
  override val repository: SelfEmploymentsRepository = SelfEmploymentsRepository()
}
