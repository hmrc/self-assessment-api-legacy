/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.models.{Period, PeriodId}
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentPeriodResourceSpec extends BaseFunctionalSpec {

  "createPeriod" should {

    "return code 201 containing a location header containing from date and to date when creating a period" in {
      val period = Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"))

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .selfEmployment
        .willBeCreatedFor(nino)
        .des()
        .selfEmployment
        .periodWillBeCreatedFor(nino)
        .when()
        .post(Jsons.SelfEmployment())
        .to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period)
        .to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader("Location",
          s"/self-assessment/ni/$nino/self-employments/\\w+/periods/2017-04-06_2017-07-04".r)
        .when()
        .get("/admin/metrics")
        .thenAssertThat()
        .body(_ \ "timers" \ "Timer-API-SelfEmployments-POST" \ "count")
        .is(1)
        .body(_ \ "timers" \ "Timer-API-SelfEmployments-periods-POST" \ "count")
        .is(1)
    }

    "return code 201 containing a location header containing from date and to date when creating a period with consolidated expenses" in {

      val period = Jsons.SelfEmployment.periodWithSimplifiedExpenses(fromDate = Some("2017-04-06"),
        toDate = Some("2017-07-04"),
        consolidatedExpenses = Some(1234))

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .selfEmployment
        .willBeCreatedFor(nino)
        .des()
        .selfEmployment
        .periodWillBeCreatedFor(nino)
        .when()
        .post(Jsons.SelfEmployment())
        .to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period)
        .to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader("Location",
          s"/self-assessment/ni/$nino/self-employments/\\w+/periods/2017-04-06_2017-07-04".r)
    }

    "return code 400 when attempting to create a period with the 'from' and 'to' dates are in the incorrect order" in {

      val period = Jsons.SelfEmployment.period(fromDate = Some("2017-04-01"), toDate = Some("2017-03-31"))

      val expectedBody = Jsons.Errors.invalidRequest(("INVALID_PERIOD", ""))

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .selfEmployment
        .willBeCreatedFor(nino)
        .when()
        .post(Jsons.SelfEmployment())
        .to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period)
        .to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
    }

    "return code 400 when attempting to create a period where the paylod contains both the 'expenses' and 'consolidatedExpenses'" in {

      val period = Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"), consolidatedExpenses = Some(12345))

      val expectedBody = Jsons.Errors.invalidRequest(("BOTH_EXPENSES_SUPPLIED", ""))

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .selfEmployment
        .willBeCreatedFor(nino)
        .when()
        .post(Jsons.SelfEmployment())
        .to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period)
        .to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
    }

    "return code 400 when attempting to create a period with no incomes and expenses" in {
      val period =
        s"""
           |{
           |  "from": "2017-03-31",
           |  "to": "2017-04-01"
           |}
         """.stripMargin

      val expectedBody =
        Jsons.Errors.invalidRequest(("NO_INCOMES_AND_EXPENSES", ""))

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .selfEmployment
        .willBeCreatedFor(nino)
        .when()
        .post(Jsons.SelfEmployment())
        .to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(Json.parse(period))
        .to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
    }

    "return code 400 with multiple validation errors when attempting to create a period with invalid from and to dates and no incomes and expenses" in {
      val period =
        s"""
           |{
           |  "from": "2017-04-01",
           |  "to": "2017-03-31"
           |}
         """.stripMargin

      val expectedBody =
        Jsons.Errors.invalidRequest(("INVALID_PERIOD", ""), ("NO_INCOMES_AND_EXPENSES", ""))

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .selfEmployment
        .willBeCreatedFor(nino)
        .when()
        .post(Jsons.SelfEmployment())
        .to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(Json.parse(period))
        .to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
    }

    "return code 403 when attempting to create a period that is not contiguous with existing periods" in {
      val nonContiguousPeriod = Jsons.SelfEmployment.period(fromDate = Some("2017-08-04"), toDate = Some("2017-09-04"))

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .selfEmployment
        .willBeCreatedFor(nino)
        .des()
        .selfEmployment
        .nonContiguousPeriodFor(nino)
        .when()
        .post(Jsons.SelfEmployment())
        .to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(nonContiguousPeriod)
        .to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsLike(Jsons.Errors.nonContiguousPeriod)
    }

    "return code 403 when attempting to create a period whose date range overlaps with another period" in {
      val overlappingPeriod = Jsons.SelfEmployment.period(fromDate = Some("2017-08-04"), toDate = Some("2017-09-04"))

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .selfEmployment
        .willBeCreatedFor(nino)
        .des()
        .selfEmployment
        .overlappingPeriodFor(nino)
        .when()
        .post(Jsons.SelfEmployment())
        .to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(overlappingPeriod)
        .to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsLike(Jsons.Errors.overlappingPeriod)
    }

    "return code 403 when attempting to create a period that is misaligned with the accounting period" in {
      val misalignedPeriod = Jsons.SelfEmployment.period(fromDate = Some("2017-08-04"), toDate = Some("2017-09-04"))

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .selfEmployment
        .willBeCreatedFor(nino)
        .des()
        .selfEmployment
        .misalignedPeriodFor(nino)
        .when()
        .post(Jsons.SelfEmployment())
        .to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(misalignedPeriod)
        .to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsLike(Jsons.Errors.misalignedPeriod)
    }

    "return code 404 when attempting to create a period for a self-employment that does not exist" in {
      val period = Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"))

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .selfEmployment
        .periodWillBeNotBeCreatedFor(nino)
        .when()
        .post(period)
        .to(s"/ni/$nino/self-employments/abc/periods")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 500 when DES is experiencing issues" in {
      val period = Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"))

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .serverErrorFor(nino)
        .when()
        .post(period)
        .to(s"/ni/$nino/self-employments/abc/periods")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "return code 500 when dependent systems are not available" in {
      val period = Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"))

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .serviceUnavailableFor(nino)
        .when()
        .post(period)
        .to(s"/ni/$nino/self-employments/abc/periods")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "return code 500 when we receive a status code from DES that we do not handle" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .isATeapotFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/abc/periods")
        .thenAssertThat()
        .statusIs(500)
    }
  }

  "updatePeriod" should {
    "return code 204 when updating a period that exists" in {
      val updatePeriod = Jsons.SelfEmployment.period(turnover = 200.25,
        otherIncome = 100.25,
        costOfGoodsBought = (200.25, 50.25),
        cisPaymentsToSubcontractors = (100.25, 55.25))

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .selfEmployment
        .periodWillBeUpdatedFor(nino, from = "2017-04-05", to = "2018-04-04")
        .when()
        .put(updatePeriod)
        .at(s"/ni/$nino/self-employments/abc/periods/2017-04-05_2018-04-04")
        .thenAssertThat()
        .statusIs(204)
    }

    "return code 400 when attempting to amend a period where the payload contains both the 'expenses' and 'consolidatedExpenses'" in {

      val updatePeriod = Jsons.SelfEmployment.period( turnover = 200.25,
                                                      otherIncome = 100.25,
                                                      costOfGoodsBought = (200.25, 50.25),
                                                      cisPaymentsToSubcontractors = (100.25, 55.25),
                                                      consolidatedExpenses = Some(12345))

      val expectedBody = Jsons.Errors.invalidRequest(("BOTH_EXPENSES_SUPPLIED", ""))

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .selfEmployment
        .periodWillBeUpdatedFor(nino, from = "2017-04-05", to = "2018-04-04")
//        .when()
//        .post(Jsons.SelfEmployment())
//        .to(s"/ni/$nino/self-employments")
//        .thenAssertThat()
//        .statusIs(201)
        .when()
        .put(updatePeriod)
        .at(s"/ni/$nino/self-employments/abc/periods/2017-04-05_2018-04-04")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
    }

    "return code 404 when attempting to update a non-existent period" in {
      val period = Jsons.SelfEmployment.period(
        fromDate = Some("2017-04-06"),
        toDate = Some("2017-07-04"),
        turnover = 100.25,
        otherIncome = 100.25,
        costOfGoodsBought = (100.25, 50.25),
        cisPaymentsToSubcontractors = (100.25, 50.25)
      )

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .selfEmployment
        .periodWillNotBeUpdatedFor(nino, from = "2017-04-05", to = "2018-04-04")
        .when()
        .put(Json.toJson(period))
        .at(s"/ni/$nino/self-employments/abc/periods/2017-04-05_2018-04-04")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 400 when attempting to update a period with no incomes and expenses" in {
      val period =
        s"""
           |{
           |  "incomes": {},
           |  "expenses": {}
           |}
         """.stripMargin

      val expectedBody =
        Jsons.Errors.invalidRequest(("NO_INCOMES_AND_EXPENSES", ""))

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .put(Json.parse(period))
        .at(s"/ni/$nino/self-employments/abc/periods/2017-04-05_2018-04-04")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
    }

    "return code 500 when we receive a status code from DES that we do not handle" in {
      val period = Jsons.SelfEmployment.period(
        fromDate = Some("2017-04-06"),
        toDate = Some("2017-07-04"),
        turnover = 100.25,
        otherIncome = 100.25,
        costOfGoodsBought = (100.25, 50.25),
        cisPaymentsToSubcontractors = (100.25, 50.25)
      )

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .isATeapotFor(nino)
        .when()
        .put(Json.toJson(period))
        .at(s"/ni/$nino/self-employments/abc/periods/2017-04-05_2018-04-04")
        .thenAssertThat()
        .statusIs(500)
    }
  }

  "retrievePeriod" should {
    "return code 200 when retrieving a period that exists" in {
      val expectedBody = Jsons.SelfEmployment.period(
        fromDate = Some("2017-04-05"),
        toDate = Some("2018-04-04"),
        turnover = 200,
        otherIncome = 200,
        costOfGoodsBought = (200, 200),
        cisPaymentsToSubcontractors = (200, 200),
        staffCosts = (200, 200),
        travelCosts = (200, 200),
        premisesRunningCosts = (200, 200),
        maintenanceCosts = (200, 200),
        adminCosts = (200, 200),
        advertisingCosts = (200, 200),
        interest = (200, 200),
        financialCharges = (200, 200),
        badDebt = (200, 200),
        professionalFees = (200, 200),
        depreciation = (200, 200),
        otherExpenses = (200, 200),
        businessEntertainmentCosts = (200, 200)
      )


      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .selfEmployment
        .periodWillBeReturnedFor(nino, from = "2017-04-05", to = "2018-04-04")
        .when()
        .get(s"/ni/$nino/self-employments/abc/periods/2017-04-05_2018-04-04")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody.toString)
        .bodyDoesNotHavePath[PeriodId]("id")
    }

    "return code 400 when retrieving a period and DES fails nino validation" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .selfEmployment
        .periodWillBeReturnedFor(nino, from = "2017-04-05", to = "2018-04-04")
        .des()
        .invalidNinoFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/abc/periods/2017-04-05_2018-04-04")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(Jsons.Errors.ninoInvalid)
    }

    "return code 404 when retrieving a period and DES fails BusinessID validation" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .invalidBusinessIdFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/abc/periods/def")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 404 when retrieving a period that does not exist" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .selfEmployment
        .noPeriodFor(nino, from = "2017-04-06", to = "2018-04-05")
        .when()
        .get(s"/ni/$nino/self-employments/abc/periods/2017-04-06_2018-04-05")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 404 when retrieving a period that has got invalid dates in the periodId" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .selfEmployment
        .periodWillBeReturnedFor(nino, from = "2017-05-04", to = "2018-06-31")
        .when()
        .get(s"/ni/$nino/self-employments/abc/periods/2017-05-04_2017-06-31")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 500 when we receive a status code of INVALID_DATE_FROM from DES" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .selfEmployment
        .invalidDateFrom(nino, from = "2017-04-06", to = "2018-04-05")
        .when()
        .get(s"/ni/$nino/self-employments/abc/periods/2017-04-06_2018-04-05")
        .thenAssertThat()
        .statusIs(500)
    }

    "return code 500 when we receive a status code of INVALID_DATE_TO from DES" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .selfEmployment
        .invalidDateTo(nino, from = "2017-04-06", to = "2018-04-05")
        .when()
        .get(s"/ni/$nino/self-employments/abc/periods/2017-04-06_2018-04-05")
        .thenAssertThat()
        .statusIs(500)
    }

    "return code 500 when we receive a status code from DES that we do not handle" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .isATeapotFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/abc/periods/2017-04-05_2018-04-04")
        .thenAssertThat()
        .statusIs(500)
    }
  }

  "retrieveAllPeriods" should {
    "return code 200 when retrieving all periods where periods.size > 0, sorted by from date" in {
      val expectedBody =
        s"""
           |[
           |  {
           |    "id": "2017-07-05_2017-08-04",
           |    "from": "2017-07-05",
           |    "to": "2017-08-04"
           |  },
           |  {
           |    "id": "2017-04-06_2017-07-04",
           |    "from": "2017-04-06",
           |    "to": "2017-07-04"
           |  }
           |]
         """.stripMargin

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .selfEmployment
        .periodsWillBeReturnedFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/abc/periods")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
        .selectFields(_ \\ "id")
        .isLength(2)
        .matches(Period.periodPattern)
    }

    "return code 200 containing an empty json body when retrieving all periods where periods.size == 0" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .selfEmployment
        .noPeriodsFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/abc/periods")
        .thenAssertThat()
        .statusIs(200)
        .jsonBodyIsEmptyArray()
    }

    "return code 400 when retrieving all periods and DES fails nino validation" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .invalidNinoFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/abc/periods")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(Jsons.Errors.ninoInvalid)
    }

    "return code 404 when retrieving all periods and DES fails BusinessID validation" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .invalidBusinessIdFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/abc/periods")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 404 when retrieving all periods for a non-existent self-employment source" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .selfEmployment
        .doesNotExistPeriodFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/abc/periods")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 500 when we receive an unexpected JSON from DES" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .selfEmployment
        .invalidPeriodsJsonFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/abc/periods")
        .thenAssertThat()
        .statusIs(500)
    }

    "return code 500 when we receive a status code from DES that we do not handle" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .isATeapotFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/abc/periods")
        .thenAssertThat()
        .statusIs(500)
    }
  }

}
