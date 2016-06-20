package uk.gov.hmrc.selfassessmentapi

import play.api.libs.json.Json
import uk.gov.hmrc.support.BaseFunctionalSpec

class TaxYearValidationSpec extends BaseFunctionalSpec {

  "if the tax year in the path is valid for a sandbox request, they" should {
    "return a 200 response" in {
      val expectedJson = Json.parse(
        s"""
          |{
          | 	"pensionContributions": {
          | 		"ukRegisteredPension": 1000.45,
          | 		"retirementAnnuity": 1000.0,
          | 		"employerScheme": 12000.05,
          | 		"overseasPension": 1234.43
          | 	},
          |   "charitableGivings": {
          |     "giftAidPayments": {
          |       "totalInTaxYear": 10000.0,
          |       "oneOff": 5000.0,
          |       "toNonUkCharities": 1000.0,
          |       "carriedBackToPreviousTaxYear": 1000.0,
          |       "carriedFromNextTaxYear": 2000.0
          |     },
          |     "sharesSecurities": 5000.0,
          |     "landProperties": 100.0,
          |     "qualifyingInvestmentsToNonUkCharities": 200.0
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
        .bodyHasLink("self", s"""/self-assessment/$saUtr/$taxYear""")
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
        .statusIs(400)
        .bodyHasPath("""(0) \ code """, "VALUE_BELOW_MINIMUM")
        .bodyHasPath("""(0) \ path """, "/taxYearProperties/childBenefit/dateBenefitStopped")
    }
  }

  "if the tax year is invalid for a sandbox request, they" should {
    "receive 400" in {
      when()
        .get(s"/sandbox/$saUtr/not-a-tax-year").withAcceptHeader()
      .thenAssertThat()
        .statusIs(400)
        .body(_ \ "message").is("ERROR_TAX_YEAR_INVALID")
    }
  }

  "if the tax year in the path is valid for a live request, they" should {
    "receive 501" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
      .when()
        .get(s"/$saUtr/$taxYear").withAcceptHeader()
      .thenAssertThat()
        .statusIs(501)
    }
  }

  "if the tax year is invalid for a live request, they" should {
    "receive 400" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
      .when()
        .get(s"/$saUtr/not-a-tax-year").withAcceptHeader()
      .thenAssertThat()
        .statusIs(400)
        .body(_ \ "message").is("ERROR_TAX_YEAR_INVALID")
    }
  }
}
