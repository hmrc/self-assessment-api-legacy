package uk.gov.hmrc.selfassessmentapi.live

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SummaryTypes
import uk.gov.hmrc.support.BaseFunctionalSpec

class SummaryControllerSpec extends BaseFunctionalSpec {

  def exampleSummaryTypeValue(summaryType: SummaryType): String = {
    (summaryType.example() \ "type").as[String]
  }

  private val selfEmploymentSummaries = Seq(SummaryTypes.Incomes, SummaryTypes.Expenses, SummaryTypes.BalancingCharges)

  "I" should {
    "be able to create, get, update and delete all summaries for all sources" in {
      Seq(SourceTypes.SelfEmployments) foreach { sourceType =>
        selfEmploymentSummaries foreach { summaryType =>
          given()
            .userIsAuthorisedForTheResource(saUtr)
          .when()
            .get(s"/$saUtr/$taxYear/${sourceType.name}")
            .thenAssertThat()
            .statusIs(200)
            .butResponseHasNo(sourceType.name)
          .when()
            .post(s"/$saUtr/$taxYear/${sourceType.name}", Some(sourceType.example()))
            .thenAssertThat()
            .statusIs(201)
            .contentTypeIsHalJson()
            .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/.+".r)
          .when()
            .get(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%")
            .thenAssertThat()
            .statusIs(200)
          .when()
            .get(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}")
            .thenAssertThat()
            .statusIs(200)
            .butResponseHasNo(sourceType.name, summaryType.name)
          .when()
            .post(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}", Some(summaryType.example()))
            .thenAssertThat()
            .statusIs(201)
            .contentTypeIsHalJson()
            .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/.+".r)
          .when()
            .get(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/%summaryId%")
            .thenAssertThat()
            .statusIs(200)
            .body(_ \ "type").is(exampleSummaryTypeValue(summaryType)).body(_ \ "amount").is((summaryType.example() \ "amount").as[BigDecimal])
          .when()
            .put(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/%summaryId%",
              Some(Json.parse(s"""{"type":"${exampleSummaryTypeValue(summaryType)}", "amount":1200.00}""")))
            .thenAssertThat()
            .statusIs(200)
            .contentTypeIsHalJson()
            .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/.+".r)
          .when()
            .get(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/%summaryId%")
            .thenAssertThat()
            .statusIs(200)
            .body(_ \ "type").is(exampleSummaryTypeValue(summaryType)).body(_ \ "amount").is(1200.00)
          .when()
            .delete(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/%summaryId%")
            .thenAssertThat()
            .statusIs(204)
          .when()
            .get(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/%summaryId%")
            .thenAssertThat()
            .statusIs(404)
        }
      }
    }
  }

  "I" should {
    "not be able to create summary with invalid payload" in {
      Seq(SourceTypes.SelfEmployments) foreach { sourceType =>
        selfEmploymentSummaries foreach { summaryType =>
          given()
            .userIsAuthorisedForTheResource(saUtr)
          .when()
            .post(s"/$saUtr/$taxYear/${sourceType.name}", Some(sourceType.example()))
            .thenAssertThat()
            .statusIs(201)
            .contentTypeIsHalJson()
            .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/.+".r)
          .when()
            .post(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}",
              Some(Json.parse(s"""{"type":"InvalidType", "amount":1000.00}""")))
            .thenAssertThat()
            .statusIs(400)
            .bodyContainsError(("/type", "NO_VALUE_FOUND"))
        }
      }
    }
  }

  "I" should {
    "not be able to update summary with invalid payload" in {
      Seq(SourceTypes.SelfEmployments) foreach { sourceType =>
        selfEmploymentSummaries foreach { summaryType =>
          given()
            .userIsAuthorisedForTheResource(saUtr)
          .when()
            .post(s"/$saUtr/$taxYear/${sourceType.name}", Some(sourceType.example()))
            .thenAssertThat()
            .statusIs(201)
            .contentTypeIsHalJson()
            .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/.+".r)
          .when()
            .post(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}", Some(summaryType.example()))
            .thenAssertThat()
            .statusIs(201)
            .contentTypeIsHalJson()
            .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/.+".r)
          .when()
            .put(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/%summaryId%",
              Some(Json.parse(s"""{"type":"InvalidType", "amount":1200.00}""")))
            .thenAssertThat()
            .statusIs(400)
            .bodyContainsError(("/type", "NO_VALUE_FOUND"))
        }
      }
    }
  }

  "I" should {
    "not be able to get a non existent summary" in {
      Seq(SourceTypes.SelfEmployments) foreach { sourceType =>
        selfEmploymentSummaries foreach { summaryType =>
          given()
            .userIsAuthorisedForTheResource(saUtr)
          .when()
            .post(s"/$saUtr/$taxYear/${sourceType.name}", Some(sourceType.example()))
            .thenAssertThat()
            .statusIs(201)
            .contentTypeIsHalJson()
            .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/.+".r)
          .when()
            .get(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/12334567")
            .thenAssertThat()
            .statusIs(404)
        }
      }
    }
  }

  "I" should {
    "not be able to delete a non existent summary" in {
      Seq(SourceTypes.SelfEmployments) foreach { sourceType =>
        selfEmploymentSummaries foreach { summaryType =>
          given()
            .userIsAuthorisedForTheResource(saUtr)
          .when()
            .post(s"/$saUtr/$taxYear/${sourceType.name}", Some(sourceType.example()))
            .thenAssertThat()
            .statusIs(201)
            .contentTypeIsHalJson()
            .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/.+".r)
          .when()
            .delete(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/12334567")
            .thenAssertThat()
            .statusIs(404)
        }
      }
    }
  }

}
