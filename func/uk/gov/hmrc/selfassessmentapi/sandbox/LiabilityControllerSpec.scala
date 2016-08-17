package uk.gov.hmrc.selfassessmentapi.sandbox

import play.api.libs.json.Json
import uk.gov.hmrc.support.BaseFunctionalSpec

class LiabilityControllerSpec extends BaseFunctionalSpec {

  "request liability" should {
    "return a 202 response with a link to retrieve the liability" in {
      when()
        .post(s"/sandbox/$saUtr/$taxYear/liability")
        .thenAssertThat()
        .statusIs(202)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"""^/self-assessment/$saUtr/$taxYear/liability""".r)
    }
  }

  "retrieve liability" should {
    "return a 200 response" in {
      val expectedJson = Json.parse(
        s"""
          |{
          |  "income": {
          |   "incomes": {
          |     "nonSavings": {
          |       "employment": [
          |         {"sourceId": "employment-1", "pay": 1000, "benefitsAndExpenses": 500, "allowableExpenses": 250, "total": 1250},
          |         {"sourceId": "employment-2", "pay": 2000, "benefitsAndExpenses": 1000, "allowableExpenses": 500, "total": 2500}
          |       ],
          |       "selfEmployment": [
          |         {"sourceId": "self-employment-1", "taxableProfit": 8200, "profit": 10000},
          |         {"sourceId": "self-employment-2", "taxableProfit": 25000, "profit": 28000}
          |       ],
          |       "ukProperties": [
          |            {"sourceId": "property1", "profit": 2000},
          |            {"sourceId": "property2", "profit": 1500}
          |          ]
          |     },
          |     "savings": {
          |       "fromUKBanksAndBuildingSocieties": [
          |         {"sourceId": "interest-income-1", "totalInterest": 100},
          |         {"sourceId": "interest-income-2", "totalInterest": 200}
          |       ]
          |     },
          |     "dividends": {
          |       "fromUKSources": [
          |         {"sourceId": "dividend-income-1", "totalDividend": 1000},
          |         {"sourceId": "dividend-income-2", "totalDividend": 2000}
          |       ]
          |     },
          |     "total": 93039
          |   },
          |   "deductions": {
          |     "incomeTaxRelief": 5000,
          |     "personalAllowance": 9440,
          |     "retirementAnnuityContract": 10000,
          |     "total": 24440
          |   },
          |   "totalIncomeOnWhichTaxIsDue": 80000
          |  },
          |  "incomeTaxCalculations": {
          |     "nonSavings": [
          |       {"taxBand": "basicRate", "taxableAmount": 10000, "chargedAt": "20%", "tax": 2000},
          |       {"taxBand": "higherRate", "taxableAmount": 10000, "chargedAt": "40%", "tax": 4000},
          |       {"taxBand": "additionalHigherRate", "taxableAmount": 10000, "chargedAt": "45%", "tax": 4500}
          |     ],
          |     "savings": [
          |       {"taxBand": "startingRate", "taxableAmount": 10000, "chargedAt": "0%", "tax": 0},
          |       {"taxBand": "nilRate", "taxableAmount": 10000, "chargedAt": "0%", "tax": 0},
          |       {"taxBand": "basicRate", "taxableAmount": 10000, "chargedAt": "20%", "tax": 2000},
          |       {"taxBand": "higherRate", "taxableAmount": 10000, "chargedAt": "40%", "tax": 4000},
          |       {"taxBand": "additionalHigherRate", "taxableAmount": 10000, "chargedAt": "45%", "tax": 4500}
          |     ],
          |     "dividends": [
          |       {"taxBand": "nilRate", "taxableAmount": 10000, "chargedAt": "0%", "tax": 0},
          |       {"taxBand": "basicRate", "taxableAmount": 10000, "chargedAt": "20%", "tax": 2000},
          |       {"taxBand": "higherRate", "taxableAmount": 10000, "chargedAt": "40%", "tax": 4000},
          |       {"taxBand": "additionalHigherRate", "taxableAmount": 10000, "chargedAt": "45%", "tax": 4500}
          |     ],
          |     "total": 31500
          |  },
          |  "taxDeducted": {
          |      "interestFromUk": 0,
          |      "deductionFromUkProperties": 0,
          |      "total": 0
          |  },
          |  "totalTaxDue": 25796.95,
          |  "totalTaxOverpaid": 0
          |}
        """.stripMargin)

      when()
        .get(s"/sandbox/$saUtr/$taxYear/liability")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"""^/self-assessment/$saUtr/$taxYear/liability""".r)
        .bodyIs(expectedJson)
    }

  }

}
