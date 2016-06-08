package uk.gov.hmrc.selfassessmentapi

import java.util.UUID

import play.api.test.FakeApplication
import uk.gov.hmrc.selfassessmentapi.domain.employment.SourceType.Employments
import uk.gov.hmrc.selfassessmentapi.domain.employment.SummaryTypes.{Expenses, Incomes}
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SourceType.SelfEmployments
import uk.gov.hmrc.selfassessmentapi.domain.ukproperty.SourceType.UKProperties
import uk.gov.hmrc.support.BaseFunctionalSpec

class FeatureSwitchSpec extends BaseFunctionalSpec {

  val sourceId = UUID.randomUUID().toString
  val summaryId = UUID.randomUUID().toString

  override lazy val app: FakeApplication = new FakeApplication(additionalConfiguration =
    Map("Test" ->
      Map("feature-switch" ->
        Map(
          "employments" -> Map("enabled" -> false),
          "self-employments" -> Map("enabled" -> false),
          "uk-properties" -> Map("expenses" -> Map("enabled" -> false))
        )
      )
    ))

  "employments and self-employments resource" should {
    "be blocked" in {
      Seq(Employments, SelfEmployments).foreach { source =>
        Seq("/sandbox", "").foreach { mode =>
          given()
            .userIsAuthorisedForTheResource(saUtr)
            .when()
            .get(s"$mode/$saUtr/$taxYear/${source.name}")
            .thenAssertThat()
            .statusIs(404)

          given()
            .userIsAuthorisedForTheResource(saUtr)
            .when()
            .get(s"$mode/$saUtr/$taxYear/${source.name}/$sourceId")
            .thenAssertThat()
            .statusIs(404)

          given()
            .userIsAuthorisedForTheResource(saUtr)
            .when()
            .get(s"$mode/$saUtr/$taxYear/${source.name}/$sourceId/incomes/$summaryId")
            .thenAssertThat()
            .statusIs(404)

          given()
            .userIsAuthorisedForTheResource(saUtr)
            .when()
            .post(s"$mode/$saUtr/$taxYear/${source.name}/$sourceId/incomes", Some(Incomes.example))
            .thenAssertThat()
            .statusIs(404)

          given()
            .userIsAuthorisedForTheResource(saUtr)
            .when()
            .put(s"$mode/$saUtr/$taxYear/${source.name}/$sourceId/incomes/$summaryId", Some(Incomes.example))
            .thenAssertThat()
            .statusIs(404)

          given()
            .userIsAuthorisedForTheResource(saUtr)
            .when()
            .post(s"$mode/$saUtr/$taxYear/${source.name}", Some(source.example))
            .thenAssertThat()
            .statusIs(404)

          given()
            .userIsAuthorisedForTheResource(saUtr)
            .when()
            .put(s"$mode/$saUtr/$taxYear/${source.name}/$sourceId", Some(source.example))
            .thenAssertThat()
            .statusIs(404)
        }
      }
    }
  }

  "only expenses resources for uk-properties" should {
    "be blocked" in {
      when()
        .get(s"/sandbox/$saUtr/$taxYear/uk-properties")
        .thenAssertThat()
        .statusIs(200)

      when()
        .get(s"/sandbox/$saUtr/$taxYear/uk-properties/$sourceId")
        .thenAssertThat()
        .statusIs(200)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/sandbox/$saUtr/$taxYear/uk-properties", Some(UKProperties.example))
        .thenAssertThat()
        .statusIs(201)

      when()
        .get(s"/sandbox/$saUtr/$taxYear/uk-properties/$sourceId/expenses")
        .thenAssertThat()
        .statusIs(404)

      when()
        .get(s"/sandbox/$saUtr/$taxYear/uk-properties/$sourceId/expenses/$summaryId")
        .thenAssertThat()
        .statusIs(404)

      when()
        .put(s"/sandbox/$saUtr/$taxYear/uk-properties/$sourceId/expenses/$summaryId", Some(Expenses.example))
        .thenAssertThat()
        .statusIs(404)
    }
  }

}
