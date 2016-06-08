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

package uk.gov.hmrc.selfassessmentapi.domain

import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json.Json._
import play.api.libs.json.{JsValue, _}
import uk.gov.hmrc.selfassessmentapi.domain.CountryCodes._
import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode._

case class CharitableGiving(giftAidPayments: Option[CountryAndAmount] = None,
                            oneOffGiftAidPayments: Option[CountryAndAmount] = None,
                            sharesSecurities: Option[CountryAndAmount] = None,
                            landProperties: Option[CountryAndAmount] = None,
                            giftAidPaymentsCarriedBackToPreviousYear: Option[CountryAndAmount] = None,
                            giftAidPaymentsCarriedForwardToNextYear:  Option[CountryAndAmount] = None)

object CharitableGiving extends BaseDomain[CharitableGiving] {

  private val NoCountryZeroAmount = CountryAndAmount(ZZZ, 0)

  override implicit val writes = Json.writes[CharitableGiving]

  override implicit val reads = (
    (__ \ "giftAidPayments").readNullable[CountryAndAmount] and
      (__ \ "oneOffGiftAidPayments").readNullable[CountryAndAmount] and
      (__ \ "sharesSecurities").readNullable[CountryAndAmount] and
      (__ \ "landProperties").readNullable[CountryAndAmount] and
      (__ \ "giftAidPaymentsCarriedBackToPreviousYear").readNullable[CountryAndAmount] and
      (__ \ "giftAidPaymentsCarriedForwardToNextYear").readNullable[CountryAndAmount]
    ) (CharitableGiving.apply _).filter(ValidationError("giftAidPayments must be defined if " +
    "oneOffGiftAidPayments or giftAidPaymentsCarriedBackToPreviousYear or giftAidPaymentsCarriedForwardToNextYear " +
    "is defined", UNDEFINED_REQUIRED_ELEMENT)) {
    donations => donations.giftAidPayments.isDefined &&
      (donations.oneOffGiftAidPayments.isDefined ||
        donations.giftAidPaymentsCarriedBackToPreviousYear.isDefined ||
        donations.giftAidPaymentsCarriedForwardToNextYear.isDefined)
  }.filter(ValidationError("giftAidPayments must be greater than oneOffGiftAidPayments", MAXIMUM_AMOUNT_EXCEEDED)) {
    donations =>
      val totalPayments = donations.giftAidPayments
      val oneOffPayments = donations.oneOffGiftAidPayments
      totalPayments.getOrElse(NoCountryZeroAmount).amount >= oneOffPayments.getOrElse(NoCountryZeroAmount).amount
  }.filter(ValidationError("giftAidPayments must be greater than or equal to the sum of " +
    "giftAidPaymentsCarriedBackToPreviousYear and giftAidPaymentsCarriedForwardToNextYear", MAXIMUM_AMOUNT_EXCEEDED)) {
    donations =>
      val totalPayments = donations.giftAidPayments
      val carriedBack = donations.giftAidPaymentsCarriedBackToPreviousYear
      val carriedForward = donations.giftAidPaymentsCarriedForwardToNextYear
      totalPayments.getOrElse(NoCountryZeroAmount).amount >=
        carriedBack.getOrElse(NoCountryZeroAmount).amount + carriedForward.getOrElse(NoCountryZeroAmount).amount
  }

  override def example(id: Option[SummaryId] = None) =
    CharitableGiving(giftAidPayments = Some(CountryAndAmount(GBR, 100000)),
      oneOffGiftAidPayments = Some(CountryAndAmount(USA, 5000.00)),
      sharesSecurities = Some(CountryAndAmount(CAN, 53000.00)),
      landProperties = Some(CountryAndAmount(RUS, 1000000.00)),
      giftAidPaymentsCarriedBackToPreviousYear = Some(CountryAndAmount(AUS, 2000.00)),
      giftAidPaymentsCarriedForwardToNextYear = Some(CountryAndAmount(NZL, 50000.00)))
}

case object CharitableGivingsType extends TaxYearPropertyType {
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
