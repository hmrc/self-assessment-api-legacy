package uk.gov.hmrc.selfassessmentapi

import play.api.libs.json.Json
import play.api.test.FakeApplication
import uk.gov.hmrc.support.BaseFunctionalSpec

// FIXME: Refactor into live and sandbox tests

class TaxYearValidationSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map("Test.feature-switch.pensionContributions.enabled" -> true,
                                                                                         "Test.feature-switch.charitableGivings.enabled" -> true,
                                                                                         "Test.feature-switch.blindPerson.enabled" -> true,
                                                                                         "Test.feature-switch.studentLoan.enabled" -> true,
                                                                                         "Test.feature-switch.taxRefundedOrSetOff.enabled" -> true,
                                                                                         "Test.feature-switch.childBenefit.enabled" -> true))

  "if the tax year in the path is valid for a sandbox request, they" should {
    "return a 200 response containing pension contributions" in {
      val expectedJson = Json.parse(
        s"""
           |{
           | 	"pensionContributions": {
           | 		"ukRegisteredPension": 1000.45,
           | 		"retirementAnnuity": 1000.0,
           | 		"employerScheme": 12000.05,
           | 		"overseasPension": 1234.43,
           |    "pensionSavings": {
           |      "excessOfAnnualAllowance": 200,
           |      "taxPaidByPensionScheme": 123.23
           |    }
           | 	},
           |   "charitableGivings": {
           |     "giftAidPayments": {
           |       "totalInTaxYear": 10000.0,
           |       "oneOff": 5000.0,
           |       "toNonUkCharities": 1000.0,
           |       "carriedBackToPreviousTaxYear": 1000.0,
           |       "carriedFromNextTaxYear": 2000.0
           |     },
           |     "sharesSecurities": {
           |       "totalInTaxYear": 2000.0,
           |       "toNonUkCharities": 500.0
           |     },
           |     "landProperties":  {
           |       "totalInTaxYear": 4000.0,
           |       "toNonUkCharities": 3000.0
           |     }
           |   },
           | 	"blindPerson": {
           | 		"country": "Wales",
           | 		"registrationAuthority": "Registrar",
           | 		"spouseSurplusAllowance": 2000.05,
           | 		"wantSpouseToUseSurplusAllowance": true
           | 	},
           |   "studentLoan": {
           |     "planType": "Plan1",
           |     "deductedByEmployers": 2000.00
           |   },
           |   "taxRefundedOrSetOff": {
           |     "amount": 2000.00
           |   },
           |   "childBenefit": {
           |    "amount": 1234.34,
           |    "numberOfChildren": 3,
           |    "dateBenefitStopped": "2016-04-05"
           |  }
           | }
        """.stripMargin)

      when()
        .get(s"/sandbox/$saUtr/$taxYear").withAcceptHeader()
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear")
        .bodyIs(expectedJson)
    }
  }

  "the payload is invalid for a sandbox request, they" should {

    "receive 400 if the dateBenefitStopped is after the end of tax year from the url " in {
      val payload = Json.parse(
        s"""
           |{
           |   "childBenefit": {
           |    "amount": 1234.34,
           |    "numberOfChildren": 3,
           |    "dateBenefitStopped": "2017-04-06"
           |  }
           | }
        """.stripMargin)
      when()
        .put(s"/sandbox/$saUtr/$taxYear", Some(payload))
        .thenAssertThat()
        .isValidationError("/taxYearProperties/childBenefit/dateBenefitStopped", "BENEFIT_STOPPED_DATE_INVALID")
    }
  }

  "if the tax year is invalid for a sandbox request, they" should {
    "receive 400" in {
      when()
        .get(s"/sandbox/$saUtr/not-a-tax-year").withAcceptHeader()
        .thenAssertThat()
        .isBadRequest("TAX_YEAR_INVALID")
    }
  }

  "if the tax year in the path is valid for a live request, they" should {
    "receive 200" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear").withAcceptHeader()
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear")
        .bodyHasLink("self-employments", s"""/self-assessment/$saUtr/$taxYear/self-employments""")
    }
  }

  "if the tax year is invalid for a live request, they" should {
    "receive 400" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/not-a-tax-year").withAcceptHeader()
        .thenAssertThat()
        .isBadRequest("TAX_YEAR_INVALID")
    }
  }

  "update tax year properties" should {
    "return 400 and validation error if payload does not contain only Pension Contributions for a live request" ignore {
      val payload = Json.parse(
        s"""
           |
           | {
           |   "pensionContributions": {
           | 		"ukRegisteredPension": 1000.45,
           | 		"retirementAnnuity": 1000.0,
           | 		"employerScheme": 12000.05,
           | 		"overseasPension": 1234.43
           | 	 },
           |   "childBenefit": {
           |    "amount": 1234.34,
           |    "numberOfChildren": 3,
           |    "dateBenefitStopped": "2017-04-06"
           |   }
           |}
        """.stripMargin)
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear", Some(payload))
        .thenAssertThat()
        .isValidationError("/taxYearProperties", "ONLY_PENSION_CONTRIBUTIONS_SUPPORTED")
    }
  }


  "if the live request is valid it" should {
    "update and retrieve the pension contributions tax year properties" ignore {

      val payload, expectedJson = Json.parse(
        s"""
           |{
           | 	"pensionContributions": {
           | 		"ukRegisteredPension": 1000.45,
           | 		"retirementAnnuity": 1000.0,
           | 		"employerScheme": 12000.05,
           | 		"overseasPension": 1234.43
           | 	}
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear", Some(payload))
        .thenAssertThat()
        .statusIs(200)
      when()
        .get(s"/$saUtr/$taxYear").withAcceptHeader()
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"""/self-assessment/$saUtr/$taxYear""")
        .bodyIs(expectedJson)
    }
  }
}

class TaxYearFeatureSwitchOnSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map("update-tax-year-properties.enabled" -> false))

  "if update-tax-year-properties switch is disabled for live mode it" should {
    "return 501 not implemented" in {

      val payload = Json.parse(
        s"""
           |{
           | 	"pensionContributions": {
           | 		"ukRegisteredPension": 1000.45,
           | 		"retirementAnnuity": 1000.0,
           | 		"employerScheme": 12000.05,
           | 		"overseasPension": 1234.43
           | 	}
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear", Some(payload))
        .thenAssertThat()
        .isNotImplemented
    }
  }
}

class TaxYearFeatureSwitchOffSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map("update-tax-year-properties.enabled" -> true))

  "if update-tax-year-properties switch is enabled for live mode it" should {
    "return 200" in {

      val payload = Json.parse(
        s"""
           |{
           | 	"pensionContributions": {
           | 		"ukRegisteredPension": 1000.45,
           | 		"retirementAnnuity": 1000.0,
           | 		"employerScheme": 12000.05,
           | 		"overseasPension": 1234.43
           | 	}
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear", Some(payload))
        .thenAssertThat()
        .statusIs(200)
    }
  }
}
