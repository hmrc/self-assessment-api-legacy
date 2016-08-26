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

package uk.gov.hmrc.selfassessmentapi.services

import org.mockito.Matchers.{eq => matchEq, any}
import org.mockito.Mockito.{times, when, verify}
import org.scalatest.mock.MockitoSugar

import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.config.FeatureSwitch
import uk.gov.hmrc.selfassessmentapi.domain.{TaxYear, TaxYearProperties}
import uk.gov.hmrc.selfassessmentapi.domain.blindperson.BlindPersons
import uk.gov.hmrc.selfassessmentapi.domain.charitablegiving.CharitableGivings
import uk.gov.hmrc.selfassessmentapi.domain.childbenefit.ChildBenefits
import uk.gov.hmrc.selfassessmentapi.domain.pensioncontribution.PensionContributions
import uk.gov.hmrc.selfassessmentapi.domain.studentsloan.StudentLoans
import uk.gov.hmrc.selfassessmentapi.domain.taxrefundedorsetoff.TaxRefundedOrSetOffs
import uk.gov.hmrc.selfassessmentapi.repositories.SelfAssessmentMongoRepository

class TaxYearPropertiesServiceSpec extends UnitSpec with MockitoSugar {
  val mockSaRepository = mock[SelfAssessmentMongoRepository]
  val mockFeatureSwitch = mock[FeatureSwitch]

  val unitUnderTest = new TaxYearPropertiesService(mockSaRepository, mockFeatureSwitch)

  "TaxYearPropertiesService.findTaxYearProperties" should {
    "only include properties that have been enabled" in {
      when(mockFeatureSwitch.isEnabled(PensionContributions)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(CharitableGivings)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(BlindPersons)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(StudentLoans)).thenReturn(false)
      when(mockFeatureSwitch.isEnabled(TaxRefundedOrSetOffs)).thenReturn(false)
      when(mockFeatureSwitch.isEnabled(ChildBenefits)).thenReturn(false)

      val properties = TaxYearProperties.example()

      when(mockSaRepository.findTaxYearProperties(any[SaUtr], any[TaxYear])).thenReturn(Some(properties))

      val expected = Some(properties.copy(studentLoan = None, taxRefundedOrSetOff = None, childBenefit = None))
      val actual = await(unitUnderTest.findTaxYearProperties(generateSaUtr(), taxYear))

      expected shouldBe actual
    }
  }

  "TaxYearPropertiesService.updateTaxYearProperties" should {
    "only include properties that have been enabled" in {
      when(mockFeatureSwitch.isEnabled(PensionContributions)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(CharitableGivings)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(BlindPersons)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(StudentLoans)).thenReturn(false)
      when(mockFeatureSwitch.isEnabled(TaxRefundedOrSetOffs)).thenReturn(false)
      when(mockFeatureSwitch.isEnabled(ChildBenefits)).thenReturn(false)

      val properties = TaxYearProperties.example()

      when(mockSaRepository.findTaxYearProperties(any[SaUtr], any[TaxYear])).thenReturn(Some(properties))

      val expected = properties.copy(studentLoan = None, taxRefundedOrSetOff = None, childBenefit = None)

      unitUnderTest.updateTaxYearProperties(generateSaUtr(), taxYear, properties)

      verify(mockSaRepository, times(1)).updateTaxYearProperties(any[SaUtr], any[TaxYear], matchEq(expected))
    }
  }
}
