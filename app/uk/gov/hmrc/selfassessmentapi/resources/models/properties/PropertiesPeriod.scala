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

package uk.gov.hmrc.selfassessmentapi.resources.models.properties

import org.joda.time.LocalDate
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, Reads, Writes, __}
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.ExpenseType.ExpenseType
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.IncomeType.IncomeType
import uk.gov.hmrc.selfassessmentapi.resources.models.{Expense, _}

case class PropertiesPeriod(from: LocalDate,
                            to: LocalDate,
                            incomes: Map[IncomeType, Income],
                            expenses: Map[ExpenseType, Expense],
                            privateUseAdjustment: Option[Amount],
                            balancingCharge: Option[Amount]) extends Period

object PropertiesPeriod extends PeriodValidator {
  import uk.gov.hmrc.selfassessmentapi.domain.JsonFormatters.PropertiesFormatters.{expenseTypeFormat, incomeTypeFormat}

  implicit val writes: Writes[PropertiesPeriod] = Json.writes[PropertiesPeriod]

  implicit val reads: Reads[PropertiesPeriod] = (
    (__ \ "from").read[LocalDate] and
      (__ \ "to").read[LocalDate] and
      (__ \ "incomes").readNullable[Map[IncomeType, Income]] and
      (__ \ "expenses").readNullable[Map[ExpenseType, Expense]] and
      (__ \ "privateUseAdjustment").readNullable[Amount](positiveAmountValidator) and
      (__ \ "balancingCharge").readNullable[Amount](positiveAmountValidator)
    ) (
    (from, to, incomes, expenses, privateUseAdjustment, balancingCharge) => {
      PropertiesPeriod(from, to, incomes.getOrElse(Map.empty), expenses.getOrElse(Map.empty), privateUseAdjustment, balancingCharge)})
    .filter(ValidationError("the period 'from' date should come before the 'to' date", ErrorCode.INVALID_PERIOD))(periodDateValidator)

}
