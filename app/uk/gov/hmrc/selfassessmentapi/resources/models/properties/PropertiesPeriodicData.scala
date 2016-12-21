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

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.resources.models._
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.ExpenseType.ExpenseType
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.IncomeType.IncomeType

case class PropertiesPeriodicData(incomes: Map[IncomeType, Income],
                                  expenses: Map[ExpenseType, SimpleExpense]) extends PeriodicData

object PropertiesPeriodicData {

  import uk.gov.hmrc.selfassessmentapi.domain.JsonFormatters.PropertiesFormatters.{expenseTypeFormat, incomeTypeFormat}

  implicit val writes: Writes[PropertiesPeriodicData] = Json.writes[PropertiesPeriodicData]

  implicit val reads: Reads[PropertiesPeriodicData] = (
      (__ \ "incomes").readNullable[Map[IncomeType, Income]] and
      (__ \ "expenses").readNullable[Map[ExpenseType, SimpleExpense]]
    ) ((incomes, expenses) => {
        PropertiesPeriodicData(incomes.getOrElse(Map.empty), expenses.getOrElse(Map.empty))
    }
  )

}
