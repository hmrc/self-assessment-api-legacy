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
import uk.gov.hmrc.selfassessmentapi.repositories.domain._
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps.SelfAssessment

object SelfAssessmentSugar extends UnitSpec {

  def aLiability(saUtr: SaUtr = generateSaUtr(),
                 taxYear: TaxYear = taxYear,
                 incomeFromEmployments: Seq[EmploymentIncome] = Nil,
                 profitFromSelfEmployments: Seq[SelfEmploymentIncome] = Nil,
                 interestFromUKBanksAndBuildingSocieties: Seq[InterestFromUKBanksAndBuildingSocieties] = Nil,
                 dividendsFromUKSources: Seq[DividendsFromUKSources] = Nil,
                 deductionsRemaining: Option[BigDecimal] = Some(0),
                 personalSavingsAllowance: Option[BigDecimal] = None,
                 retirementAnnuityContract: Option[BigDecimal] = None,
                 savingsStartingRate: Option[BigDecimal] = None,
                 profitFromUkProperties: Seq[UkPropertyIncome] = Nil,
                 incomeFromFurnishedHolidayLettings: Seq[FurnishedHolidayLettingIncome] = Nil): MongoLiability = {

    MongoLiability
      .create(saUtr, taxYear)
      .copy(incomeFromEmployments = incomeFromEmployments,
            profitFromSelfEmployments = profitFromSelfEmployments,
            interestFromUKBanksAndBuildingSocieties = interestFromUKBanksAndBuildingSocieties,
            dividendsFromUKSources = dividendsFromUKSources,
            deductionsRemaining = deductionsRemaining,
            allowancesAndReliefs = AllowancesAndReliefs(personalSavingsAllowance = personalSavingsAllowance,
                                                        savingsStartingRate = savingsStartingRate,
                                                        retirementAnnuityContract = retirementAnnuityContract),
            profitFromUkProperties = profitFromUkProperties,
            incomeFromFurnishedHolidayLettings = incomeFromFurnishedHolidayLettings)
  }

  def aLiabilityCalculationError(saUtr: SaUtr = generateSaUtr(), taxYear: TaxYear = taxYear): MongoLiabilityCalculationErrors = {
    MongoLiabilityCalculationErrors.create(
        saUtr,
        taxYear,
        Seq(MongoLiabilityCalculationError(ErrorCode.INVALID_EMPLOYMENT_TAX_PAID, "Tax Paid from your Employment(s) should not be negative")))
  }

  def aTaxBandAllocation(taxableAmount: BigDecimal, taxBand: TaxBand) =
    TaxBandAllocation(amount = taxableAmount, taxBand = taxBand)

  def now = DateTime.now(DateTimeZone.UTC)

  implicit class LiabilityExtractor(val liabilityResult: LiabilityResult) {
    def getLiabilityOrFail =
      liabilityResult.fold(calculationError => fail(s"Liability calculation failed with: $calculationError"), identity)
  }

  def aTaxYearProperty = MongoTaxYearProperties(BSONObjectID.generate, generateSaUtr(), taxYear, now, now)

  def aSelfAssessment(employments: Seq[MongoEmployment] = Nil,
                      selfEmployments: Seq[MongoSelfEmployment] = Nil,
                      unearnedIncomes: Seq[MongoUnearnedIncome] = Nil,
                      ukProperties: Seq[MongoUKProperties] = Nil,
                      taxYearProperties: Option[TaxYearProperties] = None,
                      furnishedHolidayLettings: Seq[MongoFurnishedHolidayLettings] = Nil) =
    SelfAssessment(employments,
                   selfEmployments,
                   unearnedIncomes,
                   ukProperties,
                   taxYearProperties,
                   furnishedHolidayLettings)

}
