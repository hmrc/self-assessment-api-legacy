package uk.gov.hmrc.selfassessmentapi.live

import play.api.libs.json.Json.toJson
import uk.gov.hmrc.selfassessmentapi.domain.employment.SourceType.Employments
import uk.gov.hmrc.selfassessmentapi.domain.employment.UkTaxPaid
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.Income
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SourceType.SelfEmployments
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.SourceType.UnearnedIncomes
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.{Dividend, SavingsIncome}
import uk.gov.hmrc.support.BaseFunctionalSpec

class LiabilityControllerSpec extends BaseFunctionalSpec {

  "request liability" should {

    "return a 202 response with a link to retrieve the liability" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
      .when()
        .post(s"/$saUtr/$taxYear/liability")
      .thenAssertThat()
        .statusIs(202)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"""^/self-assessment/$saUtr/$taxYear/liability""".r)
    }
  }

  "retrieve liability" should {

    "return a not found response when a liability has not been requested" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear/liability")
        .thenAssertThat()
        .isNotFound
    }

    "return a 200 response with liability details" in {

      /*
          totalAllowancesAndReliefs = 2 * 10000 (lossesBroughtForward from 2 self employments) + 4471 (personalAllowance)
       */

      val expectedJson =
        s"""
           |{
           |    "income": {
           |      "incomes": {
           |        "nonSavings": {
           |          "selfEmployment": [
           |            {
           |              "profit": 58529,
           |              "taxableProfit": 48529
           |            },
           |            {
           |              "profit": 74529,
           |              "taxableProfit": 64529
           |            }
           |          ]
           |        },
           |        "savings": {
           |          "fromUKBanksAndBuildingSocieties": [
           |            {
           |              "totalInterest": 3000
           |            }
           |          ]
           |        },
           |        "dividends": {
           |          "fromUKSources": [
           |            {
           |              "totalDividend": 3000
           |            }
           |          ]
           |        },
           |        "total": 139058
           |      },
           |      "deductions": {
           |        "incomeTaxRelief": 20000,
           |        "personalAllowance": 1471,
           |        "total": 21471
           |      },
           |      "totalIncomeOnWhichTaxIsDue": 117587
           |    },
           |    "incomeTaxCalculations": {
           |      "nonSavings": [
           |        {
           |          "chargedAt": "20%",
           |          "tax": 6400,
           |          "taxBand": "basicRate",
           |          "taxableAmount": 32000
           |        },
           |        {
           |          "chargedAt": "40%",
           |          "tax": 31834,
           |          "taxBand": "higherRate",
           |          "taxableAmount": 79587
           |        },
           |        {
           |          "chargedAt": "45%",
           |          "tax": 0,
           |          "taxBand": "additionalHigherRate",
           |          "taxableAmount": 0
           |        }
           |      ],
           |      "savings": [
           |        {
           |          "chargedAt": "0%",
           |          "tax": 0,
           |          "taxBand": "nilRate",
           |          "taxableAmount": 500
           |        },
           |        {
           |          "chargedAt": "0%",
           |          "tax": 0,
           |          "taxBand": "startingRate",
           |          "taxableAmount": 0
           |        },
           |        {
           |          "chargedAt": "20%",
           |          "tax": 0,
           |          "taxBand": "basicRate",
           |          "taxableAmount": 0
           |        },
           |        {
           |          "chargedAt": "40%",
           |          "tax": 1000,
           |          "taxBand": "higherRate",
           |          "taxableAmount": 2500
           |        },
           |        {
           |          "chargedAt": "45%",
           |          "tax": 0,
           |          "taxBand": "additionalHigherRate",
           |          "taxableAmount": 0
           |        }
           |      ],
           |      "dividends": [
           |        {
           |          "chargedAt": "0%",
           |          "tax": 0,
           |          "taxBand": "nilRate",
           |          "taxableAmount": 3000
           |        },
           |        {
           |          "chargedAt": "7.5%",
           |          "tax": 0,
           |          "taxBand": "basicRate",
           |          "taxableAmount": 0
           |        },
           |        {
           |          "chargedAt": "32.5%",
           |          "tax": 0,
           |          "taxBand": "higherRate",
           |          "taxableAmount": 0
           |        },
           |        {
           |          "chargedAt": "38.1%",
           |          "tax": 0,
           |          "taxBand": "additionalHigherRate",
           |          "taxableAmount": 0
           |        }
           |      ],
           |      "total": 39234
           |    },
           |    "taxDeducted": {
           |      "interestFromUk": 600,
           |      "fromEmployments":[
           |        { "taxPaid": 3000.0 },
           |        { "taxPaid": 5000.0 }
           |      ],
           |      "total": 8600
           |    },
           |    "totalTaxDue": 30634,
           |    "totalTaxOverpaid": 0
           |}
        """.stripMargin

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments", Some(SelfEmployments.example()))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments/%sourceId%/incomes", Some(toJson(Income.example().copy(amount = 60000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments/%sourceId%/incomes", Some(toJson(Income.example().copy(amount = 10000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments", Some(SelfEmployments.example()))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments/%sourceId%/incomes", Some(toJson(Income.example().copy(amount = 80000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments/%sourceId%/incomes", Some(toJson(Income.example().copy(amount = 6000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/employments", Some(Employments.example()))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/employments/%sourceId%/uk-taxes-paid", Some(toJson(UkTaxPaid.example().copy(amount = 1000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/employments/%sourceId%/uk-taxes-paid", Some(toJson(UkTaxPaid.example().copy(amount = 2000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/employments", Some(Employments.example()))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/employments/%sourceId%/uk-taxes-paid", Some(toJson(UkTaxPaid.example().copy(amount = 2000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/employments/%sourceId%/uk-taxes-paid", Some(toJson(UkTaxPaid.example().copy(amount = 3000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/unearned-incomes", Some(UnearnedIncomes.example()))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/unearned-incomes/%sourceId%/savings", Some(toJson(SavingsIncome.example().copy(amount = 800))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/unearned-incomes/%sourceId%/savings", Some(toJson(SavingsIncome.example().copy(amount = 1600))))
        .thenAssertThat()
        .statusIs(201)
       .when()
        .post(s"/$saUtr/$taxYear/unearned-incomes/%sourceId%/dividends", Some(toJson(Dividend.example().copy(amount = 1000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/unearned-incomes/%sourceId%/dividends", Some(toJson(Dividend.example().copy(amount = 2000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/liability")
        .thenAssertThat()
        .statusIs(202)
        .when()
        .get(s"/$saUtr/$taxYear/liability")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(expectedJson)
    }

    "return an HTTP 422 Unprocessable entity response if an error occurred in the liability calculation" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/employments", Some(Employments.example()))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/employments/%sourceId%/uk-taxes-paid", Some(toJson(UkTaxPaid.example().copy(amount = -1000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/employments/%sourceId%/uk-taxes-paid", Some(toJson(UkTaxPaid.example().copy(amount = -2000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/liability")
        .thenAssertThat()
        .statusIs(202)
        .when()
        .get(s"/$saUtr/$taxYear/liability")
        .thenAssertThat()
        .statusIs(422)
    }
  }
}
