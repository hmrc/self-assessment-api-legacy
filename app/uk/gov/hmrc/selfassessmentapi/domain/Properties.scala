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

import org.joda.time.{DateTimeZone, LocalDate}
import play.api.libs.json.{Format, Json}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.resources.models.AccountingType.AccountingType
import uk.gov.hmrc.selfassessmentapi.resources.models._
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.{PropertiesAnnualSummary, PropertiesPeriod, PropertiesPeriodicData}
import uk.gov.hmrc.selfassessmentapi.resources.models.{AccountingPeriod, PeriodId, TaxYear}

case class Properties(id: BSONObjectID,
                      nino: Nino,
                      accountingType: AccountingType,
                      lastModifiedDateTime: LocalDate = LocalDate.now(DateTimeZone.UTC),
                      accountingPeriod: AccountingPeriod = AccountingPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-05")),
                      periods: Map[PeriodId, PropertiesPeriod] = Map.empty,
                      annualSummaries: Map[TaxYear, PropertiesAnnualSummary] = Map.empty)
    extends PeriodValidator[Properties, PropertiesPeriod]
    with LastModifiedDateTime {

  def toModel = properties.Properties(accountingType = accountingType)

  def annualSummary(key: TaxYear): Option[PropertiesAnnualSummary] = annualSummaries.get(key)

  def periodExists(periodId: PeriodId): Boolean = period(periodId).nonEmpty

  def period(periodId: PeriodId): Option[PropertiesPeriod] = periods.get(periodId)

  def setPeriodsTo(periodId: PeriodId, period: PropertiesPeriod): Properties =
    this.copy(periods = periods.updated(periodId, period))

  def update(periodId: PeriodId, periodicData: PropertiesPeriodicData): Properties = {
    periods.find(period => period._1.equals(periodId)).map { period =>
      setPeriodsTo(periodId, period._2.copy(data = periodicData))
    }.get
  }
}

object Properties {
  import uk.gov.hmrc.selfassessmentapi.domain.JsonFormatters.PropertiesFormatters.annualSummaryMapFormat

  implicit val mongoFormats = ReactiveMongoFormats.mongoEntity({
    implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
    implicit val localDateFormat: Format[LocalDate] = ReactiveMongoFormats.localDateFormats
    Format(Json.reads[Properties], Json.writes[Properties])
  })
}
