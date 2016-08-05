package uk.gov.hmrc.selfassessmentapi.live

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.domain.SourceTypes.Employments
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.SourceType.FurnishedHolidayLettings
import uk.gov.hmrc.selfassessmentapi.domain.ukproperty.SourceType.UKProperties
import uk.gov.hmrc.support.BaseFunctionalSpec

class SummaryControllerSpec extends BaseFunctionalSpec {

  private def exampleSummaryTypeValue(summaryType: SummaryType): String = {
    (summaryType.example() \ "type").asOpt[String] match {
      case Some(typeValue) => typeValue
      case _ => ""
    }
  }

  val invalidAmountTestData: Set[SummaryType] = Set(selfemployment.SummaryTypes.GoodsAndServicesOwnUses) ++
                                                FurnishedHolidayLettings.summaryTypes ++ Employments.summaryTypes ++
                                                UKProperties.summaryTypes

  private def invalidRequestBody(summaryType: SummaryType) = {
    if (invalidAmountTestData.contains(summaryType)) {
      Some(Json.parse(s"""{"amount":1000.123}"""))
    } else {
      Some(Json.parse(s"""{"type":"InvalidType", "amount":1000.00, "taxDeduction":1000.00}"""))
    }
  }

  private def invalidErrorResponse(summaryType: SummaryType): (String, String) = {
    if (invalidAmountTestData.contains (summaryType) ) {
      ("/amount", "INVALID_MONETARY_AMOUNT")
    } else {
      ("/type", "NO_VALUE_FOUND")
    }
  }

  private val implementedSummaries = Map[SourceType, Set[SummaryType]](
    Employments -> Employments.summaryTypes,
    SourceTypes.SelfEmployments -> SourceTypes.SelfEmployments.summaryTypes,
    SourceTypes.UnearnedIncomes -> SourceTypes.UnearnedIncomes.summaryTypes,
    SourceTypes.FurnishedHolidayLettings -> SourceTypes.FurnishedHolidayLettings.summaryTypes,
    SourceTypes.UKProperties -> SourceTypes.UKProperties.summaryTypes)

  "I" should {
    "be able to create, get, update and delete all summaries for all sources" in {
      SourceTypes.types foreach { sourceType =>
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
              Some(Json.parse(s"""{"type":"${exampleSummaryTypeValue(summaryType)}", "amount":1200.00, "taxDeduction":1000.00}""")))
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
      SourceTypes.types foreach { sourceType =>
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
    "not be able to create summary for a non existent source" in {
      val summaryTypes = uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SummaryTypes

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/${SourceTypes.SelfEmployments.name}/1234567890/${summaryTypes.Incomes.name}",
          Some(summaryTypes.Incomes.example()))
        .withAcceptHeader()
        .thenAssertThat()
        .isNotFound
    }
  }

  "I" should {
    "not be able to update summary with invalid payload" in {
      SourceTypes.types foreach { sourceType =>
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
      SourceTypes.types foreach { sourceType =>
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
      SourceTypes.types foreach { sourceType =>
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

  "I" should {
    "not be able to update a non existent summary" in {
      SourceTypes.types foreach { sourceType =>
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
            .put(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/12334567", Some(summaryType.example()))
            .withAcceptHeader()
            .thenAssertThat()
            .isNotFound
        }
      }
    }
  }

}
