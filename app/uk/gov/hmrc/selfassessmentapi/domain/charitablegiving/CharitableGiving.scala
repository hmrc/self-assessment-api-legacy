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

import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.domain._

case class GiftAidPayments( totalInTaxYear: Option[BigDecimal] = None,
                            oneOff: Option[BigDecimal] = None,
                            toNonUkCharities: Option[BigDecimal] = None,
                            carriedBackToPreviousTaxYear: Option[BigDecimal] = None,
                            carriedFromNextTaxYear: Option[BigDecimal] = None)

object GiftAidPayments extends BaseDomain[GiftAidPayments] {

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
      totalInTaxYear = Some(10000),
      oneOff = Some(5000),
      toNonUkCharities = Some(1000),
      carriedBackToPreviousTaxYear = Some(1000),
      carriedFromNextTaxYear = Some(2000)
    )
}

case class CharitableGiving( giftAidPayments: Option[GiftAidPayments] = None,
                             sharesSecurities: Option[BigDecimal] = None,
                             landProperties: Option[BigDecimal] = None,
                             qualifyingInvestmentsToNonUkCharities: Option[BigDecimal] = None)

object CharitableGiving extends BaseDomain[CharitableGiving] {

  override implicit val writes = Json.writes[CharitableGiving]

  override implicit val reads = (
      (__ \ "giftAidPayments").readNullable[GiftAidPayments] and
      (__ \ "sharesSecurities").readNullable[BigDecimal](positiveAmountValidator("sharesSecurities")) and
      (__ \ "landProperties").readNullable[BigDecimal](positiveAmountValidator("landProperties")) and
      (__ \ "qualifyingInvestmentsToNonUkCharities").readNullable[BigDecimal](positiveAmountValidator("qualifyingInvestmentsToNonUkCharities"))
    ) (CharitableGiving.apply _)

  override def example(id: Option[String] = None) =
    CharitableGiving(
      giftAidPayments = Some(GiftAidPayments.example()),
      sharesSecurities = Some(5000.00),
      landProperties = Some(100.00),
      qualifyingInvestmentsToNonUkCharities = Some(200.00)
    )
}
