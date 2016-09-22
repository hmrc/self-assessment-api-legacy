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

package uk.gov.hmrc.selfassessmentapi.controllers.api.taxrefundedorsetoff

import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import uk.gov.hmrc.selfassessmentapi.controllers.api.PositiveMonetaryFieldDescription
import uk.gov.hmrc.selfassessmentapi.controllers.api._

case object TaxRefundedOrSetOffs extends TaxYearPropertyType {
  override val name: String = "taxRefundedOrSetOff"
  override val documentationName = "Tax Refunded or Set Off"
  override val example: JsValue = toJson(TaxRefundedOrSetOff.example())

  override def description(action: String): String = s"$action a taxRefundedOrSetOff"

  override val title: String = "Sample tax refunded or set off"

  override val fieldDescriptions = Seq(
    PositiveMonetaryFieldDescription(name, "amount", "Amount of Income Tax refunded or set off by HMRC or Jobcentre Plus")
  )
}
