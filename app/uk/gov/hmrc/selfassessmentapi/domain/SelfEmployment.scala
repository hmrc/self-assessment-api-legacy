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

import org.joda.time.LocalDate
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.controllers.api.{PeriodId, TaxYear}
import uk.gov.hmrc.selfassessmentapi.resources._
import uk.gov.hmrc.selfassessmentapi.resources.models._
import uk.gov.hmrc.selfassessmentapi.resources.models.periods.SelfEmploymentPeriod

case class SelfEmployment(id: BSONObjectID,
                          sourceId: String,
                          nino: Nino,
                          lastModifiedDateTime: LocalDate,
                          commencementDate: LocalDate,
                          annualSummaries: Map[TaxYear, SelfEmploymentAnnualSummary] = Map.empty,
                          periods: Map[PeriodId, SelfEmploymentPeriod] = Map.empty) extends LastModifiedDateTime {
  def containsPeriod(period: SelfEmploymentPeriod) = {
    periods.exists { case (_, p) => p.from == period.from && p.to == period.to }
  }

  def periodExists(periodId: PeriodId) = period(periodId).nonEmpty
  def annualSummary(taxYear: TaxYear) = annualSummaries.get(taxYear)
  def period(periodId: PeriodId) = periods.get(periodId)
  def toModel: models.SelfEmployment = models.SelfEmployment(Some(id.stringify), commencementDate)
}

object SelfEmployment {
  implicit val mongoFormats = ReactiveMongoFormats.mongoEntity({
    import uk.gov.hmrc.selfassessmentapi.domain.JsonFormatters.SelfEmploymentFormatters.annualSummaryMapFormat

    implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
    implicit val localDateFormat: Format[LocalDate] = ReactiveMongoFormats.localDateFormats
    Format(Json.reads[SelfEmployment], Json.writes[SelfEmployment])
  })
}
