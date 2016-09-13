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

package uk.gov.hmrc.selfassessmentapi.repositories.live

import play.api.libs.json.Json._
import play.modules.reactivemongo.MongoDbConnection
import reactivemongo.api.DB
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.{BSONDocument, BSONObjectID, BSONString}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.mongo.{AtomicUpdate, ReactiveRepository}
import uk.gov.hmrc.selfassessmentapi.controllers._
import uk.gov.hmrc.selfassessmentapi.controllers.api.employment.{Employment => _, _}
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceId, SummaryId, TaxYear, _}
import uk.gov.hmrc.selfassessmentapi.repositories.domain._
import uk.gov.hmrc.selfassessmentapi.repositories.{JsonItem, SourceRepository, SummaryRepository, TypedSourceSummaryRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object EmploymentRepository extends MongoDbConnection {
  private lazy val repository = new EmploymentMongoRepository()

  def apply() = repository
}

class EmploymentMongoRepository(implicit mongo: () => DB)
    extends ReactiveRepository[Employment, BSONObjectID]("employments",
                                                              mongo,
                                                              domainFormat = Employment.mongoFormats,
                                                              idFormat = ReactiveMongoFormats.objectIdFormats)
    with SourceRepository[employment.Employment]
    with AtomicUpdate[Employment]
    with TypedSourceSummaryRepository[Employment, BSONObjectID] {
  self =>

  override def indexes: Seq[Index] = Seq(
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending)), name = Some("employments_utr_taxyear"), unique = false),
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending), ("sourceId", Ascending)), name = Some("employments_utr_taxyear_sourceid"), unique = true),
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("incomes.summaryId", Ascending)), name = Some("employments_utr_taxyear_source_incomesid"), unique = true),
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("expenses.summaryId", Ascending)), name = Some("employments_utr_taxyear_source_expensesid"), unique = true),
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("benefits.summaryId", Ascending)), name = Some("employments_utr_taxyear_source_benefitsid"), unique = true),
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("ukTaxPaid.summaryId", Ascending)), name = Some("employments_utr_taxyear_source_uktaxpaidid"), unique = true),
    Index(Seq(("lastModifiedDateTime", Ascending)), name = Some("employments_last_modified"), unique = false))

  override def create(saUtr: SaUtr, taxYear: TaxYear, employment: api.employment.Employment): Future[SourceId] = {
    val mongoEmployment = Employment.create(saUtr, taxYear, employment)
    insert(mongoEmployment).map(_ => mongoEmployment.sourceId)
  }

  override def findById(saUtr: SaUtr, taxYear: TaxYear, id: SourceId): Future[Option[employment.Employment]] = {
    for (option <- findMongoObjectById(saUtr, taxYear, id)) yield option.map(_.toEmployment)
  }

  override def update(saUtr: SaUtr, taxYear: TaxYear, id: SourceId, employment: api.employment.Employment): Future[Boolean] = {
    val modifiers = BSONDocument(Seq(modifierStatementLastModified))
    for {
      result <- atomicUpdate(
                   BSONDocument("saUtr" -> BSONString(saUtr.toString),
                                "taxYear" -> BSONString(taxYear.toString),
                                "sourceId" -> BSONString(id)),
                   modifiers
               )
    } yield result.nonEmpty
  }

  override def list(saUtr: SaUtr, taxYear: TaxYear): Future[Seq[employment.Employment]] = {
    findAll(saUtr, taxYear).map(_.map(_.toEmployment))
  }

  def findAll(saUtr: SaUtr, taxYear: TaxYear): Future[Seq[Employment]] = {
    find("saUtr" -> saUtr.utr, "taxYear" -> taxYear.taxYear)
  }

  override def listAsJsonItem(saUtr: SaUtr, taxYear: TaxYear): Future[Seq[JsonItem]] = {
    list(saUtr, taxYear).map(_.map(employment => JsonItem(employment.id.get.toString, toJson(employment))))
  }

  object IncomeRepository extends SummaryRepository[Income] {
    override def create(saUtr: SaUtr,
                        taxYear: TaxYear,
                        sourceId: SourceId,
                        income: Income): Future[Option[SummaryId]] =
      self.createSummary(saUtr, taxYear, sourceId, EmploymentIncomeSummary.toMongoSummary(income))

    override def findById(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[Income]] =
      self.findSummaryById[Income](saUtr,
                                   taxYear,
                                   sourceId,
                                   mongoEmployment => mongoEmployment.incomes.find(_.summaryId == id).map(_.toIncome))

    override def update(saUtr: SaUtr,
                        taxYear: TaxYear,
                        sourceId: SourceId,
                        id: SummaryId,
                        income: Income): Future[Boolean] =
      self.updateSummary(saUtr,
                         taxYear,
                         sourceId,
                         EmploymentIncomeSummary.toMongoSummary(income, Some(id)),
                         mongoEmployment => mongoEmployment.incomes.exists(_.summaryId == id))

    override def delete(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(saUtr,
                         taxYear,
                         sourceId,
                         id,
                         EmploymentIncomeSummary.arrayName,
                         mongoEmployment => mongoEmployment.incomes.exists((_.summaryId == id)))

    override def list(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[Income]]] =
      self.listSummaries[Income](saUtr, taxYear, sourceId, mongoEmployment => mongoEmployment.incomes.map(_.toIncome))

    override def listAsJsonItem(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
      list(saUtr, taxYear, sourceId).map(_.getOrElse(Seq()).map(income =>
                JsonItem(income.id.get.toString, toJson(income))))
  }

  object ExpenseRepository extends SummaryRepository[Expense] {
    override def create(saUtr: SaUtr,
                        taxYear: TaxYear,
                        sourceId: SourceId,
                        expense: Expense): Future[Option[SummaryId]] =
      self.createSummary(saUtr, taxYear, sourceId, EmploymentExpenseSummary.toMongoSummary(expense))

    override def findById(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[Expense]] =
      self.findSummaryById[Expense](
          saUtr,
          taxYear,
          sourceId,
          mongoEmployment => mongoEmployment.expenses.find(_.summaryId == id).map(_.toExpense))

    override def update(saUtr: SaUtr,
                        taxYear: TaxYear,
                        sourceId: SourceId,
                        id: SummaryId,
                        expense: Expense): Future[Boolean] =
      self.updateSummary(saUtr,
                         taxYear,
                         sourceId,
                         EmploymentExpenseSummary.toMongoSummary(expense, Some(id)),
                         mongoEmployment => mongoEmployment.expenses.exists(_.summaryId == id))

    override def delete(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(saUtr,
                         taxYear,
                         sourceId,
                         id,
                         EmploymentExpenseSummary.arrayName,
                         mongoEmployment => mongoEmployment.expenses.exists(_.summaryId == id))

    override def list(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[Expense]]] =
      self
        .listSummaries[Expense](saUtr, taxYear, sourceId, mongoEmployment => mongoEmployment.expenses.map(_.toExpense))

    override def listAsJsonItem(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
      list(saUtr, taxYear, sourceId).map(_.getOrElse(Seq()).map(expense =>
                JsonItem(expense.id.get.toString, toJson(expense))))
  }

  object BenefitRepository extends SummaryRepository[Benefit] {
    override def create(saUtr: SaUtr,
                        taxYear: TaxYear,
                        sourceId: SourceId,
                        benefit: Benefit): Future[Option[SummaryId]] =
      self.createSummary(saUtr, taxYear, sourceId, EmploymentBenefitSummary.toMongoSummary(benefit))

    override def findById(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[Benefit]] =
      self.findSummaryById[Benefit](
          saUtr,
          taxYear,
          sourceId,
          mongoEmployment => mongoEmployment.benefits.find(_.summaryId == id).map(_.toBenefit))

    override def update(saUtr: SaUtr,
                        taxYear: TaxYear,
                        sourceId: SourceId,
                        id: SummaryId,
                        benefit: Benefit): Future[Boolean] =
      self.updateSummary(saUtr,
                         taxYear,
                         sourceId,
                         EmploymentBenefitSummary.toMongoSummary(benefit, Some(id)),
                         mongoEmployment => mongoEmployment.benefits.exists(_.summaryId == id))

    override def delete(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(saUtr,
                         taxYear,
                         sourceId,
                         id,
                         EmploymentBenefitSummary.arrayName,
                         mongoEmployment => mongoEmployment.benefits.exists(_.summaryId == id))

    override def list(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[Benefit]]] =
      self
        .listSummaries[Benefit](saUtr, taxYear, sourceId, mongoEmployment => mongoEmployment.benefits.map(_.toBenefit))

    override def listAsJsonItem(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
      list(saUtr, taxYear, sourceId).map(_.getOrElse(Seq()).map(benefit =>
                JsonItem(benefit.id.get.toString, toJson(benefit))))
  }

  object UkTaxPaidRepository extends SummaryRepository[UkTaxPaid] {
    override def create(saUtr: SaUtr,
                        taxYear: TaxYear,
                        sourceId: SourceId,
                        ukTaxPaid: UkTaxPaid): Future[Option[SummaryId]] =
      self.createSummary(saUtr, taxYear, sourceId, EmploymentUkTaxPaidSummary.toMongoSummary(ukTaxPaid))

    override def findById(saUtr: SaUtr,
                          taxYear: TaxYear,
                          sourceId: SourceId,
                          id: SummaryId): Future[Option[UkTaxPaid]] =
      self.findSummaryById[UkTaxPaid](
          saUtr,
          taxYear,
          sourceId,
          mongoEmployment => mongoEmployment.ukTaxPaid.find(_.summaryId == id).map(_.toUkTaxPaid))

    override def update(saUtr: SaUtr,
                        taxYear: TaxYear,
                        sourceId: SourceId,
                        id: SummaryId,
                        uKTaxPaid: UkTaxPaid): Future[Boolean] =
      self.updateSummary(saUtr,
                         taxYear,
                         sourceId,
                         EmploymentUkTaxPaidSummary.toMongoSummary(uKTaxPaid, Some(id)),
                         mongoEmployment => mongoEmployment.ukTaxPaid.exists(_.summaryId == id))

    override def delete(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(saUtr,
                         taxYear,
                         sourceId,
                         id,
                         EmploymentUkTaxPaidSummary.arrayName,
                         mongoEmployment => mongoEmployment.ukTaxPaid.exists(_.summaryId == id))

    override def list(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[UkTaxPaid]]] =
      self.listSummaries[UkTaxPaid](saUtr,
                                    taxYear,
                                    sourceId,
                                    mongoEmployment => mongoEmployment.ukTaxPaid.map(_.toUkTaxPaid))

    override def listAsJsonItem(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
      list(saUtr, taxYear, sourceId).map(_.getOrElse(Seq()).map(uKTaxPaid =>
                JsonItem(uKTaxPaid.id.get.toString, toJson(uKTaxPaid))))
  }

}
