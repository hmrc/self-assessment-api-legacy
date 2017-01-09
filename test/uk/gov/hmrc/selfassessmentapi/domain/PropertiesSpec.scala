/*
 * Copyright 2017 HM Revenue & Customs
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

import org.joda.time.LocalDate
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.controllers.util.NinoGenerator
import uk.gov.hmrc.selfassessmentapi.resources.models._
import uk.gov.hmrc.selfassessmentapi.resources.models.properties._

class PropertiesSpec extends UnitSpec {

  val otherAllowances = OtherPropertiesAllowances(Some(50.12), Some(50.55), Some(12.34), Some(23.47))
  val otherAdjustments = OtherPropertiesAdjustments(Some(50.12), Some(38.77), Some(12.20))
  val fhlAllowances = FHLPropertiesAllowances(Some(50.12), Some(50.55))
  val fhlAdjustments = FHLPropertiesAdjustments(Some(50.12), Some(38.77), Some(12.20))

  val otherPeriod: PropertiesPeriod = PropertiesPeriod(
    LocalDate.parse("2017-04-06"),
    LocalDate.parse("2018-04-05"),
    PropertiesPeriodicData(
      Map(IncomeType.RentIncome -> Income(10000, None)),
      Map(ExpenseType.PremisesRunningCosts -> SimpleExpense(50.55))))
  val fhlPeriod: PropertiesPeriod = PropertiesPeriod(
    LocalDate.parse("2017-04-06"),
    LocalDate.parse("2018-04-05"),
    PropertiesPeriodicData(
      Map(IncomeType.PremiumsOfLeaseGrant -> Income(1234.56, None)),
      Map(ExpenseType.FinancialCosts -> SimpleExpense(500.12))))

  val properties: Properties = Properties(
    BSONObjectID.generate,
    NinoGenerator().nextNino(),
    AccountingType.CASH,
    LocalDate.now(),
    AccountingPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-05")),
    FHLPropertiesBucket(
      Map("fhl" -> fhlPeriod),
      Map(TaxYear("2016-17") -> FHLPropertiesAnnualSummary(Some(fhlAllowances), Some(fhlAdjustments)))),
    OtherPropertiesBucket(
      Map("other" -> otherPeriod),
      Map(TaxYear("2016-17") -> OtherPropertiesAnnualSummary(Some(otherAllowances), Some(otherAdjustments)))))

  "annualSummary" should {
    "return an empty annual summary when no annual summary exists for the provided tax year" in {
      val properties = Properties(
        BSONObjectID.generate,
        NinoGenerator().nextNino(),
        AccountingType.CASH)

      properties.annualSummary(PropertyType.OTHER, TaxYear("2016-17")) shouldBe OtherPropertiesAnnualSummary(None, None)
      properties.annualSummary(PropertyType.FHL, TaxYear("2016-17")) shouldBe FHLPropertiesAnnualSummary(None, None)
    }

    "return an annual summary matching the tax year" in {
      properties.annualSummary(PropertyType.OTHER, TaxYear("2016-17")) shouldBe
        OtherPropertiesAnnualSummary(Some(otherAllowances), Some(otherAdjustments))
      properties.annualSummary(PropertyType.FHL, TaxYear("2016-17")) shouldBe
        FHLPropertiesAnnualSummary(Some(fhlAllowances), Some(fhlAdjustments))
    }
  }

  "periodExists" should {
    "return false if a period with the given id does not exist" in {
      properties.periodExists(PropertyType.OTHER, "cake") shouldBe false
      properties.periodExists(PropertyType.FHL, "cake") shouldBe false
    }

    "return true if a period with the given id exists" in {
      properties.periodExists(PropertyType.OTHER, "other") shouldBe true
      properties.periodExists(PropertyType.FHL, "fhl") shouldBe true
    }
  }

  "period" should {
    "return a period with the given id if it exists" in {
      properties.period(PropertyType.OTHER, "other") shouldBe Some(otherPeriod)
      properties.period(PropertyType.FHL, "fhl") shouldBe Some(fhlPeriod)
    }

    "return None if a period with the given id does not exist" in {
      properties.period(PropertyType.OTHER, "cake") shouldBe None
      properties.period(PropertyType.FHL, "cake") shouldBe None
    }
  }

  "setPeriodsTo" should {
    "set the periods map for the period specified by the given id" in {
      val newOtherPeriod = otherPeriod.copy(data = otherPeriod.data.copy(incomes = Map.empty))
      val newFhlPeriod = fhlPeriod.copy(data = fhlPeriod.data.copy(incomes = Map.empty))

      val newProperties = properties
        .setPeriodsTo(PropertyType.OTHER, "other", newOtherPeriod)
        .setPeriodsTo(PropertyType.FHL, "fhl", newFhlPeriod)

      newProperties.otherBucket.periods("other") shouldBe newOtherPeriod
      newProperties.fhlBucket.periods("fhl") shouldBe newFhlPeriod
    }

    "remove invalid Income types for FHL" in {
      val period = otherPeriod.copy(data = otherPeriod.data.copy(incomes =
        Map(IncomeType.RentIncome -> Income(1000, None),
          IncomeType.PremiumsOfLeaseGrant -> Income(1000, None),
          IncomeType.ReversePremiums -> Income(1000, None))))

      val newProperties = properties
        .setPeriodsTo(PropertyType.OTHER, "periodId", period)
        .setPeriodsTo(PropertyType.FHL, "periodId", period)

      val otherPeriodicData = newProperties.otherBucket.periods("periodId").data
      otherPeriodicData.incomes.size shouldBe 3

      val fhlPeriodicData = newProperties.fhlBucket.periods("periodId").data
      fhlPeriodicData.incomes.size shouldBe 1
      fhlPeriodicData.incomes.exists(income => income._1 == IncomeType.PremiumsOfLeaseGrant) shouldBe false
      fhlPeriodicData.incomes.exists(income => income._1 == IncomeType.ReversePremiums) shouldBe false
    }

    "remove invalid Expense types for FHL" in {
      val period = otherPeriod.copy(data = otherPeriod.data.copy(expenses =
        Map(ExpenseType.FinancialCosts -> SimpleExpense(1000),
          ExpenseType.CostOfServices -> SimpleExpense(1000),
          ExpenseType.PremisesRunningCosts -> SimpleExpense(1000),
          ExpenseType.ProfessionalFees -> SimpleExpense(1000),
          ExpenseType.RepairsAndMaintenance -> SimpleExpense(1000),
          ExpenseType.Other -> SimpleExpense(1000))))

      val newProperties = properties
        .setPeriodsTo(PropertyType.OTHER, "periodId", period)
        .setPeriodsTo(PropertyType.FHL, "periodId", period)

      val otherPeriodicData = newProperties.otherBucket.periods("periodId").data
      otherPeriodicData.expenses.size shouldBe 6

      val fhlPeriodicData = newProperties.fhlBucket.periods("periodId").data
      fhlPeriodicData.expenses.size shouldBe 5
      fhlPeriodicData.expenses.exists(expense => expense._1 == ExpenseType.CostOfServices) shouldBe false
    }
  }
}
