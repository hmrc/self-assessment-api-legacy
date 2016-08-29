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
import uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.ExpenseType.ExpenseType
import uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.PropertyLocationType.PropertyLocationType
import uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings._
import uk.gov.hmrc.selfassessmentapi.domain.{SourceId, SummaryId, TaxYear}

case class MongoFurnishedHolidayLettingsIncomeSummary(summaryId: SummaryId,
                                            amount: BigDecimal) extends MongoSummary with AmountHolder {
  val arrayName = MongoFurnishedHolidayLettingsIncomeSummary.arrayName

  def toIncome: Income =
    Income(id = Some(summaryId),
      amount = amount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue())
  )
}

object MongoFurnishedHolidayLettingsIncomeSummary {

  val arrayName = "incomes"

  implicit val format = Json.format[MongoFurnishedHolidayLettingsIncomeSummary]

  def toMongoSummary(income: Income, id: Option[SummaryId] = None): MongoFurnishedHolidayLettingsIncomeSummary = {
    MongoFurnishedHolidayLettingsIncomeSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      amount = income.amount
    )
  }
}

case class MongoFurnishedHolidayLettingsExpenseSummary(summaryId: SummaryId,
                                             `type`: ExpenseType,
                                             amount: BigDecimal) extends MongoSummary with AmountHolder {
  val arrayName = MongoFurnishedHolidayLettingsExpenseSummary.arrayName

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

object MongoFurnishedHolidayLettingsExpenseSummary {

  val arrayName = "expenses"

  implicit val format = Json.format[MongoFurnishedHolidayLettingsExpenseSummary]

  def toMongoSummary(expense: Expense, id: Option[SummaryId] = None): MongoFurnishedHolidayLettingsExpenseSummary = {
    MongoFurnishedHolidayLettingsExpenseSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      `type` = expense.`type`,
      amount = expense.amount
    )
  }
}

case class MongoFurnishedHolidayLettingsBalancingChargeSummary(summaryId: SummaryId,
                                                     amount: BigDecimal) extends MongoSummary with AmountHolder {
  val arrayName = MongoFurnishedHolidayLettingsBalancingChargeSummary.arrayName

  def toBalancingCharge =
    BalancingCharge(id = Some(summaryId),
      amount = amount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue())
  )
}

object MongoFurnishedHolidayLettingsBalancingChargeSummary {

  val arrayName = "balancingCharges"

  implicit val format = Json.format[MongoFurnishedHolidayLettingsBalancingChargeSummary]

  def toMongoSummary(balancingCharge: BalancingCharge, id: Option[SummaryId] = None): MongoFurnishedHolidayLettingsBalancingChargeSummary = {
    MongoFurnishedHolidayLettingsBalancingChargeSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      amount = balancingCharge.amount
    )
  }
}

case class MongoFurnishedHolidayLettingsPrivateUseAdjustmentSummary(summaryId: SummaryId, amount: BigDecimal) extends MongoSummary
  with AmountHolder {

  val arrayName = MongoFurnishedHolidayLettingsPrivateUseAdjustmentSummary.arrayName

  def toPrivateUseAdjustment = PrivateUseAdjustment(id = Some(summaryId), amount = amount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue())
  )
}

object MongoFurnishedHolidayLettingsPrivateUseAdjustmentSummary {

  val arrayName = "privateUseAdjustment"

  implicit val format = Json.format[MongoFurnishedHolidayLettingsPrivateUseAdjustmentSummary]

  def toMongoSummary(privateUseAdjustment: PrivateUseAdjustment, id: Option[SummaryId] = None): MongoFurnishedHolidayLettingsPrivateUseAdjustmentSummary = {
    MongoFurnishedHolidayLettingsPrivateUseAdjustmentSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      amount = privateUseAdjustment.amount
    )
  }
}

case class MongoFurnishedHolidayLettings(id: BSONObjectID,
                               sourceId: SourceId,
                               saUtr: SaUtr,
                               taxYear: TaxYear,
                               lastModifiedDateTime: DateTime,
                               createdDateTime: DateTime,
                               propertyLocation: PropertyLocationType,
                               allowances: Option[Allowances] = None,
                               adjustments: Option[Adjustments] = None,
                               incomes: Seq[MongoFurnishedHolidayLettingsIncomeSummary] = Nil,
                               expenses: Seq[MongoFurnishedHolidayLettingsExpenseSummary] = Nil,
                               balancingCharges: Seq[MongoFurnishedHolidayLettingsBalancingChargeSummary] = Nil,
                               privateUseAdjustment: Seq[MongoFurnishedHolidayLettingsPrivateUseAdjustmentSummary] = Nil) extends SourceMetadata {


  def capitalAllowance: BigDecimal = allowances.flatMap(_.capitalAllowance).getOrElse(0)


  def toFurnishedHolidayLettings = FurnishedHolidayLetting(
    id = Some(sourceId),
    propertyLocation = propertyLocation,
    allowances = allowances,
    adjustments = adjustments)
}

object MongoFurnishedHolidayLettings {
  implicit val dateTimeFormat = ReactiveMongoFormats.dateTimeFormats
  implicit val localDateFormat = ReactiveMongoFormats.localDateFormats

  implicit val mongoFormats = ReactiveMongoFormats.mongoEntity({
    implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
    implicit val dateTimeFormat: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats
    implicit val localDateFormat: Format[LocalDate] = ReactiveMongoFormats.localDateFormats
    Format(Json.reads[MongoFurnishedHolidayLettings], Json.writes[MongoFurnishedHolidayLettings])
  })

  def create(saUtr: SaUtr, taxYear: TaxYear, fhl: FurnishedHolidayLetting): MongoFurnishedHolidayLettings = {
    val id = BSONObjectID.generate
    val now = DateTime.now(DateTimeZone.UTC)
    MongoFurnishedHolidayLettings(
      id = id,
      sourceId = id.stringify,
      saUtr = saUtr,
      taxYear = taxYear,
      lastModifiedDateTime = now,
      createdDateTime = now,
      propertyLocation = fhl.propertyLocation,
      allowances = fhl.allowances,
      adjustments = fhl.adjustments)
  }
}
