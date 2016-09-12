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

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{Liability, _}

object LiabilitySugar extends UnitSpec {
  def aLiability(id: BSONObjectID = BSONObjectID.generate,
                 employmentIncome: Seq[EmploymentIncome] = Seq(),
                 selfEmploymentIncome: Seq[SelfEmploymentIncome] = Seq(),
                 ukPropertyIncome: Seq[UkPropertyIncome] = Seq(),
                 furnishedHolidayLettingsIncome: Seq[FurnishedHolidayLettingIncome] = Seq(),
                 savingsIncome: Seq[InterestFromUKBanksAndBuildingSocieties] = Seq(),
                 ukDividendsIncome: Seq[DividendsFromUKSources] = Seq(),
                 totalIncomeReceived: BigDecimal = 0,
                 totalTaxableIncome: BigDecimal = 0,
                 allowancesAndReliefs: AllowancesAndReliefs = AllowancesAndReliefs(),
                 taxDeducted: MongoTaxDeducted = MongoTaxDeducted(),
                 dividendTaxBandSummary: Seq[TaxBandSummary] = Seq(),
                 savingsTaxBandSummary: Seq[TaxBandSummary] = Seq(),
                 nonSavingsTaxBandSummary: Seq[TaxBandSummary] = Seq(),
                 pensionSavingsChargesSummary: Seq[TaxBandSummary] = Seq(),
                 totalIncomeTax: BigDecimal = 0,
                 totalTaxDeducted: BigDecimal = 0,
                 totalTaxDue: BigDecimal = 0,
                 totalTaxOverPaid: BigDecimal = 0
                 ): Liability = {
    Liability(id,
              id.stringify,
              generateSaUtr(),
              taxYear,
              employmentIncome,
              selfEmploymentIncome,
              ukPropertyIncome,
              furnishedHolidayLettingsIncome,
              savingsIncome,
              ukDividendsIncome,
              totalIncomeReceived,
              totalTaxableIncome,
              allowancesAndReliefs,
              taxDeducted,
              dividendTaxBandSummary,
              savingsTaxBandSummary,
              nonSavingsTaxBandSummary,
              pensionSavingsChargesSummary,
              taxes = TaxesCalculated(
                        totalIncomeTax = totalIncomeTax,
                        totalTaxDeducted = totalTaxDeducted,
                        totalTaxDue = totalTaxDue,
                        totalTaxOverPaid = totalTaxOverPaid,
                        pensionSavingsCharges = 0,
                        taxPaidByPensionScheme = 0
                      )
              )
  }
}
