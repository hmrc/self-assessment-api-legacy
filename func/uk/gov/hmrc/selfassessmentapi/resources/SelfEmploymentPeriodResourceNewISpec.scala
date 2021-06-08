/*
 * Copyright 2021 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status.{OK, CREATED, BAD_REQUEST, FORBIDDEN, CONFLICT, NOT_FOUND, INTERNAL_SERVER_ERROR}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSRequest
import uk.gov.hmrc.selfassessmentapi.NinoGenerator
import uk.gov.hmrc.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}
import uk.gov.hmrc.support.IntegrationBaseSpec
import uk.gov.hmrc.utils.Nino

class SelfEmploymentPeriodResourceNewISpec extends IntegrationBaseSpec {

  private trait Test {

    protected val nino: Nino = NinoGenerator().nextNino()
    val periodKey: String = "A1A2"
    val correlationId: String = "X-ID"
    val id: String = "abc"

    def uri: String = s"/ni/${nino.nino}/self-employments/$id/periods"

    def desUrl: String = s"/income-store/nino/${nino.nino}/self-employments/$id/periodic-summaries"

    def desResponse: JsValue = Json.parse(DesJsons.SelfEmployment.Period.createResponse())

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "createPeriod" should {
    s"return status code 201 containing a location header" when {
      "a valid request is made" in new Test {

        val requestJson: JsValue = Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"))
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.POST, desUrl, OK, desResponse)
        }

        private val response = await(request.post(requestJson))
        response.status shouldBe CREATED
        response.header("Location") shouldBe Some(s"/self-assessment/ni/${nino.nino}/self-employments/abc/periods/2017-04-06_2017-07-04")

      }

      "creating a period with bad debts expenses" in new Test {

        val requestJson: JsValue = Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"), badDebt = (-10.10, -10.10))

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.POST, desUrl, OK, desResponse)
        }

        private val response = await(request.post(requestJson))
        response.status shouldBe CREATED
        response.header("Location") shouldBe Some(s"/self-assessment/ni/${nino.nino}/self-employments/abc/periods/2017-04-06_2017-07-04")

      }

      "creating a period with consolidated expenses" in new Test {

        val requestJson: JsValue = Jsons.SelfEmployment.periodWithSimplifiedExpenses(fromDate = Some("2017-04-06"),
               toDate = Some("2017-07-04"), consolidatedExpenses = Some(1234))

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.POST, desUrl, OK, desResponse)
        }

        private val response = await(request.post(requestJson))
        response.status shouldBe CREATED
        response.header("Location") shouldBe Some(s"/self-assessment/ni/${nino.nino}/self-employments/abc/periods/2017-04-06_2017-07-04")

      }
    }

    s"return status code 400" when {
      "the 'from' and 'to' dates are in the incorrect order" in new Test {

        val requestJson: JsValue = Jsons.SelfEmployment.period(fromDate = Some("2017-04-01"), toDate = Some("2017-03-31"))
        val expectedJson: JsValue = Json.parse(
          """
            |{
            |  "code":"INVALID_REQUEST",
            |  "message":"Invalid request",
            |  "errors":[
            |     {
            |        "code":"INVALID_PERIOD",
            |        "message":"The period 'to' date is before the period 'from' date or the submission period already exists.","path":""
            |      }
            |   ]
            |}
            |""".stripMargin)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        private val response = await(request.post(requestJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe expectedJson
      }

      "the request payload contains both the 'expenses' and 'consolidatedExpenses" in new Test {

        val requestJson: JsValue = Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"), consolidatedExpenses = Some(12345))
        val expectedJson: JsValue = Json.parse(
          """
            |{
            |	"code": "INVALID_REQUEST",
            |	"message": "Invalid request",
            |	"errors": [{
            |		"code": "BOTH_EXPENSES_SUPPLIED",
            |		"message": "Both expenses and consolidatedExpenses elements cannot be present at the same time",
            |		"path": ""
            |	}]
            |}
            |""".stripMargin)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        private val response = await(request.post(requestJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe expectedJson
      }

      "the request payload has no incomes and expenses" in new Test {

        val requestJson: JsValue = Json.parse(
          s"""
             |{
             |  "from": "2017-03-31",
             |  "to": "2017-04-01"
             |}
         """.stripMargin)
        val expectedJson: JsValue = Json.parse(
          """
            |{
            |	"code": "INVALID_REQUEST",
            |	"message": "Invalid request",
            |	"errors": [{
            |		"code": "NO_INCOMES_AND_EXPENSES",
            |		"message": "No incomes and expenses are supplied",
            |		"path": ""
            |	}]
            |}
            |""".stripMargin)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        private val response = await(request.post(requestJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe expectedJson
      }
    }

    s"return status code 400 with multiple validation errors" when {
      "the request payload has no incomes and expenses and with invalid from and to dates" in new Test {

        val requestJson: JsValue = Json.parse(s"""
                                                 |{
                                                 |  "to": "2017-03-31",
                                                 |  "from": "2017-04-01"
                                                 |}""".stripMargin)

        val expectedJson: JsValue = Json.parse(
          """
            |{
            |	"code": "INVALID_REQUEST",
            |	"message": "Invalid request",
            |	"errors": [{
            |		"code": "INVALID_PERIOD",
            |		"message": "The period 'to' date is before the period 'from' date or the submission period already exists.",
            |		"path": ""
            |	}, {
            |		"code": "NO_INCOMES_AND_EXPENSES",
            |		"message": "No incomes and expenses are supplied",
            |		"path": ""
            |	}]
            |}
            |""".stripMargin)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        private val response = await(request.post(requestJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe expectedJson
      }
    }

    s"return status code 403" when {
      "the request payload period is not contiguous with existing periods" in new Test {

        val nonContiguousPeriod: JsValue = Jsons.SelfEmployment.period(fromDate = Some("2017-08-04"), toDate = Some("2017-09-04"))

        val expectedJson: JsValue = Json.parse(
          """
            |{
            |	"code": "BUSINESS_ERROR",
            |	"message": "Business validation error",
            |	"errors": [{
            |		"code": "NOT_CONTIGUOUS_PERIOD",
            |		"message": "Periods should be contiguous.",
            |		"path": ""
            |	}]
            |}
            |""".stripMargin)

        override def desResponse: JsValue = Json.parse(DesJsons.Errors.nonContiguousPeriod)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.POST, desUrl, CONFLICT, Json.parse(DesJsons.Errors.nonContiguousPeriod))
        }

        private val response = await(request.post(nonContiguousPeriod))
        response.status shouldBe FORBIDDEN
        response.json shouldBe expectedJson
      }

      "the request payload period date range overlaps with another period" in new Test {

        val overlappingPeriod: JsValue = Jsons.SelfEmployment.period(fromDate = Some("2017-08-04"), toDate = Some("2017-09-04"))

        val expectedJson: JsValue = Json.parse(
          """
            |{
            |	"code": "BUSINESS_ERROR",
            |	"message": "Business validation error",
            |	"errors": [{
            |		"code": "OVERLAPPING_PERIOD",
            |		"message": "Period overlaps with existing periods.",
            |		"path": ""
            |	}]
            |}
            |""".stripMargin)

        override def desResponse: JsValue = Json.parse(DesJsons.Errors.nonContiguousPeriod)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.POST, desUrl, CONFLICT, Json.parse(DesJsons.Errors.overlappingPeriod))
        }

        private val response = await(request.post(overlappingPeriod))
        response.status shouldBe FORBIDDEN
        response.json shouldBe expectedJson
      }

      "the request payload period is misaligned with the accounting period" in new Test {

        val misalignedPeriod: JsValue = Jsons.SelfEmployment.period(fromDate = Some("2017-08-04"), toDate = Some("2017-09-04"))

        val expectedJson: JsValue = Json.parse(
          """
            |{
            |	"code": "BUSINESS_ERROR",
            |	"message": "Business validation error",
            |	"errors": [{
            |		"code": "MISALIGNED_PERIOD",
            |		"message": "Period is not within the accounting period.",
            |		"path": ""
            |	}]
            |}
            |""".stripMargin)

        override def desResponse: JsValue = Json.parse(DesJsons.Errors.nonContiguousPeriod)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.POST, desUrl, CONFLICT, Json.parse(DesJsons.Errors.misalignedPeriod))
        }

        private val response = await(request.post(misalignedPeriod))
        response.status shouldBe FORBIDDEN
        response.json shouldBe expectedJson
      }
    }

    s"return status code 404" when {
      "attempting to create a period for a self-employment that does not exist" in new Test {

        val period: JsValue = Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"))

        override def desResponse: JsValue = Json.parse(DesJsons.Errors.nonContiguousPeriod)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.POST, desUrl, NOT_FOUND, Json.parse(DesJsons.Errors.notFound))
        }

        private val response = await(request.post(period))
        response.status shouldBe NOT_FOUND
      }
    }

    s"return status code 500" when {
      "DES is experiencing issues" in new Test {

        val period: JsValue = Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"))

        override def desResponse: JsValue = Json.parse(DesJsons.Errors.serverError)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.POST, desUrl, INTERNAL_SERVER_ERROR, Json.parse(DesJsons.Errors.notFound))
        }

        private val response = await(request.post(period))
        response.status shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}

//  "createPeriod" should {
//
//    "return code 500 when DES is experiencing issues" in {
//      val period = Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"))
//
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des()
//        .serverErrorFor(nino)
//        .when()
//        .post(period)
//        .to(s"/ni/${nino.nino}/self-employments/abc/periods")
//        .thenAssertThat()
//        .statusIs(500)
//        .bodyIsLike(Jsons.Errors.internalServerError)
//    }
//
//    "return code 500 when dependent systems are not available" in {
//      val period = Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"))
//
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des()
//        .serviceUnavailableFor(nino)
//        .when()
//        .post(period)
//        .to(s"/ni/${nino.nino}/self-employments/abc/periods")
//        .thenAssertThat()
//        .statusIs(500)
//        .bodyIsLike(Jsons.Errors.internalServerError)
//    }
//
//    "return code 500 when we receive a status code from DES that we do not handle" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des()
//        .isATeapotFor(nino)
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments/abc/periods")
//        .thenAssertThat()
//        .statusIs(500)
//    }
//  }
//
//  "updatePeriod" should {
//    "return code 204 when updating a period that exists" in {
//      val updatePeriod = Jsons.SelfEmployment.period(turnover = 200.25,
//        otherIncome = 100.25,
//        costOfGoodsBought = (200.25, 50.25),
//        cisPaymentsToSubcontractors = (100.25, 55.25))
//
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des()
//        .selfEmployment
//        .periodWillBeUpdatedFor(nino, from = "2017-04-05", to = "2018-04-04")
//        .when()
//        .put(updatePeriod)
//        .at(s"/ni/${nino.nino}/self-employments/abc/periods/2017-04-05_2018-04-04")
//        .thenAssertThat()
//        .statusIs(204)
//    }
//
//    "return code 204 when updating a period that exists with bad debts" in {
//      val updatePeriod = Jsons.SelfEmployment.period(turnover = 200.25,
//        otherIncome = 100.25,
//        costOfGoodsBought = (200.25, 50.25),
//        cisPaymentsToSubcontractors = (100.25, 55.25),
//        badDebt = (-10.10, -10.10)
//      )
//
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des()
//        .selfEmployment
//        .periodWillBeUpdatedFor(nino, from = "2017-04-05", to = "2018-04-04")
//        .when()
//        .put(updatePeriod)
//        .at(s"/ni/${nino.nino}/self-employments/abc/periods/2017-04-05_2018-04-04")
//        .thenAssertThat()
//        .statusIs(204)
//    }
//
//    "return code 400 when attempting to amend a period where the payload contains both the 'expenses' and 'consolidatedExpenses'" in {
//
//      val updatePeriod = Jsons.SelfEmployment.period(turnover = 200.25,
//        otherIncome = 100.25,
//        costOfGoodsBought = (200.25, 50.25),
//        cisPaymentsToSubcontractors = (100.25, 55.25),
//        consolidatedExpenses = Some(12345))
//
//      val expectedBody = Jsons.Errors.invalidRequest(("BOTH_EXPENSES_SUPPLIED", ""))
//
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des()
//        .selfEmployment
//        .periodWillBeUpdatedFor(nino, from = "2017-04-05", to = "2018-04-04")
//        .when()
//        .put(updatePeriod)
//        .at(s"/ni/${nino.nino}/self-employments/abc/periods/2017-04-05_2018-04-04")
//        .thenAssertThat()
//        .statusIs(400)
//        .contentTypeIsJson()
//        .bodyIsLike(expectedBody)
//    }
//
//    "return code 404 when attempting to update a non-existent period" in {
//      val period = Jsons.SelfEmployment.period(
//        fromDate = Some("2017-04-06"),
//        toDate = Some("2017-07-04"),
//        turnover = 100.25,
//        otherIncome = 100.25,
//        costOfGoodsBought = (100.25, 50.25),
//        cisPaymentsToSubcontractors = (100.25, 50.25)
//      )
//
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des()
//        .selfEmployment
//        .periodWillNotBeUpdatedFor(nino, from = "2017-04-05", to = "2018-04-04")
//        .when()
//        .put(Json.toJson(period))
//        .at(s"/ni/${nino.nino}/self-employments/abc/periods/2017-04-05_2018-04-04")
//        .thenAssertThat()
//        .statusIs(404)
//    }
//
//    "return code 400 when attempting to update a period with no incomes and expenses" in {
//      val period =
//        s"""
//           |{
//           |  "incomes": {},
//           |  "expenses": {}
//           |}
//         """.stripMargin
//
//      val expectedBody =
//        Jsons.Errors.invalidRequest(("NO_INCOMES_AND_EXPENSES", ""))
//
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .when()
//        .put(Json.parse(period))
//        .at(s"/ni/${nino.nino}/self-employments/abc/periods/2017-04-05_2018-04-04")
//        .thenAssertThat()
//        .statusIs(400)
//        .contentTypeIsJson()
//        .bodyIsLike(expectedBody)
//    }
//
//    "return code 500 when we receive a status code from DES that we do not handle" in {
//      val period = Jsons.SelfEmployment.period(
//        fromDate = Some("2017-04-06"),
//        toDate = Some("2017-07-04"),
//        turnover = 100.25,
//        otherIncome = 100.25,
//        costOfGoodsBought = (100.25, 50.25),
//        cisPaymentsToSubcontractors = (100.25, 50.25)
//      )
//
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des()
//        .isATeapotFor(nino)
//        .when()
//        .put(Json.toJson(period))
//        .at(s"/ni/${nino.nino}/self-employments/abc/periods/2017-04-05_2018-04-04")
//        .thenAssertThat()
//        .statusIs(500)
//    }
//  }
//
//  "retrievePeriod" should {
//    "return code 200 when retrieving a period that exists" in {
//      val expectedBody = Jsons.SelfEmployment.period(
//        fromDate = Some("2017-04-05"),
//        toDate = Some("2018-04-04"),
//        turnover = 200,
//        otherIncome = 200,
//        costOfGoodsBought = (200, 200),
//        cisPaymentsToSubcontractors = (200, 200),
//        staffCosts = (200, 200),
//        travelCosts = (200, 200),
//        premisesRunningCosts = (200, 200),
//        maintenanceCosts = (200, 200),
//        adminCosts = (200, 200),
//        advertisingCosts = (200, 200),
//        interest = (200, 200),
//        financialCharges = (200, 200),
//        badDebt = (200, 200),
//        professionalFees = (200, 200),
//        depreciation = (200, 200),
//        otherExpenses = (200, 200),
//        businessEntertainmentCosts = (200, 200)
//      )
//
//
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des()
//        .selfEmployment
//        .periodWillBeReturnedFor(nino, from = "2017-04-05", to = "2018-04-04")
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments/abc/periods/2017-04-05_2018-04-04")
//        .thenAssertThat()
//        .statusIs(200)
//        .contentTypeIsJson()
//        .bodyIsLike(expectedBody.toString)
//        .bodyDoesNotHavePath[PeriodId]("id")
//    }
//
//    "return code 200 when retrieving a period that exists with negative bad debts" in {
//      val expectedBody = Jsons.SelfEmployment.period(
//        fromDate = Some("2017-04-05"),
//        toDate = Some("2018-04-04"),
//        turnover = 200,
//        otherIncome = 200,
//        costOfGoodsBought = (200, 200),
//        cisPaymentsToSubcontractors = (200, 200),
//        staffCosts = (200, 200),
//        travelCosts = (200, 200),
//        premisesRunningCosts = (200, 200),
//        maintenanceCosts = (200, 200),
//        adminCosts = (200, 200),
//        advertisingCosts = (200, 200),
//        interest = (200, 200),
//        financialCharges = (200, 200),
//        badDebt = (-200, -200),
//        professionalFees = (200, 200),
//        depreciation = (200, 200),
//        otherExpenses = (200, 200),
//        businessEntertainmentCosts = (200, 200)
//      )
//
//
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des()
//        .selfEmployment
//        .periodWithNegativeBadDebtsWillBeReturnedFor(nino, from = "2017-04-05", to = "2018-04-04")
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments/abc/periods/2017-04-05_2018-04-04")
//        .thenAssertThat()
//        .statusIs(200)
//        .contentTypeIsJson()
//        .bodyIsLike(expectedBody.toString)
//        .bodyDoesNotHavePath[PeriodId]("id")
//    }
//
//    "return code 400 when retrieving a period and DES fails nino validation" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des()
//        .selfEmployment
//        .periodWillBeReturnedFor(nino, from = "2017-04-05", to = "2018-04-04")
//        .des()
//        .invalidNinoFor(nino)
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments/abc/periods/2017-04-05_2018-04-04")
//        .thenAssertThat()
//        .statusIs(400)
//        .bodyIsLike(Jsons.Errors.ninoInvalid)
//    }
//
//    "return code 404 when retrieving a period and DES fails BusinessID validation" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des()
//        .invalidBusinessIdFor(nino)
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments/abc/periods/def")
//        .thenAssertThat()
//        .statusIs(404)
//    }
//
//    "return code 404 when retrieving a period that does not exist" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des()
//        .selfEmployment
//        .noPeriodFor(nino, from = "2017-04-06", to = "2018-04-05")
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments/abc/periods/2017-04-06_2018-04-05")
//        .thenAssertThat()
//        .statusIs(404)
//    }
//
//    "return code 404 when retrieving a period that has got invalid dates in the periodId" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des()
//        .selfEmployment
//        .periodWillBeReturnedFor(nino, from = "2017-05-04", to = "2018-06-31")
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments/abc/periods/2017-05-04_2017-06-31")
//        .thenAssertThat()
//        .statusIs(404)
//    }
//
//    "return code 500 when we receive a status code of INVALID_DATE_FROM from DES" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des()
//        .selfEmployment
//        .invalidDateFrom(nino, from = "2017-04-06", to = "2018-04-05")
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments/abc/periods/2017-04-06_2018-04-05")
//        .thenAssertThat()
//        .statusIs(500)
//    }
//
//    "return code 500 when we receive a status code of INVALID_DATE_TO from DES" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des()
//        .selfEmployment
//        .invalidDateTo(nino, from = "2017-04-06", to = "2018-04-05")
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments/abc/periods/2017-04-06_2018-04-05")
//        .thenAssertThat()
//        .statusIs(500)
//    }
//
//    "return code 500 when we receive a status code from DES that we do not handle" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des()
//        .isATeapotFor(nino)
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments/abc/periods/2017-04-05_2018-04-04")
//        .thenAssertThat()
//        .statusIs(500)
//    }
//  }
//
//  "retrieveAllPeriods" should {
//    "return code 200 when retrieving all periods where periods.size > 0, sorted by from date" in {
//      val expectedBody =
//        s"""
//           |[
//           |  {
//           |    "id": "2017-07-05_2017-08-04",
//           |    "from": "2017-07-05",
//           |    "to": "2017-08-04"
//           |  },
//           |  {
//           |    "id": "2017-04-06_2017-07-04",
//           |    "from": "2017-04-06",
//           |    "to": "2017-07-04"
//           |  }
//           |]
//         """.stripMargin
//
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des()
//        .selfEmployment
//        .periodsWillBeReturnedFor(nino)
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments/abc/periods")
//        .thenAssertThat()
//        .statusIs(200)
//        .contentTypeIsJson()
//        .bodyIsLike(expectedBody)
//        .selectFields(_ \\ "id")
//        .isLength(2)
//        .matches(Period.periodPattern)
//    }
//
//    "return code 200 containing an empty json body when retrieving all periods where periods.size == 0" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des()
//        .selfEmployment
//        .noPeriodsFor(nino)
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments/abc/periods")
//        .thenAssertThat()
//        .statusIs(200)
//        .jsonBodyIsEmptyArray()
//    }
//
//    "return code 400 when retrieving all periods and DES fails nino validation" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des()
//        .invalidNinoFor(nino)
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments/abc/periods")
//        .thenAssertThat()
//        .statusIs(400)
//        .bodyIsLike(Jsons.Errors.ninoInvalid)
//    }
//
//    "return code 404 when retrieving all periods and DES fails BusinessID validation" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des()
//        .invalidBusinessIdFor(nino)
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments/abc/periods")
//        .thenAssertThat()
//        .statusIs(404)
//    }
//
//    "return code 404 when retrieving all periods for a non-existent self-employment source" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des()
//        .selfEmployment
//        .doesNotExistPeriodFor(nino)
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments/abc/periods")
//        .thenAssertThat()
//        .statusIs(404)
//    }
//
//    "return code 500 when we receive an unexpected JSON from DES" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des()
//        .selfEmployment
//        .invalidPeriodsJsonFor(nino)
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments/abc/periods")
//        .thenAssertThat()
//        .statusIs(500)
//    }
//
//    "return code 500 when we receive a status code from DES that we do not handle" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des()
//        .isATeapotFor(nino)
//        .when()
//        .get(s"/ni/${nino.nino}/self-employments/abc/periods")
//        .thenAssertThat()
//        .statusIs(500)
//    }
//  }
//
//}
