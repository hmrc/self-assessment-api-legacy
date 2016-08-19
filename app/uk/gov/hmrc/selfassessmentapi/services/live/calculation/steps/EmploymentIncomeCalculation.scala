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

package uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps

import uk.gov.hmrc.selfassessmentapi.repositories.domain.{EmploymentIncome, LiabilityResult, MongoLiability}
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps.Math._

object EmploymentIncomeCalculation extends CalculationStep {

  override def run(selfAssessment: SelfAssessment, liability: MongoLiability): LiabilityResult = {

    val incomeFromEmployment = selfAssessment.employments.map { employment =>
      val sumIncome = employment.incomes.map(_.amount).sum
      val sumBenefits = employment.benefits.map(_.amount).sum
      val sumExpenses = employment.expenses.map(_.amount).sum
      val cappedExpenses = capAt(sumExpenses, sumIncome + sumBenefits)
      val totalIncome = roundDown(positiveOrZero(sumIncome + sumBenefits - cappedExpenses))
      EmploymentIncome(sourceId = employment.sourceId, pay = sumIncome, benefitsAndExpenses = sumBenefits, allowableExpenses = cappedExpenses, total = totalIncome)
    }

    liability.copy(incomeFromEmployments = incomeFromEmployment)
  }

}