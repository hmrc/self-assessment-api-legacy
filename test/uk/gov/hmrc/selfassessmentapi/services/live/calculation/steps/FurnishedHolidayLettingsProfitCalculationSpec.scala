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

import uk.gov.hmrc.selfassessmentapi.FurnishedHolidayLettingsSugar._
import uk.gov.hmrc.selfassessmentapi.SelfAssessmentSugar._
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.ExpenseType._
import uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.{Adjustments, Allowances}
import uk.gov.hmrc.selfassessmentapi.repositories.domain._

class FurnishedHolidayLettingsProfitCalculationSpec extends UnitSpec {

  private val liability = aLiability()

  private val furnishedHolidayLettingId = "furnishedHolidayLettingId"

  "calculate profit for furnished holiday lettings" should {

    "not record any profit if there are no self employments" in {

      FurnishedHolidayLettingsProfitCalculation.run(SelfAssessment(), liability) shouldBe liability
    }

    "add all incomes, expenses, balancingCharges and privateUSeAdjustment to profit" in {

      val selfAssessment = SelfAssessment(furnishedHolidayLettings = Seq(
        aFurnishedHolidayLetting(furnishedHolidayLettingId).copy(
          incomes = Seq(
            anIncome(1200.01),
            anIncome(799.99)
          ),
          expenses = Seq(
            anExpense(200, FinancialCosts),
            anExpense(200, ProfessionalFees)
          ),
          balancingCharges = Seq(
            aBalancingCharge(10),
            aBalancingCharge(20)
          ),
          privateUseAdjustment = Seq(
            aPrivateUseAdjustment(50)
          )
        )
      ))

      FurnishedHolidayLettingsProfitCalculation.run(selfAssessment, liability) shouldBe liability.copy(incomeFromFurnishedHolidayLettings = Seq(
        FurnishedHolidayLettingIncome(furnishedHolidayLettingId, profit = 1680)
      ))
    }

    "subtract all expenses apart from depreciation from profit" in {

      val selfAssessment = SelfAssessment(furnishedHolidayLettings = Seq(
        aFurnishedHolidayLetting(furnishedHolidayLettingId).copy(
          incomes = Seq(
            anIncome(2000)
          ),
          expenses = Seq(
            anExpense(100, FinancialCosts),
            anExpense(50.01, ProfessionalFees),
            anExpense(49.99, PremisesRunningCosts),
            anExpense(50, Other)
          )
        )))

      FurnishedHolidayLettingsProfitCalculation.run(selfAssessment, liability) shouldBe liability.copy(incomeFromFurnishedHolidayLettings = Seq(
        FurnishedHolidayLettingIncome(furnishedHolidayLettingId, profit = 1750)
      ))
    }

    "subtract all allowances from profit" in {

      val selfAssessment = SelfAssessment(furnishedHolidayLettings = Seq(
        aFurnishedHolidayLetting(furnishedHolidayLettingId).copy(
          incomes = Seq(
            anIncome(2000)
          ),
          allowances = Some(Allowances(
            capitalAllowance = Some(10)
          ))
        )))

      FurnishedHolidayLettingsProfitCalculation.run(selfAssessment, liability) shouldBe liability.copy(incomeFromFurnishedHolidayLettings = Seq(
        FurnishedHolidayLettingIncome(furnishedHolidayLettingId, profit = 1990)
      ))
    }

    "round down profit and taxableProfit to the nearest pound" in {

      val selfAssessment = SelfAssessment(furnishedHolidayLettings = Seq(
        aFurnishedHolidayLetting(furnishedHolidayLettingId).copy(
          incomes = Seq(
            anIncome(1299.01)
          ),
          allowances = Some(Allowances(
            capitalAllowance = Some(0.02)
          ))
        )))

      FurnishedHolidayLettingsProfitCalculation.run(selfAssessment, liability) shouldBe liability.copy(incomeFromFurnishedHolidayLettings = Seq(
        FurnishedHolidayLettingIncome(furnishedHolidayLettingId, profit = 1298)
      ))
    }

    "return zero as profit and ignore lossBroughtForward if expenses are bigger than incomes (loss)" in {

      val selfAssessment = SelfAssessment(furnishedHolidayLettings = Seq(
        aFurnishedHolidayLetting(furnishedHolidayLettingId).copy(
          incomes = Seq(
            anIncome(2000)
          ),
          expenses = Seq(
            anExpense(4000, FinancialCosts)
          ),
          adjustments = Some(Adjustments(
            lossBroughtForward = Some(1000)
          ))
        )))

      FurnishedHolidayLettingsProfitCalculation.run(selfAssessment, liability) shouldBe liability.copy(incomeFromFurnishedHolidayLettings = Seq(
        FurnishedHolidayLettingIncome(furnishedHolidayLettingId, profit = 0)
      ))
    }

    "calculate profit for multiple self employments" in {

      val selfAssessment = SelfAssessment(furnishedHolidayLettings = Seq(
        aFurnishedHolidayLetting("se1").copy(
          incomes = Seq(
            anIncome(1200)
          )),
        aFurnishedHolidayLetting("se2").copy(
          incomes = Seq(
            anIncome(800)
          ))
      ))

      FurnishedHolidayLettingsProfitCalculation.run(selfAssessment, liability) shouldBe liability.copy(incomeFromFurnishedHolidayLettings = Seq(
        FurnishedHolidayLettingIncome("se1", profit = 1200),
        FurnishedHolidayLettingIncome("se2", profit = 800)
      ))
    }
  }
}
