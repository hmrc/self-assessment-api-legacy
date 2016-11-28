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

package uk.gov.hmrc.selfassessmentapi.domain

import org.joda.time._
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.controllers.api.{PeriodId, TaxYear}
import uk.gov.hmrc.selfassessmentapi.resources._
import uk.gov.hmrc.selfassessmentapi.resources.models.AccountingType._
import uk.gov.hmrc.selfassessmentapi.resources.models._
import uk.gov.hmrc.selfassessmentapi.resources.models.periods.SelfEmploymentPeriod

case class SelfEmployment(id: BSONObjectID,
                          sourceId: String,
                          nino: Nino,
                          lastModifiedDateTime: LocalDate,
                          accountingPeriod: AccountingPeriod,
                          accountingType: AccountingType,
                          commencementDate: LocalDate,
                          annualSummaries: Map[TaxYear, SelfEmploymentAnnualSummary] = Map.empty,
                          periods: Map[PeriodId, SelfEmploymentPeriod] = Map.empty) extends LastModifiedDateTime {

  def containsGap(period: SelfEmploymentPeriod): Boolean = {
    val newPeriod = new Interval(period.from.toDateTimeAtStartOfDay(DateTimeZone.UTC), period.to.toDateTimeAtStartOfDay(DateTimeZone.UTC))

    val existingIntervals = periods.values.toSeq.sorted.reverse.map { period =>
      new Interval(period.from.toDateTimeAtStartOfDay(DateTimeZone.UTC), period.to.toDateTimeAtStartOfDay(DateTimeZone.UTC))
    }

    existingIntervals match {
      case Seq() => false
      case head +: _ => !head.gap(newPeriod).toDuration.isEqual(Duration.standardDays(1))
    }
  }

  def containsOverlappingPeriod(period: SelfEmploymentPeriod): Boolean = {
    val newPeriod = new Interval(period.from.toDateTimeAtStartOfDay(DateTimeZone.UTC), period.from.toDateTimeAtStartOfDay(DateTimeZone.UTC))

    periods.exists { case (_, p) =>
      val existingPeriod = new Interval(p.from.toDateTimeAtStartOfDay(DateTimeZone.UTC), p.to.toDateTimeAtStartOfDay(DateTimeZone.UTC))
      newPeriod.overlaps(existingPeriod) || newPeriod.abuts(existingPeriod)
    }
  }

  def containsMisalignedPeriod(period: SelfEmploymentPeriod): Boolean = {
    if (periods.isEmpty) !period.from.isEqual(accountingPeriod.start)
    else !(period.to.isBefore(accountingPeriod.end) || period.to.isEqual(accountingPeriod.end))
  }

  def periodExists(periodId: PeriodId): Boolean = period(periodId).nonEmpty
  def annualSummary(taxYear: TaxYear): Option[SelfEmploymentAnnualSummary] = annualSummaries.get(taxYear)
  def period(periodId: PeriodId): Option[SelfEmploymentPeriod] = periods.get(periodId)
  def toModel: models.SelfEmployment =
    models.SelfEmployment(Some(id.stringify), accountingPeriod, accountingType, commencementDate)
}

object SelfEmployment {
  implicit val mongoFormats = ReactiveMongoFormats.mongoEntity({
    import uk.gov.hmrc.selfassessmentapi.domain.JsonFormatters.SelfEmploymentFormatters.annualSummaryMapFormat

    implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
    implicit val localDateFormat: Format[LocalDate] = ReactiveMongoFormats.localDateFormats
    Format(Json.reads[SelfEmployment], Json.writes[SelfEmployment])
  })
}
