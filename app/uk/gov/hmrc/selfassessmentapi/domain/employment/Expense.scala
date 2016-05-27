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

package uk.gov.hmrc.selfassessmentapi.domain.employment

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.definition.EnumJson
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.domain.employment.ExpenseType.ExpenseType


object ExpenseType extends Enumeration {
  type ExpenseType = Value
  val TravelAndSubsistence, FixedDeductions, ProfessionalFees, Other = Value
}

case class Expense(id: Option[SummaryId] = None,
                   `type`: ExpenseType, amount: BigDecimal)

object Expense extends BaseDomain[Expense]{

  implicit val types = EnumJson.enumFormat(ExpenseType, Some("Employments expense type is invalid"))
  implicit val writes = Json.writes[Expense]
  implicit val reads: Reads[Expense] = (
    Reads.pure(None) and
      (__ \ "type").read[ExpenseType] and
      (__ \ "amount").read[BigDecimal](positiveAmountValidator("amount"))
    ) (Expense.apply _)

  lazy val example: Expense = Expense(None, ExpenseType.TravelAndSubsistence, BigDecimal(10000.00))

  override def example(id: SummaryId): Expense = Expense(Some(id), ExpenseType.TravelAndSubsistence, BigDecimal(10000.00))
}
