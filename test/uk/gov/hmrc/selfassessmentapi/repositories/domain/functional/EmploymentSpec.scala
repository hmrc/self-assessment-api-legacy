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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.functional

import uk.gov.hmrc.selfassessmentapi.domain.employment.{BenefitType, ExpenseType, IncomeType}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{EmploymentIncome, MongoUkTaxPaidForEmployment}
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps.SelfAssessment
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.EmploymentSugar._
import uk.gov.hmrc.selfassessmentapi.domain.UkTaxPaidForEmployment

class EmploymentSpec extends UnitSpec {

  private val employmentId1 = "e1"
  private val employmentId2 = "e2"

  "TotalProfit" should {
    "be sum of rounded down profit from each employment" in {
      val selfAssessment = SelfAssessment(
        employments = Seq(anEmployment(employmentId1).copy(
          incomes = Seq(
            anIncome(IncomeType.Salary, 1000.12),
            anIncome(IncomeType.Other, 500.65)
          )
        ),
          anEmployment(employmentId2).copy(
            incomes = Seq(
              anIncome(IncomeType.Salary, 2000.45),
              anIncome(IncomeType.Other, 1000.23)
            )
          )))

      Employment.TotalProfit(selfAssessment) shouldBe 4500
    }
  }

  "Profit" should {

    "be equal to Income + Benefits - Expenses" in {

      val employment = anEmployment(employmentId1).copy(
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
      )

      Employment.Profit(employment) shouldBe 1700
    }

    "be 0 when expenses exceeds combined value of incomes and benefits" in {

      val employment =
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
        )

      Employment.Profit(employment) shouldBe 0
    }

    "be round down to nearest pound" in {
      val employment =
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
        )

      Employment.Profit(employment) shouldBe 1701
    }
  }

  "TotalTaxPaid" should {
    "return a value of 0 when the total tax paid is negative" in {
      val employment = anEmployment().copy(
        ukTaxPaid = Seq(anUkTaxPaidSummary(amount = -500),
                        anUkTaxPaidSummary(amount = -250)))

      val sa = SelfAssessment(employments = Seq(employment))

      Employment.TotalTaxPaid(sa) shouldBe 0
    }
  }

  "employment incomes" should {

    "be calculated from multiple employment sources" in {

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

      Employment.Incomes(selfAssessment) shouldBe Seq(
        EmploymentIncome(employmentId1, 1500, 500, 300, 1700),
        EmploymentIncome(employmentId2, 3000, 500, 1500, 2000))
    }

    "be calculated from a single employment sources" in {

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

      Employment.Incomes(selfAssessment) shouldBe Seq(
        EmploymentIncome(employmentId1, 1500, 500, 300, 1700))
    }
  }


  "tax paid on employments" should {

    "return the UK tax paid as zero and an empty list of UK taxes paid for employments if there are no employments" in {
      Employment.TaxesPaid(SelfAssessment(employments = Nil)) shouldBe empty
    }


    "return the UK tax paid as zero if the sum of UK taxes paid is zero" in {
      val employment1UkTaxPaidSummary1 = anUkTaxPaidSummary("ukTaxPaid1", -112.45)
      val employment1ukTaxPaidSummary2 = anUkTaxPaidSummary("ukTaxPaid2", -934.87)
      val employment2UkTaxPaidSummary1 = anUkTaxPaidSummary("ukTaxPaid1", 0)
      val employment2ukTaxPaidSummary2 = anUkTaxPaidSummary("ukTaxPaid2", 0)
      val employment1 =
        anEmployment().copy(ukTaxPaid = Seq(employment1UkTaxPaidSummary1, employment1ukTaxPaidSummary2))
      val employment2 =
        anEmployment().copy(ukTaxPaid = Seq(employment2UkTaxPaidSummary1, employment2ukTaxPaidSummary2))

      Employment.TaxesPaid(SelfAssessment(employments = Seq(employment1, employment2))) shouldBe
        Seq(UkTaxPaidForEmployment(employment1.sourceId, -1047.32),
          UkTaxPaidForEmployment(employment2.sourceId, 0))
    }

    "throw a calculation exception if none of the sum of the UK tax paid for a given employment is positive" in {
      val employment1UkTaxPaidSummary1 = anUkTaxPaidSummary("ukTaxPaid1", 112.45)
      val employment1ukTaxPaidSummary2 = anUkTaxPaidSummary("ukTaxPaid2", -934.87)
      val employment2UkTaxPaidSummary1 = anUkTaxPaidSummary("ukTaxPaid1", 199.45)
      val employment2ukTaxPaidSummary2 = anUkTaxPaidSummary("ukTaxPaid2", -300.87)
      val employment1 =
        anEmployment().copy(ukTaxPaid = Seq(employment1UkTaxPaidSummary1, employment1ukTaxPaidSummary2))
      val employment2 =
        anEmployment().copy(ukTaxPaid = Seq(employment2UkTaxPaidSummary1, employment2ukTaxPaidSummary2))

      a[LiabilityCalculationException] shouldBe thrownBy {Employment.TaxesPaid(SelfAssessment(employments = Seq(employment1, employment2)))}
    }

    "cap the UK tax paid at zero if the total tax paid is not positive" in {
      val employment1UkTaxPaidSummary1 = anUkTaxPaidSummary("ukTaxPaid1", -112.45)
      val employment1ukTaxPaidSummary2 = anUkTaxPaidSummary("ukTaxPaid2", -934.87)
      val employment2UkTaxPaidSummary1 = anUkTaxPaidSummary("ukTaxPaid1", 199.45)
      val employment2ukTaxPaidSummary2 = anUkTaxPaidSummary("ukTaxPaid2", 300.87)
      val employment1 =
        anEmployment().copy(ukTaxPaid = Seq(employment1UkTaxPaidSummary1, employment1ukTaxPaidSummary2))
      val employment2 =
        anEmployment().copy(ukTaxPaid = Seq(employment2UkTaxPaidSummary1, employment2ukTaxPaidSummary2))

      Employment.TaxesPaid(SelfAssessment(employments = Seq(employment1, employment2))) shouldBe
        Seq(UkTaxPaidForEmployment(employment1.sourceId, -1047.32),
          UkTaxPaidForEmployment(employment2.sourceId, 500.32))
    }

    "calculate the tax deducted as the rounded up sum of UK tax paid across all employments" in {
      val employment1UkTaxPaidSummary1 = anUkTaxPaidSummary("ukTaxPaid1", -112.45)
      val employment1ukTaxPaidSummary2 = anUkTaxPaidSummary("ukTaxPaid2", -34.87)
      val employment2UkTaxPaidSummary1 = anUkTaxPaidSummary("ukTaxPaid1", 299.45)
      val employment2ukTaxPaidSummary2 = anUkTaxPaidSummary("ukTaxPaid2", 300.87)
      val employment1 =
        anEmployment().copy(ukTaxPaid = Seq(employment1UkTaxPaidSummary1, employment1ukTaxPaidSummary2))
      val employment2 =
        anEmployment().copy(ukTaxPaid = Seq(employment2UkTaxPaidSummary1, employment2ukTaxPaidSummary2))

      Employment.TaxesPaid(SelfAssessment(employments = Seq(employment1, employment2))) shouldBe
        Seq(UkTaxPaidForEmployment(employment1.sourceId, -147.32),
          UkTaxPaidForEmployment(employment2.sourceId, 600.32))
    }
  }

}
