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
import reactivemongo.bson.{BSONDocument, BSONObjectID, BSONString}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.mongo.{AtomicUpdate, ReactiveRepository}
import uk.gov.hmrc.selfassessmentapi.domain.employment._
import uk.gov.hmrc.selfassessmentapi.domain.{SourceId, SummaryId, TaxYear}
import uk.gov.hmrc.selfassessmentapi.repositories.domain._
import uk.gov.hmrc.selfassessmentapi.repositories.{JsonItem, SourceRepository, SummaryRepository, TypedSourceSummaryRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object EmploymentRepository extends MongoDbConnection {
  private lazy val repository = new EmploymentMongoRepository()

  def apply() = repository
}

class EmploymentMongoRepository(implicit mongo: () => DB)
    extends ReactiveRepository[MongoEmployment, BSONObjectID]("employments",
                                                              mongo,
                                                              domainFormat = MongoEmployment.mongoFormats,
                                                              idFormat = ReactiveMongoFormats.objectIdFormats)
    with SourceRepository[Employment]
    with AtomicUpdate[MongoEmployment]
    with TypedSourceSummaryRepository[MongoEmployment, BSONObjectID] {
  self =>
  override def create(saUtr: SaUtr, taxYear: TaxYear, employment: Employment): Future[SourceId] = {
    val mongoEmployment = MongoEmployment.create(saUtr, taxYear, employment)
    insert(mongoEmployment).map(_ => mongoEmployment.sourceId)
  }

  override def findById(saUtr: SaUtr, taxYear: TaxYear, id: SourceId): Future[Option[Employment]] = {
    for (option <- findMongoObjectById(saUtr, taxYear, id)) yield option.map(_.toEmployment)
  }

  override def update(saUtr: SaUtr, taxYear: TaxYear, id: SourceId, employment: Employment): Future[Boolean] = {
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

  override def list(saUtr: SaUtr, taxYear: TaxYear): Future[Seq[Employment]] = {
    findAll(saUtr, taxYear).map(_.map(_.toEmployment))
  }

  def findAll(saUtr: SaUtr, taxYear: TaxYear): Future[Seq[MongoEmployment]] = {
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
      self.createSummary(saUtr, taxYear, sourceId, MongoEmploymentIncomeSummary.toMongoSummary(income))

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
                         MongoEmploymentIncomeSummary.toMongoSummary(income, Some(id)),
                         mongoEmployment => mongoEmployment.incomes.exists(_.summaryId == id))

    override def delete(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(saUtr,
                         taxYear,
                         sourceId,
                         id,
                         MongoEmploymentIncomeSummary.arrayName,
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
      self.createSummary(saUtr, taxYear, sourceId, MongoEmploymentExpenseSummary.toMongoSummary(expense))

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
                         MongoEmploymentExpenseSummary.toMongoSummary(expense, Some(id)),
                         mongoEmployment => mongoEmployment.expenses.exists(_.summaryId == id))

    override def delete(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(saUtr,
                         taxYear,
                         sourceId,
                         id,
                         MongoEmploymentExpenseSummary.arrayName,
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
      self.createSummary(saUtr, taxYear, sourceId, MongoEmploymentBenefitSummary.toMongoSummary(benefit))

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
                         MongoEmploymentBenefitSummary.toMongoSummary(benefit, Some(id)),
                         mongoEmployment => mongoEmployment.benefits.exists(_.summaryId == id))

    override def delete(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(saUtr,
                         taxYear,
                         sourceId,
                         id,
                         MongoEmploymentBenefitSummary.arrayName,
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
      self.createSummary(saUtr, taxYear, sourceId, MongoEmploymentUkTaxPaidSummary.toMongoSummary(ukTaxPaid))

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
                         MongoEmploymentUkTaxPaidSummary.toMongoSummary(uKTaxPaid, Some(id)),
                         mongoEmployment => mongoEmployment.ukTaxPaid.exists(_.summaryId == id))

    override def delete(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(saUtr,
                         taxYear,
                         sourceId,
                         id,
                         MongoEmploymentUkTaxPaidSummary.arrayName,
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
