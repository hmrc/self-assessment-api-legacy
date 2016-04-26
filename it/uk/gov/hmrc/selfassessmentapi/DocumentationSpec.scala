package uk.gov.hmrc.selfassessmentapi

import play.api.libs.json.{JsArray, Json}
import uk.gov.hmrc.support.BaseFunctionalSpec

import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global

class DocumentationSpec extends BaseFunctionalSpec {

  "Request to /api/definition" should {
    "return 200 with json response" in {
      given()
        .when()
        .get("/api/definition")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIs("application/json; charset=utf-8")
    }
  }

  "Request to /api/documentation/<version>/<endpoint>" should {
    "return 200 with xml response for all endpoints in definition.json" in {
      val contents = Source.fromInputStream(this.getClass.getResourceAsStream("/public/api/definition.json")).mkString
      val json = Json.parse(contents)
      (json \ "api" \ "versions").as[JsArray].value foreach { version =>
        val v = (version \ "version").as[String]
        (version \ "endpoints").as[JsArray].value foreach { endpoint =>
          val name = (endpoint \ "endpointName").as[String]
          val nameInUrl = name.replaceAll(" ", "-")
          given()
            .when()
            .get(s"/api/documentation/$v/$nameInUrl")
            .thenAssertThat()
            .statusIs(200)
            .contentTypeIs("application/xml")
        }

      }
    }
  }

}
