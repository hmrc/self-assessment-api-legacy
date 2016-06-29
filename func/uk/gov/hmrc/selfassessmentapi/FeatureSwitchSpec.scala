package uk.gov.hmrc.selfassessmentapi

import java.util.UUID

import uk.gov.hmrc.selfassessmentapi.domain.employment.SourceType.Employments
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SourceType.SelfEmployments
import uk.gov.hmrc.support.BaseFunctionalSpec

class FeatureSwitchSpec extends BaseFunctionalSpec {

  val sourceId = UUID.randomUUID().toString
  val summaryId = UUID.randomUUID().toString

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
