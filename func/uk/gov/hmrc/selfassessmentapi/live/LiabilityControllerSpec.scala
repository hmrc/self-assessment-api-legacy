package uk.gov.hmrc.selfassessmentapi.live

import play.api.libs.json.Json
import uk.gov.hmrc.support.BaseFunctionalSpec

class LiabilityControllerSpec extends BaseFunctionalSpec {

  "request liability" should {

    "return a 202 response with a link to retrieve the liability" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
      .when()
        .post(s"/$saUtr/$taxYear/liabilities")
      .thenAssertThat()
        .statusIs(202)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"""^/self-assessment/$saUtr/$taxYear/liabilities/.+""".r)
    }
  }

  "retrieve liability" should {

    "return a 200 response with liability details" in {
      val expectedJson = Json.parse(
        s"""
           |{
           |  "income": {
           |    "incomes": [],
           |     "totalIncomeReceived": 0,
           |     "personalAllowance": 0,
           |     "totalTaxableIncome": 0
           |  },
           |  "incomeTax": {
           |    "calculations": [],
           |    "total": 0
           |  },
           |  "credits": [],
           |  "class4Nic": {
           |      "calculations": [],
           |      "total": 0
           |  },
           |  "totalTaxDue": 0
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
      .when()
        .post(s"/$saUtr/$taxYear/liabilities")
      .thenAssertThat()
        .statusIs(202)
        .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/liabilities/.+".r)
      .when()
        .get(s"/$saUtr/$taxYear/liabilities/%liabilityId%")
      .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyIs(expectedJson)
    }
  }

  "delete liability" should {
    "return a resourceIsNotImplemented response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .delete(s"/$saUtr/$taxYear/liabilities/1234")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

}
