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
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.definition.EnumJson
import uk.gov.hmrc.selfassessmentapi.domain.SelfEmploymentExpenseCategory.SelfEmploymentExpenseCategory


object SelfEmploymentExpenseCategory extends Enumeration {
  type SelfEmploymentExpenseCategory = Value
  val CoGBought, CISPayments, StaffCosts, TravelCosts, PremissesRunnigCosts, MaintenanceCosts, AdminCosts,
  AdvertisingCosts, Internet, FinancialCharges, BadDept, ProfessionalFees, Deprecation, Other = Value
}

case class SelfEmploymentExpense(id: Option[SelfEmploymentExpenseId] = None,
                                 taxYear: String, category: SelfEmploymentExpenseCategory, amount: BigDecimal)

object SelfEmploymentExpense {

  implicit val seIncomeTypes = EnumJson.enumFormat(SelfEmploymentExpenseCategory)
  implicit val seIncomeWrites = Json.writes[SelfEmploymentExpense]
  implicit val seIncomeReads: Reads[SelfEmploymentExpense] = (
    (__ \ "id").readNullable[SelfEmploymentExpenseId] and
      (__ \ "taxYear").read[String](taxYearValidator) and
      (__ \ "category").read[SelfEmploymentExpenseCategory] and
      (__ \ "amount").read[BigDecimal](amountValidator)
    ) (SelfEmploymentExpense.apply _)

}
