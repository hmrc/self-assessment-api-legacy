package uk.gov.hmrc.selfassessmentapi.sandbox

import play.api.libs.json.Json
import uk.gov.hmrc.support.BaseFunctionalSpec

class LiabilityControllerSpec extends BaseFunctionalSpec {

  "request liability" should {
    "return a 202 response with a link to retrieve the liability" in {
      when()
        .post(s"/sandbox/$saUtr/$taxYear/liabilities")
        .thenAssertThat()
        .statusIs(202)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"""^/self-assessment/$saUtr/$taxYear/liabilities/.+""".r)
    }
  }

  "retrieve liability" should {
    "return a 200 response" in {
      val expectedJson = Json.parse(
        s"""
          |{
          |  "income": {
          |    "incomes": {
          |       "selfEmployment": [
          |         {"sourceId": "self-employment-1", "taxableProfit": 8200, "profit": 10000},
          |         {"sourceId": "self-employment-2", "taxableProfit": 25000, "profit": 28000}
          |       ],
          |       "employment": [
          |         {"sourceId": "employment-1", "taxableProfit": 5000, "profit": 5000}
          |       ]
          |      },
          |     "totalIncomeReceived": 93039,
          |     "personalAllowance": 9440,
          |     "totalTaxableIncome": 83599,
          |     "totalIncomeOnWhichTaxIsDue": 80000
          |  },
          |  "incomeTax": {
          |    "calculations": [
          |     {"type": "pay-pensions-profits", "amount": 32010, "percentage": 20, "total": 6402},
          |     {"type": "pay-pensions-profits", "amount": 41030, "percentage": 40, "total": 16412},
          |     {"type": "interest-received", "amount": 0, "percentage": 10, "total": 0},
          |     {"type": "interest-received", "amount": 0, "percentage": 20, "total": 0},
          |     {"type": "interest-received", "amount": 93, "percentage": 40, "total": 37.2},
          |     {"type": "dividends", "amount": 0, "percentage": 10, "total": 0},
          |     {"type": "dividends", "amount": 466, "percentage": 32.5, "total": 151.45}
          |    ],
          |    "total": 23002.65
          |  },
          |  "credits": [
          |        {"type": "dividend", "amount": 46.6},
          |        {"type": "interest-charged", "amount": 12.25}
          |  ],
          |  "class4Nic": {
          |      "calculations": [
          |          { "type": "class-4-nic", "amount": 33695, "percentage": 9, "total": 3032.55},
          |          { "type": "class-4-nic", "amount": 41030, "percentage": 2, "total": 820.60}
          |      ],
          |      "total": 3853.15
          |  },
          |  "totalTaxDue": 25796.95,
          |  "totalAllowancesAndReliefs": 10000.00
          |}
        """.stripMargin)

      when()
        .get(s"/sandbox/$saUtr/$taxYear/liabilities/1234")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"""^/self-assessment/$saUtr/$taxYear/liabilities/1234""".r)
        .bodyIs(expectedJson)
    }

    "return a valid response when retrieving list of liabilities" in {
      when()
        .get(s"/sandbox/$saUtr/$taxYear/liabilities")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/liabilities")
        .bodyHasPath("""_embedded \ liabilities(0) \ _links \ self \ href""", s"/self-assessment/$saUtr/$taxYear/liabilities/1234")
        .bodyHasPath("""_embedded \ liabilities(1) \ _links \ self \ href""", s"/self-assessment/$saUtr/$taxYear/liabilities/4321")
        .bodyHasPath("""_embedded \ liabilities(2) \ _links \ self \ href""", s"/self-assessment/$saUtr/$taxYear/liabilities/7777")
    }

  }

  "delete liability" should {
    "return a 204 response" in {
      when()
        .delete(s"/sandbox/$saUtr/$taxYear/liabilities/1234")
        .thenAssertThat()
        .statusIs(204)
    }
  }

}
