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
import uk.gov.hmrc.selfassessmentapi.controllers.api.dividend.{Dividend, DividendIncome}
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceId, SummaryId, TaxYear}
import uk.gov.hmrc.selfassessmentapi.repositories._
import uk.gov.hmrc.selfassessmentapi.repositories.domain._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future



object DividendRepository extends MongoDbConnection {
  private lazy val repository = new DividendMongoRepository()
  def apply() = repository
}

class DividendMongoRepository(implicit mongo: () => DB)
  extends ReactiveRepository[MongoDividend, BSONObjectID](
    "dividends", mongo,
    domainFormat = MongoDividend.mongoFormats,
    idFormat = ReactiveMongoFormats.objectIdFormats)
    with SourceRepository[Dividend] with AtomicUpdate[MongoDividend] with TypedSourceSummaryRepository[MongoDividend, BSONObjectID] {

  self =>

  override def indexes: Seq[Index] = Seq(
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending)), name = Some("div_utr_taxyear"), unique = false),
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending), ("sourceId", Ascending)), name = Some("div_utr_taxyear_sourceid"), unique = true),
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("dividends.summaryId", Ascending)), name = Some("div_utr_taxyear_source_dividendsid"), unique = true),
    Index(Seq(("lastModifiedDateTime", Ascending)), name = Some("div_last_modified"), unique = false))


  override def create(saUtr: SaUtr, taxYear: TaxYear, ui: Dividend): Future[SourceId] = {
    val mongoSe = MongoDividend.create(saUtr, taxYear, ui)
    insert(mongoSe).map(_ => mongoSe.sourceId)
  }

  override def findById(saUtr: SaUtr, taxYear: TaxYear, id: SourceId): Future[Option[Dividend]] = {
    for(option <- findMongoObjectById(saUtr, taxYear, id)) yield option.map(_.toDividend)
  }

  override def list(saUtr: SaUtr, taxYear: TaxYear): Future[Seq[Dividend]] = {
    for (list <- find("saUtr" -> saUtr.utr, "taxYear" -> taxYear.taxYear)) yield list.map(_.toDividend)
  }

  def findAll(saUtr: SaUtr, taxYear: TaxYear): Future[Seq[MongoDividend]] = {
    find("saUtr" -> saUtr.utr, "taxYear" -> taxYear.taxYear)
  }

  override def listAsJsonItem(saUtr: SaUtr, taxYear: TaxYear): Future[Seq[JsonItem]] =
    list(saUtr, taxYear).map(_.map(dividend => JsonItem(dividend.id.get.toString, toJson(dividend))))

  /*
    We need to perform updates manually as we are using one collection per source and it includes the arrays of summaries. This
    update is however partial so we should only update the fields provided and not override the summary arrays.
   */
  override def update(saUtr: SaUtr, taxYear: TaxYear, id: SourceId, unearnedIncome: Dividend): Future[Boolean] = {
    val modifiers = BSONDocument(Seq(modifierStatementLastModified))
    for {
      result <- atomicUpdate(
        BSONDocument("saUtr" -> BSONString(saUtr.toString), "taxYear" -> BSONString(taxYear.toString), "sourceId" -> BSONString(id)),
        modifiers
      )
    } yield result.nonEmpty
  }

  object DividendIncomeRepository extends SummaryRepository[DividendIncome] {
    override def create(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, income: DividendIncome): Future[Option[SummaryId]] =
      self.createSummary(saUtr, taxYear, sourceId, DividendIncomeSummary.toMongoSummary(income))

    override def findById(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[DividendIncome]] =
      self.findSummaryById[DividendIncome](saUtr, taxYear, sourceId, (md: MongoDividend) => md.incomes.find(_.summaryId == id).map(_.toDividendIncome))

    override def update(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, income: DividendIncome): Future[Boolean] =
      self.updateSummary(saUtr, taxYear, sourceId, DividendIncomeSummary.toMongoSummary(income, Some(id)), (md: MongoDividend) => md.incomes.exists(_.summaryId == id))

    override def delete(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(saUtr, taxYear, sourceId, id, DividendIncomeSummary.arrayName, (md: MongoDividend) => md.incomes.exists(_.summaryId == id))

    override def list(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[DividendIncome]]] =
      self.listSummaries[DividendIncome](saUtr, taxYear, sourceId, (md: MongoDividend) => md.incomes.map(_.toDividendIncome))

    override def listAsJsonItem(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
      list(saUtr, taxYear,sourceId).map(_.getOrElse(Seq()).map(item => JsonItem(item.id.get.toString, toJson(item))))
  }
}
