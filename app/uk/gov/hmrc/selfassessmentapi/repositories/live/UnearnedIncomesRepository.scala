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

import play.api.libs.json.Json.toJson
import play.modules.reactivemongo.MongoDbConnection
import reactivemongo.api.DB
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.{BSONDocument, BSONObjectID, BSONString}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.mongo.{AtomicUpdate, ReactiveRepository}
import uk.gov.hmrc.selfassessmentapi.controllers.api.unearnedincome.{Benefit, Dividend, SavingsIncome, UnearnedIncome}
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceId, SummaryId, TaxYear}
import uk.gov.hmrc.selfassessmentapi.repositories._
import uk.gov.hmrc.selfassessmentapi.repositories.domain._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future



object UnearnedIncomeRepository extends MongoDbConnection {
  private lazy val repository = new UnearnedIncomeMongoRepository()
  def apply() = repository
}

class UnearnedIncomeMongoRepository(implicit mongo: () => DB)
  extends ReactiveRepository[MongoUnearnedIncome, BSONObjectID](
    "unearnedIncomes",
    mongo,
    domainFormat = MongoUnearnedIncome.mongoFormats,
    idFormat = ReactiveMongoFormats.objectIdFormats)
    with SourceRepository[UnearnedIncome] with AtomicUpdate[MongoUnearnedIncome] with TypedSourceSummaryRepository[MongoUnearnedIncome, BSONObjectID] {

  self =>

  override def indexes: Seq[Index] = Seq(
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending)), name = Some("ui_utr_taxyear"), unique = false),
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending), ("sourceId", Ascending)), name = Some("ui_utr_taxyear_sourceid"), unique = true),
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("savings.summaryId", Ascending)), name = Some("ui_utr_taxyear_source_savingsid"), unique = true),
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("dividends.summaryId", Ascending)), name = Some("ui_utr_taxyear_source_dividendsid"), unique = true),
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("benefits.summaryId", Ascending)), name = Some("ui_utr_taxyear_source_benefitsid"), unique = true),
    Index(Seq(("lastModifiedDateTime", Ascending)), name = Some("ui_last_modified"), unique = false))


  override def create(saUtr: SaUtr, taxYear: TaxYear, ui: UnearnedIncome): Future[SourceId] = {
    val mongoSe = MongoUnearnedIncome.create(saUtr, taxYear, ui)
    insert(mongoSe).map(_ => mongoSe.sourceId)
  }

  override def findById(saUtr: SaUtr, taxYear: TaxYear, id: SourceId): Future[Option[UnearnedIncome]] = {
    for(option <- findMongoObjectById(saUtr, taxYear, id)) yield option.map(_.toUnearnedIncome)
  }

  override def list(saUtr: SaUtr, taxYear: TaxYear): Future[Seq[UnearnedIncome]] = {
    for (list <- find("saUtr" -> saUtr.utr, "taxYear" -> taxYear.taxYear)) yield list.map(_.toUnearnedIncome)
  }

  def findAll(saUtr: SaUtr, taxYear: TaxYear): Future[Seq[MongoUnearnedIncome]] = {
    find("saUtr" -> saUtr.utr, "taxYear" -> taxYear.taxYear)
  }


  override def listAsJsonItem(saUtr: SaUtr, taxYear: TaxYear): Future[Seq[JsonItem]] =
    list(saUtr, taxYear).map(_.map(se => JsonItem(se.id.get.toString, toJson(se))))

  /*
    We need to perform updates manually as we are using one collection per source and it includes the arrays of summaries. This
    update is however partial so we should only update the fields provided and not override the summary arrays.
   */
  override def update(saUtr: SaUtr, taxYear: TaxYear, id: SourceId, unearnedIncome: UnearnedIncome): Future[Boolean] = {
    val modifiers = BSONDocument(Seq(modifierStatementLastModified))
    for {
      result <- atomicUpdate(
        BSONDocument("saUtr" -> BSONString(saUtr.toString), "taxYear" -> BSONString(taxYear.toString), "sourceId" -> BSONString(id)),
        modifiers
      )
    } yield result.nonEmpty
  }

  object SavingsIncomeRepository extends SummaryRepository[SavingsIncome] {
    override def create(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, income: SavingsIncome): Future[Option[SummaryId]] =
      self.createSummary(saUtr, taxYear, sourceId, MongoUnearnedIncomesSavingsIncomeSummary.toMongoSummary(income))

    override def findById(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[SavingsIncome]] =
      self.findSummaryById[SavingsIncome](saUtr, taxYear, sourceId, (se: MongoUnearnedIncome) => se.savings.find(_.summaryId == id).map(_.toSavingsIncome))

    override def update(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, income: SavingsIncome): Future[Boolean] =
      self.updateSummary(saUtr, taxYear, sourceId, MongoUnearnedIncomesSavingsIncomeSummary.toMongoSummary(income, Some(id)), (se: MongoUnearnedIncome) => se.savings.exists(_.summaryId == id))

    override def delete(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(saUtr, taxYear, sourceId, id, MongoUnearnedIncomesSavingsIncomeSummary.arrayName, (se: MongoUnearnedIncome) => se.savings.exists(_.summaryId == id))

    override def list(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[SavingsIncome]]] =
      self.listSummaries[SavingsIncome](saUtr, taxYear, sourceId, (se: MongoUnearnedIncome) => se.savings.map(_.toSavingsIncome))

    override def listAsJsonItem(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
      list(saUtr, taxYear,sourceId).map(_.getOrElse(Seq()).map(income => JsonItem(income.id.get.toString, toJson(income))))
  }

  object DividendRepository extends SummaryRepository[Dividend] {
    override def create(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, expense: Dividend): Future[Option[SummaryId]] =
      self.createSummary(saUtr, taxYear, sourceId, MongoUnearnedIncomesDividendSummary.toMongoSummary(expense))

    override def findById(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[Dividend]] =
      self.findSummaryById[Dividend](saUtr, taxYear, sourceId, (se: MongoUnearnedIncome) => se.dividends.find(_.summaryId == id).map(_.toDividend))

    override def update(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, expense: Dividend): Future[Boolean] =
      self.updateSummary(saUtr, taxYear, sourceId, MongoUnearnedIncomesDividendSummary.toMongoSummary(expense, Some(id)), (se: MongoUnearnedIncome) => se.dividends.exists(_.summaryId == id))

    override def delete(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(saUtr, taxYear, sourceId, id, MongoUnearnedIncomesDividendSummary.arrayName, (se: MongoUnearnedIncome) => se.dividends.exists(_.summaryId == id))

    override def list(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[Dividend]]] =
      self.listSummaries[Dividend](saUtr, taxYear, sourceId, (se: MongoUnearnedIncome) => se.dividends.map(_.toDividend))

    override def listAsJsonItem(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
      list(saUtr, taxYear,sourceId).map(_.getOrElse(Seq()).map(expense => JsonItem(expense.id.get.toString, toJson(expense))))
  }

  object BenefitRepository extends SummaryRepository[Benefit] {
    override def create(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, benefit: Benefit): Future[Option[SummaryId]] =
      self.createSummary(saUtr, taxYear, sourceId, MongoUnearnedIncomesBenefitSummary.toMongoSummary(benefit))

    override def findById(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[Benefit]] =
      self.findSummaryById[Benefit](saUtr, taxYear, sourceId, (se: MongoUnearnedIncome) => se.benefits.find(_.summaryId == id).map(_.toBenefit))

    override def update(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, benefit: Benefit): Future[Boolean] =
      self.updateSummary(saUtr, taxYear, sourceId, MongoUnearnedIncomesBenefitSummary.toMongoSummary(benefit, Some(id)), (se: MongoUnearnedIncome) => se.benefits.exists(_.summaryId == id))

    override def delete(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(saUtr, taxYear, sourceId, id, MongoUnearnedIncomesBenefitSummary.arrayName, (se: MongoUnearnedIncome) => se.benefits.exists(_.summaryId == id))

    override def list(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[Benefit]]] =
      self.listSummaries[Benefit](saUtr, taxYear, sourceId, (se: MongoUnearnedIncome) => se.benefits.map(_.toBenefit))

    override def listAsJsonItem(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
      list(saUtr, taxYear,sourceId).map(_.getOrElse(Seq()).map(benefit => JsonItem(benefit.id.get.toString, toJson(benefit))))
  }

}
