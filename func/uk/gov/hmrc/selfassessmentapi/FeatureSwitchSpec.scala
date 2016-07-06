package uk.gov.hmrc.selfassessmentapi

import java.util.UUID

import play.api.test.FakeApplication
import uk.gov.hmrc.selfassessmentapi.domain.SourceId
import uk.gov.hmrc.selfassessmentapi.domain.employment.SourceType.Employments
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SourceType.SelfEmployments
import uk.gov.hmrc.support.BaseFunctionalSpec

class FeatureSwitchSpec extends BaseFunctionalSpec {

  val sourceId = UUID.randomUUID().toString
  val summaryId = UUID.randomUUID().toString

  private val conf: Map[String, Map[SourceId, Map[SourceId, Map[SourceId, Any]]]] =
    Map("Test" ->
      Map("feature-switch" ->
        Map(
          "self-employments" -> Map("enabled" -> true, "incomes" -> Map("enabled" -> true),
            "expenses" -> Map("enabled" -> false), "balancing-charges" -> Map("enabled" -> false),
            "goods-and-services-own-uses" -> Map("enabled" -> false)))
          ))

  override lazy val app: FakeApplication = new FakeApplication(additionalConfiguration = conf)

  "self-employments and self employment incomes" should {
    "be visible" in {

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear/${SelfEmployments.name}")
        .thenAssertThat()
        .statusIs(200)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear/${SelfEmployments.name}/$sourceId/incomes")
        .thenAssertThat()
        .statusIs(200)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear/${SelfEmployments.name}/$sourceId/expenses")
        .thenAssertThat()
        .statusIs(501)
    }
  }

  "employments source" should {
    "not be visible" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear/${Employments.name}")
        .thenAssertThat()
        .statusIs(501)
    }
  }

}
