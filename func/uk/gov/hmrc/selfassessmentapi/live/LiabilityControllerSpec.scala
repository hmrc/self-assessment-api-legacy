package uk.gov.hmrc.selfassessmentapi.live

import play.api.libs.json.Json.toJson
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SourceType.SelfEmployments
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.{Income, SummaryTypes}
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

      val sourceType = SelfEmployments
      val summaryType = SummaryTypes.Incomes

      val expectedJson =
        s"""
           |{
           |    "class4Nic": {
           |        "calculations": [],
           |        "total": 0
           |    },
           |    "credits": [],
           |    "income": {
           |        "incomes": {
           |            "employment": [],
           |            "selfEmployment": [
           |                {
           |                    "profit": 58529,
           |                    "taxableProfit": 48529
           |                },
           |                {
           |                    "profit": 74529,
           |                    "taxableProfit": 64529
           |                }
           |            ]
           |        },
           |        "personalAllowance": 4471,
           |        "totalIncomeReceived": 133058,
           |        "totalTaxableIncome": 113058,
           |        "totalIncomeOnWhichTaxIsDue": 108587
           |    },
           |    "incomeTax": {
           |        "calculations": [],
           |        "total": 0
           |    },
           |    "totalTaxDue": 0,
           |    "totalAllowancesAndReliefs": 0
           |}
        """.stripMargin

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/${sourceType.name}", Some(sourceType.example()))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}", Some(toJson(Income.example().copy(amount = 60000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}", Some(toJson(Income.example().copy(amount = 10000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/${sourceType.name}", Some(sourceType.example()))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}", Some(toJson(Income.example().copy(amount = 80000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}", Some(toJson(Income.example().copy(amount = 6000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/liabilities")
        .thenAssertThat()
        .statusIs(202)
        .when()
        .get(s"/$saUtr/$taxYear/liabilities/%liabilityId%")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(expectedJson)
    }
  }

  "delete liability" should {
    "return a resourceIsNotImplemented response" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .delete(s"/$saUtr/$taxYear/liabilities/1234")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "find liability" should {
    "return a resourceIsNotImplemented response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear/liabilities")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

}
