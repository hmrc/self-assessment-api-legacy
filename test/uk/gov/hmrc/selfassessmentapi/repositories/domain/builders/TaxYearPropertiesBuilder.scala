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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.builders

import org.joda.time.LocalDate
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.controllers.api.TaxYearProperties
import uk.gov.hmrc.selfassessmentapi.controllers.api.UkCountryCodes.UkCountryCode
import uk.gov.hmrc.selfassessmentapi.controllers.api.blindperson.BlindPerson
import uk.gov.hmrc.selfassessmentapi.controllers.api.charitablegiving._
import uk.gov.hmrc.selfassessmentapi.controllers.api.childbenefit.ChildBenefit
import uk.gov.hmrc.selfassessmentapi.controllers.api.pensioncontribution.{PensionContribution, PensionSaving}
import uk.gov.hmrc.selfassessmentapi.controllers.api.studentsloan.StudentLoan
import uk.gov.hmrc.selfassessmentapi.controllers.api.studentsloan.StudentLoanPlanType.StudentLoanPlanType
import uk.gov.hmrc.selfassessmentapi.controllers.api.taxrefundedorsetoff.TaxRefundedOrSetOff

case class TaxYearPropertiesBuilder(objectID: BSONObjectID = BSONObjectID.generate) {
  def create(): TaxYearProperties = taxYearProperties

  private var taxYearProperties = TaxYearProperties()

  def withPensionContributions() = {
    taxYearProperties = taxYearProperties.copy(pensionContributions = Some(PensionContribution()))
    this
  }

  def withCharitableGivings() = {
    taxYearProperties = taxYearProperties.copy(charitableGivings = Some(CharitableGiving()))
    this
  }

  def withBlindPerson(country: UkCountryCode, registrationAuthority: String, spouseSurplusAllowance: BigDecimal,
                      wantsSpouseToUseSurplusAllowance: Boolean) = {
    taxYearProperties = taxYearProperties.copy(
      blindPerson = Some(
        BlindPerson(Some(country),
        Some(registrationAuthority),
        Some(spouseSurplusAllowance),
        Some(wantsSpouseToUseSurplusAllowance))))
    this
  }

  def withStudentLoan(planType: StudentLoanPlanType, deductedByEmployers: BigDecimal) = {
    taxYearProperties = taxYearProperties.copy(studentLoan = Some(StudentLoan(planType, Some(deductedByEmployers))))
    this
  }

  def withTaxRefundedOrSetOff(amount: BigDecimal) = {
    taxYearProperties = taxYearProperties.copy(taxRefundedOrSetOff = Some(TaxRefundedOrSetOff(amount)))
    this
  }

  def withChildBenefit(amount: BigDecimal, numberOfChildren: Int, dateBenefitStopped: LocalDate) = {
    taxYearProperties = taxYearProperties.copy(childBenefit =
      Some(ChildBenefit(amount, numberOfChildren, Some(dateBenefitStopped))))
    this
  }

  /*
   * Pension Contributions
   */

  def ukRegisteredPension(amount: BigDecimal) = {
    taxYearProperties = taxYearProperties.copy(
      pensionContributions = taxYearProperties.pensionContributions.map(_.copy(ukRegisteredPension = Some(amount))))
    this
  }

  def retirementAnnuityContract(amount: BigDecimal) = {
    taxYearProperties = taxYearProperties.copy(
      pensionContributions = taxYearProperties.pensionContributions.map(_.copy(retirementAnnuity = Some(amount))))
    this
  }

  def employerScheme(amount: BigDecimal) = {
    taxYearProperties = taxYearProperties.copy(
      pensionContributions = taxYearProperties.pensionContributions.map(_.copy(employerScheme = Some(amount))))
    this
  }

  def overseasPension(amount: BigDecimal) = {
    taxYearProperties = taxYearProperties.copy(
      pensionContributions = taxYearProperties.pensionContributions.map(_.copy(overseasPension = Some(amount))))
    this
  }

  def pensionSavings(excessOfAnnualAllowance: BigDecimal = 0, taxPaidByPensionScheme: BigDecimal = 0) = {
    taxYearProperties = taxYearProperties.copy(pensionContributions =
      taxYearProperties.pensionContributions.map(_.copy(pensionSavings =
        Some(PensionSaving(excessOfAnnualAllowance = Some(excessOfAnnualAllowance), taxPaidByPensionScheme = Some(taxPaidByPensionScheme))))))
    this
  }

  /*
   * Charitable Givings
   */

  def giftAidPayments(totalInTaxYear: BigDecimal, oneOff: BigDecimal, toNonUkCharities: BigDecimal,
                      carriedBackToPreviousTaxYear: BigDecimal, carriedFromNextTaxYear: BigDecimal) = {
    taxYearProperties = taxYearProperties.copy(charitableGivings =
      taxYearProperties.charitableGivings.map(_.copy(giftAidPayments = Some(GiftAidPayments(
        Some(totalInTaxYear),
        Some(oneOff),
        Some(toNonUkCharities),
        Some(carriedBackToPreviousTaxYear),
        Some(carriedFromNextTaxYear))))))
    this
  }

  def sharesSecurities(totalInTaxYear: BigDecimal, toNonUkCharities: BigDecimal) = {
    taxYearProperties = taxYearProperties.copy(charitableGivings =
      taxYearProperties.charitableGivings.map(_.copy(sharesSecurities =
        Some(SharesAndSecurities(totalInTaxYear, Some(toNonUkCharities))))))
    this
  }

  def landAndProperties(totalInTaxYear: BigDecimal, toNonUkCharities: BigDecimal) = {
    taxYearProperties = taxYearProperties.copy(charitableGivings =
      taxYearProperties.charitableGivings.map(_.copy(landProperties =
        Some(LandAndProperties(totalInTaxYear, Some(toNonUkCharities))))))
    this
  }
}
