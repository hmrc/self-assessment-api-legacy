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
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.BenefitType.BenefitType
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.DividendType._
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.SavingsIncomeType._
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.{Benefit, Dividend, SavingsIncome, UnearnedIncome}
import uk.gov.hmrc.selfassessmentapi.domain.{TaxYear, _}

case class MongoUnearnedIncomesSavingsIncomeSummary(summaryId: SummaryId,
                                            `type`: SavingsIncomeType,
                                            amount: BigDecimal) extends MongoSummary {

  val arrayName = MongoUnearnedIncomesSavingsIncomeSummary.arrayName

  def toSavingsIncome: SavingsIncome =
    SavingsIncome(id = Some(summaryId),
      `type` = `type`,
      amount = amount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue()),
    "type" -> BSONString(`type`.toString)
  )
}

object MongoUnearnedIncomesSavingsIncomeSummary {

  val arrayName = "savings"

  implicit val format = Json.format[MongoUnearnedIncomesSavingsIncomeSummary]

  def toMongoSummary(income: SavingsIncome, id: Option[SummaryId] = None): MongoUnearnedIncomesSavingsIncomeSummary = {
    MongoUnearnedIncomesSavingsIncomeSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      `type` = income.`type`,
      amount = income.amount
    )
  }
}

case class MongoUnearnedIncomesBenefitSummary(summaryId: SummaryId,
                                              `type`: BenefitType,
                                              amount: BigDecimal,
                                              taxDeduction: BigDecimal) extends MongoSummary {

  val arrayName = MongoUnearnedIncomesBenefitSummary.arrayName

  def toBenefit: Benefit =
    Benefit(id = Some(summaryId),
      `type` = `type`,
      amount = amount,
      taxDeduction = taxDeduction)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue()),
    "taxDeduction" -> BSONDouble(taxDeduction.doubleValue()),
    "type" -> BSONString(`type`.toString)
  )
}

object MongoUnearnedIncomesBenefitSummary {

  val arrayName = "benefits"

  implicit val format = Json.format[MongoUnearnedIncomesBenefitSummary]

  def toMongoSummary(income: Benefit, id: Option[SummaryId] = None): MongoUnearnedIncomesBenefitSummary = {
    MongoUnearnedIncomesBenefitSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      `type` = income.`type`,
      amount = income.amount,
      taxDeduction = income.taxDeduction
    )
  }
}

case class MongoUnearnedIncomesDividendSummary(summaryId: SummaryId,
                                             `type`: DividendType,
                                             amount: BigDecimal) extends MongoSummary {
  val arrayName = MongoUnearnedIncomesDividendSummary.arrayName

  def toDividend: Dividend =
    Dividend(id = Some(summaryId),
      `type` = `type`,
      amount = amount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue()),
    "type" -> BSONString(`type`.toString)
  )
}

object MongoUnearnedIncomesDividendSummary {

  val arrayName = "dividends"

  implicit val format = Json.format[MongoUnearnedIncomesDividendSummary]

  def toMongoSummary(dividend: Dividend, id: Option[SummaryId] = None): MongoUnearnedIncomesDividendSummary = {
    MongoUnearnedIncomesDividendSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      `type` = dividend.`type`,
      amount = dividend.amount
    )
  }
}

case class MongoUnearnedIncome(id: BSONObjectID,
                               sourceId: SourceId,
                               saUtr: SaUtr,
                               taxYear: TaxYear,
                               lastModifiedDateTime: DateTime,
                               createdDateTime: DateTime,
                               savings: Seq[MongoUnearnedIncomesSavingsIncomeSummary] = Nil,
                               dividends: Seq[MongoUnearnedIncomesDividendSummary] = Nil,
                               benefits: Seq[MongoUnearnedIncomesBenefitSummary] = Nil) extends SourceMetadata {

  def toUnearnedIncome = UnearnedIncome(id = Some(sourceId))

  def taxedSavingsInterest = savings.filter(_.`type` == InterestFromBanksTaxed).map(_.amount).sum
}

object MongoUnearnedIncome {
  implicit val dateTimeFormat = ReactiveMongoFormats.dateTimeFormats
  implicit val localDateFormat = ReactiveMongoFormats.localDateFormats

  implicit val mongoFormats = ReactiveMongoFormats.mongoEntity({
    implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
    implicit val dateTimeFormat: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats
    implicit val localDateFormat: Format[LocalDate] = ReactiveMongoFormats.localDateFormats
    Format(Json.reads[MongoUnearnedIncome], Json.writes[MongoUnearnedIncome])
  })

  def create(saUtr: SaUtr, taxYear: TaxYear, se: UnearnedIncome): MongoUnearnedIncome = {
    val id = BSONObjectID.generate
    val now = DateTime.now(DateTimeZone.UTC)
    MongoUnearnedIncome(
      id = id,
      sourceId = id.stringify,
      saUtr = saUtr,
      taxYear = taxYear,
      lastModifiedDateTime = now,
      createdDateTime = now
      )
  }
}
