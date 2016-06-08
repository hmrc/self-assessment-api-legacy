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

package uk.gov.hmrc.selfassessmentapi.domain.charitablegiving

import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import uk.gov.hmrc.selfassessmentapi.domain.CountryCodes._
import uk.gov.hmrc.selfassessmentapi.domain.{CountryAndAmount, ObjectFieldDescription, TaxYearPropertyType}

case object CharitableGivings extends TaxYearPropertyType {
  override val name: String = "charitable-givings"
  override val example: JsValue = toJson(CharitableGiving.example())

  override def description(action: String): String = s"$action a charitable giving"

  override val title: String = "Sample charitable givings"

  override val fieldDescriptions = Seq(
    ObjectFieldDescription(name, "giftAidPayments", toJson(CountryAndAmount(GBR, 100000)), optional = true),
    ObjectFieldDescription(name, "oneOffGiftAidPayments", toJson(CountryAndAmount(GBR, 5000)), optional = true),
    ObjectFieldDescription(name, "sharesSecurities", toJson(CountryAndAmount(GBR, 5000)), optional = true),
    ObjectFieldDescription(name, "landProperties", toJson(CountryAndAmount(GBR, 1000000)), optional = true),
    ObjectFieldDescription(name, "giftAidPaymentsCarriedBackToPreviousYear", toJson(CountryAndAmount(GBR, 1000)), optional = true),
    ObjectFieldDescription(name, "giftAidPaymentsCarriedForwardToNextYear", toJson(CountryAndAmount(USA, 50000.00)), optional = true)
  )
}
