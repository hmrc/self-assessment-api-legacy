/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models.properties

import play.api.libs.functional.syntax._
import play.api.libs.json._

sealed trait PropertiesAnnualSummary

case class OtherPropertiesAnnualSummary(allowances: Option[OtherPropertiesAllowances],
                                        adjustments: Option[OtherPropertiesAdjustments]) extends PropertiesAnnualSummary

object OtherPropertiesAnnualSummary {
  implicit val writes: Writes[OtherPropertiesAnnualSummary] = Json.writes[OtherPropertiesAnnualSummary]

  implicit val reads: Reads[OtherPropertiesAnnualSummary] = (
    (__ \ "allowances").readNullable[OtherPropertiesAllowances] and
      (__ \ "adjustments").readNullable[OtherPropertiesAdjustments]
    ) (OtherPropertiesAnnualSummary.apply _)
}

case class FHLPropertiesAnnualSummary(allowances: Option[FHLPropertiesAllowances],
                                      adjustments: Option[FHLPropertiesAdjustments]) extends PropertiesAnnualSummary

object FHLPropertiesAnnualSummary {
  implicit val writes: Writes[FHLPropertiesAnnualSummary] = Json.writes[FHLPropertiesAnnualSummary]

  implicit val reads: Reads[FHLPropertiesAnnualSummary] = (
    (__ \ "allowances").readNullable[FHLPropertiesAllowances] and
      (__ \ "adjustments").readNullable[FHLPropertiesAdjustments]
    ) (FHLPropertiesAnnualSummary.apply _)
}
