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

package uk.gov.hmrc.selfassessmentapi.models.giftaid

import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json.{Writes, __, _}
import uk.gov.hmrc.selfassessmentapi.models.{ErrorCode, Validation, des}
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.models.Validation._

case class GiftAidPayments(totalPayments: Option[BigDecimal] = None,
                           totalOneOffPayments: Option[BigDecimal] = None,
                           totalPaymentsBeforeTaxYearStart: Option[BigDecimal] = None,
                           totalPaymentsAfterTaxYearEnd: Option[BigDecimal] = None,
                           sharesOrSecurities: Option[BigDecimal] = None,
                           ukCharityGift: Option[GiftAidUKCharityPayments] = None,
                           nonUKCharityGift: Option[GiftAidNonUKCharityPayments] = None
                          ) {
  def hasPayments = totalPayments.isDefined || totalOneOffPayments.isDefined ||
    totalPaymentsBeforeTaxYearStart.isDefined || totalPaymentsAfterTaxYearEnd.isDefined ||
    sharesOrSecurities.isDefined || ukCharityGift.isDefined || nonUKCharityGift.isDefined
}

object GiftAidPayments{

  implicit val writes: Writes[GiftAidPayments] = Json.writes[GiftAidPayments]

  implicit val reads: Reads[GiftAidPayments] = (
    (__ \ "totalPayments").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "totalOneOffPayments").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "totalPaymentsBeforeTaxYearStart").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "totalPaymentsAfterTaxYearEnd").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "sharesOrSecurities").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "ukCharityGift").readNullable[GiftAidUKCharityPayments] and
      (__ \ "nonUKCharityGift").readNullable[GiftAidNonUKCharityPayments]
    )(GiftAidPayments.apply _)
    .filter(ValidationError(s"Gift aid payments provided are invalid and cannot not be processed",
      ErrorCode.INVALID_REQUEST))(_.hasPayments)
    .validate(Seq(
     Validation(JsPath(),
        totalPaymentsValidator,
        ValidationError("Gift aid totalPayments cannot be less than the sum of totalOneOffPayments and nonUKCharityGift payments", ErrorCode.TOTAL_PAYMENTS_LESS))))

  def totalPaymentsValidator(giftAidPayments: GiftAidPayments): Boolean =
    if(giftAidPayments.totalPayments.isDefined || giftAidPayments.nonUKCharityGift.exists(_.payments.isDefined)
            || giftAidPayments.totalOneOffPayments.isDefined) {
      giftAidPayments.totalPayments.exists(_.>=(giftAidPayments.totalOneOffPayments.getOrElse(BigDecimal(0)) +
        giftAidPayments.nonUKCharityGift.map{ nonCharity => nonCharity.payments.getOrElse(BigDecimal(0))}.getOrElse(0)))
    }
    else
        true

  def from(desGAP: des.giftaid.GiftAidPayments): GiftAidPayments = {
    GiftAidPayments(totalPayments = desGAP.totalPayments,
      totalOneOffPayments = desGAP.totalOneOffPayments,
      totalPaymentsBeforeTaxYearStart = desGAP.totalPaymentsBeforeTaxYearStart,
      totalPaymentsAfterTaxYearEnd = desGAP.totalPaymentsAfterTaxYearEnd,
      sharesOrSecurities = desGAP.sharesOrSecurities,
      ukCharityGift = GiftAidUKCharityPayments.from(desGAP.ukCharityGift),
      nonUKCharityGift = GiftAidNonUKCharityPayments.from(desGAP.nonUKCharityGift))
  }
}

case class GiftAidUKCharityPayments(landAndBuildings: BigDecimal)

object GiftAidUKCharityPayments {

  implicit val writes: Writes[GiftAidUKCharityPayments] = Json.writes[GiftAidUKCharityPayments]

  implicit val reads: Reads[GiftAidUKCharityPayments] =
    (__ \ "landAndBuildings").read[BigDecimal](nonNegativeAmountValidator).map(GiftAidUKCharityPayments(_))

  def from(desGAP: Option[des.giftaid.GiftAidUKCharityPayments]): Option[GiftAidUKCharityPayments] = {
    if(desGAP.isDefined)
      Some(GiftAidUKCharityPayments(landAndBuildings = desGAP.get.landAndBuildings))
    else
      None
  }
}

case class GiftAidNonUKCharityPayments(investments: Option[BigDecimal] = None, payments: Option[BigDecimal] = None){
  def hasCharities = investments.isDefined || payments.isDefined
}

object GiftAidNonUKCharityPayments {

  implicit val writes: Writes[GiftAidNonUKCharityPayments] = Json.writes[GiftAidNonUKCharityPayments]

  implicit val reads: Reads[GiftAidNonUKCharityPayments] = (
    (__ \ "investments").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "payments").readNullable[BigDecimal](nonNegativeAmountValidator)
    )(GiftAidNonUKCharityPayments.apply _)
    .filter(ValidationError(s"Gift aid non UK charity payments provided are invalid and cannot be processed",
      ErrorCode.INVALID_GIFT_AID_PAYMENTS))(_.hasCharities)

  def from(desGAP: Option[des.giftaid.GiftAidNonUKCharityPayments]): Option[GiftAidNonUKCharityPayments] = {
    if(desGAP.exists(_.hasCharities))
      Some(GiftAidNonUKCharityPayments(investments = desGAP.get.investments, payments = desGAP.get.payments))
    else
      None
  }
}