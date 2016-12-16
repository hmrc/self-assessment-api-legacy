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

import play.api.libs.json.{Json, Reads, Writes}
import uk.gov.hmrc.selfassessmentapi.resources.models.{AnnualSummary, PeriodId, TaxYear}
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.{FHLPropertiesAnnualSummary, OtherPropertiesAnnualSummary, PropertiesPeriod}

trait PropertiesBucket extends PeriodValidator[PropertiesPeriod] {
  val periods: Map[PeriodId, PropertiesPeriod]
  val annualSummaries: Map[TaxYear, AnnualSummary]
}

case class FHLPropertiesBucket(periods: Map[PeriodId, PropertiesPeriod],
                               annualSummaries: Map[TaxYear, FHLPropertiesAnnualSummary]) extends PropertiesBucket

case class OtherPropertiesBucket(periods: Map[PeriodId, PropertiesPeriod],
                                 annualSummaries: Map[TaxYear, OtherPropertiesAnnualSummary]) extends PropertiesBucket

object FHLPropertiesBucket {
  import uk.gov.hmrc.selfassessmentapi.domain.JsonFormatters.PropertiesFormatters.annualSummaryFHLMapFormat

  implicit val reads: Reads[FHLPropertiesBucket] = Json.reads[FHLPropertiesBucket]
  implicit val writes: Writes[FHLPropertiesBucket] = Json.writes[FHLPropertiesBucket]
}

object OtherPropertiesBucket {
  import uk.gov.hmrc.selfassessmentapi.domain.JsonFormatters.PropertiesFormatters.annualSummaryOtherMapFormat

  implicit val reads: Reads[OtherPropertiesBucket] = Json.reads[OtherPropertiesBucket]
  implicit val writes: Writes[OtherPropertiesBucket] = Json.writes[OtherPropertiesBucket]
}
