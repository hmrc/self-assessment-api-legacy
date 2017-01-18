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

package uk.gov.hmrc.selfassessmentapi.resources.models.properties

import org.joda.time.LocalDate
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.resources.models._
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.ExpenseType.ExpenseType
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.FHLExpenseType.FHLExpenseType
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.FHLIncomeType.FHLIncomeType
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.IncomeType.IncomeType

case class FHLProperties(from: LocalDate, to: LocalDate, data: FHLPeriodicData) extends Period

object FHLProperties extends PeriodValidator[FHLProperties] {
  import uk.gov.hmrc.selfassessmentapi.domain.JsonFormatters.PropertiesFormatters.{fhlExpenseTypeFormat, fhlIncomeTypeFormat}

  implicit val writes = new Writes[FHLProperties] {
    override def writes(o: FHLProperties): JsValue = {
      Json.obj(
        "from" -> o.from,
        "to" -> o.to,
        "incomes" -> o.data.incomes,
        "expenses" -> o.data.expenses
      )
    }
  }

  implicit val reads: Reads[FHLProperties] = (
    (__ \ "from").read[LocalDate] and
      (__ \ "to").read[LocalDate] and
      (__ \ "incomes").readNullable[Map[FHLIncomeType, SimpleIncome]] and
      (__ \ "expenses").readNullable[Map[FHLExpenseType, SimpleExpense]]
  )((from, to, incomes, expenses) => {
    FHLProperties(from, to, FHLPeriodicData(incomes.getOrElse(Map.empty), expenses.getOrElse(Map.empty)))
  }).filter(ValidationError("the period 'from' date should come before the 'to' date", ErrorCode.INVALID_PERIOD))(
    periodDateValidator)

}

case class OtherProperties(from: LocalDate, to: LocalDate, data: OtherPeriodicData) extends Period

object OtherProperties extends PeriodValidator[OtherProperties] {
  import uk.gov.hmrc.selfassessmentapi.domain.JsonFormatters.PropertiesFormatters.{expenseTypeFormat, incomeTypeFormat}

  implicit val writes = new Writes[OtherProperties] {
    override def writes(o: OtherProperties): JsValue = {
      Json.obj(
        "from" -> o.from,
        "to" -> o.to,
        "incomes" -> o.data.incomes,
        "expenses" -> o.data.expenses
      )
    }
  }

  implicit val reads: Reads[OtherProperties] = (
    (__ \ "from").read[LocalDate] and
      (__ \ "to").read[LocalDate] and
      (__ \ "incomes").readNullable[Map[IncomeType, Income]] and
      (__ \ "expenses").readNullable[Map[ExpenseType, SimpleExpense]]
  )((from, to, incomes, expenses) => {
    OtherProperties(from, to, OtherPeriodicData(incomes.getOrElse(Map.empty), expenses.getOrElse(Map.empty)))
  }).filter(ValidationError("the period 'from' date should come before the 'to' date", ErrorCode.INVALID_PERIOD))(
    periodDateValidator)

}

case class FHLPeriodicData(incomes: Map[FHLIncomeType, SimpleIncome], expenses: Map[FHLExpenseType, SimpleExpense])
    extends PeriodicData

object FHLPeriodicData {
  import uk.gov.hmrc.selfassessmentapi.domain.JsonFormatters.PropertiesFormatters.{fhlExpenseTypeFormat, fhlIncomeTypeFormat}

  implicit val writes: Writes[FHLPeriodicData] = Json.writes[FHLPeriodicData]

  implicit val reads: Reads[FHLPeriodicData] = (
    (__ \ "incomes").readNullable[Map[FHLIncomeType, SimpleIncome]] and
      (__ \ "expenses").readNullable[Map[FHLExpenseType, SimpleExpense]]
  )((incomes, expenses) => {
    FHLPeriodicData(incomes.getOrElse(Map.empty), expenses.getOrElse(Map.empty))
  })
}

case class OtherPeriodicData(incomes: Map[IncomeType, Income], expenses: Map[ExpenseType, SimpleExpense])
    extends PeriodicData

object OtherPeriodicData {
  import uk.gov.hmrc.selfassessmentapi.domain.JsonFormatters.PropertiesFormatters.{expenseTypeFormat, incomeTypeFormat}

  implicit val writes: Writes[OtherPeriodicData] = Json.writes[OtherPeriodicData]

  implicit val reads: Reads[OtherPeriodicData] = (
    (__ \ "incomes").readNullable[Map[IncomeType, Income]] and
      (__ \ "expenses").readNullable[Map[ExpenseType, SimpleExpense]]
  )((incomes, expenses) => {
    OtherPeriodicData(incomes.getOrElse(Map.empty), expenses.getOrElse(Map.empty))
  })
}
