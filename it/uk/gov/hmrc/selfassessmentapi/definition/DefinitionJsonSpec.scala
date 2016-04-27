package uk.gov.hmrc.selfassessmentapi.definition

import org.scalatest.WordSpec
import play.api.libs.json.Json

import scala.io.Source
import JsonFormatters._

class DefinitionJsonSpec extends WordSpec {

  "definition.json" should {
    "be valid" in {
      val contents = Source.fromInputStream(this.getClass.getResourceAsStream("/public/api/definition.json")).mkString
      val json = Json.parse(contents)
      (json \ "api").validate[APIDefinition].asEither match {
        case Right(result) =>
        case Left(errors) => fail(s"definition.json is invalid: ${errors.mkString(",")}")
      }
    }
  }

}
