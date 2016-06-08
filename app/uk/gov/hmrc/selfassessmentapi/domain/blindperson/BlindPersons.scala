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

package uk.gov.hmrc.selfassessmentapi.domain.blindperson

import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import uk.gov.hmrc.selfassessmentapi.domain.{FullFieldDescription, PositiveMonetaryFieldDescription, TaxYearPropertyType, UkCountryCodes}

case object BlindPersons extends TaxYearPropertyType {
  override val name: String = "blind-persons"
  override val example: JsValue = toJson(BlindPerson.example())

  override def description(action: String): String = s"$action a blind-person"

  override val title: String = "Sample blind persons allowance"

  override val fieldDescriptions = Seq(
    FullFieldDescription(name, "country", "String", UkCountryCodes.England.toString, "Country code"),
    FullFieldDescription(name, "registrationAuthority", "String", "Registrar", "Registration authority", optional = true),
    PositiveMonetaryFieldDescription(name, "spouseSurplusAllowance", optional = true),
    FullFieldDescription(name, "wantSpouseToUseSurplusAllowance", "Boolean", "true", "Wants spouse to use surplus allowance")
  )
}
