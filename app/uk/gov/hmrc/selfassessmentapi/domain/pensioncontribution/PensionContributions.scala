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

package uk.gov.hmrc.selfassessmentapi.domain.pensioncontribution

import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import uk.gov.hmrc.selfassessmentapi.domain.{PositiveMonetaryFieldDescription, TaxYearPropertyType}

case object PensionContributions extends TaxYearPropertyType {
  override val name: String = "pension-contributions"
  override val example: JsValue = toJson(PensionContribution.example())

  override def description(action: String): String = s"$action a pension-contribution"

  override val title: String = "Sample pension contributions"

  override val fieldDescriptions = Seq(
    PositiveMonetaryFieldDescription(name, "ukRegisteredPension", optional = true),
    PositiveMonetaryFieldDescription(name, "retirementAnnuity", optional = true),
    PositiveMonetaryFieldDescription(name, "employerScheme", optional = true),
    PositiveMonetaryFieldDescription(name, "overseasPension", optional = true)
  )
}
