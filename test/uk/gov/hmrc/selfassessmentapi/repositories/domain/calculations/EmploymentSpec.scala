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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.calculations

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.controllers.api.{EmploymentIncome, UkTaxPaidForEmployment}
import uk.gov.hmrc.selfassessmentapi.controllers.api.employment.{BenefitType, ExpenseType, IncomeType}
import uk.gov.hmrc.selfassessmentapi.controllers.api.SelfAssessment
import uk.gov.hmrc.selfassessmentapi.repositories.domain.builders.EmploymentBuilder

class EmploymentSpec extends UnitSpec {

  "TotalProfit" should {
    "be sum of rounded down profit from each employment" in {
      val employment1 = EmploymentBuilder()
        .withIncomes(
          (IncomeType.Salary, 1000.12),
          (IncomeType.Other, 500.65))
        .create()

      val employment2 = EmploymentBuilder()
        .withIncomes(
          (IncomeType.Salary, 2000.45),
          (IncomeType.Other, 1000.23))
        .create()

      val selfAssessment = SelfAssessment(employments = Seq(employment1, employment2))

      Employment.TotalProfit(selfAssessment) shouldBe 4500
    }
  }

  "Profit" should {

    "be equal to Income + Benefits - Expenses" in {

      val employment = EmploymentBuilder()
        .withIncomes(
          (IncomeType.Salary, 1000),
          (IncomeType.Other, 500))
        .withBenefits(
          (BenefitType.Accommodation, 100),
          (BenefitType.Other, 400))
        .withExpenses(
          (ExpenseType.TravelAndSubsistence, 100),
          (ExpenseType.ProfessionalFees, 200))
        .create()

      Employment.Profit(employment) shouldBe 1700
    }

    "be 0 when expenses exceeds combined value of incomes and benefits" in {

      val employment = EmploymentBuilder()
        .withIncomes(
          (IncomeType.Salary, 100),
          (IncomeType.Other, 200))
        .withBenefits(
          (BenefitType.Accommodation, 10),
          (BenefitType.Other, 40))
        .withExpenses(
          (ExpenseType.TravelAndSubsistence, 200),
          (ExpenseType.ProfessionalFees, 400))
        .create()

      Employment.Profit(employment) shouldBe 0
    }

    "be round down to nearest pound" in {
      val employment = EmploymentBuilder()
        .withIncomes(
          (IncomeType.Salary, 1000.90),
          (IncomeType.Other, 500.75))
        .withBenefits(
          (BenefitType.Accommodation, 100.10),
          (BenefitType.Other, 400.20))
        .withExpenses(
          (ExpenseType.TravelAndSubsistence, 100.10),
          (ExpenseType.ProfessionalFees, 200.40))
        .create()

      Employment.Profit(employment) shouldBe 1701
    }
  }

  "TotalTaxPaid" should {
    "return a value of 0 when the total tax paid is negative" in {
      val employment = EmploymentBuilder()
        .withUkTaxPaid(-750)
        .create()

      val sa = SelfAssessment(employments = Seq(employment))

      Employment.TotalTaxPaid(sa) shouldBe 0
    }
  }

  "employment incomes" should {

    "be calculated from multiple employment sources" in {

      val dummyID = BSONObjectID.generate

      val employment1 = EmploymentBuilder(dummyID)
        .withIncomes(
          (IncomeType.Salary, 1000),
          (IncomeType.Other, 500))
        .withBenefits(
          (BenefitType.Accommodation, 100),
          (BenefitType.Other, 400))
        .withExpenses(
          (ExpenseType.TravelAndSubsistence, 100),
          (ExpenseType.ProfessionalFees, 200))
        .create()

      val employment2 = EmploymentBuilder(dummyID)
        .withIncomes(
          (IncomeType.Salary, 2000),
          (IncomeType.Other, 1000))
        .withBenefits(
          (BenefitType.CompanyVehicle, 100),
          (BenefitType.ExpensesPayments, 400))
        .withExpenses(
          (ExpenseType.TravelAndSubsistence, 500),
          (ExpenseType.ProfessionalFees, 1000))
        .create()

      val selfAssessment = SelfAssessment(employments = Seq(employment1, employment2))

      Employment.Incomes(selfAssessment) shouldBe Seq(
        EmploymentIncome(dummyID.stringify, 1500, 500, 300, 1700),
        EmploymentIncome(dummyID.stringify, 3000, 500, 1500, 2000))
    }

    "be calculated from a single employment sources" in {

      val dummyID = BSONObjectID.generate

      val employment = EmploymentBuilder(dummyID)
        .withIncomes(
          (IncomeType.Salary, 1000),
          (IncomeType.Other, 500))
        .withBenefits(
          (BenefitType.Accommodation, 100),
          (BenefitType.Other, 400))
        .withExpenses(
          (ExpenseType.TravelAndSubsistence, 100),
          (ExpenseType.ProfessionalFees, 200))
        .create()

      val selfAssessment = SelfAssessment(employments = Seq(employment))

      Employment.Incomes(selfAssessment) shouldBe Seq(
        EmploymentIncome(dummyID.stringify, 1500, 500, 300, 1700))
    }
  }


  "tax paid on employments" should {

    "return the UK tax paid as zero and an empty list of UK taxes paid for employments if there are no employments" in {
      Employment.TaxesPaid(SelfAssessment(employments = Nil)) shouldBe empty
    }


    "return the UK tax paid as zero if the sum of UK taxes paid is zero" in {
      val employment1 = EmploymentBuilder()
        .withUkTaxPaid(-1047.32)
        .create()

      val employment2 = EmploymentBuilder().create()

      Employment.TaxesPaid(SelfAssessment(employments = Seq(employment1, employment2))) shouldBe
        Seq(UkTaxPaidForEmployment(employment1.sourceId, -1047.32),
          UkTaxPaidForEmployment(employment2.sourceId, 0))
    }

    "cap the UK tax paid at zero if the total tax paid is not positive" in {
      val employment1 = EmploymentBuilder()
        .withUkTaxPaid(-1047.32)
        .create()

      val employment2 = EmploymentBuilder()
        .withUkTaxPaid(500.32)
        .create()

      Employment.TaxesPaid(SelfAssessment(employments = Seq(employment1, employment2))) shouldBe
        Seq(UkTaxPaidForEmployment(employment1.sourceId, -1047.32),
          UkTaxPaidForEmployment(employment2.sourceId, 500.32))
    }

    "calculate the tax deducted as the rounded up sum of UK tax paid across all employments" in {

      val employment1 = EmploymentBuilder()
        .withUkTaxPaid(-147.32)
        .create()

      val employment2 = EmploymentBuilder()
        .withUkTaxPaid(600.32)
        .create()

      Employment.TaxesPaid(SelfAssessment(employments = Seq(employment1, employment2))) shouldBe
        Seq(UkTaxPaidForEmployment(employment1.sourceId, -147.32),
          UkTaxPaidForEmployment(employment2.sourceId, 600.32))
    }
  }

}
