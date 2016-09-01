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

import uk.gov.hmrc.selfassessmentapi.SelfAssessmentSugar._
import uk.gov.hmrc.selfassessmentapi._
import uk.gov.hmrc.selfassessmentapi.domain.SelfAssessment
import uk.gov.hmrc.selfassessmentapi.domain.pensioncontribution.PensionContribution

class DeductionsSpec extends UnitSpec {

  "IncomeTaxRelief" should {
    "sum of ukPropertyTotalLossBroughtForward,  selfEmploymentTotalLossBroughtForward, furnishedHolidayLettingTotalLossBroughtForward " in {
      Deductions.IncomeTaxRelief(ukPropertyTotalLBF = 100, selfEmploymentTotalLBF = 100, furnishedHolidayLettingTotalLBF = 100) shouldBe 300
    }
  }

  "PersonalAllowance" should {
    "be 11,000 if Even(TotalIncomeReceived - IncomeTaxRelief) <= 100,001" in {
      Deductions.PersonalAllowance(totalIncomeReceived = 100001, incomeTaxRelief = 1, pensionContribution = 1) shouldBe 11000
      Deductions.PersonalAllowance(totalIncomeReceived = 100002, incomeTaxRelief = 1, pensionContribution = 1) shouldBe 11000
      Deductions.PersonalAllowance(totalIncomeReceived = 100003, incomeTaxRelief = 1, pensionContribution = 1) shouldBe 11000
    }

    "be 11,000 - (Even(TotalIncomeReceived - IncomeTaxRelief) - 100,000)/2 if (100,001 < TotalIncomeReceived < 122,000)" in {
      Deductions.PersonalAllowance(totalIncomeReceived = 122001, incomeTaxRelief = 1, pensionContribution = 1) shouldBe (11000 - (121999 - 100000) / 2)
      Deductions.PersonalAllowance(totalIncomeReceived = 120002, incomeTaxRelief = 1, pensionContribution = 1) shouldBe (11000 - (120000 - 100000) / 2)
      Deductions.PersonalAllowance(totalIncomeReceived = 100003, incomeTaxRelief = 1, pensionContribution = 1) shouldBe (11000 - (100001 - 100000) / 2)
    }

    "be 0 if Even(TotalIncomeReceived - IncomeTaxRelief) >= 122,000" in {
      Deductions.PersonalAllowance(totalIncomeReceived = 122000, incomeTaxRelief = 0, pensionContribution = 0) shouldBe 0
      Deductions.PersonalAllowance(totalIncomeReceived = 122001, incomeTaxRelief = 0, pensionContribution = 0) shouldBe 0
      Deductions.PersonalAllowance(totalIncomeReceived = 132000, incomeTaxRelief = 9500, pensionContribution = 500) shouldBe 0
    }
  }

  "RetirementAnnuityContract" should {
    "be sum of the retirement annuity contributions, overseas pensions and employer pension contributions" in {
      val selfAssessment = SelfAssessment(taxYearProperties = Some(aTaxYearProperty
            .copy(pensionContributions = Some(PensionContribution(retirementAnnuity = Some(500.73),
              overseasPension = Some(500.23),
              employerScheme = Some(500.11))))
        .toTaxYearProperties))

      Deductions.RetirementAnnuityContract(selfAssessment) shouldBe 1502
    }

    "be 0 when are no pension contributions" in {
      Deductions.RetirementAnnuityContract(SelfAssessment()) shouldBe 0
    }
  }

  "PensionContribution" should {
    "be sum of the retirement annuity contributions, overseas pensions, employer pension contributions and uk registered pension" in {
      val selfAssessment = SelfAssessment(taxYearProperties = Some(aTaxYearProperty
        .copy(pensionContributions = Some(PensionContribution(retirementAnnuity = Some(500),
          overseasPension = Some(500),
          employerScheme = Some(500),
          ukRegisteredPension = Some(500))))
        .toTaxYearProperties))

      Deductions.PensionContribution(selfAssessment) shouldBe 2000
    }

    "be 0 when are no pension contributions" in {
      Deductions.PensionContribution(SelfAssessment()) shouldBe 0
    }
  }

  "Total Deductions" should {
    "be sum of IncomeTaxRelief + PersonalAllowance + RetirementAnnuityContract" in {
      Deductions.Total(incomeTaxRelief = 100, personalAllowance = 100, retirementAnnuityContract = 100) shouldBe 300
    }
  }
}
