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

package uk.gov.hmrc.r2.selfassessmentapi.models.properties

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.r2.selfassessmentapi.models.des

sealed trait PropertiesAnnualSummary

case class OtherPropertiesAnnualSummary(allowances: Option[OtherPropertiesAllowances],
                                        adjustments: Option[OtherPropertiesAdjustments],
                                        other: Option[OtherPropertiesOther]) extends PropertiesAnnualSummary

object OtherPropertiesAnnualSummary {
  implicit val writes: Writes[OtherPropertiesAnnualSummary] = Json.writes[OtherPropertiesAnnualSummary]

  implicit val reads: Reads[OtherPropertiesAnnualSummary] = (
    (__ \ "allowances").readNullable[OtherPropertiesAllowances] and
      (__ \ "adjustments").readNullable[OtherPropertiesAdjustments] and
      (__ \ "other").readNullable[OtherPropertiesOther]
    ) (OtherPropertiesAnnualSummary.apply _)

  def from(summary: des.OtherPropertiesAnnualSummary): OtherPropertiesAnnualSummary = {
    val allowances = for {
        allow <- summary.annualAllowances
    } yield OtherPropertiesAllowances(
        allow.annualInvestmentAllowance,
        allow.otherCapitalAllowance,
        allow.costOfReplacingDomGoods,
        allow.zeroEmissionGoodsVehicleAllowance,
        allow.businessPremisesRenovationAllowance,
        allow.propertyIncomeAllowance
    )
    val adjustments = for {
      adj <- summary.annualAdjustments
    } yield OtherPropertiesAdjustments(
      adj.lossBroughtForward,
      adj.privateUseAdjustment,
      adj.balancingCharge,
      adj.businessPremisesRenovationAllowanceBalancingCharges
    )

    val other = for {
      adj <- summary.annualAdjustments
    } yield OtherPropertiesOther(
      nonResidentLandlord = Option(adj.nonResidentLandlord),
      rarJointLet = adj.ukOtherRentARoom.map(_.jointlyLet)
    )

    OtherPropertiesAnnualSummary(allowances, adjustments, other)
  }

}

case class FHLPropertiesAnnualSummary(allowances: Option[FHLPropertiesAllowances],
                                      adjustments: Option[FHLPropertiesAdjustments],
                                      other: Option[FHLPropertiesOther]) extends PropertiesAnnualSummary

object FHLPropertiesAnnualSummary {
  implicit val writes: Writes[FHLPropertiesAnnualSummary] = Json.writes[FHLPropertiesAnnualSummary]

  implicit val reads: Reads[FHLPropertiesAnnualSummary] = (
    (__ \ "allowances").readNullable[FHLPropertiesAllowances] and
      (__ \ "adjustments").readNullable[FHLPropertiesAdjustments] and
      (__ \ "other").readNullable[FHLPropertiesOther]
    ) (FHLPropertiesAnnualSummary.apply _)

  def from(summary: des.FHLPropertiesAnnualSummary): FHLPropertiesAnnualSummary = {
    val allowances = for {
      allow <- summary.annualAllowances
    } yield FHLPropertiesAllowances(
      allow.annualInvestmentAllowance,
      allow.otherCapitalAllowance,
      allow.businessPremisesRenovationAllowance,
      allow.propertyIncomeAllowance)

    val adjustments = for {
      adj <- summary.annualAdjustments
    } yield FHLPropertiesAdjustments(
      adj.lossBroughtForward,
      adj.privateUseAdjustment,
      adj.balancingCharge,
      adj.bpraBalancingCharge,
      adj.periodOfGraceAdjustment)

    val other = for {
      other <- summary.annualOther
    } yield FHLPropertiesOther(
      other.nonResidentLandlord,
      other.rarJointLet)
      
    FHLPropertiesAnnualSummary(allowances, adjustments, other)
  }
}
