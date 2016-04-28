package uk.gov.hmrc.selfassessmentapi.definition

import uk.gov.hmrc.selfassessmentapi.controllers.definition.{APIStatus, SelfAssessmentApiDefinition}
import uk.gov.hmrc.support.BaseFunctionalSpec

class DocumentationSpec extends BaseFunctionalSpec {

  "Request to /api/definition" should {
    "return 200 with json response" in {
      given()
        .when()
        .get("/api/definition").withoutAcceptHeader()
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIs("application/json; charset=utf-8")
    }
  }

  "Request to /api/documentation/<version>/<endpoint>" should {
    "return 200 with xml response for all endpoints in SelfAssessmentDefinition" in {
      val definition = new SelfAssessmentApiDefinition("self-assessment", APIStatus.PROTOTYPED).definition
      definition.api.versions foreach { version =>
        version.endpoints foreach { endpoint =>
          val name = endpoint.endpointName
          val nameInUrl = name.replaceAll(" ", "-")
          given()
            .when()
            .get(s"/api/documentation/${version.version}/$nameInUrl").withoutAcceptHeader()
            .thenAssertThat()
            .statusIs(200)
            .contentTypeIs("application/xml")
        }

      }
    }
  }

}
