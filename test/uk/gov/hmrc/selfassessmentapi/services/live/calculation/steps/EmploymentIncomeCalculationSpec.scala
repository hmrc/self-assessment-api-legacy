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

import uk.gov.hmrc.selfassessmentapi.EmploymentSugar._
import uk.gov.hmrc.selfassessmentapi.SelfAssessmentSugar._
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.domain.employment.{BenefitType, ExpenseType, IncomeType}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{EmploymentIncome, MongoLiability}

class EmploymentIncomeCalculationSpec extends UnitSpec {

  private val liability = MongoLiability.create(generateSaUtr(), taxYear)
  private val employmentId1 = "e1"
  private val employmentId2 = "e2"

  "run" should {

    "calculate total employment income from multiple employment sources" in {

      val selfAssessment = SelfAssessment(
          employments = Seq(anEmployment(employmentId1).copy(
                                incomes = Seq(
                                    anIncome(IncomeType.Salary, 1000),
                                    anIncome(IncomeType.Other, 500)
                                ),
                                benefits = Seq(
                                    aBenefit(BenefitType.Accommodation, 100),
                                    aBenefit(BenefitType.Other, 400)
                                ),
                                expenses = Seq(
                                    anExpense(ExpenseType.TravelAndSubsistence, 100),
                                    anExpense(ExpenseType.ProfessionalFees, 200)
                                )
                            ),
                            anEmployment(employmentId2).copy(
                                incomes = Seq(
                                    anIncome(IncomeType.Salary, 2000),
                                    anIncome(IncomeType.Other, 1000)
                                ),
                                benefits = Seq(
                                    aBenefit(BenefitType.CompanyVehicle, 100),
                                    aBenefit(BenefitType.ExpensesPayments, 400)
                                ),
                                expenses = Seq(
                                    anExpense(ExpenseType.TravelAndSubsistence, 500),
                                    anExpense(ExpenseType.ProfessionalFees, 1000)
                                )
                            )))

      EmploymentIncomeCalculation.run(selfAssessment, liability).getLiabilityOrFail.incomeFromEmployments shouldBe Seq(
          EmploymentIncome(employmentId1, 1500, 500, 300, 1700),
          EmploymentIncome(employmentId2, 3000, 500, 1500, 2000))
    }

    "calculate total employment income from a single employment sources" in {

      val selfAssessment = SelfAssessment(
          employments = Seq(
              anEmployment(employmentId1).copy(
                  incomes = Seq(
                      anIncome(IncomeType.Salary, 1000),
                      anIncome(IncomeType.Other, 500)
                  ),
                  benefits = Seq(
                      aBenefit(BenefitType.Accommodation, 100),
                      aBenefit(BenefitType.Other, 400)
                  ),
                  expenses = Seq(
                      anExpense(ExpenseType.TravelAndSubsistence, 100),
                      anExpense(ExpenseType.ProfessionalFees, 200)
                  )
              )))

      EmploymentIncomeCalculation.run(selfAssessment, liability).getLiabilityOrFail.incomeFromEmployments shouldBe Seq(
          EmploymentIncome(employmentId1, 1500, 500, 300, 1700))
    }

    "calculate total employment income when expenses exceeds combined value of incomes and benefits" in {

      val selfAssessment = SelfAssessment(
          employments = Seq(
              anEmployment(employmentId1).copy(
                  incomes = Seq(
                      anIncome(IncomeType.Salary, 100),
                      anIncome(IncomeType.Other, 200)
                  ),
                  benefits = Seq(
                      aBenefit(BenefitType.Accommodation, 10),
                      aBenefit(BenefitType.Other, 40)
                  ),
                  expenses = Seq(
                      anExpense(ExpenseType.TravelAndSubsistence, 200),
                      anExpense(ExpenseType.ProfessionalFees, 400)
                  )
              )))

      EmploymentIncomeCalculation.run(selfAssessment, liability).getLiabilityOrFail.incomeFromEmployments shouldBe Seq(
          EmploymentIncome(employmentId1, 300, 50, 350, 0))
    }

    "calculate and round down total employment income" in {

      val selfAssessment = SelfAssessment(
          employments = Seq(
              anEmployment(employmentId1).copy(
                  incomes = Seq(
                      anIncome(IncomeType.Salary, 1000.90),
                      anIncome(IncomeType.Other, 500.75)
                  ),
                  benefits = Seq(
                      aBenefit(BenefitType.Accommodation, 100.10),
                      aBenefit(BenefitType.Other, 400.20)
                  ),
                  expenses = Seq(
                      anExpense(ExpenseType.TravelAndSubsistence, 100.10),
                      anExpense(ExpenseType.ProfessionalFees, 200.40)
                  )
              )))

      EmploymentIncomeCalculation.run(selfAssessment, liability).getLiabilityOrFail.incomeFromEmployments shouldBe Seq(
          EmploymentIncome(employmentId1, 1501.65, 500.30, 300.50, 1701))
    }
  }
}
