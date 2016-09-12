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

package uk.gov.hmrc.selfassessmentapi.repositories.domain

import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import play.api.libs.json.{Format, Json}
import reactivemongo.bson.{BSONDocument, BSONDouble, BSONObjectID, BSONString}
import uk.gov.hmrc.domain._
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.controllers.api.pensioncontribution.PensionContribution
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.BalancingChargeType.BalancingChargeType
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.ExpenseType.ExpenseType
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.IncomeType.IncomeType
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment._
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.domain.TaxYearProperties

case class MongoSelfEmploymentIncomeSummary(summaryId: SummaryId,
                                            `type`: IncomeType,
                                            amount: BigDecimal) extends MongoSummary with AmountHolder {
  val arrayName = MongoSelfEmploymentIncomeSummary.arrayName

  def toIncome: Income =
    Income(id = Some(summaryId),
      `type` = `type`,
      amount = amount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue()),
    "type" -> BSONString(`type`.toString)
  )
}

object MongoSelfEmploymentIncomeSummary {

  val arrayName = "incomes"

  implicit val format = Json.format[MongoSelfEmploymentIncomeSummary]

  def toMongoSummary(income: Income, id: Option[SummaryId] = None): MongoSelfEmploymentIncomeSummary = {
    MongoSelfEmploymentIncomeSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      `type` = income.`type`,
      amount = income.amount
    )
  }
}

case class MongoSelfEmploymentExpenseSummary(summaryId: SummaryId,
                                             `type`: ExpenseType,
                                             amount: BigDecimal) extends MongoSummary with AmountHolder {
  val arrayName = MongoSelfEmploymentExpenseSummary.arrayName

  def toExpense: Expense =
    Expense(id = Some(summaryId),
      `type` = `type`,
      amount = amount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue()),
    "type" -> BSONString(`type`.toString)
  )
}

object MongoSelfEmploymentExpenseSummary {

  val arrayName = "expenses"

  implicit val format = Json.format[MongoSelfEmploymentExpenseSummary]

  def toMongoSummary(expense: Expense, id: Option[SummaryId] = None): MongoSelfEmploymentExpenseSummary = {
    MongoSelfEmploymentExpenseSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      `type` = expense.`type`,
      amount = expense.amount
    )
  }
}

case class MongoSelfEmploymentBalancingChargeSummary(summaryId: SummaryId,
                                                     `type`: BalancingChargeType,
                                                     amount: BigDecimal) extends MongoSummary with AmountHolder {
  val arrayName = MongoSelfEmploymentBalancingChargeSummary.arrayName

  def toBalancingCharge =
    BalancingCharge(id = Some(summaryId),
      `type` = `type`,
      amount = amount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue()),
    "type" -> BSONString(`type`.toString)
  )
}

object MongoSelfEmploymentBalancingChargeSummary {

  val arrayName = "balancingCharges"

  implicit val format = Json.format[MongoSelfEmploymentBalancingChargeSummary]

  def toMongoSummary(balancingCharge: BalancingCharge, id: Option[SummaryId] = None): MongoSelfEmploymentBalancingChargeSummary = {
    MongoSelfEmploymentBalancingChargeSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      `type` = balancingCharge.`type`,
      amount = balancingCharge.amount
    )
  }
}

case class MongoSelfEmploymentGoodsAndServicesOwnUseSummary(summaryId: SummaryId, amount: BigDecimal) extends MongoSummary with AmountHolder {

  val arrayName = MongoSelfEmploymentGoodsAndServicesOwnUseSummary.arrayName

  def toGoodsAndServicesOwnUse = GoodsAndServicesOwnUse(id = Some(summaryId), amount = amount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue())
  )
}

object MongoSelfEmploymentGoodsAndServicesOwnUseSummary {

  val arrayName = "goodsAndServicesOwnUse"

  implicit val format = Json.format[MongoSelfEmploymentGoodsAndServicesOwnUseSummary]

  def toMongoSummary(goodsAndServicesOwnUse: GoodsAndServicesOwnUse, id: Option[SummaryId] = None): MongoSelfEmploymentGoodsAndServicesOwnUseSummary = {
    MongoSelfEmploymentGoodsAndServicesOwnUseSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      amount = goodsAndServicesOwnUse.amount
    )
  }
}

