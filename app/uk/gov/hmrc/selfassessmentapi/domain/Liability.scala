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

import play.api.libs.json.Json

case class Amount(`type`: String, amount: BigDecimal)

object Amount {
  implicit val format = Json.format[Amount]
}

case class Calculation(`type`: String, amount: BigDecimal, percentage: BigDecimal, total: BigDecimal)

object Calculation {
  implicit val format = Json.format[Calculation]
}

case class Income(incomes: Seq[Amount], totalIncomeReceived: BigDecimal, personalAllowance: BigDecimal, totalTaxableIncome: BigDecimal)

object Income {
  implicit val format = Json.format[Income]
}

case class CalculatedAmount(calculations: Seq[Calculation], total: BigDecimal)

object CalculatedAmount {
  implicit val format = Json.format[CalculatedAmount]
}

case class Liability(id: Option[LiabilityId] = None, taxYear: TaxYear, income: Income, incomeTax: CalculatedAmount, credits: Seq[Amount], class4Nic: CalculatedAmount, totalTaxDue: BigDecimal)

object Liability {
  implicit val format = Json.format[Liability]
}
