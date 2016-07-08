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

case class Income(sourceId: String, taxableProfit: BigDecimal, profit: BigDecimal)

object Income {
  implicit val format = Json.format[Income]
}

case class IncomeFromSources(selfEmployment: Seq[Income], employment: Seq[Income])

object IncomeFromSources {
  implicit val format = Json.format[IncomeFromSources]
}

case class IncomeSummary(incomes: IncomeFromSources, totalIncomeReceived: BigDecimal, personalAllowance: BigDecimal, totalTaxableIncome: BigDecimal)

object IncomeSummary {
  implicit val format = Json.format[IncomeSummary]
}

case class CalculatedAmount(calculations: Seq[Calculation], total: BigDecimal)

object CalculatedAmount {
  implicit val format = Json.format[CalculatedAmount]
}

case class Liability(id: Option[LiabilityId] = None, income: IncomeSummary, incomeTax: CalculatedAmount, credits: Seq[Amount], class4Nic: CalculatedAmount, totalTaxDue: BigDecimal, totalAllowancesAndReliefs: BigDecimal)

object Liability {
  implicit val format = Json.format[Liability]

  def example(id: LiabilityId): Liability =
    Liability(
      id = Some(id),
      income = IncomeSummary(
        incomes = IncomeFromSources(
          selfEmployment = Seq(
            Income("self-employment-1", 8200, 10000),
            Income("self-employment-2", 25000, 28000)
          ),
          employment = Seq(
            Income("employment-1", 5000, 5000)
          )
        ),
        totalIncomeReceived = BigDecimal(93039),
        personalAllowance = BigDecimal(9440),
        totalTaxableIncome = BigDecimal(83599)
      ),
      incomeTax = CalculatedAmount(
        calculations = Seq(
          Calculation("pay-pensions-profits", BigDecimal(32010), BigDecimal(20), BigDecimal(6402)),
          Calculation("pay-pensions-profits", BigDecimal(41030), BigDecimal(40), BigDecimal(16412)),
          Calculation("interest-received", BigDecimal(0), BigDecimal(10), BigDecimal(0)),
          Calculation("interest-received", BigDecimal(0), BigDecimal(20), BigDecimal(0)),
          Calculation("interest-received", BigDecimal(93), BigDecimal(40), BigDecimal(37.2)),
          Calculation("dividends", BigDecimal(0), BigDecimal(10), BigDecimal(0)),
          Calculation("dividends", BigDecimal(466), BigDecimal(32.5), BigDecimal(151.45))
        ),
        total = BigDecimal(23002.65)
      ),
      credits = Seq(
        Amount("dividend", BigDecimal(46.6)),
        Amount("interest-charged", BigDecimal(12.25))
      ),
      class4Nic = CalculatedAmount(
        calculations = Seq(
          Calculation("class-4-nic", BigDecimal(33695), BigDecimal(9), BigDecimal(3032.55)),
          Calculation("class-4-nic", BigDecimal(41030), BigDecimal(2), BigDecimal(820.60))
        ),
        total = BigDecimal(3853.15)
      ),
      totalTaxDue = BigDecimal(25796.95),
      totalAllowancesAndReliefs = BigDecimal(10000.00)
    )
}
