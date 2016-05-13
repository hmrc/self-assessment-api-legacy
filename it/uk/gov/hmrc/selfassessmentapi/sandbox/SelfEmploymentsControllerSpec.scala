package uk.gov.hmrc.selfassessmentapi.sandbox

import java.util.UUID

import org.joda.time.LocalDate
import play.api.libs.json.Json._
import uk.gov.hmrc.selfassessmentapi.domain.SelfEmployment
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentsControllerSpec extends BaseFunctionalSpec {

  val selfEmploymentId = UUID.randomUUID().toString

  "Sandbox Self employment" should {

    "return a 201 response with links to newly created self employment" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/sandbox/$saUtr/self-employments", Some(toJson(Some(SelfEmployment(None, "name", LocalDate.now.minusDays(1))))))
        .thenAssertThat()
        .statusIs(201)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/self-employments/.+".r)
    }

    "Create self-employment with invalid data" should {
      "return a 400 response" in {
        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .post(s"/sandbox/$saUtr/self-employments", Some(toJson(SelfEmployment(name = "a" * 101, commencementDate = LocalDate.now().plusDays(1)))))
          .thenAssertThat()
          .statusIs(400)
          .body1(_ \\ "code").is("COMMENCEMENT_DATE_NOT_IN_THE_PAST", "MAX_FIELD_LENGTH_EXCEEDED")
          .body1(_ \\ "path").is("/commencementDate", "/name")
      }
    }

    "return a valid response containing an existing self employment" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/sandbox/$saUtr/self-employments/$selfEmploymentId")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/self-employments/$selfEmploymentId")
    }

    "return a valid response when retrieving list of self employments" in {
      given()
        .when()
        .get(s"/sandbox/$saUtr/self-employments")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/self-employments")
        .bodyHasPath("""_embedded \ selfEmployments(0) \ _links \ self \ href""", s"/self-assessment/$saUtr/self-employments/1234")
        .bodyHasPath("""_embedded \ selfEmployments(1) \ _links \ self \ href""", s"/self-assessment/$saUtr/self-employments/5678")
        .bodyHasPath("""_embedded \ selfEmployments(2) \ _links \ self \ href""", s"/self-assessment/$saUtr/self-employments/9101")
    }

    "return 200 and a valid response when an existing self employment is modified" in {
      given()
        .when()
        .put(s"/sandbox/$saUtr/self-employments/$selfEmploymentId", Some(toJson(Some(SelfEmployment(None, "name", LocalDate.now.minusDays(1))))))
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/self-employments/$selfEmploymentId")
    }

    "return 204 response when an existing self employment is deleted" in {
      given()
        .when()
        .delete(s"/sandbox/$saUtr/self-employments/$selfEmploymentId")
        .thenAssertThat()
        .statusIs(204)
    }
  }
}
