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

package uk.gov.hmrc.selfassessmentapi

import org.joda.time.{DateTime, DateTimeZone}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.BalancingChargeType._
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.ExpenseType._
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.IncomeType._
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.DividendType.{DividendType, FromUKCompanies}
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.SavingsIncomeType._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{TaxBandAllocation, _}
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps.SelfAssessment

trait SelfEmploymentSugar {

  this: UnitSpec =>

  def aSelfEmployment(id: SourceId = BSONObjectID.generate.stringify, saUtr: SaUtr = generateSaUtr(), taxYear: TaxYear = taxYear) = MongoSelfEmployment(BSONObjectID.generate, id, saUtr, taxYear, now, now, now.toLocalDate)

  def anUnearnedIncomes(id: SourceId = BSONObjectID.generate.stringify, saUtr: SaUtr = generateSaUtr(), taxYear: TaxYear = taxYear) = MongoUnearnedIncome(BSONObjectID.generate, id, saUtr, taxYear, now, now)

  def anUnearnedInterestIncomeSummary(summaryId: SummaryId = BSONObjectID.generate.stringify, `type`: SavingsIncomeType = InterestFromBanksUntaxed, amount: BigDecimal) = MongoUnearnedIncomesSavingsIncomeSummary(summaryId, `type`, amount)

  def anUnearnedDividendIncomeSummary(summaryId: SummaryId = BSONObjectID.generate.stringify, `type`: DividendType = FromUKCompanies, amount: BigDecimal) = MongoUnearnedIncomesDividendSummary(summaryId, `type`, amount)

  def income(`type`: IncomeType, amount: BigDecimal) = MongoSelfEmploymentIncomeSummary(BSONObjectID.generate.stringify, `type`, amount)

  def expense(`type`: ExpenseType, amount: BigDecimal) = MongoSelfEmploymentExpenseSummary(BSONObjectID.generate.stringify, `type`, amount)

  def balancingCharge(`type`: BalancingChargeType, amount: BigDecimal) = MongoSelfEmploymentBalancingChargeSummary(BSONObjectID.generate.stringify, `type`, amount)

  def goodsAndServices(amount: BigDecimal) = MongoSelfEmploymentGoodsAndServicesOwnUseSummary(BSONObjectID.generate.stringify, amount)

  def aLiability(saUtr: SaUtr = generateSaUtr(), taxYear: TaxYear = taxYear, incomeFromEmployments: Seq[EmploymentIncome] = Nil, profitFromSelfEmployments: Seq[SelfEmploymentIncome] = Nil,
                 interestFromUKBanksAndBuildingSocieties: Seq[InterestFromUKBanksAndBuildingSocieties] = Nil, dividendsFromUKSources: Seq[DividendsFromUKSources] = Nil,
                 deductionsRemaining: Option[BigDecimal] = Some(0), personalSavingsAllowance: Option[BigDecimal] = None, savingsStartingRate: Option[BigDecimal] = None, profitFromUkProperties: Seq[UkPropertyIncome] = Nil): MongoLiability = {

    MongoLiability.create(saUtr, taxYear).copy( incomeFromEmployments = incomeFromEmployments, profitFromSelfEmployments = profitFromSelfEmployments, interestFromUKBanksAndBuildingSocieties = interestFromUKBanksAndBuildingSocieties,
                                                dividendsFromUKSources = dividendsFromUKSources, deductionsRemaining = deductionsRemaining,
                                                allowancesAndReliefs = AllowancesAndReliefs(personalSavingsAllowance = personalSavingsAllowance, savingsStartingRate = savingsStartingRate),
                                                profitFromUkProperties = profitFromUkProperties)
  }

  def aSelfEmploymentIncome(profit: BigDecimal = 0, taxableProfit: BigDecimal = 0) =
    SelfEmploymentIncome(sourceId = BSONObjectID.generate.stringify, taxableProfit = taxableProfit, profit = profit)

  def aTaxBandAllocation(taxableAmount: BigDecimal, taxBand: TaxBand) = TaxBandAllocation(amount = taxableAmount, taxBand = taxBand)

  def aUkProperty(id: SourceId = BSONObjectID.generate.stringify) = MongoUKProperties(BSONObjectID.generate, id, generateSaUtr(), taxYear)

  def aUkPropertyIncome(profit: BigDecimal): UkPropertyIncome = UkPropertyIncome(generateSaUtr().utr, profit)

  def aUkPropertyTaxPaidSummary(amount: BigDecimal) = MongoUKPropertiesTaxPaidSummary(BSONObjectID.generate.stringify, amount)

  def aSelfAssessment(employments: Seq[MongoEmployment] = Nil, selfEmployments: Seq[MongoSelfEmployment] = Nil,
                      unearnedIncomes: Seq[MongoUnearnedIncome] = Nil, ukProperties: Seq[MongoUKProperties] = Nil) =
    SelfAssessment(employments, selfEmployments, unearnedIncomes, ukProperties)

  private def now = DateTime.now(DateTimeZone.UTC)
}
