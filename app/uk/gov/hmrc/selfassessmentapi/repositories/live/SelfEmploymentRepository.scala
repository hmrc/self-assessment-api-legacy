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

import org.joda.time.DateTimeZone
import play.api.libs.json.Json.toJson
import play.modules.reactivemongo.MongoDbConnection
import reactivemongo.api.DB
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONDouble, BSONNull, BSONObjectID, BSONString}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.mongo.{AtomicUpdate, ReactiveRepository}
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment._
import uk.gov.hmrc.selfassessmentapi.domain.{SourceId, SummaryId, TaxYear}
import uk.gov.hmrc.selfassessmentapi.repositories.domain._
import uk.gov.hmrc.selfassessmentapi.repositories._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{MongoSelfEmployment, MongoSelfEmploymentExpenseSummary, MongoSelfEmploymentIncomeSummary}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SelfEmploymentRepository extends MongoDbConnection {
  private lazy val repository = new SelfEmploymentMongoRepository()

  def apply() = repository
}

class SelfEmploymentMongoRepository(implicit mongo: () => DB)
  extends ReactiveRepository[MongoSelfEmployment, BSONObjectID](
    "selfEmployments",
    mongo,
    domainFormat = MongoSelfEmployment.mongoFormats,
    idFormat = ReactiveMongoFormats.objectIdFormats)
    with SourceRepository[SelfEmployment] with AtomicUpdate[MongoSelfEmployment] with TypedSourceSummaryRepository[MongoSelfEmployment, BSONObjectID]{

  self =>

  override def indexes: Seq[Index] = Seq(
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending)), name = Some("se_utr_taxyear"), unique = false),
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending), ("sourceId", Ascending)), name = Some("se_utr_taxyear_sourceid"), unique = true),
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("incomes.summaryId", Ascending)), name = Some("se_utr_taxyear_source_incomesid"), unique = true),
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("expenses.summaryId", Ascending)), name = Some("se_utr_taxyear_source_expensesid"), unique = true),
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("balancingCharges.summaryId", Ascending)), name = Some("se_utr_taxyear_source_balancingchargesid"), unique = true),
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("goodsAndServicesOwnUse.summaryId", Ascending)), name = Some("se_utr_taxyear_source_goodsandservicesownuseid"), unique = true),
    Index(Seq(("lastModifiedDateTime", Ascending)), name = Some("se_last_modified"), unique = false))


  override def create(saUtr: SaUtr, taxYear: TaxYear, se: SelfEmployment): Future[SourceId] = {
    val mongoSe = MongoSelfEmployment.create(saUtr, taxYear, se)
    insert(mongoSe).map(_ => mongoSe.sourceId)
  }

  override def findById(saUtr: SaUtr, taxYear: TaxYear, id: SourceId): Future[Option[SelfEmployment]] = {
    for(option <- findMongoObjectById(saUtr, taxYear, id)) yield option.map(_.toSelfEmployment)
  }

  override def list(saUtr: SaUtr, taxYear: TaxYear): Future[Seq[SelfEmployment]] = {
    findAll(saUtr, taxYear).map(_.map(_.toSelfEmployment))
  }

  override def listAsJsonItem(saUtr: SaUtr, taxYear: TaxYear): Future[Seq[JsonItem]] = {
    list(saUtr, taxYear).map(_.map(se => JsonItem(se.id.get.toString, toJson(se))))
  }

  def findAll(saUtr: SaUtr, taxYear: TaxYear): Future[Seq[MongoSelfEmployment]] = {
    find("saUtr" -> saUtr.utr, "taxYear" -> taxYear.taxYear)
  }

  /*
    We need to perform updates manually as we are using one collection per source and it includes the arrays of summaries. This
    update is however partial so we should only update the fields provided and not override the summary arrays.
   */
  override def update(saUtr: SaUtr, taxYear: TaxYear, id: SourceId, selfEmployment: SelfEmployment): Future[Boolean] = {
    val baseModifiers = Seq(
      "$set" -> BSONDocument("commencementDate" -> BSONDateTime(selfEmployment.commencementDate.toDateTimeAtStartOfDay(DateTimeZone.UTC).getMillis)),
      modifierStatementLastModified
    )

    val allowancesModifiers = selfEmployment.allowances.map(allowances =>
      Seq(
        "$set" -> BSONDocument("allowances" -> BSONDocument(Seq(
          "annualInvestmentAllowance" -> allowances.annualInvestmentAllowance.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
          "capitalAllowanceMainPool" -> allowances.capitalAllowanceMainPool.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
          "capitalAllowanceSpecialRatePool" -> allowances.capitalAllowanceSpecialRatePool.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
          "businessPremisesRenovationAllowance" -> allowances.businessPremisesRenovationAllowance.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
          "enhancedCapitalAllowance" -> allowances.enhancedCapitalAllowance.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
          "allowancesOnSales" -> allowances.allowancesOnSales.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull)
        )))
      )
    ).getOrElse(Seq("$set" -> BSONDocument("allowances" -> BSONNull)))

    val adjustmentsModifiers = selfEmployment.adjustments.map(adjustments =>
      Seq(
        "$set" -> BSONDocument("adjustments" -> BSONDocument(Seq(
          "accountingAdjustment" -> adjustments.accountingAdjustment.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
          "averagingAdjustment" -> adjustments.averagingAdjustment.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
          "basisAdjustment" -> adjustments.basisAdjustment.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
          "includedNonTaxableProfits" -> adjustments.includedNonTaxableProfits.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
          "lossBroughtForward" -> adjustments.lossBroughtForward.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
          "outstandingBusinessIncome" -> adjustments.outstandingBusinessIncome.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
          "overlapReliefUsed" -> adjustments.overlapReliefUsed.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull)
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
      self.createSummary(saUtr, taxYear, sourceId, MongoSelfEmploymentIncomeSummary.toMongoSummary(income))

    override def findById(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[Income]] =
      self.findSummaryById[Income](saUtr, taxYear, sourceId, (se: MongoSelfEmployment) => se.incomes.find(_.summaryId == id).map(_.toIncome))

    override def update(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, income: Income): Future[Boolean] =
      self.updateSummary(saUtr, taxYear, sourceId, MongoSelfEmploymentIncomeSummary.toMongoSummary(income, Some(id)), (se: MongoSelfEmployment) => se.incomes.exists(_.summaryId == id))

    override def delete(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(saUtr, taxYear, sourceId, id, MongoSelfEmploymentIncomeSummary.arrayName, (se: MongoSelfEmployment) => se.incomes.exists(_.summaryId == id))

    override def list(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[Income]]] =
      self.listSummaries[Income](saUtr, taxYear, sourceId, (se: MongoSelfEmployment) => se.incomes.map(_.toIncome))

    override def listAsJsonItem(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
      list(saUtr, taxYear,sourceId).map(_.getOrElse(Seq()).map(income => JsonItem(income.id.get.toString, toJson(income))))
  }

  object ExpenseRepository extends SummaryRepository[Expense] {
    override def create(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, expense: Expense): Future[Option[SummaryId]] =
      self.createSummary(saUtr, taxYear, sourceId, MongoSelfEmploymentExpenseSummary.toMongoSummary(expense))

    override def findById(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[Expense]] =
      self.findSummaryById[Expense](saUtr, taxYear, sourceId, (se: MongoSelfEmployment) => se.expenses.find(_.summaryId == id).map(_.toExpense))

    override def update(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, expense: Expense): Future[Boolean] =
      self.updateSummary(saUtr, taxYear, sourceId, MongoSelfEmploymentExpenseSummary.toMongoSummary(expense, Some(id)), (se: MongoSelfEmployment) => se.expenses.exists(_.summaryId == id))

    override def delete(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(saUtr, taxYear, sourceId, id, MongoSelfEmploymentExpenseSummary.arrayName, (se: MongoSelfEmployment) => se.expenses.exists(_.summaryId == id))

    override def list(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[Expense]]] =
      self.listSummaries[Expense](saUtr, taxYear, sourceId, (se: MongoSelfEmployment) => se.expenses.map(_.toExpense))

    override def listAsJsonItem(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
      list(saUtr, taxYear,sourceId).map(_.getOrElse(Seq()).map(expense => JsonItem(expense.id.get.toString, toJson(expense))))
  }

  object BalancingChargeRepository extends SummaryRepository[BalancingCharge] {
    override def create(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, balancingCharge: BalancingCharge): Future[Option[SummaryId]] =
      self.createSummary(saUtr, taxYear, sourceId, MongoSelfEmploymentBalancingChargeSummary.toMongoSummary(balancingCharge))

    override def findById(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[BalancingCharge]] =
      self.findSummaryById[BalancingCharge](saUtr, taxYear, sourceId, (se: MongoSelfEmployment) => se.balancingCharges.find(_.summaryId == id).map(_.toBalancingCharge))

    override def update(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, balancingCharge: BalancingCharge): Future[Boolean] =
      self.updateSummary(saUtr, taxYear, sourceId, MongoSelfEmploymentBalancingChargeSummary.toMongoSummary(balancingCharge, Some(id)),
        (se: MongoSelfEmployment) => se.balancingCharges.exists(_.summaryId == id))

    override def delete(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(saUtr, taxYear, sourceId, id, MongoSelfEmploymentBalancingChargeSummary.arrayName, (se: MongoSelfEmployment) => se.balancingCharges.exists(_.summaryId == id))

    override def list(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[BalancingCharge]]] =
      self.listSummaries[BalancingCharge](saUtr, taxYear, sourceId, (se: MongoSelfEmployment) => se.balancingCharges.map(_.toBalancingCharge))

    override def listAsJsonItem(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
      list(saUtr, taxYear,sourceId).map(_.getOrElse(Seq()).map(balancingCharge => JsonItem(balancingCharge.id.get.toString, toJson(balancingCharge))))
  }

  object GoodsAndServicesOwnUseRepository extends SummaryRepository[GoodsAndServicesOwnUse] {
    override def create(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, goodsAndServicesOwnUse: GoodsAndServicesOwnUse): Future[Option[SummaryId]] =
      self.createSummary(saUtr, taxYear, sourceId, MongoSelfEmploymentGoodsAndServicesOwnUseSummary.toMongoSummary(goodsAndServicesOwnUse))

    override def findById(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[GoodsAndServicesOwnUse]] =
      self.findSummaryById[GoodsAndServicesOwnUse](saUtr, taxYear, sourceId, (se: MongoSelfEmployment) => se.goodsAndServicesOwnUse.find(_.summaryId == id).map(_.toGoodsAndServicesOwnUse))

    override def update(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, goodsAndServicesOwnUse: GoodsAndServicesOwnUse): Future[Boolean] =
      self.updateSummary(saUtr, taxYear, sourceId, MongoSelfEmploymentGoodsAndServicesOwnUseSummary.toMongoSummary(goodsAndServicesOwnUse, Some(id)),
        (se: MongoSelfEmployment) => se.goodsAndServicesOwnUse.exists(_.summaryId == id))

    override def delete(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(saUtr, taxYear, sourceId, id, MongoSelfEmploymentGoodsAndServicesOwnUseSummary.arrayName, (se: MongoSelfEmployment) => se.goodsAndServicesOwnUse.exists(_.summaryId == id))

    override def list(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[GoodsAndServicesOwnUse]]] =
      self.listSummaries[GoodsAndServicesOwnUse](saUtr, taxYear, sourceId, (se: MongoSelfEmployment) => se.goodsAndServicesOwnUse.map(_.toGoodsAndServicesOwnUse))

    override def listAsJsonItem(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
      list(saUtr, taxYear,sourceId).map(_.getOrElse(Seq()).map(goodsAndServicesOwnUse => JsonItem(goodsAndServicesOwnUse.id.get.toString, toJson(goodsAndServicesOwnUse))))
  }


}
