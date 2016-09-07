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

package uk.gov.hmrc.selfassessmentapi.services.live

import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceId, TaxYear}
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.SelfEmployment
import uk.gov.hmrc.selfassessmentapi.repositories.live.SelfEmploymentRepository

import scala.concurrent.Future

object SelfEmploymentService extends uk.gov.hmrc.selfassessmentapi.services.SourceService[SelfEmployment] {

  val repo = SelfEmploymentRepository()

  override def create(saUtr: SaUtr, taxYear: TaxYear, source: SelfEmployment): Future[SourceId] = {
    repo.create(saUtr, taxYear, source)
  }

  override def findById(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Option[SelfEmployment]] = {
    repo.findById(saUtr, taxYear, sourceId)
  }

  override def list(saUtr: SaUtr, taxYear: TaxYear): Future[Seq[SelfEmployment]] = ???

  override def update(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, source: SelfEmployment): Future[Boolean] = ???

  override def delete(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Boolean] = ???

}
