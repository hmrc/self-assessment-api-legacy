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
import uk.gov.hmrc.selfassessmentapi.controllers.api.employment.BenefitType.BenefitType
import uk.gov.hmrc.selfassessmentapi.controllers.api.employment.ExpenseType.ExpenseType
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceId, SummaryId, TaxYear}
import uk.gov.hmrc.selfassessmentapi.controllers.api.employment._
import uk.gov.hmrc.selfassessmentapi.controllers.api.employment.IncomeType.IncomeType

case class MongoEmploymentIncomeSummary(summaryId: SummaryId, `type`: IncomeType, amount: BigDecimal)
    extends MongoSummary with AmountHolder {
  val arrayName = MongoEmploymentIncomeSummary.arrayName

  def toIncome: Income =
    Income(id = Some(summaryId), `type` = `type`, amount = amount)

  def toBsonDocument = BSONDocument(
      "summaryId" -> summaryId,
      "amount" -> BSONDouble(amount.doubleValue()),
      "type" -> BSONString(`type`.toString)
  )
}

object MongoEmploymentIncomeSummary {

  val arrayName = "incomes"

  implicit val format = Json.format[MongoEmploymentIncomeSummary]

  def toMongoSummary(income: Income, id: Option[SummaryId] = None): MongoEmploymentIncomeSummary = {
    MongoEmploymentIncomeSummary(
        summaryId = id.getOrElse(BSONObjectID.generate.stringify),
        `type` = income.`type`,
        amount = income.amount
    )
  }
}

case class MongoEmploymentExpenseSummary(summaryId: SummaryId, `type`: ExpenseType, amount: BigDecimal)
    extends MongoSummary with AmountHolder {
  val arrayName = MongoEmploymentExpenseSummary.arrayName

  def toExpense: Expense =
    Expense(id = Some(summaryId), `type` = `type`, amount = amount)

  def toBsonDocument = BSONDocument(
      "summaryId" -> summaryId,
      "amount" -> BSONDouble(amount.doubleValue()),
      "type" -> BSONString(`type`.toString)
  )
}

object MongoEmploymentExpenseSummary {

  val arrayName = "expenses"

  implicit val format = Json.format[MongoEmploymentExpenseSummary]

  def toMongoSummary(expense: Expense, id: Option[SummaryId] = None): MongoEmploymentExpenseSummary = {
    MongoEmploymentExpenseSummary(
        summaryId = id.getOrElse(BSONObjectID.generate.stringify),
        `type` = expense.`type`,
        amount = expense.amount
    )
  }
}

case class MongoEmploymentBenefitSummary(summaryId: SummaryId, `type`: BenefitType, amount: BigDecimal)
    extends MongoSummary with AmountHolder {
  val arrayName = MongoEmploymentBenefitSummary.arrayName

  def toBenefit: Benefit =
    Benefit(id = Some(summaryId), `type` = `type`, amount = amount)

  def toBsonDocument = BSONDocument(
      "summaryId" -> summaryId,
      "amount" -> BSONDouble(amount.doubleValue()),
      "type" -> BSONString(`type`.toString)
  )
}

object MongoEmploymentBenefitSummary {

  val arrayName = "benefits"

  implicit val format = Json.format[MongoEmploymentBenefitSummary]

  def toMongoSummary(benefit: Benefit, id: Option[SummaryId] = None): MongoEmploymentBenefitSummary = {
    MongoEmploymentBenefitSummary(
        summaryId = id.getOrElse(BSONObjectID.generate.stringify),
        `type` = benefit.`type`,
        amount = benefit.amount
    )
  }
}

case class MongoEmploymentUkTaxPaidSummary(summaryId: SummaryId, amount: BigDecimal) extends MongoSummary {
  val arrayName = MongoEmploymentUkTaxPaidSummary.arrayName

  def toUkTaxPaid: UkTaxPaid =
    UkTaxPaid(id = Some(summaryId), amount = amount)

  def toBsonDocument = BSONDocument(
      "summaryId" -> summaryId,
      "amount" -> BSONDouble(amount.doubleValue())
  )
}

object MongoEmploymentUkTaxPaidSummary {

  val arrayName = "ukTaxPaid"

  implicit val format = Json.format[MongoEmploymentUkTaxPaidSummary]

  def toMongoSummary(uKTaxPaid: UkTaxPaid, id: Option[SummaryId] = None): MongoEmploymentUkTaxPaidSummary = {
    MongoEmploymentUkTaxPaidSummary(
        summaryId = id.getOrElse(BSONObjectID.generate.stringify),
        amount = uKTaxPaid.amount
    )
  }
}

case class MongoEmployment(id: BSONObjectID,
                           sourceId: SourceId,
                           saUtr: SaUtr,
                           taxYear: TaxYear,
                           lastModifiedDateTime: DateTime,
                           createdDateTime: DateTime,
                           incomes: Seq[MongoEmploymentIncomeSummary] = Nil,
                           expenses: Seq[MongoEmploymentExpenseSummary] = Nil,
                           benefits: Seq[MongoEmploymentBenefitSummary] = Nil,
                           ukTaxPaid: Seq[MongoEmploymentUkTaxPaidSummary] = Nil)
    extends SourceMetadata {

  def toEmployment = Employment(id = Some(sourceId))
}

object MongoEmployment {
  implicit val dateTimeFormat = ReactiveMongoFormats.dateTimeFormats
  implicit val localDateFormat = ReactiveMongoFormats.localDateFormats

  implicit val mongoFormats = ReactiveMongoFormats.mongoEntity({
    implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
    implicit val dateTimeFormat: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats
    implicit val localDateFormat: Format[LocalDate] = ReactiveMongoFormats.localDateFormats
    Format(Json.reads[MongoEmployment], Json.writes[MongoEmployment])
  })

  def create(saUtr: SaUtr, taxYear: TaxYear, employment: Employment): MongoEmployment = {
    val id = BSONObjectID.generate
    val now = DateTime.now(DateTimeZone.UTC)
    MongoEmployment(id = id,
                    sourceId = id.stringify,
                    saUtr = saUtr,
                    taxYear = taxYear,
                    lastModifiedDateTime = now,
                    createdDateTime = now)
  }
}
