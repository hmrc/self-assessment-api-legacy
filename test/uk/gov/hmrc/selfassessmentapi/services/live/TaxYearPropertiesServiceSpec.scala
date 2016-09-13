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

package uk.gov.hmrc.selfassessmentapi.services.live

import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.config.FeatureSwitch
import uk.gov.hmrc.selfassessmentapi.controllers.api.blindperson.{BlindPerson, BlindPersons}
import uk.gov.hmrc.selfassessmentapi.controllers.api.charitablegiving.{CharitableGiving, CharitableGivings, GiftAidPayments}
import uk.gov.hmrc.selfassessmentapi.controllers.api.childbenefit.{ChildBenefit, ChildBenefits}
import uk.gov.hmrc.selfassessmentapi.controllers.api.pensioncontribution.{PensionContribution, PensionContributions}
import uk.gov.hmrc.selfassessmentapi.controllers.api.studentsloan.{StudentLoan, StudentLoanPlanType, StudentLoans}
import uk.gov.hmrc.selfassessmentapi.controllers.api.taxrefundedorsetoff.{TaxRefundedOrSetOff, TaxRefundedOrSetOffs}
import uk.gov.hmrc.selfassessmentapi.controllers.api.TaxYearProperties
import uk.gov.hmrc.selfassessmentapi.controllers.api.{TaxYear, UkCountryCodes}
import uk.gov.hmrc.selfassessmentapi.repositories.SelfAssessmentMongoRepository

import scala.concurrent.Future

/**
    The FeatureSwitch initialization in TaxYearProperties is preventing newing up in a TaxYearProperties object in the test!!!
    We need to separate documentation generation code from domain; the documentation generation code must be a function of domain
    and embedded in the domain
    Also, we should elevate each TaxYear property to path thus making the feature switch similar to the feature switch on source
    and summary URLs
*/
class TaxYearPropertiesServiceSpec extends UnitSpec with MockitoSugar {

  val mockSaRepository = mock[SelfAssessmentMongoRepository]
  val mockFeatureSwitch = mock[FeatureSwitch]
  val service = new TaxYearPropertiesService(mockSaRepository, mockFeatureSwitch)

  "TaxYearPropertiesService.findTaxYearProperties" should {
    val taxYearProperties = new TaxYearProperties(
      studentLoan = Some(StudentLoan(planType = StudentLoanPlanType.Plan1, deductedByEmployers = Some(10.0))),
      blindPerson = Some(BlindPerson(country = Some(UkCountryCodes.England))),
      childBenefit = Some(ChildBenefit(100, 2)),
      charitableGivings = Some(CharitableGiving(giftAidPayments = Some(GiftAidPayments(totalInTaxYear = Some(100))))),
      taxRefundedOrSetOff = Some(TaxRefundedOrSetOff(200)),
      pensionContributions = Some(PensionContribution(ukRegisteredPension = Some(300)))
    )

    "only include properties that have been enabled" ignore {

      when(mockFeatureSwitch.isEnabled(PensionContributions)).thenReturn(false)
      when(mockFeatureSwitch.isEnabled(CharitableGivings)).thenReturn(false)
      when(mockFeatureSwitch.isEnabled(BlindPersons)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(StudentLoans)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(TaxRefundedOrSetOffs)).thenReturn(false)
      when(mockFeatureSwitch.isEnabled(ChildBenefits)).thenReturn(false)

      when(mockSaRepository.findTaxYearProperties(any[SaUtr], any[TaxYear])).thenReturn(Future.successful(Some(taxYearProperties)))

      val actualProperties = await(service.findTaxYearProperties(generateSaUtr(), taxYear))

      actualProperties.map(_.studentLoan) shouldBe Some(StudentLoan(planType = StudentLoanPlanType.Plan1, deductedByEmployers = Some(10.0)))
      actualProperties.map(_.blindPerson) shouldBe Some(BlindPerson(country = Some(UkCountryCodes.England)))
      actualProperties.map(_.childBenefit) shouldBe None
      actualProperties.map(_.childBenefit) shouldBe None
    }
  }

  "TaxYearPropertiesService.updateTaxYearProperties" should {
    val taxYearProperties = new TaxYearProperties(
      studentLoan = Some(StudentLoan(planType = StudentLoanPlanType.Plan1, deductedByEmployers = Some(10.0))),
      blindPerson = Some(BlindPerson(country = Some(UkCountryCodes.England))),
      childBenefit = Some(ChildBenefit(100, 2)),
      charitableGivings = Some(CharitableGiving(giftAidPayments = Some(GiftAidPayments(totalInTaxYear = Some(100))))),
      taxRefundedOrSetOff = Some(TaxRefundedOrSetOff(200)),
      pensionContributions = Some(PensionContribution(ukRegisteredPension = Some(300)))
    )

    "update the tax year properties when the provided TaxYearProperties object contains no disabled features" ignore {
      val saUtr = generateSaUtr()

      when(mockFeatureSwitch.isEnabled(PensionContributions)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(CharitableGivings)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(BlindPersons)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(StudentLoans)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(TaxRefundedOrSetOffs)).thenReturn(false)
      when(mockFeatureSwitch.isEnabled(ChildBenefits)).thenReturn(false)

      service.updateTaxYearProperties(saUtr, taxYear, taxYearProperties)

      verify(mockSaRepository, times(1)).updateTaxYearProperties(saUtr, taxYear, TaxYearProperties(
        studentLoan = Some(StudentLoan(planType = StudentLoanPlanType.Plan1, deductedByEmployers = Some(10.0))),
        blindPerson = Some(BlindPerson(country = Some(UkCountryCodes.England))),
        charitableGivings = Some(CharitableGiving(giftAidPayments = Some(GiftAidPayments(totalInTaxYear = Some(100))))),
        pensionContributions = Some(PensionContribution(ukRegisteredPension = Some(300)))
      ))
    }

    "not update the tax year properties when the provided TaxYearProperties object contains disabled features" ignore {
      when(mockFeatureSwitch.isEnabled(PensionContributions)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(CharitableGivings)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(BlindPersons)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(StudentLoans)).thenReturn(false)
      when(mockFeatureSwitch.isEnabled(TaxRefundedOrSetOffs)).thenReturn(false)
      when(mockFeatureSwitch.isEnabled(ChildBenefits)).thenReturn(false)

      service.updateTaxYearProperties(generateSaUtr(), taxYear, taxYearProperties)

      verify(mockSaRepository, times(0)).updateTaxYearProperties(any[SaUtr], any[TaxYear], any[TaxYearProperties])
    }
  }
}
