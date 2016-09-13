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

import org.scalatest.Matchers
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.controllers._
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.{SelfEmployment => _}
import uk.gov.hmrc.selfassessmentapi.controllers.api.{Liability => _, SelfAssessment => _, _}
import uk.gov.hmrc.selfassessmentapi.repositories.domain._
import uk.gov.hmrc.selfassessmentapi.{UnitSpec, _}

class LiabilitySpec extends UnitSpec {

  "MongoLiability.toLiability" should {

    "map to liability" in {

      val liability = new Liability(BSONObjectID.generate, "", saUtr = SaUtr(""), taxYear = TaxYear(""),
        employmentIncome = Seq(
          EmploymentIncome(sourceId = "eId1",
            pay = 100,
            benefitsAndExpenses = 50,
            allowableExpenses = 50,
            total = 100),
          EmploymentIncome(sourceId = "eId2",
            pay = 200,
            benefitsAndExpenses = 100,
            allowableExpenses = 100,
            total = 200)
        ),
        selfEmploymentIncome = Seq(
          SelfEmploymentIncome(sourceId = "seId1", taxableProfit = 10, profit = 20),
          SelfEmploymentIncome(sourceId = "seId2", taxableProfit = 20, profit = 40)
        ),
        ukPropertyIncome = Seq(UkPropertyIncome("property1", profit = 2000)),
        furnishedHolidayLettingsIncome = Seq(
          FurnishedHolidayLettingIncome(sourceId = "fhlId1", profit = 20),
          FurnishedHolidayLettingIncome(sourceId = "fhlId2", profit = 40)
        ),
        savingsIncome = Seq(
          InterestFromUKBanksAndBuildingSocieties(sourceId = "interestId1", totalInterest = 20),
          InterestFromUKBanksAndBuildingSocieties(sourceId = "interestId2", totalInterest = 40)
        ),
        ukDividendsIncome = Seq(DividendsFromUKSources("divId1", totalDividend = 100)),
        totalIncomeReceived = 1000,
        totalTaxableIncome = 4000,
        allowancesAndReliefs = AllowancesAndReliefs(personalAllowance = Some(3000), incomeTaxRelief = Some(2000), retirementAnnuityContract = Some(1000)),
        taxDeducted = repositories.domain.TaxDeducted(
          interestFromUk = 50,
          deductionFromUkProperties = Seq(TaxPaidForUkProperty(sourceId = "propTaxPaid", taxPaid = 30)),
          ukTaxesPaidForEmployments = Seq(api.UkTaxPaidForEmployment(sourceId = "empTaxPaid", taxPaid = 20))),
        nonSavingsTaxBandSummary = Seq(
          TaxBandSummary("basicRate", taxableAmount = 100, chargedAt = "20%", tax = 20),
          TaxBandSummary("higherRate", taxableAmount = 100, chargedAt = "40%", tax = 40),
          TaxBandSummary("additionalHigherRate", taxableAmount = 100, chargedAt = "45%", tax = 45)
        ),
        savingsTaxBandSummary = Seq(
          TaxBandSummary("basicRate", taxableAmount = 200, chargedAt = "20%", tax = 40),
          TaxBandSummary("higherRate", taxableAmount = 200, chargedAt = "40%", tax = 80),
          TaxBandSummary("additionalHigherRate", taxableAmount = 200, chargedAt = "45%", tax = 90)
        ),
        dividendTaxBandSummary = Seq(
          TaxBandSummary("basicRate", taxableAmount = 300, chargedAt = "20%", tax = 60),
          TaxBandSummary("higherRate", taxableAmount = 300, chargedAt = "40%", tax = 120),
          TaxBandSummary("additionalHigherRate", taxableAmount = 300, chargedAt = "45%", tax = 135)
        ),
        pensionSavingsChargesSummary = Seq(
          TaxBandSummary("basicRate", taxableAmount = 300, chargedAt = "20%", tax = 60),
          TaxBandSummary("higherRate", taxableAmount = 300, chargedAt = "40%", tax = 120),
          TaxBandSummary("additionalHigherRate", taxableAmount = 300, chargedAt = "45%", tax = 135)
        ),
        taxes = TaxesCalculated(
          totalIncomeTax = 630,
          totalTaxDeducted = 100,
          totalTaxDue = 530,
          totalTaxOverPaid = 0,
          pensionSavingsCharges = 0,
          taxPaidByPensionScheme = 0
        )
      )

      val liabilityDto = liability.toLiability

      liabilityDto.income.incomes.nonSavings.employment should contain theSameElementsAs Seq(EmploymentIncome(sourceId = "eId1",
                                                                                              pay = 100,
                                                                                              benefitsAndExpenses = 50,
                                                                                              allowableExpenses = 50,
                                                                                              total = 100),
                                                                                            EmploymentIncome(sourceId = "eId2",
                                                                                              pay = 200,
                                                                                              benefitsAndExpenses = 100,
                                                                                              allowableExpenses = 100,
                                                                                              total = 200))

      liabilityDto.income.incomes.nonSavings.selfEmployment should contain theSameElementsAs Seq(SelfEmploymentIncome("seId1", taxableProfit = 10, profit = 20),
                                                                      SelfEmploymentIncome("seId2", taxableProfit = 20, profit = 40))

      liabilityDto.income.incomes.nonSavings.ukProperties should contain theSameElementsAs Seq(UkPropertyIncome("property1", profit = 2000))

      liabilityDto.income.incomes.nonSavings.furnishedHolidayLettings should contain theSameElementsAs Seq(FurnishedHolidayLettingIncome("fhlId1", 20),
                                                                                                            FurnishedHolidayLettingIncome("fhlId2", 40))

      liabilityDto.income.incomes.savings shouldBe SavingsIncomes(fromUKBanksAndBuildingSocieties = Seq(
          InterestFromUKBanksAndBuildingSocieties("interestId1", totalInterest = 20),
          InterestFromUKBanksAndBuildingSocieties("interestId2", totalInterest = 40)
        )
      )

      liabilityDto.income.incomes.dividends shouldBe DividendsIncomes(fromUKSources = Seq(DividendsFromUKSources("divId1", totalDividend = 100)))

      liabilityDto.income.incomes.total shouldBe 1000

      liabilityDto.income.deductions.get shouldBe api.Deductions(personalAllowance = 3000, incomeTaxRelief = 2000,
                                                        retirementAnnuityContract = 1000,
                                                        total = 6000)

      liabilityDto.income.totalIncomeOnWhichTaxIsDue shouldBe 4000

      liabilityDto.incomeTaxCalculations.nonSavings should contain theSameElementsAs Seq(
        TaxBandSummary("basicRate", taxableAmount = 100, chargedAt = "20%", tax = 20),
        TaxBandSummary("higherRate", taxableAmount = 100, chargedAt = "40%", tax = 40),
        TaxBandSummary("additionalHigherRate", taxableAmount = 100, chargedAt = "45%", tax = 45)
      )

      liabilityDto.incomeTaxCalculations.savings should contain theSameElementsAs Seq(
        TaxBandSummary("basicRate", taxableAmount = 200, chargedAt = "20%", tax = 40),
        TaxBandSummary("higherRate", taxableAmount = 200, chargedAt = "40%", tax = 80),
        TaxBandSummary("additionalHigherRate", taxableAmount = 200, chargedAt = "45%", tax = 90)
      )

      liabilityDto.incomeTaxCalculations.dividends should contain theSameElementsAs Seq(
        TaxBandSummary("basicRate", taxableAmount = 300, chargedAt = "20%", tax = 60),
        TaxBandSummary("higherRate", taxableAmount = 300, chargedAt = "40%", tax = 120),
        TaxBandSummary("additionalHigherRate", taxableAmount = 300, chargedAt = "45%", tax = 135)
      )

      liabilityDto.taxDeducted.interestFromUk shouldBe 50
      liabilityDto.taxDeducted.fromEmployments shouldBe Seq(api.UkTaxPaidForEmployment(sourceId = "empTaxPaid", taxPaid = 20))
      liabilityDto.taxDeducted.fromUkProperties shouldBe Seq(TaxPaidForUkProperty(sourceId = "propTaxPaid", taxPaid = 30))

      liabilityDto.taxDeducted.total shouldBe 100

      liabilityDto.totalTaxDue shouldBe 530

      liabilityDto.totalTaxOverpaid shouldBe 0

    }
  }

