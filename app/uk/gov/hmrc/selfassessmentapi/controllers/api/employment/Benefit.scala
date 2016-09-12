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

package uk.gov.hmrc.selfassessmentapi.controllers.api.employment

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.definition.EnumJson
import uk.gov.hmrc.selfassessmentapi.controllers.api.employment.BenefitType.BenefitType


object BenefitType extends Enumeration {
  type BenefitType = Value
  val CompanyVehicle, Fuel, PrivateInsurance, VouchersCCAndExcessMileage, GoodsProvidedByEmployer,
      Accommodation, ExpensesPayments, Other = Value
  implicit val employmentBenefitTypes = EnumJson.enumFormat(BenefitType, Some("Employment Benefit type is invalid"))
}

case class Benefit(id: Option[SummaryId] = None,
                   `type`: BenefitType, amount: BigDecimal)

object Benefit extends JsonMarshaller[Benefit] {

  implicit val seExpenseTypes = EnumJson.enumFormat(BenefitType, Some("Employment Benefit type is invalid"))
  implicit val writes = Json.writes[Benefit]
  implicit val reads: Reads[Benefit] = (
    Reads.pure(None) and
      (__ \ "type").read[BenefitType] and
      (__ \ "amount").read[BigDecimal](positiveAmountValidator("amount"))
    ) (Benefit.apply _)

  override def example(id: Option[SummaryId]) = Benefit(id, BenefitType.PrivateInsurance, BigDecimal(1000))
}
