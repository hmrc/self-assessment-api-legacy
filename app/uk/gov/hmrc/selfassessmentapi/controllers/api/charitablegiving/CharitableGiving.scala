/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.controllers.api.charitablegiving

import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.controllers.api._

case class GiftAidPayments( totalInTaxYear: Option[BigDecimal] = None,
                            oneOff: Option[BigDecimal] = None,
                            toNonUkCharities: Option[BigDecimal] = None,
                            carriedBackToPreviousTaxYear: Option[BigDecimal] = None,
                            carriedFromNextTaxYear: Option[BigDecimal] = None)

object GiftAidPayments extends JsonMarshaller[GiftAidPayments] {

  override implicit val writes = Json.writes[GiftAidPayments]

  override implicit val reads = (
      (__ \ "totalInTaxYear").readNullable[BigDecimal](positiveAmountValidator("totalInTaxYear")) and
      (__ \ "oneOff").readNullable[BigDecimal](positiveAmountValidator("oneOff")) and
      (__ \ "toNonUkCharities").readNullable[BigDecimal](positiveAmountValidator("toNonUkCharities")) and
      (__ \ "carriedBackToPreviousTaxYear").readNullable[BigDecimal](positiveAmountValidator("carriedBackToPreviousTaxYear")) and
      (__ \ "carriedFromNextTaxYear").readNullable[BigDecimal](positiveAmountValidator("carriedFromNextTaxYear"))
    ) (GiftAidPayments.apply _)
    .filter(
      ValidationError("totalInTaxYear must be defined if oneOff or toNonUkCharities or carriedBackToPreviousTaxYear is defined", UNDEFINED_REQUIRED_ELEMENT)
    ) { p =>
      p.totalInTaxYear.isDefined || (p.oneOff.isEmpty && p.toNonUkCharities.isEmpty && p.carriedBackToPreviousTaxYear.isEmpty)
    }
    .filter(
      ValidationError("totalInTaxYear must be greater or equal to oneOff", MAXIMUM_AMOUNT_EXCEEDED)
    ) { p =>
      p.totalInTaxYear.getOrElse(BigDecimal(0)) >= p.oneOff.getOrElse(BigDecimal(0))
    }
    .filter(
      ValidationError("totalInTaxYear must be greater or equal to toNonUkCharities", MAXIMUM_AMOUNT_EXCEEDED)
    ) { p =>
      p.totalInTaxYear.getOrElse(BigDecimal(0)) >= p.toNonUkCharities.getOrElse(BigDecimal(0))
    }
    .filter(
      ValidationError("totalInTaxYear must be greater or equal to carriedBackToPreviousTaxYear", MAXIMUM_AMOUNT_EXCEEDED)
    ) { p =>
      p.totalInTaxYear.getOrElse(BigDecimal(0)) >= p.carriedBackToPreviousTaxYear.getOrElse(BigDecimal(0))
    }

  override def example(id: Option[String] = None) =
    GiftAidPayments(
      totalInTaxYear = Some(10000.00),
      oneOff = Some(5000.00),
      toNonUkCharities = Some(1000.00),
      carriedBackToPreviousTaxYear = Some(1000.00),
      carriedFromNextTaxYear = Some(2000.00)
    )
}

case class SharesAndSecurities( totalInTaxYear: BigDecimal,
                                toNonUkCharities: Option[BigDecimal] = None)

object SharesAndSecurities extends JsonMarshaller[SharesAndSecurities] {

  override implicit val writes = Json.writes[SharesAndSecurities]

  override implicit val reads = (
      (__ \ "totalInTaxYear").read[BigDecimal](positiveAmountValidator("totalInTaxYear")) and
      (__ \ "toNonUkCharities").readNullable[BigDecimal](positiveAmountValidator("toNonUkCharities"))
    ) (SharesAndSecurities.apply _)
    .filter(
      ValidationError("totalInTaxYear must be greater or equal to toNonUkCharities", MAXIMUM_AMOUNT_EXCEEDED)
    ) { p =>
      p.totalInTaxYear >= p.toNonUkCharities.getOrElse(BigDecimal(0))
    }

  override def example(id: Option[String] = None) =
    SharesAndSecurities(
      totalInTaxYear = 2000.00,
      toNonUkCharities = Some(500.00)
    )
}

case class LandAndProperties( totalInTaxYear: BigDecimal,
                              toNonUkCharities: Option[BigDecimal] = None)

object LandAndProperties extends JsonMarshaller[LandAndProperties] {

  override implicit val writes = Json.writes[LandAndProperties]

  override implicit val reads = (
      (__ \ "totalInTaxYear").read[BigDecimal](positiveAmountValidator("totalInTaxYear")) and
      (__ \ "toNonUkCharities").readNullable[BigDecimal](positiveAmountValidator("toNonUkCharities"))
    ) (LandAndProperties.apply _)
    .filter(
      ValidationError("totalInTaxYear must be greater or equal to toNonUkCharities", MAXIMUM_AMOUNT_EXCEEDED)
    ) { p =>
      p.totalInTaxYear >= p.toNonUkCharities.getOrElse(BigDecimal(0))
    }

  override def example(id: Option[String] = None) =
    LandAndProperties(
      totalInTaxYear = 4000.00,
      toNonUkCharities = Some(3000.00)
    )
}

case class CharitableGiving( giftAidPayments: Option[GiftAidPayments] = None,
                             sharesSecurities: Option[SharesAndSecurities] = None,
                             landProperties: Option[LandAndProperties] = None)

object CharitableGiving extends JsonMarshaller[CharitableGiving] {

  override implicit val writes = Json.writes[CharitableGiving]

  override implicit val reads = (
      (__ \ "giftAidPayments").readNullable[GiftAidPayments] and
      (__ \ "sharesSecurities").readNullable[SharesAndSecurities]and
      (__ \ "landProperties").readNullable[LandAndProperties]
    ) (CharitableGiving.apply _)

  override def example(id: Option[String] = None) =
    CharitableGiving(
      giftAidPayments = Some(GiftAidPayments.example()),
      sharesSecurities = Some(SharesAndSecurities.example()),
      landProperties = Some(LandAndProperties.example())
    )
}
