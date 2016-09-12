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

import uk.gov.hmrc.selfassessmentapi.FurnishedHolidayLettingsSugar._
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SelfAssessment, FurnishedHolidayLettingIncome}
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.ExpenseType._
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.{Adjustments, Allowances, PropertyLocationType}

class FurnishedHolidayLettingSpec extends UnitSpec {

  private val furnishedHolidayLettingId = "furnishedHolidayLettingId"

  "AdjustedProfit" should {
    "be equal to (Incomes + BalancingCharges + PrivateUseAdjustment) - (Expenses + CapitalAllowance)" in {

      val letting =
        aFurnishedHolidayLetting(furnishedHolidayLettingId).copy(
          incomes = Seq(
            fhlIncome(1200.01),
            fhlIncome(799.99)
          ),
          expenses = Seq(
            fhlExpense(200, FinancialCosts),
            fhlExpense(200, ProfessionalFees),
            fhlExpense(49.99, PremisesRunningCosts),
            fhlExpense(50, Other)
          ),
          balancingCharges = Seq(
            fhlBalancingCharge(10),
            fhlBalancingCharge(20)
          ),
          privateUseAdjustment = Seq(
            fhlPrivateUseAdjustment(50)
          ),
          allowances = Some(Allowances(capitalAllowance = Some(100)))
        )

      FurnishedHolidayLetting.AdjustedProfits(letting) shouldBe 1480

    }

    "be rounded down to the nearest pound" in {
        val letting = aFurnishedHolidayLetting(furnishedHolidayLettingId).copy(
          incomes = Seq(
            fhlIncome(1299.01)
          ),
          allowances = Some(Allowances(
            capitalAllowance = Some(0.02)
          ))
        )

      FurnishedHolidayLetting.AdjustedProfits(letting) shouldBe 1298
    }

    "be 0 if (Expenses + CapitalAllowance) is greater than (Incomes + BalancingCharges + PrivateUseAdjustment)" in {

      val letting =
        aFurnishedHolidayLetting(furnishedHolidayLettingId).copy(
          incomes = Seq(
            fhlIncome(2000)
          ),
          expenses = Seq(
            fhlExpense(4000, FinancialCosts)
          ),
          adjustments = Some(Adjustments(
            lossBroughtForward = Some(1000)
          ))
        )

      FurnishedHolidayLetting.AdjustedProfits(letting) shouldBe 0
    }

  }

  "Incomes" should {

    "be empty there are no self employments" in {

      FurnishedHolidayLetting.Incomes(SelfAssessment()) shouldBe empty
    }

    "be computed for a single holiday letting" in {
      val selfAssessment = SelfAssessment(furnishedHolidayLettings = Seq(
        aFurnishedHolidayLetting(furnishedHolidayLettingId).copy(
          incomes = Seq(
            fhlIncome(1200.01)
          )
        )
      ))

      FurnishedHolidayLetting.Incomes(selfAssessment) should contain theSameElementsAs Seq(
        FurnishedHolidayLettingIncome(furnishedHolidayLettingId, profit = 1200)
      )
    }

    "be computed for multiple self employments" in {

      val selfAssessment = SelfAssessment(furnishedHolidayLettings = Seq(
        aFurnishedHolidayLetting("se1").copy(
          incomes = Seq(
            fhlIncome(1200)
          )),
        aFurnishedHolidayLetting("se2").copy(
          incomes = Seq(
            fhlIncome(800)
          ))
      ))

      FurnishedHolidayLetting.Incomes(selfAssessment) should contain theSameElementsAs Seq(
        FurnishedHolidayLettingIncome("se1", profit = 1200),
        FurnishedHolidayLettingIncome("se2", profit = 800)
      )
    }
  }

  "TotalLossBroughtForward" should {
    "be Sum(CapAt(Sum(LossBroughtForward), Sum(AdjustedProfit)) Per Location)" in {
      val furnishedHolidayLettings =
        Seq(aFurnishedHolidayLetting(propertyLocation = PropertyLocationType.UK).copy(
            incomes = Seq(fhlIncome(100)),
            adjustments = Some(fhlAdjustments(lossBroughtForward = 50))
          ),
          aFurnishedHolidayLetting(propertyLocation = PropertyLocationType.UK).copy(
            incomes = Seq(fhlIncome(350), fhlIncome(50), fhlIncome(100)),
            adjustments = Some(fhlAdjustments(lossBroughtForward = 600))
          ),
          aFurnishedHolidayLetting(propertyLocation = PropertyLocationType.EEA).copy(
            incomes = Seq(fhlIncome(50), fhlIncome(50)),
            adjustments = Some(fhlAdjustments(lossBroughtForward = 500))
          ),
          aFurnishedHolidayLetting(propertyLocation = PropertyLocationType.EEA).copy(
            incomes = Seq(fhlIncome(500)),
            adjustments = Some(fhlAdjustments(lossBroughtForward = 450))
          )
        )

      FurnishedHolidayLetting.TotalLossBroughtForward(SelfAssessment().copy(
        furnishedHolidayLettings = furnishedHolidayLettings)) shouldBe 1200
    }
  }
}