  "FunctionalLiability.create" should {
    "correctly compute values for employments" in {
      import uk.gov.hmrc.selfassessmentapi.controllers.api.employment._

      val employment = TestEmployment()
        .withIncomes((IncomeType.Salary, 20000), (IncomeType.Other, 2500.50))
        .withExpenses((ExpenseType.TravelAndSubsistence, 500.50), (ExpenseType.ProfessionalFees, 250.52))
        .withBenefits((BenefitType.Accommodation, 1000), (BenefitType.PrivateInsurance, 200))
        .withUkTaxPaid(500.25)
        .create()

      ComputeLiabilityFor(employments = Seq(employment))
        .andAssertThat()
        .personalAllowanceIs(11000)
        .nonSavings()
          .basicRateBandSummaryIs(11949, 2389.8)
          .higherRateBandSummaryIs(0,0)
          .additionalHigherRateBandSummaryIs(0,0)
        .totalIncomeReceivedIs(22949)
        .totalIncomeTaxIs(2389.8)
        .totalTaxDueIs(1889.55)

    }

    "correctly compute values for self-employments" in {
      import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment._

      val selfEmployment = TestSelfEmployment()
          .withAllowances(
            annualInvestmentAllowance = 23000.22,
            capitalAllowanceMainPool = 200.75,
            capitalAllowanceSpecialRatePool = 12.50,
            businessPremisesRenovationAllowance = 10000.72,
            enhancedCapitalAllowance = 123.45,
            allowancesOnSales = 50.50)
          .withAdjustments(
            includedNonTaxableProfits = 100.25,
            basisAdjustment = 50.50,
            overlapReliefUsed = 12.50,
            averagingAdjustment = 100.23,
            lossBroughtForward = 500.05,
            outstandingBusinessIncome = 123.45)
          .incomes((IncomeType.Turnover, 200000.22))
          .expenses((ExpenseType.PremisesRunningCosts, 12334.56))
          .balancingCharges((BalancingChargeType.BPRA, 500.25))
          .goodsAndServicesOwnUse(200.02)
          .create()

      ComputeLiabilityFor(selfEmployments = Seq(selfEmployment))
        .andAssertThat()
        .personalAllowanceIs(0)
        .incomeTaxReliefIs(501)
        .nonSavings()
          .basicRateBandSummaryIs(32000, 6400)
          .higherRateBandSummaryIs(118000,47200)
          .additionalHigherRateBandSummaryIs(4638,2087.1)
        .totalIncomeReceivedIs(155139)
        .totalTaxableIncomeIs(154638)
        .totalIncomeTaxIs(55687.10)
        .totalTaxDueIs(55687.10)

    }

    "correctly compute values for UK properties" in {
      import uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty._

      val ukProperty = TestUkProperty(rentARoomRelief = 500.25)
        .withAllowances(annualInvestmentAllowance = 200,
          otherCapitalAllowance=123.45,
          wearAndTearAllowance=12.25)
        .lossBroughtForward(500)
        .incomes((IncomeType.RentIncome, 175000))
        .expenses((ExpenseType.PremisesRunningCosts, 243.34))
        .balancingCharges(200)
        .privateUseAdjustment(125.25)
        .taxesPaid(12500.56)
        .create()

      ComputeLiabilityFor(ukProperties = Seq(ukProperty))
        .andAssertThat()
        .personalAllowanceIs(0)
        .incomeTaxReliefIs(500)
        .nonSavings()
        .basicRateBandSummaryIs(32000, 6400)
        .higherRateBandSummaryIs(118000, 47200)
        .additionalHigherRateBandSummaryIs(23745.96, 10685.68)
        .totalIncomeReceivedIs(174245.96)
        .totalTaxableIncomeIs(173745.96)
        .totalIncomeTaxIs(64285.68)
        .totalTaxDueIs(51785.12)
    }

    "correctly compute values for savings" in {

      val unearnedIncome = TestUnearnedIncome()
        .withSavings(
          (unearnedincome.SavingsIncomeType.InterestFromBanksUntaxed, 150000.73),
          (unearnedincome.SavingsIncomeType.InterestFromBanksTaxed, 5000.23))
        .create()


      ComputeLiabilityFor(unearnedIncomes = Seq(unearnedIncome))
        .andAssertThat()
        .incomeTaxReliefIs(0)
        .personalAllowanceIs(0)
        .savings()
        .startingRateBandSummaryIs(5000)
        .nilRateBandSummaryIs(0)
        .basicRateBandSummaryIs(27000, 5400)
        .higherRateBandSummaryIs(118000, 47200)
        .additionalHigherRateBandSummaryIs(6251, 2812.95)
        .totalIncomeReceivedIs(156251)
        .totalTaxableIncomeIs(156251)
        .totalIncomeTaxIs(55412.95)
        .totalTaxDueIs(54162.89)
    }

    "correctly compute values for dividends" in {
      import uk.gov.hmrc.selfassessmentapi.controllers.api.unearnedincome._

      val unearnedIncome = TestUnearnedIncome()
          .withDividends(
            (DividendType.FromUKCompanies, 75000.33),
            (DividendType.OtherFromUKCompanies, 125000.25))
        .create()

      ComputeLiabilityFor(unearnedIncomes = Seq(unearnedIncome))
        .andAssertThat()
        .incomeTaxReliefIs(0)
        .personalAllowanceIs(0)
        .dividends()
        .nilRateBandSummaryIs(5000)
        .basicRateBandSummaryIs(27000, 2025)
        .higherRateBandSummaryIs(118000, 38350)
        .additionalHigherRateBandSummaryIs(50000, 19050)
        .totalIncomeReceivedIs(200000)
        .totalTaxableIncomeIs(200000)
        .totalIncomeTaxIs(59425)
        .totalTaxDueIs(59425)
    }

    "correctly compute values for furnished holiday lettings" in {
      import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings._

      val furnishedHolidayLetting = TestFurnishedHolidayLetting(capitalAllowance = 123.45)
          .propertyLocation(PropertyLocationType.UK)
          .lossBroughtForward(500.50)
          .incomes(15250.50)
          .expenses((ExpenseType.PremisesRunningCosts, 1250.25))
          .balancingCharges(150.20)
          .privateUseAdjustments(750.65)
          .create()

      ComputeLiabilityFor(furnishedHolidayLettings = Seq(furnishedHolidayLetting))
        .andAssertThat()
        .incomeTaxReliefIs(500.5)
        .personalAllowanceIs(11000)
        .nonSavings()
        .basicRateBandSummaryIs(3276.5, 655.3)
        .higherRateBandSummaryIs(0, 0)
        .additionalHigherRateBandSummaryIs(0, 0)
        .totalIncomeReceivedIs(14777)
        .totalTaxableIncomeIs(3276.5)
        .totalIncomeTaxIs(655.3)
        .totalTaxDueIs(655.3)
    }

    "correctly compute values for a whole self assessment" in {
      import uk.gov.hmrc.selfassessmentapi.controllers.api._

      val employments = TestEmployment()
        .withIncomes((employment.IncomeType.Salary, 20000), (employment.IncomeType.Other, 2500.50))
        .withExpenses((employment.ExpenseType.TravelAndSubsistence, 500.50),(employment.ExpenseType.ProfessionalFees, 250.52))
        .withBenefits((employment.BenefitType.Accommodation, 1000), (employment.BenefitType.PrivateInsurance, 200))
        .withUkTaxPaid(500.25)
        .create()

      val selfEmployments = TestSelfEmployment()
        .withAllowances(
          annualInvestmentAllowance = 23000.22,
          capitalAllowanceMainPool = 200.75,
          capitalAllowanceSpecialRatePool = 12.50,
          businessPremisesRenovationAllowance = 10000.72,
          enhancedCapitalAllowance = 123.45,
          allowancesOnSales = 50.50)
        .withAdjustments(
          includedNonTaxableProfits = 100.25,
          basisAdjustment = 50.50,
          overlapReliefUsed = 12.50,
          averagingAdjustment = 100.23,
          lossBroughtForward = 500.05,
          outstandingBusinessIncome = 123.45)
        .incomes((selfemployment.IncomeType.Turnover, 200000.22))
        .expenses((selfemployment.ExpenseType.PremisesRunningCosts, 12334.56))
        .balancingCharges((selfemployment.BalancingChargeType.BPRA, 500.25))
        .goodsAndServicesOwnUse(200.02)
        .create()

      val ukProperties = TestUkProperty(rentARoomRelief = 500.25)
        .withAllowances(
          annualInvestmentAllowance = 200,
          otherCapitalAllowance = 123.45,
          wearAndTearAllowance = 12.25)
        .lossBroughtForward(500)
        .incomes((ukproperty.IncomeType.RentIncome, 175000))
        .expenses((ukproperty.ExpenseType.PremisesRunningCosts, 243.34))
        .balancingCharges(200)
        .privateUseAdjustment(125.25)
        .taxesPaid(12500.56)
        .create()

      val furnishedHolidayLetting = TestFurnishedHolidayLetting(123.45)
        .propertyLocation(furnishedholidaylettings.PropertyLocationType.UK)
        .lossBroughtForward(500.50)
        .incomes(15250.50)
        .expenses((furnishedholidaylettings.ExpenseType.PremisesRunningCosts, 1250.25))
        .balancingCharges(150.20)
        .privateUseAdjustments(750.65)
        .create()

      val unearnedIncome = TestUnearnedIncome()
        .withSavings((unearnedincome.SavingsIncomeType.InterestFromBanksUntaxed, 150000.73), (unearnedincome.SavingsIncomeType.InterestFromBanksTaxed, 5000.23))
        .withDividends((unearnedincome.DividendType.FromUKCompanies, 75000.33), (unearnedincome.DividendType.OtherFromUKCompanies, 125000.25))
        .create()

      ComputeLiabilityFor(
        employments = Seq(employments),
        selfEmployments = Seq(selfEmployments),
        ukProperties = Seq(ukProperties),
        unearnedIncomes = Seq(unearnedIncome),
        furnishedHolidayLettings = Seq(furnishedHolidayLetting))
        .andAssertThat()
        .personalAllowanceIs(0)
        .incomeTaxReliefIs(1501.5)
        .nonSavings()
          .basicRateBandSummaryIs(32000, 6400)
          .higherRateBandSummaryIs(118000, 47200)
          .additionalHigherRateBandSummaryIs(215609.46, 97024.25)
        .savings()
          .startingRateBandSummaryIs(0)
          .nilRateBandSummaryIs(0)
          .basicRateBandSummaryIs(0, 0)
          .higherRateBandSummaryIs(0, 0)
          .additionalHigherRateBandSummaryIs(156251, 70312.95)
        .dividends()
          .nilRateBandSummaryIs(5000)
          .basicRateBandSummaryIs(0, 0)
          .higherRateBandSummaryIs(0, 0)
          .additionalHigherRateBandSummaryIs(195000, 74295)
        .totalIncomeReceivedIs(723361.96)
        .totalTaxableIncomeIs(721860.46)
        .totalIncomeTaxIs(295232.20)
        .totalTaxDueIs(280981.33)
    }

    "correctly compute a liability when taxable non-savings income is less than zero" in {
      val selfEmployments = TestSelfEmployment()
        .withAllowances(
          annualInvestmentAllowance = 2300.22,
          capitalAllowanceMainPool = 200.75,
          capitalAllowanceSpecialRatePool = 12.50,
          businessPremisesRenovationAllowance = 1000.72,
          enhancedCapitalAllowance = 123.45,
          allowancesOnSales = 50.50)
        .withAdjustments(
          includedNonTaxableProfits = 100.25,
          basisAdjustment = 50.50,
          overlapReliefUsed = 12.50,
          averagingAdjustment = 100.23,
          lossBroughtForward = 5000.05,
          outstandingBusinessIncome = 123.45)
        .incomes((selfemployment.IncomeType.Turnover, 30000.22))
        .expenses((selfemployment.ExpenseType.PremisesRunningCosts, 12334.56))
        .balancingCharges((selfemployment.BalancingChargeType.BPRA, 500.25))
        .goodsAndServicesOwnUse(200.02)
        .create()

      val ukProperties = TestUkProperty(rentARoomRelief = 500.25)
        .withAllowances(
          annualInvestmentAllowance = 200,
          otherCapitalAllowance = 123.45,
          wearAndTearAllowance = 12.25)
        .lossBroughtForward(3000)
        .incomes((ukproperty.IncomeType.RentIncome, 3000))
        .expenses((ukproperty.ExpenseType.PremisesRunningCosts, 243.34))
        .balancingCharges(200)
        .privateUseAdjustment(125.25)
        .taxesPaid(12500.56)
        .create()

      val furnishedHolidayLetting = TestFurnishedHolidayLetting(123.45)
        .propertyLocation(furnishedholidaylettings.PropertyLocationType.UK)
        .lossBroughtForward(0.50)
        .incomes(1525.50)
        .expenses((furnishedholidaylettings.ExpenseType.PremisesRunningCosts, 1250.25))
        .balancingCharges(150.20)
        .privateUseAdjustments(750.65)
        .create()

      val unearnedIncome = TestUnearnedIncome()
        .withSavings((unearnedincome.SavingsIncomeType.InterestFromBanksUntaxed, 2000.73), (unearnedincome.SavingsIncomeType.InterestFromBanksUntaxed, 2000.23))
        .create()

      val sa = api.SelfAssessment(selfEmployments = Seq(selfEmployments),
      ukProperties = Seq(ukProperties),
      unearnedIncomes = Seq(unearnedIncome),
      furnishedHolidayLettings = Seq(furnishedHolidayLetting))

      println(NonSavings.TotalIncome(sa))
      println(Savings.TotalIncome(sa))
      println(Deductions.Total(sa))
      println(NonSavings.TotalTaxableIncome(sa))
      println(Savings.TotalTaxableIncome(sa))


      ComputeLiabilityFor(
        selfEmployments = Seq(selfEmployments),
        ukProperties = Seq(ukProperties),
        unearnedIncomes = Seq(unearnedIncome),
        furnishedHolidayLettings = Seq(furnishedHolidayLetting))
        .andAssertThat()
        .personalAllowanceIs(11000)
        .incomeTaxReliefIs(7247.50)
        .nonSavings()
        .basicRateBandSummaryIs(0, 0)
        .higherRateBandSummaryIs(0, 0)
        .additionalHigherRateBandSummaryIs(0, 0)
        .savings()
        .startingRateBandSummaryIs(3889.46)
        .nilRateBandSummaryIs(0)
        .basicRateBandSummaryIs(0, 0)
        .higherRateBandSummaryIs(0, 0)
        .additionalHigherRateBandSummaryIs(0, 0)
        .dividends()
        .nilRateBandSummaryIs(0)
        .basicRateBandSummaryIs(0, 0)
        .higherRateBandSummaryIs(0, 0)
        .additionalHigherRateBandSummaryIs(0, 0)
        .totalIncomeReceivedIs(22136.96)
        .totalTaxableIncomeIs(3889.46)
        .totalIncomeTaxIs(0)
        .totalTaxDueIs(0)

    }
  }
}

