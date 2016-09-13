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
import reactivemongo.bson.{BSONDocument, BSONDouble, BSONNull, BSONObjectID, BSONString}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.mongo.{AtomicUpdate, ReactiveRepository}
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings._
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceId, SummaryId, TaxYear}
import uk.gov.hmrc.selfassessmentapi.repositories._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{FurnishedHolidayLettings, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object FurnishedHolidayLettingsRepository extends MongoDbConnection {
  private lazy val repository = new FurnishedHolidayLettingsMongoRepository()

  def apply() = repository
}

class FurnishedHolidayLettingsMongoRepository(implicit mongo: () => DB)
  extends ReactiveRepository[FurnishedHolidayLettings, BSONObjectID](
    "furnishedHolidayLettings",
    mongo,
    domainFormat = FurnishedHolidayLettings.mongoFormats,
    idFormat = ReactiveMongoFormats.objectIdFormats)
    with SourceRepository[FurnishedHolidayLetting] with AtomicUpdate[FurnishedHolidayLettings] with TypedSourceSummaryRepository[FurnishedHolidayLettings, BSONObjectID] {

  self =>

  override def indexes: Seq[Index] = Seq(
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending)), name = Some("fhl_utr_taxyear"), unique = false),
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending), ("sourceId", Ascending)), name = Some("fhl_utr_taxyear_sourceid"), unique = true),
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("incomes.summaryId", Ascending)), name = Some("fhl_utr_taxyear_source_incomesid"), unique = true),
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("expenses.summaryId", Ascending)), name = Some("fhl_utr_taxyear_source_expensesid"), unique = true),
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("balancingCharges.summaryId", Ascending)), name = Some("fhl_utr_taxyear_source_balancingchargesid"), unique = true),
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("privateUseAdjustment.summaryId", Ascending)), name = Some("fhl_utr_taxyear_source_privateuseadjustmentid"), unique = true),
    Index(Seq(("lastModifiedDateTime", Ascending)), name = Some("fhl_last_modified"), unique = false))


  override def create(saUtr: SaUtr, taxYear: TaxYear, se: FurnishedHolidayLetting): Future[SourceId] = {
    val mongoSe = FurnishedHolidayLettings.create(saUtr, taxYear, se)
    insert(mongoSe).map(_ => mongoSe.sourceId)
  }

  override def findById(saUtr: SaUtr, taxYear: TaxYear, id: SourceId): Future[Option[FurnishedHolidayLetting]] = {
    for (option <- findMongoObjectById(saUtr, taxYear, id)) yield option.map(_.toFurnishedHolidayLettings)
  }

  override def list(saUtr: SaUtr, taxYear: TaxYear): Future[Seq[FurnishedHolidayLetting]] = {
    findAll(saUtr, taxYear).map(_.map(_.toFurnishedHolidayLettings))
  }

  override def listAsJsonItem(saUtr: SaUtr, taxYear: TaxYear): Future[Seq[JsonItem]] = {
    list(saUtr, taxYear).map(_.map(se => JsonItem(se.id.get.toString, toJson(se))))
  }

  def findAll(saUtr: SaUtr, taxYear: TaxYear): Future[Seq[FurnishedHolidayLettings]] = {
    find("saUtr" -> saUtr.utr, "taxYear" -> taxYear.taxYear)
  }

  /*
    We need to perform updates manually as we are using one collection per source and it includes the arrays of summaries. This
    update is however partial so we should only update the fields provided and not override the summary arrays.
   */
  override def update(saUtr: SaUtr, taxYear: TaxYear, id: SourceId, furnishedHolidayLetting: FurnishedHolidayLetting): Future[Boolean] = {
    val baseModifiers = Seq(
      "$set" -> BSONDocument("propertyLocation" -> BSONString(furnishedHolidayLetting.propertyLocation.toString)),
      modifierStatementLastModified
    )

    val allowancesModifiers = furnishedHolidayLetting.allowances.map(allowances =>
      Seq(
        "$set" -> BSONDocument("allowances" -> BSONDocument(Seq(
          "capitalAllowance" -> allowances.capitalAllowance.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull)
        )))
      )
    ).getOrElse(Seq("$set" -> BSONDocument("allowances" -> BSONNull)))

    val adjustmentsModifiers = furnishedHolidayLetting.adjustments.map(adjustments =>
      Seq(
        "$set" -> BSONDocument("adjustments" -> BSONDocument(Seq(
          "lossBroughtForward" -> adjustments.lossBroughtForward.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull)
        )))
      )
    ).getOrElse(Seq("$set" -> BSONDocument("adjustments" -> BSONNull)))

    val modifiers = BSONDocument(baseModifiers ++ allowancesModifiers ++ adjustmentsModifiers)

    for {
      result <- atomicUpdate(
        BSONDocument("saUtr" -> BSONString(saUtr.toString), "taxYear" -> BSONString(taxYear.toString), "sourceId" -> BSONString(id)),
        modifiers
      )
    } yield result.nonEmpty
  }

  object IncomeRepository extends SummaryRepository[Income] {
    override def create(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, income: Income): Future[Option[SummaryId]] =
      self.createSummary(saUtr, taxYear, sourceId, FurnishedHolidayLettingsIncomeSummary.toMongoSummary(income))

    override def findById(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[Income]] =
      self.findSummaryById[Income](saUtr, taxYear, sourceId, (se: FurnishedHolidayLettings) => se.incomes.find(_.summaryId == id).map(_.toIncome))

    override def update(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, income: Income): Future[Boolean] =
      self.updateSummary(saUtr, taxYear, sourceId, FurnishedHolidayLettingsIncomeSummary.toMongoSummary(income, Some(id)), (se: FurnishedHolidayLettings) => se.incomes.exists(_.summaryId == id))

    override def delete(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(saUtr, taxYear, sourceId, id, FurnishedHolidayLettingsIncomeSummary.arrayName, (se: FurnishedHolidayLettings) => se.incomes.exists(_.summaryId == id))

    override def list(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[Income]]] =
      self.listSummaries[Income](saUtr, taxYear, sourceId, (se: FurnishedHolidayLettings) => se.incomes.map(_.toIncome))

    override def listAsJsonItem(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
      list(saUtr, taxYear, sourceId).map(_.getOrElse(Seq()).map(income => JsonItem(income.id.get.toString, toJson(income))))
  }

  object ExpenseRepository extends SummaryRepository[Expense] {
    override def create(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, expense: Expense): Future[Option[SummaryId]] =
      self.createSummary(saUtr, taxYear, sourceId, FurnishedHolidayLettingsExpenseSummary.toMongoSummary(expense))

    override def findById(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[Expense]] =
      self.findSummaryById[Expense](saUtr, taxYear, sourceId, (se: FurnishedHolidayLettings) => se.expenses.find(_.summaryId == id).map(_.toExpense))

    override def update(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, expense: Expense): Future[Boolean] =
      self.updateSummary(saUtr, taxYear, sourceId, FurnishedHolidayLettingsExpenseSummary.toMongoSummary(expense, Some(id)), (se: FurnishedHolidayLettings) => se.expenses.exists(_.summaryId == id))

    override def delete(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(saUtr, taxYear, sourceId, id, FurnishedHolidayLettingsExpenseSummary.arrayName, (se: FurnishedHolidayLettings) => se.expenses.exists(_.summaryId == id))

    override def list(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[Expense]]] =
      self.listSummaries[Expense](saUtr, taxYear, sourceId, (se: FurnishedHolidayLettings) => se.expenses.map(_.toExpense))

    override def listAsJsonItem(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
      list(saUtr, taxYear, sourceId).map(_.getOrElse(Seq()).map(expense => JsonItem(expense.id.get.toString, toJson(expense))))
  }

  object BalancingChargeRepository extends SummaryRepository[BalancingCharge] {
    override def create(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, balancingCharge: BalancingCharge): Future[Option[SummaryId]] =
      self.createSummary(saUtr, taxYear, sourceId, FurnishedHolidayLettingsBalancingChargeSummary.toMongoSummary(balancingCharge))

    override def findById(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[BalancingCharge]] =
      self.findSummaryById[BalancingCharge](saUtr, taxYear, sourceId, (se: FurnishedHolidayLettings) => se.balancingCharges.find(_.summaryId == id).map(_.toBalancingCharge))

    override def update(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, balancingCharge: BalancingCharge): Future[Boolean] =
      self.updateSummary(saUtr, taxYear, sourceId, FurnishedHolidayLettingsBalancingChargeSummary.toMongoSummary(balancingCharge, Some(id)),
        (se: FurnishedHolidayLettings) => se.balancingCharges.exists(_.summaryId == id))

    override def delete(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(saUtr, taxYear, sourceId, id, FurnishedHolidayLettingsBalancingChargeSummary.arrayName, (se: FurnishedHolidayLettings) => se.balancingCharges.exists(_.summaryId == id))

    override def list(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[BalancingCharge]]] =
      self.listSummaries[BalancingCharge](saUtr, taxYear, sourceId, (se: FurnishedHolidayLettings) => se.balancingCharges.map(_.toBalancingCharge))

    override def listAsJsonItem(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
      list(saUtr, taxYear, sourceId).map(_.getOrElse(Seq()).map(balancingCharge => JsonItem(balancingCharge.id.get.toString, toJson(balancingCharge))))
  }

  object PrivateUseAdjustmentRepository extends SummaryRepository[PrivateUseAdjustment] {
    override def create(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, goodsAndServicesOwnUse: PrivateUseAdjustment): Future[Option[SummaryId]] =
      self.createSummary(saUtr, taxYear, sourceId, FurnishedHolidayLettingsPrivateUseAdjustmentSummary.toMongoSummary(goodsAndServicesOwnUse))

    override def findById(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[PrivateUseAdjustment]] =
      self.findSummaryById[PrivateUseAdjustment](saUtr, taxYear, sourceId, (se: FurnishedHolidayLettings) => se.privateUseAdjustment.find(_.summaryId == id).map(_.toPrivateUseAdjustment))

    override def update(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, goodsAndServicesOwnUse: PrivateUseAdjustment): Future[Boolean] =
      self.updateSummary(saUtr, taxYear, sourceId, FurnishedHolidayLettingsPrivateUseAdjustmentSummary.toMongoSummary(goodsAndServicesOwnUse, Some(id)),
        (se: FurnishedHolidayLettings) => se.privateUseAdjustment.exists(_.summaryId == id))

    override def delete(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(saUtr, taxYear, sourceId, id, FurnishedHolidayLettingsPrivateUseAdjustmentSummary.arrayName, (se: FurnishedHolidayLettings) => se.privateUseAdjustment.exists(_.summaryId == id))

    override def list(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[PrivateUseAdjustment]]] =
      self.listSummaries[PrivateUseAdjustment](saUtr, taxYear, sourceId, (se: FurnishedHolidayLettings) => se.privateUseAdjustment.map(_.toPrivateUseAdjustment))

    override def listAsJsonItem(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
      list(saUtr, taxYear, sourceId).map(_.getOrElse(Seq()).map(privateUseAdjustment => JsonItem(privateUseAdjustment.id.get.toString, toJson(privateUseAdjustment))))
  }

}