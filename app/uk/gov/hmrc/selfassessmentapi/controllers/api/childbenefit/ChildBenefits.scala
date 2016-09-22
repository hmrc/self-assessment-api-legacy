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

package uk.gov.hmrc.selfassessmentapi.controllers.api.childbenefit

import play.api.libs.json.Json.toJson
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.api.{ErrorCode, FullFieldDescription, PositiveMonetaryFieldDescription}
import ErrorCode.{apply => _}
import uk.gov.hmrc.selfassessmentapi.controllers.api._

case object ChildBenefits extends TaxYearPropertyType {
  override val name: String = "childBenefit"
  override val documentationName = "Child Benefit"
  override val example: JsValue = toJson(ChildBenefit.example())
  override def description(action: String): String = s"$action a childBenefit"
  override val title: String = "Sample child benefit"
  override val fieldDescriptions = Seq(
    PositiveMonetaryFieldDescription(name, "amount", "Total amount of Child Benefit taxpayer and their partner got for the tax year"),
    FullFieldDescription(name, "numberOfChildren", "Int", "Number of children taxpayer and their partner got Child Benefit for"),
    FullFieldDescription(name, "dateBenefitStopped", "Date", "The date that taxpayer and their partner stopped getting all Child Benefit payments", optional = true)
  )

}
