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
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceId, SummaryId, TaxYear}

import scala.concurrent.Future

trait SummaryRepository[T] {

  def create(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, summary: T): Future[Option[SummaryId]]

  def findById(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[T]]

  def update(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, summary: T): Future[Boolean]

  def delete(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean]

  def list(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[T]]]

  def listAsJsonItem(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]]
}

case class SummaryRepositoryWrapper[T](private val target: SummaryRepository[T]) extends SummaryRepository[T] {

  lazy val selfAssessmentRepository = SelfAssessmentRepository()

  override def create(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, income: T): Future[Option[SummaryId]] = {
    selfAssessmentRepository.touch(saUtr, taxYear)
    target.create(saUtr, taxYear, sourceId, income)
  }

  override def update(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, summary: T): Future[Boolean] = {
    selfAssessmentRepository.touch(saUtr, taxYear)
    target.update(saUtr, taxYear, sourceId, id, summary)
  }

  override def findById(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[T]] =
    target.findById(saUtr, taxYear, sourceId, id)

  override def delete(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] = {
    selfAssessmentRepository.touch(saUtr, taxYear)
    target.delete(saUtr, taxYear, sourceId, id)
  }

  override def list(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[T]]] =
    target.list(saUtr, taxYear, sourceId)

  override def listAsJsonItem(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
    target.listAsJsonItem(saUtr, taxYear, sourceId)
}