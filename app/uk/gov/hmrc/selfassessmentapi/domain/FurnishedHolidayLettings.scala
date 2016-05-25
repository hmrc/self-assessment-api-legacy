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

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.definition.EnumJson
import uk.gov.hmrc.selfassessmentapi.domain.PropertyLocationType.PropertyLocationType

object PropertyLocationType extends Enumeration {
  type PropertyLocationType = Value
  val UK, EEA = Value
}

case class PrivateUseAdjustment(id: Option[SummaryId]=None, amount: BigDecimal)

object PrivateUseAdjustment {

  implicit val writes = Json.writes[PrivateUseAdjustment]

  implicit val reads = (
    Reads.pure(None) and
      (__ \ "amount").read[BigDecimal](positiveAmountValidator("amount"))
    ) (PrivateUseAdjustment.apply _)

  lazy val example = PrivateUseAdjustment(amount = BigDecimal(1234.34))
}

case class FurnishedHolidayLettingsAllowances(capitalAllowance: BigDecimal)

object FurnishedHolidayLettingsAllowances {
  implicit val writes = Json.writes[FurnishedHolidayLettingsAllowances]

  implicit val reads: Reads[FurnishedHolidayLettingsAllowances] = (__ \ "capitalAllowance").read[BigDecimal](positiveAmountValidator("capitalAllowance")).map {
    FurnishedHolidayLettingsAllowances(_)
  }
}

case class FurnishedHolidayLettingsAdjustments(lossBroughtForward: BigDecimal)

object FurnishedHolidayLettingsAdjustments {
  implicit val writes = Json.writes[FurnishedHolidayLettingsAdjustments]

  implicit val reads: Reads[FurnishedHolidayLettingsAdjustments] = (__ \ "lossBroughtForward").read[BigDecimal](positiveAmountValidator("lossBroughtForward")).map {
    FurnishedHolidayLettingsAdjustments(_)
  }
}


case class FurnishedHolidayLettings(id: Option[FurnishedHolidayLettingsId] = None,
                                    name: String,
                                    propertyLocation: PropertyLocationType,
                                    allowances: Option[FurnishedHolidayLettingsAllowances] = None,
                                    adjustments: Option[FurnishedHolidayLettingsAdjustments] = None)


object FurnishedHolidayLettings {

  implicit val propertyLocationTypes = EnumJson.enumFormat(PropertyLocationType, Some("Furnished holiday lettings property location type is invalid"))

  implicit val writes = Json.writes[FurnishedHolidayLettings]

  implicit val reads: Reads[FurnishedHolidayLettings] = (
    Reads.pure(None) and
      (__ \ "name").read[String](lengthValidator) and
      (__ \ "propertyLocation").read[PropertyLocationType] and
      (__ \ "allowances").readNullable[FurnishedHolidayLettingsAllowances] and
      (__ \ "adjustments").readNullable[FurnishedHolidayLettingsAdjustments]
    ) (FurnishedHolidayLettings.apply _)


  lazy val example: FurnishedHolidayLettings = FurnishedHolidayLettings(None, "Cosa del Sol apartment", PropertyLocationType.UK,
    Some(FurnishedHolidayLettingsAllowances(BigDecimal(1000.00))),
    Some(FurnishedHolidayLettingsAdjustments(BigDecimal(500.00))))
}
