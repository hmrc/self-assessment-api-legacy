/*
 * Copyright 2018 HM Revenue & Customs
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
import uk.gov.hmrc.selfassessmentapi.models._

case class OtherPropertiesAdjustments(lossBroughtForward: Option[Amount] = None,
                                      privateUseAdjustment: Option[Amount] = None,
                                      balancingCharge: Option[Amount] = None)

object OtherPropertiesAdjustments {
  implicit val writes: Writes[OtherPropertiesAdjustments] = Json.writes[OtherPropertiesAdjustments]

  implicit val reads: Reads[OtherPropertiesAdjustments] = (
    (__ \ "lossBroughtForward").readNullable[Amount](nonNegativeAmountValidator) and
      (__ \ "privateUseAdjustment").readNullable[Amount](nonNegativeAmountValidator) and
      (__ \ "balancingCharge").readNullable[Amount](nonNegativeAmountValidator)
  ) (OtherPropertiesAdjustments.apply _)
}
