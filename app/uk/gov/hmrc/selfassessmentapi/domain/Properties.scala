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
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.selfassessmentapi.resources.models.properties._
import uk.gov.hmrc.selfassessmentapi.resources.models.{AccountingPeriod, TaxYear}

case class Properties(id: BSONObjectID,
                      nino: Nino,
                      accountingType: AccountingType,
                      lastModifiedDateTime: LocalDate = LocalDate.now(DateTimeZone.UTC),
                      accountingPeriod: AccountingPeriod = AccountingPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-05")),
                      fhlBucket: FHLPropertiesBucket = FHLPropertiesBucket(Map.empty, Map.empty),
                      otherBucket: OtherPropertiesBucket = OtherPropertiesBucket(Map.empty, Map.empty))
    extends LastModifiedDateTime {

  def toModel = properties.Properties(accountingType = accountingType)

  def validatePeriod(propertyType: PropertyType, period: PropertiesPeriod): Either[Errors.Error, Properties] = {
    val validationErrors = propertyType match {
      case PropertyType.OTHER => otherBucket.validatePeriod(period, accountingPeriod)
      case PropertyType.FHL => fhlBucket.validatePeriod(period, accountingPeriod)
    }

    validationErrors.map(Left(_)).getOrElse(Right(this))
  }

  def annualSummary(propertyType: PropertyType, key: TaxYear): AnnualSummary = propertyType match {
    case PropertyType.OTHER => otherBucket.annualSummaries.getOrElse(key, OtherPropertiesAnnualSummary(None, None))
    case PropertyType.FHL => fhlBucket.annualSummaries.getOrElse(key, FHLPropertiesAnnualSummary(None, None))
  }

  def periodExists(propertyType: PropertyType, periodId: PeriodId): Boolean = period(propertyType, periodId).nonEmpty

  def period(propertyType: PropertyType, periodId: PeriodId): Option[PropertiesPeriod] = propertyType match {
    case PropertyType.OTHER => otherBucket.periods.get(periodId)
    case PropertyType.FHL => fhlBucket.periods.get(periodId)
  }

  def setPeriodsTo(propertyType: PropertyType, periodId: PeriodId, period: PropertiesPeriod): Properties = propertyType match {
    case PropertyType.OTHER => this.copy(otherBucket = otherBucket.copy(periods = otherBucket.periods.updated(periodId, period)))
    case PropertyType.FHL => this.copy(fhlBucket = fhlBucket.copy(periods = fhlBucket.periods.updated(periodId, period)))
  }

  def update(propertyType: PropertyType, periodId: PeriodId, periodicData: PropertiesPeriodicData): Properties = {
    val periodOpt = propertyType match {
      case PropertyType.OTHER => otherBucket.periods.find(period => period._1.equals(periodId))
      case PropertyType.FHL => fhlBucket.periods.find(period => period._1.equals(periodId))
    }

    periodOpt.map { period =>
      setPeriodsTo(propertyType, periodId, period._2.copy(data = periodicData))
    }.get
  }
}

object Properties {
  implicit val mongoFormats: Format[Properties] = ReactiveMongoFormats.mongoEntity({
    implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
    implicit val localDateFormat: Format[LocalDate] = ReactiveMongoFormats.localDateFormats
    Format(Json.reads[Properties], Json.writes[Properties])
  })
}
