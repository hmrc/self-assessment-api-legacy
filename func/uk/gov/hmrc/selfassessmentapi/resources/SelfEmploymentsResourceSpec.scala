package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.resources.models.{PeriodId, SourceId}
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentsResourceSpec extends BaseFunctionalSpec {

  "create" should {
    "return code 201 when creating a valid a self-employment source of income" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.selfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader("Location", s"/self-assessment/ni/$nino/self-employments/\\w+".r)
    }

    "return code 400 (INVALID_REQUEST) when attempting to create a self-employment with an invalid dates in the accountingPeriod" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.selfEmployment(accPeriodStart = "01-01-2017", accPeriodEnd = "02-01-2017")).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.invalidRequest(("INVALID_DATE", "/accountingPeriod/start"), ("INVALID_DATE", "/accountingPeriod/end")))
    }

    "return code 400 (INVALID_VALUE) when attempting to create a self-employment with an invalid accounting type" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.selfEmployment(accountingType = "INVALID_ACC_TYPE")).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.invalidRequest(("INVALID_VALUE", "/accountingType")))
    }
  }

  "update" should {
    "return code 204 when successfully updating a self-employment resource" in {
      val updatedSelfEmployment = Jsons.selfEmployment(accPeriodStart = "2017-04-01", accPeriodEnd = "2017-06-01",
        accountingType = "ACCRUAL", commencementDate = "2016-01-01")

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.selfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(updatedSelfEmployment).at("%sourceLocation%")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get("%sourceLocation%")
        .thenAssertThat()
        .bodyIsLike(updatedSelfEmployment.toString)
    }

    "return code 404 when attempting to update a non-existent self-employment resource" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.selfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Jsons.selfEmployment()).at(s"/ni/$nino/self-employments/invalidSourceId")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 400 (INVALID_DATE) when attempting to update a self-employment with a non-ISO (i.e. YYYY-MM-DD) date" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.selfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Jsons.selfEmployment(commencementDate = "22-10-2016")).at(s"%sourceLocation%")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.invalidRequest(("INVALID_DATE", "/commencementDate")))
    }

    "return code 400 (INVALID_DATE) when attempting to update a self-employment with an empty date" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.selfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Jsons.selfEmployment(commencementDate = "")).at(s"%sourceLocation%")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.invalidRequest(("INVALID_DATE", "/commencementDate")))
    }

    "return code 400 (MANDATORY_FIELD_MISSING) when attempting to update a self-employment with an empty body" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.selfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Json.parse("{}")).at(s"%sourceLocation%")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.invalidRequest(("MANDATORY_FIELD_MISSING", "/accountingPeriod"),
          ("MANDATORY_FIELD_MISSING", "/accountingType"), ("MANDATORY_FIELD_MISSING", "/commencementDate")))
    }

    "return code 400 (INVALID_VALUE) when attempting to update a self-employment with an invalid accounting type" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.selfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Jsons.selfEmployment(accountingType = "INVALID")).at(s"%sourceLocation%")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.invalidRequest(("INVALID_VALUE", "/accountingType")))
    }
  }

  "retrieve" should {
    "return code 200 when retrieving a self-employment resource that exists" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.selfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%sourceLocation%")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.selfEmployment().toString())
        .bodyDoesNotHavePath[SourceId]("id")
    }

    "return code 404 when retrieving a self-employment resource that does not exist" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/self-employments/invalidSourceId")
        .thenAssertThat()
        .statusIs(404)
    }
  }

  "retrieveAll" should {
    "return code 200 when retrieving self-employments that exist" in {

      val expectedBody =
        s"""
           |[
           |  ${Jsons.selfEmployment().toString()}
           |]
         """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.selfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
    }

    "return code 200 with an empty body when the user has no self-employment sources" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(200)
        .jsonBodyIsEmptyArray
    }
  }

  "updateAnnualSummary" should {
    "return code 204 when updating an annual summary for a valid self-employment source" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.selfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Jsons.selfEmploymentAnnualSummary()).at(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(204)
    }

    "return code 404 when updating an annual summary for an invalid self-employment source" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .put(Jsons.selfEmploymentAnnualSummary()).at(s"/ni/$nino/self-employments/sillysource/$taxYear")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 400 when updating an annual summary providing an invalid adjustment & allowance" in {
      val annualSummaries = Jsons.selfEmploymentAnnualSummary(
        includedNonTaxableProfits = -100, overlapReliefUsed = -100,
        goodsAndServicesOwnUse = -100, capitalAllowanceMainPool = -100)

      val expectedBody = Jsons.Errors.invalidRequest(
         ("INVALID_MONETARY_AMOUNT", "/adjustments/includedNonTaxableProfits"),
        ("INVALID_MONETARY_AMOUNT", "/adjustments/overlapReliefUsed"),
        ("INVALID_MONETARY_AMOUNT", "/adjustments/goodsAndServicesOwnUse"),
        ("INVALID_MONETARY_AMOUNT", "/allowances/capitalAllowanceMainPool"))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.selfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(annualSummaries).at(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
    }
  }

  "retrieveAnnualSummary" should {
    "return code 200 when retrieving an annual summary that exists" in {
      val annualSummaries = Jsons.selfEmploymentAnnualSummary()
      val expectedJson = annualSummaries.toString()

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.selfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(annualSummaries).at(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedJson)
    }

    "return code 200 containing and empty object when retrieving a non-existent annual summary" in {

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.selfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .jsonBodyIsEmptyObject
    }
  }

  "createPeriod" should {
    "return code 201 containing a location header when creating a period" in {

      val period = Jsons.selfEmploymentPeriod(
        fromDate = Some("2017-04-06"),
        toDate = Some("2017-07-04"))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.selfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader("Location", s"/self-assessment/ni/$nino/self-employments/\\w+/periods/\\w+".r)
    }

    "return code 400 when attempting to create a period with the 'from' and 'to' dates are in the incorrect order" in {
      val periodOne = s"""{
                      |  "from": "2017-04-01",
                      |  "to": "2017-03-31"
                      |}""".stripMargin

      val expectedBody = Jsons.Errors.invalidRequest(("INVALID_PERIOD", ""))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.selfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(Json.parse(periodOne)).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
    }

    "return code 403 when attempting to create a period whose date range overlaps" in {
      val periodOne = Jsons.selfEmploymentPeriod(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"))
      val periodTwo = Jsons.selfEmploymentPeriod(fromDate = Some("2017-07-05"), toDate = Some("2017-08-04"))
      val overlappingPeriod = Jsons.selfEmploymentPeriod(fromDate = Some("2017-08-04"), toDate = Some("2017-09-04"))

      val expectedBody = Jsons.Errors.businessError(("OVERLAPPING_PERIOD", ""))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.selfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodOne).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodTwo).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(overlappingPeriod).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsLike(expectedBody)

    }

    "return code 403 when attempting to create a period that would leave a gap between the latest period and the one provided" in {
      val periodOne = Jsons.selfEmploymentPeriod(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"))
      val periodTwoWithGap = Jsons.selfEmploymentPeriod(fromDate = Some("2017-07-06"), toDate = Some("2017-08-04"))

      val expectedBody = Jsons.Errors.businessError(("GAP_PERIOD", ""))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.selfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodOne).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodTwoWithGap).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsLike(expectedBody)
    }
  }

  "updatePeriod" should {
    "return code 204 when updating a period that exists" in {
      val period = Jsons.selfEmploymentPeriod(
        fromDate = Some("2017-04-06"),
        toDate = Some("2017-07-04"),
        turnover = 100.25,
        otherIncome = 100.25,
        costOfGoodsBought = (100.25, 50.25),
        cisPaymentsToSubcontractors = (100.25, 50.25))

      val updatePeriod = Jsons.selfEmploymentPeriod(
        turnover = 200.25,
        otherIncome = 100.25,
        costOfGoodsBought = (200.25, 50.25),
        cisPaymentsToSubcontractors = (100.25, 55.25))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.selfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(updatePeriod).at(s"%periodLocation%")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get(s"%periodLocation%")
        .thenAssertThat()
        .bodyIsLike(updatePeriod.toString)
    }

    "return code 404 when attempting to update a non-existent period" in {
      val period = Jsons.selfEmploymentPeriod(
        fromDate = Some("2017-04-06"),
        toDate = Some("2017-07-04"),
        turnover = 100.25,
        otherIncome = 100.25,
        costOfGoodsBought = (100.25, 50.25),
        cisPaymentsToSubcontractors = (100.25, 50.25))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.selfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Json.toJson(period)).at(s"%sourceLocation%/periods/thereisnoperiodhere")
        .thenAssertThat()
        .statusIs(404)
    }
  }

  "retrievePeriod" should {
    "return code 200 when retrieving a period that exists" in {
      val period = Jsons.selfEmploymentPeriod(
        fromDate = Some("2017-04-06"),
        toDate = Some("2017-07-04"),
        turnover = 100.25,
        otherIncome = 100.25,
        costOfGoodsBought = (100.25, 50.25),
        cisPaymentsToSubcontractors = (100.25, 50.25))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.selfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%periodLocation%")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(period.toString)
        .bodyDoesNotHavePath[PeriodId]("id")
    }

    "return code 404 when retrieving a period that does not exist" in {

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.selfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%sourceLocation%/periods/oops")
        .thenAssertThat()
        .statusIs(404)
    }
  }

  "retrieveAllPeriods" should {
    "return code 200 when retrieving all periods where periods.size > 0, sorted by from date" in {
      val periodOne = Jsons.selfEmploymentPeriod(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"))
      val periodTwo = Jsons.selfEmploymentPeriod(fromDate = Some("2017-07-05"), toDate = Some("2017-08-04"))

      val expectedBody =
        s"""
           |[
           |  {
           |    "from": "2017-04-06",
           |    "to": "2017-07-04"
           |  },
           |  {
           |    "from": "2017-07-05",
           |    "to": "2017-08-04"
           |  }
           |]
         """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.selfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodOne).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodTwo).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
        .selectFields(_ \\ "id").isLength(2).matches("\\w+".r)
    }

    "return code 200 containing an empty json body when retrieving all periods where periods.size == 0" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.selfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(200)
        .jsonBodyIsEmptyArray
    }
  }
}
