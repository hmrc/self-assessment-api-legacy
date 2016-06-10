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
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.IncomeType.IncomeType
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.{Adjustments, Allowances, Income, SelfEmployment}
import uk.gov.hmrc.selfassessmentapi.domain.{SourceId, SummaryId, TaxYear}

case class MongoSelfEmploymentIncomeSummary(summaryId: SummaryId,
                                            `type`: IncomeType,
                                            amount: BigDecimal) extends MongoSummary {
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

case class MongoSelfEmployment(id: BSONObjectID,
                               sourceId: SourceId,
                               saUtr: SaUtr,
                               taxYear: TaxYear,
                               lastModifiedDateTime: DateTime,
                               createdDateTime: DateTime,
                               commencementDate: LocalDate,
                               allowances: Option[Allowances] = None,
                               adjustments: Option[Adjustments] = None,
                               incomes: Seq[MongoSelfEmploymentIncomeSummary] = Nil) extends SourceMetadata {

  def toSelfEmployment = SelfEmployment(
    id = Some(sourceId),
    commencementDate = commencementDate,
    allowances = allowances,
    adjustments = adjustments)
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
