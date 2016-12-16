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

package uk.gov.hmrc.selfassessmentapi.resources.models.properties

import play.api.libs.functional.syntax._
import play.api.libs.json._

import uk.gov.hmrc.selfassessmentapi.resources.models._

case class Adjustments(lossBroughtForward: Option[Amount] = None,
                       rentARoomExempt: Option[Amount] = None,
                       privateUseAdjustment: Option[Amount] = None,
                       balancingCharge: Option[Amount] = None)

object Adjustments {
  implicit val writes: Writes[Adjustments] = Json.writes[Adjustments]

  implicit val reads: Reads[Adjustments] = (
    (__ \ "lossBroughtForward").readNullable[Amount](positiveAmountValidator) and
      (__ \ "rentARoomExempt").readNullable[Amount](positiveAmountValidator) and
      (__ \ "privateUseAdjustment").readNullable[Amount](positiveAmountValidator) and
      (__ \ "balancingCharge").readNullable[Amount](positiveAmountValidator)
  ) (Adjustments.apply _)
}
