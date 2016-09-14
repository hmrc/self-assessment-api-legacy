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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.builders

import uk.gov.hmrc.selfassessmentapi.EmploymentSugar
import uk.gov.hmrc.selfassessmentapi.repositories.domain.Employment

/**
  * Created by Office on 13/09/2016.
  */
case class EmploymentBuilder() {
  import uk.gov.hmrc.selfassessmentapi.controllers.api.employment.{BenefitType, ExpenseType, IncomeType}

  private var anEmployment: Employment = EmploymentSugar.anEmployment()

  def withIncomes(incomes: (IncomeType.IncomeType, BigDecimal)*) = {
    anEmployment = anEmployment.copy(incomes = incomes.map (income => EmploymentSugar.anIncome(income._1, income._2)))
    this
  }

  def withExpenses(expenses: (ExpenseType.ExpenseType, BigDecimal)*) = {
    anEmployment = anEmployment.copy(expenses = expenses.map (expense => EmploymentSugar.anExpense(expense._1, expense._2)))
    this
  }

  def withBenefits(benefits: (BenefitType.BenefitType, BigDecimal)*) = {
    anEmployment = anEmployment.copy(benefits = benefits.map (benefit => EmploymentSugar.aBenefit(benefit._1, benefit._2)))
    this
  }

  def withUkTaxPaid(taxPaid: BigDecimal) = {
    anEmployment = anEmployment.copy(ukTaxPaid = Seq(EmploymentSugar.aUkTaxPaidSummary(amount = taxPaid)))
    this
  }

  def create() = anEmployment
}
