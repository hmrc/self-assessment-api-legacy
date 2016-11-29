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

package uk.gov.hmrc.selfassessmentapi.resources.models.periods

import com.github.nscala_time.time.OrderingImplicits
import org.joda.time.LocalDate
import play.api.data.validation.ValidationError
import play.api.libs.json._
import play.api.libs.functional.syntax._
import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.BalancingChargeType.BalancingChargeType
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.ExpenseType
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.ExpenseType.ExpenseType
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.IncomeType.IncomeType
import uk.gov.hmrc.selfassessmentapi.resources.models._

case class SelfEmploymentPeriod(from: LocalDate,
                                to: LocalDate,
                                incomes: Map[IncomeType, Income],
                                expenses: Map[ExpenseType, Expense],
                                balancingCharges: Map[BalancingChargeType, BalancingCharge],
                                goodsAndServicesOwnUse: Option[Amount]) extends Period

object SelfEmploymentPeriod extends PeriodValidator {
  import uk.gov.hmrc.selfassessmentapi.domain.JsonFormatters.SelfEmploymentFormatters.{expenseTypeFormat, incomeTypeFormat, balancingChargeTypeFormat}

  implicit val writes: Writes[SelfEmploymentPeriod] = Json.writes[SelfEmploymentPeriod]
  implicit val reads: Reads[SelfEmploymentPeriod] = (
      (__ \ "from").read[LocalDate] and
      (__ \ "to").read[LocalDate] and
      (__ \ "incomes").readNullable[Map[IncomeType, Income]] and
      (__ \ "expenses").readNullable[Map[ExpenseType, Expense]](depreciationValidator) and
      (__ \ "balancingCharges").readNullable[Map[BalancingChargeType, BalancingCharge]] and
      (__ \ "goodsAndServicesOwnUse").readNullable[Amount](positiveAmountValidator)
    ) (
    (from, to, income, expense, balancing, goods) => {
      SelfEmploymentPeriod(from, to, income.getOrElse(Map.empty), expense.getOrElse(Map.empty), balancing.getOrElse(Map.empty), goods)})
    .filter(ValidationError("the period 'from' date should come before the 'to' date", ErrorCode.INVALID_PERIOD))(periodDateValidator)

  private implicit val dateTimeOrder: Ordering[LocalDate] = OrderingImplicits.LocalDateOrdering
  implicit val order: Ordering[SelfEmploymentPeriod] = Ordering.by(_.from)

  private def periodDateValidator(period: SelfEmploymentPeriod) = period.from.isBefore(period.to)

  private def depreciationValidator = Reads.of[Map[ExpenseType, Expense]].filter(
    ValidationError("the disallowableAmount for depreciation expenses must be the same as the amount", ErrorCode.DEPRECIATION_DISALLOWABLE_AMOUNT)
  )(_.get(ExpenseType.Depreciation).forall(e => e.amount == e.disallowableAmount.getOrElse(false)))
}