case class MongoTaxYearProperties(id: BSONObjectID,
                                  saUtr: SaUtr,
                                  taxYear: TaxYear,
                                  lastModifiedDateTime: DateTime,
                                  createdDateTime: DateTime,
                                  pensionContributions: Option[PensionContribution] = None)
  extends SelfAssessmentMetadata {

  def toTaxYearProperties = TaxYearProperties(
    id = Some(id.stringify),
    pensionContributions = pensionContributions)
}

object MongoTaxYearProperties {

  implicit val dateTimeFormat = ReactiveMongoFormats.dateTimeFormats

  implicit val mongoFormats = ReactiveMongoFormats.mongoEntity({
    implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
    implicit val dateTimeFormat: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats
    Format(Json.reads[MongoTaxYearProperties], Json.writes[MongoTaxYearProperties])
  })
}

case class MongoSelfEmployment(id: BSONObjectID,
                               sourceId: SourceId,
                               saUtr: SaUtr,
                               taxYear: TaxYear,
                               lastModifiedDateTime: DateTime,
                               createdDateTime: DateTime,
                               commencementDate: LocalDate,
                               allowances: Option[Allowances] = None,
                               adjustments: Option[Adjustments] = None,
                               incomes: Seq[MongoSelfEmploymentIncomeSummary] = Nil,
                               expenses: Seq[MongoSelfEmploymentExpenseSummary] = Nil,
                               balancingCharges: Seq[MongoSelfEmploymentBalancingChargeSummary] = Nil,
                               goodsAndServicesOwnUse: Seq[MongoSelfEmploymentGoodsAndServicesOwnUseSummary] = Nil) extends SourceMetadata {

  def adjustedProfits: BigDecimal = PositiveOrZero(profitIncreases - profitReductions)

  private def profitIncreases: BigDecimal = {
    val adjustments = this.adjustments.map { a =>
      Sum(a.basisAdjustment, a.accountingAdjustment, a.averagingAdjustment)
    }.getOrElse(BigDecimal(0))

    Total(incomes) + Total(balancingCharges) + Total(goodsAndServicesOwnUse) + adjustments
  }

  private def profitReductions: BigDecimal = {
    val expenses = Some(this.expenses.filterNot(_.`type` == selfemployment.ExpenseType.Depreciation).map(_.amount).sum)
    val allowances = this.allowances.map(_.total)
    val adjustments = this.adjustments.map { a => Sum(a.includedNonTaxableProfits, a.overlapReliefUsed) }

    Sum(expenses, allowances, adjustments)
  }

  def lossBroughtForward = adjustments.flatMap(_.lossBroughtForward).getOrElse(BigDecimal(0))

  def toSelfEmployment = SelfEmployment(
    id = Some(sourceId),
    commencementDate = commencementDate,
    allowances = allowances,
    adjustments = adjustments)

  lazy val outstandingBusinessIncome = ValueOrZero(adjustments.flatMap(_.outstandingBusinessIncome))
}

object MongoSelfEmployment {
  implicit val dateTimeFormat = ReactiveMongoFormats.dateTimeFormats
  implicit val localDateFormat = ReactiveMongoFormats.localDateFormats

  implicit val mongoFormats = ReactiveMongoFormats.mongoEntity({
    implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
    implicit val dateTimeFormat: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats
    implicit val localDateFormat: Format[LocalDate] = ReactiveMongoFormats.localDateFormats
    Format(Json.reads[MongoSelfEmployment], Json.writes[MongoSelfEmployment])
  })

  def create(saUtr: SaUtr, taxYear: TaxYear, se: SelfEmployment): MongoSelfEmployment = {
    val id = BSONObjectID.generate
    val now = DateTime.now(DateTimeZone.UTC)
    MongoSelfEmployment(
      id = id,
      sourceId = id.stringify,
      saUtr = saUtr,
      taxYear = taxYear,
      lastModifiedDateTime = now,
      createdDateTime = now,
      commencementDate = se.commencementDate,
      allowances = se.allowances,
      adjustments = se.adjustments)
  }
}
