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

package uk.gov.hmrc.selfassessmentapi.models.selfemployment

import play.api.data.validation.ValidationError
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import uk.gov.hmrc.selfassessmentapi.models.Validation._
import uk.gov.hmrc.selfassessmentapi.models.{Amount, ErrorCode, Validation, _}


case class Expenses(costOfGoodsBought: Option[Amount] = None,
                    cisPaymentsToSubcontractors: Option[Amount] = None,
                    staffCosts: Option[Amount] = None,
                    travelCosts: Option[Amount] = None,
                    premisesRunningCosts: Option[Amount] = None,
                    maintenanceCosts: Option[Amount] = None,
                    adminCosts: Option[Amount] = None,
                    advertisingCosts: Option[Amount] = None,
                    businessEntertainmentCosts: Option[Amount] = None,
                    interest: Option[Amount] = None,
                    financialCharges: Option[Amount] = None,
                    badDebt: Option[Amount] = None,
                    professionalFees: Option[Amount] = None,
                    depreciation: Option[Amount] = None,
                    other: Option[Amount] = None) {

  def hasExpenses: Boolean =
    costOfGoodsBought.isDefined ||
      cisPaymentsToSubcontractors.isDefined ||
      staffCosts.isDefined ||
      travelCosts.isDefined ||
      premisesRunningCosts.isDefined ||
      maintenanceCosts.isDefined ||
      adminCosts.isDefined ||
      advertisingCosts.isDefined ||
      businessEntertainmentCosts.isDefined ||
      interest.isDefined ||
      financialCharges.isDefined ||
      badDebt.isDefined ||
      professionalFees.isDefined ||
      depreciation.isDefined ||
      other.isDefined
}

object Expenses {

  private def onError(field: String) = ValidationError(s"The $field should be a non-negative number less than 99999999999999.98 with up to 2 decimal places", ErrorCode.INVALID_MONETARY_AMOUNT)
  private def validIf(amount: Amount) = amount >= 0 && amount.scale < 3 && amount <= MAX_AMOUNT

  implicit val writes: Writes[Expenses] = Json.writes[Expenses]
  implicit val reads: Reads[Expenses] = Json.reads[Expenses]
    .validate(
    Seq(
      Validation[Expenses](JsPath \ "costOfGoodsBought", _.costOfGoodsBought.forall(validIf), onError("costOfGoodsBought")),
      Validation[Expenses](JsPath \ "cisPaymentsToSubcontractors", _.cisPaymentsToSubcontractors.forall(validIf), onError("cisPaymentsToSubcontractors")),
      Validation[Expenses](JsPath \ "staffCosts", _.staffCosts.forall(validIf), onError("staffCosts")),
      Validation[Expenses](JsPath \ "travelCosts", _.travelCosts.forall(validIf), onError("travelCosts")),
      Validation[Expenses](JsPath \ "premisesRunningCosts", _.premisesRunningCosts.forall(validIf), onError("premisesRunningCosts")),
      Validation[Expenses](JsPath \ "maintenanceCosts", _.maintenanceCosts.forall(validIf), onError("maintenanceCosts")),
      Validation[Expenses](JsPath \ "adminCosts", _.adminCosts.forall(validIf), onError("adminCosts")),
      Validation[Expenses](JsPath \ "advertisingCosts", _.advertisingCosts.forall(validIf), onError("advertisingCosts")),
      Validation[Expenses](JsPath \ "businessEntertainmentCosts", _.businessEntertainmentCosts.forall(validIf), onError("businessEntertainmentCosts")),
      Validation[Expenses](JsPath \ "interest", _.interest.forall(validIf), onError("interest")),
      Validation[Expenses](JsPath \ "financialCharges", _.financialCharges.forall(validIf), onError("financialCharges")),
      Validation[Expenses](JsPath \ "badDebt", _.badDebt.forall(validIf), onError("badDebt")),
      Validation[Expenses](JsPath \ "professionalFees", _.professionalFees.forall(validIf), onError("professionalFees")),
      Validation[Expenses](JsPath \ "depreciation", _.depreciation.forall(validIf), onError("depreciation")),
      Validation[Expenses](JsPath \ "other", _.other.forall(validIf), onError("other"))
    )
  )
}
