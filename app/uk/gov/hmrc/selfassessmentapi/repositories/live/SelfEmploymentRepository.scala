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
import play.modules.reactivemongo.MongoDbConnection
import reactivemongo.api.DB
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONDouble, BSONNull, BSONObjectID, BSONString}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.mongo.{AtomicUpdate, ReactiveRepository}
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.{Income, SelfEmployment}
import uk.gov.hmrc.selfassessmentapi.domain.{SourceId, SummaryId, TaxYear}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{MongoSelfEmployment, MongoSelfEmploymentIncomeSummary}
import uk.gov.hmrc.selfassessmentapi.repositories.{JsonItem, SourceRepository, TypedSourceSummaryRepository}
import play.api.libs.json.Json.toJson

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait SelfEmploymentIncomesRepository {
  def createIncome(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, income: Income): Future[Option[SummaryId]]

  def findIncomeById(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[Income]]

  def updateIncome(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, income: Income): Future[Boolean]

  def deleteIncome(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean]

  def listIncomes(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[Income]]]
}


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
    with SourceRepository[SelfEmployment] with SelfEmploymentIncomesRepository with AtomicUpdate[MongoSelfEmployment] with TypedSourceSummaryRepository[MongoSelfEmployment, BSONObjectID]{

  override def indexes: Seq[Index] = Seq(
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending)), name = Some("se_utr_taxyear"), unique = false),
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending), ("sourceId", Ascending)), name = Some("se_utr_taxyear_sourceid"), unique = true),
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("incomes.summaryId", Ascending)), name = Some("se_utr_taxyear_source_incomesid"), unique = true),
    Index(Seq(("lastModifiedDateTime", Ascending)), name = Some("se_last_modified"), unique = false))


  override def create(saUtr: SaUtr, taxYear: TaxYear, se: SelfEmployment): Future[SourceId] = {
    val mongoSe = MongoSelfEmployment.create(saUtr, taxYear, se)
    insert(mongoSe).map(_ => mongoSe.sourceId)
  }

  override def findById(saUtr: SaUtr, taxYear: TaxYear, id: SourceId): Future[Option[SelfEmployment]] = {
    for(option <- findMongoObjectById(saUtr, taxYear, id)) yield option.map(_.toSelfEmployment)
  }

  override def list(saUtr: SaUtr, taxYear: TaxYear): Future[Seq[SelfEmployment]] = {
    for (list <- find("saUtr" -> saUtr.utr, "taxYear" -> taxYear.taxYear)) yield list.map(_.toSelfEmployment)
  }

  override def listAsJsonItem(saUtr: SaUtr, taxYear: TaxYear): Future[Seq[JsonItem]] =
    list(saUtr, taxYear).map(_.map(se => JsonItem(se.id.get.toString, toJson(se))))

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
          "restrictedCapitalAllowance" -> allowances.restrictedCapitalAllowance.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
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


  override def createIncome(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, income: Income): Future[Option[SummaryId]] =
    createSummary(saUtr, taxYear, sourceId, MongoSelfEmploymentIncomeSummary.toMongoSummary(income))

  override def findIncomeById(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[Income]] =
    findSummaryById[Income](saUtr, taxYear, sourceId, (se: MongoSelfEmployment) => se.incomes.find(_.summaryId == id).map(_.toIncome))

  override def updateIncome(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, income: Income): Future[Boolean] =
    updateSummary(saUtr, taxYear, sourceId, MongoSelfEmploymentIncomeSummary.toMongoSummary(income, Some(id)), (se: MongoSelfEmployment) => se.incomes.exists(_.summaryId == id))

  override def deleteIncome(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
    deleteSummary(saUtr, taxYear, sourceId, id, MongoSelfEmploymentIncomeSummary.arrayName, (se: MongoSelfEmployment) => se.incomes.exists(_.summaryId == id))

  override def listIncomes(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[Income]]] =
    listSummaries[Income](saUtr, taxYear, sourceId, (se: MongoSelfEmployment) => se.incomes.map(_.toIncome))


}