case class ComputeLiabilityFor(employments: Seq[Employment] = Nil, selfEmployments: Seq[SelfEmployment] = Nil,
                               ukProperties: Seq[UKProperties] = Nil, unearnedIncomes: Seq[UnearnedIncome] = Nil, furnishedHolidayLettings: Seq[FurnishedHolidayLettings] = Nil) {
  def andAssertThat() = LiabilityResultAssertions(Liability.create(SaUtr("123456789"), TaxYear("2016-17"),
    api.SelfAssessment(employments = employments, selfEmployments = selfEmployments, ukProperties = ukProperties, unearnedIncomes = unearnedIncomes, furnishedHolidayLettings = furnishedHolidayLettings)))
}

case class LiabilityResultAssertions(liability: Liability) extends Matchers {

  def totalTaxableIncomeIs(amount: BigDecimal) = {
    liability.totalTaxableIncome shouldBe amount
    this
  }
  def totalTaxDueIs(amount: BigDecimal) = {
    liability.taxes.totalTaxDue shouldBe amount
    this
  }

  def totalIncomeTaxIs(amount: BigDecimal) = {
    liability.taxes.totalIncomeTax shouldBe amount
    this
  }

  def totalIncomeReceivedIs(amount: BigDecimal) = {
    liability.totalIncomeReceived shouldBe amount
    this
  }

  def nonSavings() = NonSavingsAssertions(liability)

  def savings() = SavingsAssertions(liability)

  def dividends() = DividendAssertions(liability)

  def personalAllowanceIs(personalAllowance: BigDecimal) = {
    liability.allowancesAndReliefs.personalAllowance shouldBe Some(personalAllowance)
    this
  }
  def incomeTaxReliefIs(amount: BigDecimal) = {
    liability.allowancesAndReliefs.incomeTaxRelief shouldBe Some(amount)
    this
  }

  trait CommonTaxBandSummaries {
    val taxBandSummaries: Seq[TaxBandSummary]
    val basicRateCharge = "20%"
    val higherRateCharge = "40%"
    val additionalHigherRateCharge = "45%"

    def additionalHigherRateBandSummaryIs(amount: BigDecimal, tax: BigDecimal) = {
      taxBandSummaries.find(_.taxBand == "additionalHigherRate") shouldBe Some(TaxBandSummary("additionalHigherRate", amount, additionalHigherRateCharge, tax))
      LiabilityResultAssertions.this
    }

    def higherRateBandSummaryIs(amount: BigDecimal, tax: BigDecimal) = {
      taxBandSummaries.find(_.taxBand == "higherRate") shouldBe Some(TaxBandSummary("higherRate", amount, higherRateCharge, tax))
      this
    }

    def basicRateBandSummaryIs(amount: BigDecimal, tax: BigDecimal) = {
      taxBandSummaries.find(_.taxBand == "basicRate") shouldBe Some(TaxBandSummary("basicRate", amount, basicRateCharge, tax))
      this
    }
  }

  case class NonSavingsAssertions(functionalLiability: Liability) extends CommonTaxBandSummaries {
    override val taxBandSummaries = functionalLiability.nonSavingsTaxBandSummary
  }

  case class SavingsAssertions(functionalLiability: Liability) extends CommonTaxBandSummaries {
    override val taxBandSummaries = functionalLiability.savingsTaxBandSummary

    def startingRateBandSummaryIs(amount: BigDecimal) = {
      taxBandSummaries.find(_.taxBand == "startingRate") shouldBe Some(TaxBandSummary("startingRate", amount, "0%", 0))
      this
    }

    def nilRateBandSummaryIs(amount: BigDecimal) = {
      taxBandSummaries.find(_.taxBand == "nilRate") shouldBe Some(TaxBandSummary("nilRate", amount, "0%", 0))
      this
    }
  }

  case class DividendAssertions(liability: Liability) extends CommonTaxBandSummaries {
    override val taxBandSummaries = liability.dividendTaxBandSummary
    override val basicRateCharge = "7.5%"
    override val higherRateCharge = "32.5%"
    override val additionalHigherRateCharge = "38.1%"

    def nilRateBandSummaryIs(amount: BigDecimal) = {
      taxBandSummaries.find(_.taxBand == "nilRate") shouldBe Some(TaxBandSummary("nilRate", amount, "0%", 0))
      this
    }
  }
}

