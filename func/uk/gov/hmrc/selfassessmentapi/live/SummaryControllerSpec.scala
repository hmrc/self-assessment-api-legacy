package uk.gov.hmrc.selfassessmentapi.live

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.support.BaseFunctionalSpec

class SummaryControllerSpec extends BaseFunctionalSpec {

  private def exampleSummaryTypeValue(summaryType: SummaryType): String = {
    (summaryType.example() \ "type").asOpt[String] match {
      case Some(typeValue) => typeValue
      case _ => ""
    }
  }

  private def invalidRequestBody(summaryType: SummaryType) = {
    if (summaryType == selfemployment.SummaryTypes.GoodsAndServicesOwnUses) Some(Json.parse(s"""{"amount":1000.123}"""))
    else Some(Json.parse(s"""{"type":"InvalidType", "amount":1000.00}"""))
  }

  private def invalidErrorResponse(summaryType: SummaryType): (String, String) = {
    if (summaryType == selfemployment.SummaryTypes.GoodsAndServicesOwnUses) ("/amount", "INVALID_MONETARY_AMOUNT")
    else ("/type", "NO_VALUE_FOUND")
  }

  private val implementedSources = Seq(SourceTypes.SelfEmployments, SourceTypes.UnearnedIncomes)

  private val implementedSummaries = Map(SourceTypes.SelfEmployments -> SourceTypes.SelfEmployments.summaryTypes,
    SourceTypes.UnearnedIncomes -> Set(unearnedincome.SummaryTypes.SavingsIncomes, unearnedincome.SummaryTypes.Dividends))

  "I" should {
    "be able to create, get, update and delete all summaries for all sources" in {
      implementedSources foreach { sourceType =>
        implementedSummaries(sourceType) foreach { summaryType =>
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
            .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/%summaryId%")
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
            .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/%summaryId%")
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
            .withAcceptHeader()
            .thenAssertThat()
            .isNotFound
        }
      }
    }
  }



  "I" should {
    "not be able to create summary with invalid payload" in {
      implementedSources foreach { sourceType =>
        implementedSummaries(sourceType) foreach { summaryType =>
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
              invalidRequestBody(summaryType))
            .withAcceptHeader()
            .thenAssertThat()
            .isValidationError(invalidErrorResponse(summaryType))
        }
      }
    }
  }



  "I" should {
    "not be able to update summary with invalid payload" in {
      implementedSources foreach { sourceType =>
        implementedSummaries(sourceType) foreach { summaryType =>
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
            .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/%summaryId%")
          .when()
            .put(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/%summaryId%",
              invalidRequestBody(summaryType))
            .withAcceptHeader()
            .thenAssertThat()
            .isValidationError(invalidErrorResponse(summaryType))
        }
      }
    }
  }

  "I" should {
    "not be able to get a non existent summary" in {
      implementedSources foreach { sourceType =>
        implementedSummaries(sourceType) foreach { summaryType =>
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
            .withAcceptHeader()
            .thenAssertThat()
            .isNotFound
        }
      }
    }
  }

  "I" should {
    "not be able to delete a non existent summary" in {
      implementedSources foreach { sourceType =>
        implementedSummaries(sourceType) foreach { summaryType =>
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
            .withAcceptHeader()
            .thenAssertThat()
            .isNotFound
        }
      }
    }
  }

}
