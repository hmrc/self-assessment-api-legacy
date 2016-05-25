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

case class UKPropertyAllowances(annualInvestmentAllowance: Option[BigDecimal] = None,
                                businessPremisesRenovationAllowance: Option[BigDecimal] = None,
                                otherCapitalAllowance: Option[BigDecimal] = None,
                                wearAndTearAllowance: Option[BigDecimal] = None
                               )

object UKPropertyAllowances {
  implicit val writes = Json.writes[UKPropertyAllowances]

  implicit val reads: Reads[UKPropertyAllowances] = (
    (__ \ "annualInvestmentAllowance").readNullable[BigDecimal](positiveAmountValidator("annualInvestmentAllowance")) and
      (__ \ "businessPremisesRenovationAllowance").readNullable[BigDecimal](positiveAmountValidator("businessPremisesRenovationAllowance")) and
      (__ \ "otherCapitalAllowance").readNullable[BigDecimal](positiveAmountValidator("otherCapitalAllowance")) and
      (__ \ "wearAndTearAllowance").readNullable[BigDecimal](positiveAmountValidator("wearAndTearAllowance"))
    ) (UKPropertyAllowances.apply _)
}

case class UKPropertyAdjustments(lossBroughtForward: Option[BigDecimal] = None)

object UKPropertyAdjustments {
  implicit val writes = Json.writes[UKPropertyAdjustments]

  implicit val reads: Reads[UKPropertyAdjustments] = (__ \ "lossBroughtForward").readNullable[BigDecimal](positiveAmountValidator("lossBroughtForward")).map {
    UKPropertyAdjustments(_)
  }
}


case class UKProperty(id: Option[SourceId] = None,
                      name: String,
                      allowances: Option[UKPropertyAllowances] = None,
                      adjustments: Option[UKPropertyAdjustments] = None,
                      rentARoomRelief: Option[BigDecimal] = None)


object UKProperty {

  implicit val propertyLocationTypes = EnumJson.enumFormat(PropertyLocationType, Some("Furnished holiday lettings property location type is invalid"))

  implicit val writes = Json.writes[UKProperty]

  implicit val reads: Reads[UKProperty] = (
    Reads.pure(None) and
      (__ \ "name").read[String](lengthValidator) and
      (__ \ "allowances").readNullable[UKPropertyAllowances] and
      (__ \ "adjustments").readNullable[UKPropertyAdjustments] and
      (__ \ "rentARoomRelief").readNullable[BigDecimal](positiveAmountValidator("rentARoomRelief"))
    ) (UKProperty.apply _)


  lazy val example: UKProperty = UKProperty(
    None,
    "London Apartment",
    Some(UKPropertyAllowances(Some(BigDecimal(1000.00)), Some(BigDecimal(600.00)), Some(BigDecimal(50.00)), Some(BigDecimal(3399.99)))),
    Some(UKPropertyAdjustments(Some(BigDecimal(250.00)))),
    Some(BigDecimal(7500.00)))
}
