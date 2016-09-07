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

package uk.gov.hmrc.selfassessmentapi.controllers.api.blindperson

import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import uk.gov.hmrc.selfassessmentapi.controllers.api._

case object BlindPersons extends TaxYearPropertyType {
  override val name: String = "blindPerson"
  override val example: JsValue = toJson(BlindPerson.example())

  override def description(action: String): String = s"$action a blindPerson"

  override val title: String = "Sample blind persons allowance"

  override val fieldDescriptions = Seq(
    FullFieldDescription(name, "country", "String", "Country of taxpayer's residence", optional = true),
    FullFieldDescription(name, "registrationAuthority", "String", "Name of the local authority or other register: is mandatory if the country of residence is England or Wales", optional = true),
    PositiveMonetaryFieldDescription(name, "spouseSurplusAllowance", "True if the taxpayer wants their spouse’s, or civil partner’s, surplus allowance", optional = true),
    FullFieldDescription(name, "wantSpouseToUseSurplusAllowance", "Boolean", "True if the taxpayer wants their spouse, or civil partner, to have their surplus allowance: can only be supplied if the person is registered blind in a country", optional = true)
  )
}
