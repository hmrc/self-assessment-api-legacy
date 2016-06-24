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

package uk.gov.hmrc.selfassessmentapi.repositories

import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.domain._

import scala.concurrent.Future

trait SummaryRepository[T] {

  def createSummary(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, income: T): Future[Option[SummaryId]]

  def findSummaryById(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[T]]

  def updateSummary(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, income: T): Future[Boolean]

  def deleteSummary(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean]

  def listSummaries(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[T]]]

  def listAsJsonItem(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]]
}
