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
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.domain.ukproperty.ExpenseType.ExpenseType
import uk.gov.hmrc.selfassessmentapi.domain.ukproperty.IncomeType.IncomeType
import uk.gov.hmrc.selfassessmentapi.domain.ukproperty._
import uk.gov.hmrc.selfassessmentapi.domain._

case class MongoUKPropertiesIncomeSummary(summaryId: SummaryId,
                                          `type`: IncomeType,
                                          amount: BigDecimal) extends MongoSummary with AmountHolder {

  val arrayName = MongoUKPropertiesIncomeSummary.arrayName

  def toIncome: Income = Income(id = Some(summaryId), `type` = `type`, amount = amount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue()),
    "type" -> BSONString(`type`.toString)
  )
}

object MongoUKPropertiesIncomeSummary {

  val arrayName = "incomes"

  implicit val format = Json.format[MongoUKPropertiesIncomeSummary]

  def toMongoSummary(income: Income, id: Option[SummaryId] = None): MongoUKPropertiesIncomeSummary = {
    MongoUKPropertiesIncomeSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      `type` = income.`type`,
      amount = income.amount
    )
  }
}

case class MongoUKPropertiesExpenseSummary(summaryId: SummaryId,
                                             `type`: ExpenseType,
                                             amount: BigDecimal) extends MongoSummary with AmountHolder {
  val arrayName = MongoUKPropertiesExpenseSummary.arrayName

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

object MongoUKPropertiesExpenseSummary {

  val arrayName = "expenses"

  implicit val format = Json.format[MongoUKPropertiesExpenseSummary]

  def toMongoSummary(expense: Expense, id: Option[SummaryId] = None): MongoUKPropertiesExpenseSummary = {
    MongoUKPropertiesExpenseSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      `type` = expense.`type`,
      amount = expense.amount
    )
  }
}

case class MongoUKPropertiesBalancingChargeSummary(summaryId: SummaryId,
                                                     amount: BigDecimal) extends MongoSummary with AmountHolder {
  val arrayName = MongoUKPropertiesBalancingChargeSummary.arrayName

  def toBalancingCharge =
    BalancingCharge(id = Some(summaryId),
      amount = amount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue())
  )
}

object MongoUKPropertiesBalancingChargeSummary {

  val arrayName = "balancingCharges"

  implicit val format = Json.format[MongoUKPropertiesBalancingChargeSummary]

  def toMongoSummary(balancingCharge: BalancingCharge, id: Option[SummaryId] = None): MongoUKPropertiesBalancingChargeSummary = {
    MongoUKPropertiesBalancingChargeSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      amount = balancingCharge.amount
    )
  }
}

case class MongoUKPropertiesPrivateUseAdjustmentSummary(summaryId: SummaryId, amount: BigDecimal) extends MongoSummary with AmountHolder {

  val arrayName = MongoUKPropertiesPrivateUseAdjustmentSummary.arrayName

  def toGoodsAndServicesOwnUse = PrivateUseAdjustment(id = Some(summaryId), amount = amount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue())
  )
}

object MongoUKPropertiesPrivateUseAdjustmentSummary {

  val arrayName = "privateUseAdjustment"

  implicit val format = Json.format[MongoUKPropertiesPrivateUseAdjustmentSummary]

  def toMongoSummary(privateUseAdjustment: PrivateUseAdjustment, id: Option[SummaryId] = None): MongoUKPropertiesPrivateUseAdjustmentSummary = {
    MongoUKPropertiesPrivateUseAdjustmentSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      amount = privateUseAdjustment.amount
    )
  }
}

case class MongoUKPropertiesTaxPaidSummary(summaryId: SummaryId, amount: BigDecimal) extends MongoSummary {

  val arrayName = MongoUKPropertiesTaxPaidSummary.arrayName

  def toTaxPaid = TaxPaid(id = Some(summaryId), amount = amount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue())
  )
}

object MongoUKPropertiesTaxPaidSummary {

  val arrayName = "taxesPaid"

  implicit val format = Json.format[MongoUKPropertiesTaxPaidSummary]

  def toMongoSummary(taxPaid: TaxPaid, id: Option[SummaryId] = None): MongoUKPropertiesTaxPaidSummary = {
    MongoUKPropertiesTaxPaidSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      amount = taxPaid.amount
    )
  }
}

case class MongoUKProperties(id: BSONObjectID,
                               sourceId: SourceId,
                               saUtr: SaUtr,
                               taxYear: TaxYear,
                               lastModifiedDateTime: DateTime = DateTime.now(DateTimeZone.UTC),
                               createdDateTime: DateTime = DateTime.now(DateTimeZone.UTC),
                               rentARoomRelief: Option[BigDecimal] = None,
                               allowances: Option[Allowances] = None,
                               adjustments: Option[Adjustments] = None,
                               incomes: Seq[MongoUKPropertiesIncomeSummary] = Nil,
                               expenses: Seq[MongoUKPropertiesExpenseSummary] = Nil,
                               balancingCharges: Seq[MongoUKPropertiesBalancingChargeSummary] = Nil,
                               privateUseAdjustment: Seq[MongoUKPropertiesPrivateUseAdjustmentSummary] = Nil,
                               taxesPaid: Seq[MongoUKPropertiesTaxPaidSummary] = Nil) extends SourceMetadata {
  def rentARoomReliefAmount = rentARoomRelief.getOrElse(BigDecimal(0))

  def allowancesTotal = allowances.map(_.total).getOrElse(BigDecimal(0))

  def lossBroughtForward = adjustments.flatMap(_.lossBroughtForward).getOrElse(BigDecimal(0))

  def adjustedProfit = {
    PositiveOrZero(Total(incomes) + Total(balancingCharges) + Total(privateUseAdjustment) -
      Total(expenses) - allowancesTotal - rentARoomReliefAmount)
  }

  def toUKProperties = UKProperty(
    id = Some(sourceId),
    rentARoomRelief = rentARoomRelief,
    allowances = allowances,
    adjustments = adjustments)
}

object MongoUKProperties {
  implicit val dateTimeFormat = ReactiveMongoFormats.dateTimeFormats
  implicit val localDateFormat = ReactiveMongoFormats.localDateFormats

  implicit val mongoFormats = ReactiveMongoFormats.mongoEntity({
    implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
    implicit val dateTimeFormat: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats
    implicit val localDateFormat: Format[LocalDate] = ReactiveMongoFormats.localDateFormats
    Format(Json.reads[MongoUKProperties], Json.writes[MongoUKProperties])
  })

  def create(saUtr: SaUtr, taxYear: TaxYear, ukp: UKProperty): MongoUKProperties = {
    val id = BSONObjectID.generate
    MongoUKProperties(
      id = id,
      sourceId = id.stringify,
      saUtr = saUtr,
      taxYear = taxYear,
      rentARoomRelief = ukp.rentARoomRelief,
      allowances = ukp.allowances,
      adjustments = ukp.adjustments)
  }
}
