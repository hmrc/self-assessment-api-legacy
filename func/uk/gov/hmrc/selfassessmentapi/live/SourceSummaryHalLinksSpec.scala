package uk.gov.hmrc.selfassessmentapi.live

import play.api.test.FakeApplication
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.SourceType.SelfEmployments
import uk.gov.hmrc.support.BaseFunctionalSpec

class SourceSummaryHalLinksSpec extends BaseFunctionalSpec {

  private val conf: Map[String, Map[SourceId, Map[SourceId, Map[SourceId, Any]]]] =
    Map("Test" ->
      Map("feature-switch" ->
        Map(
          "self-employments" -> Map("enabled" -> true,
            "incomes" -> Map("enabled" -> true),
            "expenses" -> Map("enabled" -> false)))))

  override lazy val app: FakeApplication = new FakeApplication(additionalConfiguration = conf)

  "Request to discover tax year" should {
    "have Hal links for self-employments" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLinksForSourceType(SelfEmployments, saUtr, taxYear)
    }
  }

  "Request to create self-employments" should {
    "have Hal links for incomes" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments", Some(SelfEmployments.example()))
        .thenAssertThat()
        .statusIs(201)
        .contentTypeIsHalJson()
        .bodyHasSummaryLink(SelfEmployments, selfemployment.SummaryTypes.Incomes, saUtr, taxYear)
    }

    "not have Hal links for expenses" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments", Some(SelfEmployments.example()))
        .thenAssertThat()
        .statusIs(201)
        .contentTypeIsHalJson()
        .bodyDoesNotHaveSummaryLink(SelfEmployments, selfemployment.SummaryTypes.Expenses, saUtr, taxYear)
    }
  }

  "Request to read self-employments" should {
    "have Hal links for incomes" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
          .post(s"/$saUtr/$taxYear/self-employments", Some(SelfEmployments.example()))
          .thenAssertThat()
          .statusIs(201)
        .when()
          .get(s"/$saUtr/$taxYear/self-employments/%sourceId%")
          .thenAssertThat()
          .statusIs(200)
          .contentTypeIsHalJson()
          .bodyHasSummaryLink(SelfEmployments, selfemployment.SummaryTypes.Incomes, saUtr, taxYear)
    }

    "not have Hal links for expenses" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
          .post(s"/$saUtr/$taxYear/self-employments", Some(SelfEmployments.example()))
          .thenAssertThat()
          .statusIs(201)
        .when()
          .get(s"/$saUtr/$taxYear/self-employments/%sourceId%")
          .thenAssertThat()
          .statusIs(200)
          .contentTypeIsHalJson()
          .bodyDoesNotHaveSummaryLink(SelfEmployments, selfemployment.SummaryTypes.Expenses, saUtr, taxYear)
    }
  }

  "Request to update self-employments" should {
    "have Hal links for incomes" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
          .post(s"/$saUtr/$taxYear/self-employments", Some(SelfEmployments.example()))
          .thenAssertThat()
          .statusIs(201)
        .when()
          .put(s"/$saUtr/$taxYear/self-employments/%sourceId%", Some(SelfEmployments.example()))
          .thenAssertThat()
          .statusIs(200)
          .contentTypeIsHalJson()
          .bodyHasSummaryLink(SelfEmployments, selfemployment.SummaryTypes.Incomes, saUtr, taxYear)
    }

    "not have Hal links for expenses" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
          .post(s"/$saUtr/$taxYear/self-employments", Some(SelfEmployments.example()))
          .thenAssertThat()
          .statusIs(201)
        .when()
          .put(s"/$saUtr/$taxYear/self-employments/%sourceId%", Some(SelfEmployments.example()))
          .thenAssertThat()
          .statusIs(200)
          .contentTypeIsHalJson()
          .bodyDoesNotHaveSummaryLink(SelfEmployments, selfemployment.SummaryTypes.Expenses, saUtr, taxYear)
    }
  }


}
