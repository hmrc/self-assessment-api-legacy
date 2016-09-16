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

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.SelfAssessmentSugar._
import uk.gov.hmrc.selfassessmentapi.controllers.api.employment.{BenefitType, ExpenseType, IncomeType}
import uk.gov.hmrc.selfassessmentapi.repositories.domain._

case class EmploymentBuilder(objectID: BSONObjectID = BSONObjectID.generate) {

  private var anEmployment: Employment = Employment(objectID, objectID.stringify, generateSaUtr(), taxYear, now, now)

  def withIncomes(incomes: (IncomeType.IncomeType, BigDecimal)*) = {
    anEmployment = anEmployment.copy(incomes = incomes.map (
      income => EmploymentIncomeSummary(objectID.stringify, income._1, income._2)))
    this
  }

  def withExpenses(expenses: (ExpenseType.ExpenseType, BigDecimal)*) = {
    anEmployment = anEmployment.copy(expenses = expenses.map (
      expense => EmploymentExpenseSummary(objectID.stringify, expense._1, expense._2)))
    this
  }

  def withBenefits(benefits: (BenefitType.BenefitType, BigDecimal)*) = {
    anEmployment = anEmployment.copy(benefits = benefits.map (
      benefit => EmploymentBenefitSummary(objectID.stringify, benefit._1, benefit._2)))
    this
  }

  def withUkTaxPaid(taxPaid: BigDecimal) = {
    anEmployment = anEmployment.copy(ukTaxPaid =
      Seq(EmploymentUkTaxPaidSummary(objectID.stringify, amount = taxPaid)))
    this
  }

  def create() = anEmployment
}
