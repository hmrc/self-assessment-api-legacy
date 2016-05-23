package uk.gov.hmrc.selfassessmentapi.sandbox

import play.api.libs.json.Json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.domain.{SelfEmploymentIncome, SourceTypes}
import uk.gov.hmrc.selfassessmentapi.domain.SelfEmploymentIncomeType._
import uk.gov.hmrc.support.BaseFunctionalSpec

class SummaryControllerSpec extends BaseFunctionalSpec {

  val sourceId = BSONObjectID.generate.stringify
  val summaryId = BSONObjectID.generate.stringify

  "Create summary with valid data" should {
    "return a 201 when the resource is created" in {
      SourceTypes.types.foreach { sourceType =>
        sourceType.summaryTypes.foreach { summaryType =>
          given()
            .when()
            .post(s"/sandbox/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}", Some(summaryType.example))
            .thenAssertThat()
            .statusIs(201)
            .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}/.+".r)
        }
      }
    }
  }

  // Only a single validation test is needed to show the validator is invoked. All validations are tested at a unit level.
  "Create summary with invalid data" should {
    "return a 400 validation error" in {
      given()
        .when()
        .post(s"/sandbox/$saUtr/$taxYear/self-employments/$sourceId/incomes", Some(toJson(SelfEmploymentIncome(None, Turnover, BigDecimal(-1000.12)))))
        .thenAssertThat()
        .statusIs(400)
    }
  }

  "Retrieve an existent summary id" should {
    "return a HAL resource" in {
      SourceTypes.types.foreach { sourceType =>
        sourceType.summaryTypes.foreach { summaryType =>
          given()
            .when()
            .get(s"/sandbox/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}/$summaryId")
            .thenAssertThat()
            .statusIs(200)
            .contentTypeIsHalJson()
            .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}/$summaryId")
        }
      }
    }
  }

  "Retrieve all summaries" should {
    "return all summaries as HAL resources" in {
      SourceTypes.types.foreach { sourceType =>
        sourceType.summaryTypes.foreach { summaryType =>
          given()
            .when()
            .get(s"/sandbox/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}")
            .thenAssertThat()
            .statusIs(200)
            .contentTypeIsHalJson()
            .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}")
            .bodyHasPath(s"""_embedded \\ ${summaryType.name}(0) \\ _links \\ self \\ href""", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}/.+".r)
            .bodyHasPath(s"""_embedded \\ ${summaryType.name}(1) \\ _links \\ self \\ href""", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}/.+".r)
            .bodyHasPath(s"""_embedded \\ ${summaryType.name}(2) \\ _links \\ self \\ href""", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}/.+".r)
        }
      }
    }
  }

  "Update summary" should {
    "return a 200 with HAL resource" in {
      SourceTypes.types.foreach { sourceType =>
        sourceType.summaryTypes.foreach { summaryType =>
          given()
            .when()
            .put(s"/sandbox/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}/$summaryId", Some(summaryType.example))
            .thenAssertThat()
            .statusIs(200)
            .contentTypeIsHalJson()
            .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}/$summaryId")
        }
      }
    }
  }

  "Delete summary" should {
    "return a 204 response" in {
      SourceTypes.types.foreach { sourceType =>
        sourceType.summaryTypes.foreach { summaryType =>
          given()
            .when()
            .delete(s"/sandbox/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}/$summaryId")
            .thenAssertThat()
            .statusIs(204)
        }
      }
    }
  }

}
