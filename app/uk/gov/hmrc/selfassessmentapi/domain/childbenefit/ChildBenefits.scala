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

package uk.gov.hmrc.selfassessmentapi.domain.childbenefit

import play.api.libs.json.Json.toJson
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode.{apply => _}
import uk.gov.hmrc.selfassessmentapi.domain._

case object ChildBenefits extends TaxYearPropertyType {
  override val name: String = "childBenefit"
  override val example: JsValue = toJson(ChildBenefit.example())
  override def description(action: String): String = s"$action a childBenefit"
  override val title: String = "Sample child benefit"
  override val fieldDescriptions = Seq(
    PositiveMonetaryFieldDescription(name, "amount"),
    FullFieldDescription(name, "numberOfChildren", "Int", "3", "Number of children"),
    FullFieldDescription(name, "dateBenefitStopped", "Date", "2016-04-23", "Date the child benefit stopped", optional = true)
  )

}
