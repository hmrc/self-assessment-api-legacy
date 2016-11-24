package uk.gov.hmrc.selfassessmentapi.featureswitch

import org.joda.time.LocalDate
import play.api.libs.json.Json
import play.api.test.FakeApplication
import uk.gov.hmrc.selfassessmentapi.resources.models.{SelfEmployment, SelfEmploymentAnnualSummary}
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentPeriodicSummaryFeatureSwitchSpec extends BaseFunctionalSpec {

  private val conf =
    Map("Test" ->
      Map("feature-switch" ->
        Map("self-employments" ->
          Map("enabled" -> true, "annual" -> Map("enabled" -> true), "periods" -> Map("enabled" -> false))
        )
      )
    )

  override lazy val app: FakeApplication = new FakeApplication(additionalConfiguration = conf)

  "self-employments" should {
    "not be visible if feature Switched Off" in {
      val selfEmployment = Json.toJson(SelfEmployment(commencementDate = LocalDate.parse("2016-01-01")))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/nino/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(404)
    }
  }

}


