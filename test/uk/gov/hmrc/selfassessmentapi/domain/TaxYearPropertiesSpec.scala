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

package uk.gov.hmrc.selfassessmentapi.domain

import uk.gov.hmrc.selfassessmentapi.domain.CountryCodes.GBR
import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode._

class TaxYearPropertiesSpec extends JsonSpec {


  "format" should {
    "round trip valid PensionContribution json" in {
      roundTripJson(PensionContribution.example())
    }

     "round trip valid CharitableGiving json" in {
      roundTripJson(CharitableGiving.example())
    }

    "round trip valid TaxYearProperties json" in {
      roundTripJson(TaxYearProperties.example())
    }
  }

  "validate" should {

    "in PensionContribution reject amounts with more than 2 decimal values" in {
      Seq(BigDecimal(1000.123), BigDecimal(1000.1234), BigDecimal(1000.12345), BigDecimal(1000.123456789)).foreach { testAmount =>
        assertValidationError[PensionContribution](
          PensionContribution(ukRegisteredPension = Some(testAmount)),
          Map("/ukRegisteredPension" -> INVALID_MONETARY_AMOUNT), "Expected invalid uk registered pension with more than 2 decimal places")
        assertValidationError[PensionContribution](
          PensionContribution(retirementAnnuity = Some(testAmount)),
          Map("/retirementAnnuity" -> INVALID_MONETARY_AMOUNT), "Expected invalid retirement annuity with more than 2 decimal places")
        assertValidationError[PensionContribution](
          PensionContribution(employerScheme = Some(testAmount)),
          Map("/employerScheme" -> INVALID_MONETARY_AMOUNT), "Expected invalid employer annuity with more than 2 decimal places")
        assertValidationError[PensionContribution](
          PensionContribution(overseasPension = Some(testAmount)),
          Map("/overseasPension" -> INVALID_MONETARY_AMOUNT), "Expected invalid overseas pension with more than 2 decimal places")

      }
    }

    "in PensionContribution reject negative amount" in {
      Seq(BigDecimal(-1000.12), BigDecimal(-1000)).foreach { testAmount =>
        assertValidationError[PensionContribution](
          PensionContribution(ukRegisteredPension = Some(testAmount)),
          Map("/ukRegisteredPension" -> INVALID_MONETARY_AMOUNT), "Expected invalid uk registered pension with more than 2 decimal places")
        assertValidationError[PensionContribution](
          PensionContribution(retirementAnnuity = Some(testAmount)),
          Map("/retirementAnnuity" -> INVALID_MONETARY_AMOUNT), "Expected invalid retirement annuity with more than 2 decimal places")
        assertValidationError[PensionContribution](
          PensionContribution(employerScheme = Some(testAmount)),
          Map("/employerScheme" -> INVALID_MONETARY_AMOUNT), "Expected invalid employer annuity with more than 2 decimal places")
        assertValidationError[PensionContribution](
          PensionContribution(overseasPension = Some(testAmount)),
          Map("/overseasPension" -> INVALID_MONETARY_AMOUNT), "Expected invalid overseas pension with more than 2 decimal places")
      }
    }

    "in CharitableGiving reject amounts with more than 2 decimal values" in {
      Seq(BigDecimal(1000.123), BigDecimal(1000.1234), BigDecimal(1000.12345), BigDecimal(1000.123456789)).foreach { testAmount =>
        assertValidationError[CharitableGiving](
          CharitableGiving(giftAidPayments = Some(CountryAndAmount(GBR, testAmount))),
          Map("/giftAidPayments/amount" -> INVALID_MONETARY_AMOUNT), "Expected invalid uk registered pension with more than 2 decimal places")
        assertValidationError[CharitableGiving](
          CharitableGiving(oneOffGiftAidPayments = Some(CountryAndAmount(GBR, testAmount))),
          Map("/oneOffGiftAidPayments/amount" -> INVALID_MONETARY_AMOUNT), "Expected invalid uk registered pension with more than 2 decimal places")
        assertValidationError[CharitableGiving](
          CharitableGiving(sharesSecurities = Some(CountryAndAmount(GBR, testAmount))),
          Map("/sharesSecurities/amount" -> INVALID_MONETARY_AMOUNT), "Expected invalid uk registered pension with more than 2 decimal places")
        assertValidationError[CharitableGiving](
          CharitableGiving(landProperties = Some(CountryAndAmount(GBR, testAmount))),
          Map("/landProperties/amount" -> INVALID_MONETARY_AMOUNT), "Expected invalid uk registered pension with more than 2 decimal places")
        assertValidationError[CharitableGiving](
          CharitableGiving(giftAidPaymentsCarriedBackToPreviousYear = Some(CountryAndAmount(GBR, testAmount))),
          Map("/giftAidPaymentsCarriedBackToPreviousYear/amount" -> INVALID_MONETARY_AMOUNT), "Expected invalid uk registered pension with more than 2 decimal places")
        assertValidationError[CharitableGiving](
          CharitableGiving(giftAidPaymentsCarriedForwardToNextYear = Some(CountryAndAmount(GBR, testAmount))),
          Map("/giftAidPaymentsCarriedForwardToNextYear/amount" -> INVALID_MONETARY_AMOUNT), "Expected invalid uk registered pension with more than 2 decimal places")
      }
    }

    "in CharitableGiving reject negative amount" in {
      Seq(BigDecimal(-1000.12), BigDecimal(-1000)).foreach { testAmount =>
        assertValidationError[CharitableGiving](
          CharitableGiving(giftAidPayments = Some(CountryAndAmount(GBR, testAmount))),
          Map("/giftAidPayments/amount" -> INVALID_MONETARY_AMOUNT), "Expected invalid uk registered pension with more than 2 decimal places")
        assertValidationError[CharitableGiving](
          CharitableGiving(oneOffGiftAidPayments = Some(CountryAndAmount(GBR, testAmount))),
          Map("/oneOffGiftAidPayments/amount" -> INVALID_MONETARY_AMOUNT), "Expected invalid uk registered pension with more than 2 decimal places")
        assertValidationError[CharitableGiving](
          CharitableGiving(sharesSecurities = Some(CountryAndAmount(GBR, testAmount))),
          Map("/sharesSecurities/amount" -> INVALID_MONETARY_AMOUNT), "Expected invalid uk registered pension with more than 2 decimal places")
        assertValidationError[CharitableGiving](
          CharitableGiving(landProperties = Some(CountryAndAmount(GBR, testAmount))),
          Map("/landProperties/amount" -> INVALID_MONETARY_AMOUNT), "Expected invalid uk registered pension with more than 2 decimal places")
        assertValidationError[CharitableGiving](
          CharitableGiving(giftAidPaymentsCarriedBackToPreviousYear = Some(CountryAndAmount(GBR, testAmount))),
          Map("/giftAidPaymentsCarriedBackToPreviousYear/amount" -> INVALID_MONETARY_AMOUNT), "Expected invalid uk registered pension with more than 2 decimal places")
        assertValidationError[CharitableGiving](
          CharitableGiving(giftAidPaymentsCarriedForwardToNextYear = Some(CountryAndAmount(GBR, testAmount))),
          Map("/giftAidPaymentsCarriedForwardToNextYear/amount" -> INVALID_MONETARY_AMOUNT), "Expected invalid uk registered pension with more than 2 decimal places")
      }
    }

    "reject if oneOffGiftAidPayments is defined but giftAidPayments element is not defined" in {
      assertValidationError[CharitableGiving](
        CharitableGiving(oneOffGiftAidPayments = Some(CountryAndAmount(GBR, 1000.00))),
        Map("" -> UNDEFINED_REQUIRED_ELEMENT), "Expected CharitableGiving instance with giftAitPayments = None")
    }

    "reject if oneOffGiftAidPayments is defined and greater than giftAidPayments" in {
      assertValidationError[CharitableGiving](
        CharitableGiving(giftAidPayments= Some(CountryAndAmount(GBR, 999.99)),
                         oneOffGiftAidPayments = Some(CountryAndAmount(GBR, 1000.00))),
        Map("" -> MAXIMUM_AMOUNT_EXCEEDED), "Expected CharitableGiving instance with giftAitPayments < oneOffGiftAidPayments")
    }

    "reject if giftAidPaymentsCarriedBackToPreviousYear defined but giftAidPayments element is not defined" in {
      assertValidationError[CharitableGiving](
        CharitableGiving(giftAidPaymentsCarriedBackToPreviousYear = Some(CountryAndAmount(GBR, 1000.00))),
        Map("" -> UNDEFINED_REQUIRED_ELEMENT), "Expected CharitableGiving instance with giftAitPayments = None")
    }

    "reject if giftAidPaymentsCarriedForwardToNextYear defined but giftAidPayments element is not defined" in {
      assertValidationError[CharitableGiving](
        CharitableGiving(giftAidPaymentsCarriedForwardToNextYear = Some(CountryAndAmount(GBR, 1000.00))),
        Map("" -> UNDEFINED_REQUIRED_ELEMENT), "Expected CharitableGiving instance with giftAitPayments = None")
    }

    "reject if giftAidPaymentsCarriedForwardToNextYear + giftAidPaymentsCarriedBackToPreviousYear > giftAidPayments" in {
      assertValidationError[CharitableGiving](
        CharitableGiving(giftAidPayments= Some(CountryAndAmount(GBR, 999.99)),
                         giftAidPaymentsCarriedForwardToNextYear = Some(CountryAndAmount(GBR, 500.00)),
                         giftAidPaymentsCarriedBackToPreviousYear = Some(CountryAndAmount(GBR, 500.00))),
        Map("" -> MAXIMUM_AMOUNT_EXCEEDED), "Expected CharitableGiving instance with " +
          "giftAitPayments < (giftAidPaymentsCarriedForwardToNextYear + giftAidPaymentsCarriedBackToPreviousYear)")
    }
  }

}