case class TestEmployment() {
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

case class TestSelfEmployment() {
  def create() = selfEmployment

  import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment._

  private var selfEmployment: repositories.domain.SelfEmployment = SelfEmploymentSugar.aSelfEmployment().copy(allowances = Some(Allowances()), adjustments = Some(Adjustments()))

  def withAllowances(allowancesOnSales: BigDecimal, enhancedCapitalAllowance: BigDecimal,
                     businessPremisesRenovationAllowance: BigDecimal, capitalAllowanceSpecialRatePool: BigDecimal,
                     capitalAllowanceMainPool: BigDecimal, annualInvestmentAllowance: BigDecimal) = {
    selfEmployment = selfEmployment.copy(allowances = selfEmployment.allowances.map(_.copy(
      allowancesOnSales = Some(allowancesOnSales),
      enhancedCapitalAllowance = Some(enhancedCapitalAllowance),
      businessPremisesRenovationAllowance = Some(businessPremisesRenovationAllowance),
      capitalAllowanceSpecialRatePool = Some(capitalAllowanceSpecialRatePool),
      capitalAllowanceMainPool = Some(capitalAllowanceMainPool),
      annualInvestmentAllowance = Some(annualInvestmentAllowance))))

    this
  }

  def withAdjustments(outstandingBusinessIncome: BigDecimal, lossBroughtForward: BigDecimal,
                      averagingAdjustment: BigDecimal, overlapReliefUsed: BigDecimal,
                      basisAdjustment: BigDecimal, includedNonTaxableProfits: BigDecimal) = {
    selfEmployment = selfEmployment.copy(adjustments = selfEmployment.adjustments.map(_.copy(
      outstandingBusinessIncome = Some(outstandingBusinessIncome),
      lossBroughtForward = Some(lossBroughtForward),
      averagingAdjustment = Some(averagingAdjustment),
      overlapReliefUsed = Some(overlapReliefUsed),
      basisAdjustment = Some(basisAdjustment),
      includedNonTaxableProfits = Some(includedNonTaxableProfits))))

    this
  }

  def incomes(incomes: (IncomeType.IncomeType, BigDecimal)*) = {
    selfEmployment = selfEmployment.copy(incomes = incomes.map (income => SelfEmploymentSugar.anIncome(income._1, income._2)))
    this
  }

  def expenses(expenses: (ExpenseType.ExpenseType, BigDecimal)*) = {
    selfEmployment = selfEmployment.copy(expenses = expenses.map (expense => SelfEmploymentSugar.anExpense(expense._1, expense._2)))
    this
  }

  def balancingCharges(balancingCharges: (BalancingChargeType.BalancingChargeType, BigDecimal)*) = {
    selfEmployment = selfEmployment.copy(balancingCharges = balancingCharges.map (balancingCharge => SelfEmploymentSugar.aBalancingCharge(balancingCharge._1, balancingCharge._2)))
    this
  }

  def goodsAndServicesOwnUse(amounts: BigDecimal*) = {
    selfEmployment = selfEmployment.copy(goodsAndServicesOwnUse = amounts.map(SelfEmploymentSugar.aGoodsAndServices))
    this
  }
}

case class TestUkProperty(rentARoomRelief: BigDecimal) {
  import uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty._

  private var ukProperties: UKProperties = UKPropertySugar.aUkProperty().copy(rentARoomRelief = Some(rentARoomRelief),
    allowances = Some(Allowances()), adjustments = Some(Adjustments()))

  def withAllowances(annualInvestmentAllowance: BigDecimal, otherCapitalAllowance: BigDecimal, wearAndTearAllowance: BigDecimal) = {
    ukProperties = ukProperties.copy(allowances = ukProperties.allowances.map(_.copy(otherCapitalAllowance = Some(otherCapitalAllowance),
      annualInvestmentAllowance = Some(annualInvestmentAllowance), wearAndTearAllowance = Some(wearAndTearAllowance))))
    this
  }

  def lossBroughtForward(amount: BigDecimal) = {
    ukProperties = ukProperties.copy(adjustments = Some(Adjustments(lossBroughtForward = Some(amount))))
    this
  }

  def incomes(incomes: (IncomeType.IncomeType, BigDecimal)*) = {
    ukProperties = ukProperties.copy(incomes = incomes.map (income => UKPropertiesIncomeSummary("", income._1, income._2)))
    this
  }

  def expenses(expenses: (ExpenseType.ExpenseType, BigDecimal)*) = {
    ukProperties = ukProperties.copy(expenses = expenses.map (expense => UKPropertiesExpenseSummary("", expense._1, expense._2)))
    this
  }

  def balancingCharges(amounts: BigDecimal*) = {
    ukProperties = ukProperties.copy(balancingCharges = amounts.map (amount => UKPropertiesBalancingChargeSummary("", amount)))
    this
  }

  def privateUseAdjustment(amounts: BigDecimal*) = {
    ukProperties = ukProperties.copy(privateUseAdjustment = amounts.map (amount => UKPropertiesPrivateUseAdjustmentSummary("", amount)))
    this
  }

  def taxesPaid(amounts: BigDecimal*) = {
    ukProperties = ukProperties.copy(taxesPaid = amounts.map (amount => UKPropertiesTaxPaidSummary("", amount)))
    this
  }

  def create() = ukProperties
}

case class TestUnearnedIncome() {
  import uk.gov.hmrc.selfassessmentapi.controllers.api.unearnedincome.DividendType._
  import uk.gov.hmrc.selfassessmentapi.controllers.api.unearnedincome.SavingsIncomeType._

  private var unearnedIncomes: UnearnedIncome = UnearnedIncomesSugar.anIncome()

  def withSavings(savings: (SavingsIncomeType, BigDecimal)*) = {
    unearnedIncomes = unearnedIncomes.copy(savings = savings.map(saving => UnearnedIncomesSavingsIncomeSummary("", saving._1, saving._2)))
    this
  }

  def withDividends(dividends: (DividendType, BigDecimal)*) = {
    unearnedIncomes = unearnedIncomes.copy(dividends = dividends.map(dividend => UnearnedIncomesDividendSummary("", dividend._1, dividend._2)))
    this
  }

  def create() = unearnedIncomes

}

case class TestFurnishedHolidayLetting(capitalAllowance: BigDecimal) {
  import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings._

  private var furnishedHolidayLetting: FurnishedHolidayLettings = FurnishedHolidayLettingsSugar
    .aFurnishedHolidayLetting().copy(allowances = Some(Allowances(capitalAllowance = Some(capitalAllowance))))

  def propertyLocation(propertyLocation: PropertyLocationType.PropertyLocationType) = {
    furnishedHolidayLetting = furnishedHolidayLetting.copy(propertyLocation = propertyLocation)
    this
  }

  def lossBroughtForward(amount: BigDecimal) = {
    furnishedHolidayLetting = furnishedHolidayLetting.copy(adjustments = Some(Adjustments(lossBroughtForward = Some(amount))))
    this
  }

  def incomes(incomes: BigDecimal*) = {
    furnishedHolidayLetting = furnishedHolidayLetting.copy(incomes = incomes.map (FurnishedHolidayLettingsIncomeSummary("", _)))
    this
  }

  def expenses(expenses: (ExpenseType.ExpenseType, BigDecimal)*) = {
    furnishedHolidayLetting = furnishedHolidayLetting.copy(expenses = expenses.map (expense => FurnishedHolidayLettingsExpenseSummary("", expense._1, expense._2)))
    this
  }

  def balancingCharges(amounts: BigDecimal*) = {
    furnishedHolidayLetting = furnishedHolidayLetting.copy(balancingCharges = amounts.map (FurnishedHolidayLettingsBalancingChargeSummary("", _)))
    this
  }

  def privateUseAdjustments(amounts: BigDecimal*) = {
    furnishedHolidayLetting = furnishedHolidayLetting.copy(privateUseAdjustment = amounts.map (FurnishedHolidayLettingsPrivateUseAdjustmentSummary("", _)))
    this
  }

  def create() = furnishedHolidayLetting
}
