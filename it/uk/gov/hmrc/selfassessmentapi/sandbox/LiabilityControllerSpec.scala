package uk.gov.hmrc.selfassessmentapi.sandbox

import play.api.libs.json.Json
import uk.gov.hmrc.support.BaseFunctionalSpec

class LiabilityControllerSpec extends BaseFunctionalSpec {

  val saUtr = generateSaUtr()

  "request liability" should {
    "return a 202 response with a link to retrieve the liability" in {
      given()
        .when()
        .post(s"/sandbox/$saUtr/liabilities")
        .thenAssertThat()
        .statusIs(202)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"""^/self-assessment/$saUtr/liabilities/.+""".r)
    }
  }

  "retrieve liability" should {
    "return a 200 response" in {
      val expectedJson = Json.parse(
        """
          |{
          |  "taxPeriod": "2015-16-Q1",
          |  "income": {
          |    "incomes": [
          |        {"type": "self-employment-profit", "amount": 92480},
          |         {"type": "uk-interest-received", "amount": 93},
          |         {"type": "uk-dividends", "amount": 466}
          |      ],
          |     "totalIncomeReceived": 93039,
          |     "personalAllowance": 9440,
          |     "totalTaxableIncome": 83599
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
          |  "totalTaxDue": 25796.95
          |}
        """.stripMargin)

      given()
        .when()
        .get(s"/sandbox/$saUtr/liabilities/1234")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"""^/self-assessment/$saUtr/liabilities/1234""".r)
        .bodyIs(expectedJson)
    }

    "return a valid response when retrieving list of liabilities" in {
      given()
        .when()
        .get(s"/sandbox/$saUtr/liabilities")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/liabilities")
        .bodyHasPath("""_embedded \ liabilities(0) \ _links \ self \ href""", s"/self-assessment/$saUtr/liabilities/1234")
        .bodyHasPath("""_embedded \ liabilities(1) \ _links \ self \ href""", s"/self-assessment/$saUtr/liabilities/4321")
        .bodyHasPath("""_embedded \ liabilities(2) \ _links \ self \ href""", s"/self-assessment/$saUtr/liabilities/7777")
    }

  }

  "delete liability" should {
    "return a 204 response" in {
      given()
        .when()
        .delete(s"/sandbox/$saUtr/liabilities/1234")
        .thenAssertThat()
        .statusIs(204)
    }
  }

}
