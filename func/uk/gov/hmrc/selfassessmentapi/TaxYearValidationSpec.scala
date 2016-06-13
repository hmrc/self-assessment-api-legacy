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
          | 	"charitableGivings": {
          | 		"giftAidPayments": {
          | 			"countryCode": "GBR",
          | 			"amount": 100000
          | 		},
          | 		"oneOffGiftAidPayments": {
          | 			"countryCode": "USA",
          | 			"amount": 5000.0
          | 		},
          | 		"sharesSecurities": {
          | 			"countryCode": "CAN",
          | 			"amount": 53000.0
          |	  	},
          |	  	"landProperties": {
          |	  		"countryCode": "RUS",
          |	  		"amount": 1000000.0
          |	  	},
          |	  	"giftAidPaymentsCarriedBackToPreviousYear": {
          | 			"countryCode": "AUS",
          | 			"amount": 2000.0
          | 		},
          | 		"giftAidPaymentsCarriedForwardToNextYear": {
          | 			"countryCode": "NZL",
          | 			"amount": 50000.0
          | 		}
          | 	},
          | 	"blindPerson": {
          | 		"country": "Wales",
          | 		"registrationAuthority": "Registrar",
          | 		"spouseSurplusAllowance": 2000.05,
          | 		"wantSpouseToUseSurplusAllowance": true
          | 	},
          |   "studentLoan": {
          |     "planType": "Plan1",
          |     "deductedByEmployers": 2000.00
          |   }
          | }
        """.stripMargin)

      given()
      .when()
        .get(s"/sandbox/$saUtr/$taxYear").withAcceptHeader()
      .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"""/self-assessment/$saUtr/$taxYear""")
        .bodyIs(expectedJson)
    }
  }

  "if the tax year is invalid for a sandbox request, they" should {
    "receive 400" in {
      given()
      .when()
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
