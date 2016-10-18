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

package uk.gov.hmrc.selfassessmentapi.controllers.api.benefit

import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.api.{JsonMarshaller, ErrorCode}
import uk.gov.hmrc.selfassessmentapi.controllers.definition.EnumJson.enumFormat
import ErrorCode._
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.api.benefit.BenefitType.BenefitType

object BenefitType extends Enumeration {
  type BenefitType = Value
  val StatePension, StatePensionLumpSum, PensionsAnnuitiesPayments, TaxableIncapacityBenefit, JobSeekersAllowance, OtherTaxableStatePensions = Value
  implicit val format = enumFormat(BenefitType, Some("Unearned Income Benefit type is invalid"))
}

case class Income(id: Option[String] = None, `type`: BenefitType, amount: BigDecimal, taxDeduction: BigDecimal)

object Income extends JsonMarshaller[Income] {

  implicit val writes = Json.writes[Income]

  implicit val reads: Reads[Income] = (
    Reads.pure(None) and
      (__ \ "type").read[BenefitType] and
      (__ \ "amount").read[BigDecimal](positiveAmountValidator("amount")) and
      (__ \ "taxDeduction").read[BigDecimal](positiveAmountValidator("taxDeduction"))
    ) (Income.apply _).filter(ValidationError("taxDeduction must be less than or equal to the amount", INVALID_TAX_DEDUCTION_AMOUNT)) {
    benefits => benefits.taxDeduction <= benefits.amount
  }

  override def example(id: Option[SummaryId]) = Income(id, BenefitType.StatePension, BigDecimal(1000.00), BigDecimal(400.00))
}