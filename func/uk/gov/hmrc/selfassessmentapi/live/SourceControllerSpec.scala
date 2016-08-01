package uk.gov.hmrc.selfassessmentapi.live

import org.joda.time.LocalDate
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson
import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode.COMMENCEMENT_DATE_NOT_IN_THE_PAST
import uk.gov.hmrc.selfassessmentapi.domain.employment.Employment
import uk.gov.hmrc.selfassessmentapi.domain.employment.SourceType.Employments
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SelfEmployment
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SelfEmployment._
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SourceType.SelfEmployments
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.SourceType.UnearnedIncomes
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.UnearnedIncome
import uk.gov.hmrc.selfassessmentapi.domain.{SourceType, SourceTypes}
import uk.gov.hmrc.support.BaseFunctionalSpec

import scala.util.matching.Regex

case class ExpectedError(path: String, code: String, httpStatusCode: Regex = "400".r)
case class ExpectedUpdate(path: JsValue => JsValue, value: String = "")

case class ErrorScenario(invalidInput: JsValue, error: ExpectedError)
case class UpdateScenario(updatedValue: JsValue, expectedUpdate: ExpectedUpdate)

class SourceControllerSpec extends BaseFunctionalSpec {

  val implementedSourceTypes = Set(SourceTypes.SelfEmployments, SourceTypes.UnearnedIncomes, SourceTypes.Employments)

  val notImplementedSourceTypes = Set(SourceTypes.FurnishedHolidayLettings, SourceTypes.UKProperties)

  val ok: Regex = "20.".r
  val errorScenarios: Map[SourceType, ErrorScenario] = Map(
    SelfEmployments -> ErrorScenario(invalidInput = toJson(SelfEmployment.example().copy(commencementDate = LocalDate.now().plusDays(1))),
      error = ExpectedError(path = "/commencementDate", code = s"$COMMENCEMENT_DATE_NOT_IN_THE_PAST")),
    UnearnedIncomes -> ErrorScenario(invalidInput = toJson(UnearnedIncome.example()), error = ExpectedError(path = "", code = "", httpStatusCode = ok)),
    Employments -> ErrorScenario(invalidInput = toJson(Employment.example()), error = ExpectedError(path = "", code = "", httpStatusCode = ok))
  )

  val updateScenarios: Map[SourceType, UpdateScenario] = Map(
    SelfEmployments -> UpdateScenario(updatedValue = toJson(SelfEmployment.example().copy(commencementDate = LocalDate.now().minusDays(1))),
      expectedUpdate = ExpectedUpdate(path = _ \ "commencementDate", value = LocalDate.now().minusDays(1).toString("yyyy-MM-dd"))),
    UnearnedIncomes -> UpdateScenario(updatedValue = toJson(UnearnedIncome.example()),
      expectedUpdate = ExpectedUpdate(path = _ \ "_id", value = "")),
    Employments -> UpdateScenario(updatedValue = toJson(Employment.example()),
      expectedUpdate = ExpectedUpdate(path = _ \ "_id", value = ""))
  )

  "I" should {
    "be able to create, update and delete a self assessment source" in {
      implementedSourceTypes.foreach { sourceType =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
        .when()
          .get(s"/$saUtr/$taxYear/${sourceType.name}")
          .withAcceptHeader()
          .thenAssertThat()
          .statusIs(200)
          .butResponseHasNo(sourceType.name)
        when()
          .post(Some(sourceType.example())).to(s"/$saUtr/$taxYear/${sourceType.name}")
          .withAcceptHeader()
          .thenAssertThat()
          .statusIs(201)
          .contentTypeIsHalJson()
          .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/.+".r)
          .bodyHasSummaryLinks(sourceType, saUtr, taxYear)
        .when()
          .get(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%")
          .withAcceptHeader()
          .thenAssertThat()
          .statusIs(200)
          .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/%sourceId%")
        .when()
          .put(Some(updateScenarios(sourceType).updatedValue)).at(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%")
          .withAcceptHeader()
          .thenAssertThat()
          .statusIs(200)
          .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/%sourceId%")
        .when()
          .get(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%")
          .withAcceptHeader()
          .thenAssertThat()
          .statusIs(200)
          .body(updateScenarios(sourceType).expectedUpdate.path).is(updateScenarios(sourceType).expectedUpdate.value)
        .when()
          .delete(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%")
          .thenAssertThat()
          .statusIs(204)
        .when()
          .get(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%")
          .withAcceptHeader()
          .thenAssertThat()
          .isNotFound
      }
    }
  }

  "I" should {
    "not be able to get a invalid source type" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
      .when()
        .get(s"/$saUtr/$taxYear/blah")
        .withAcceptHeader()
        .thenAssertThat()
        .isNotFound
    }

    "not be able to get a non-existent source" in {
      implementedSourceTypes.foreach { sourceType =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
        .when()
          .get(s"/$saUtr/$taxYear/${sourceType.name}/asdfasdf")
          .withAcceptHeader()
          .thenAssertThat()
          .isNotFound

        given()
          .userIsAuthorisedForTheResource(saUtr)
        .when()
          .put(s"/$saUtr/$taxYear/${sourceType.name}/asdfasdf", Some(sourceType.example()))
          .withAcceptHeader()
          .thenAssertThat()
          .isNotFound

        given()
            .userIsAuthorisedForTheResource(saUtr)
          .when()
            .delete(s"/$saUtr/$taxYear/${sourceType.name}/asdfasdf")
            .withAcceptHeader()
            .thenAssertThat()
            .isNotFound

      }
    }

    "not be able to create a source with invalid data" in {
      implementedSourceTypes.filter(errorScenarios(_).error.httpStatusCode != ok).foreach { sourceType =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
        .when()
          .post(s"/$saUtr/$taxYear/${sourceType.name}", Some(errorScenarios(sourceType).invalidInput))
          .withAcceptHeader()
          .thenAssertThat()
          .isValidationError(errorScenarios(sourceType).error.path, errorScenarios(sourceType).error.code)
      }
    }

    "not be able to update a source with invalid data" in {
      implementedSourceTypes.filter(errorScenarios(_).error.httpStatusCode != ok).foreach { sourceType =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
        .when()
          .post(s"/$saUtr/$taxYear/${sourceType.name}", Some(sourceType.example()))
          .thenAssertThat()
          .statusIs(201)
        .when()
          .put(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%", Some(errorScenarios(sourceType).invalidInput))
          .thenAssertThat()
          .isValidationError(errorScenarios(sourceType).error.path, errorScenarios(sourceType).error.code)
      }
    }

    "not be able to update a non-existent" in {
      implementedSourceTypes.foreach { sourceType =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
        .when()
          .put(s"/$saUtr/$taxYear/${sourceType.name}/non-existent-source", Some(updateScenarios(sourceType).updatedValue))
          .thenAssertThat()
          .isNotFound
      }
    }

    "not be able to delete a non-existent" in {
      implementedSourceTypes.foreach { sourceType =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
        .when()
          .delete(s"/$saUtr/$taxYear/${sourceType.name}/non-existent-source")
          .thenAssertThat()
          .isNotFound
      }
    }
  }

  "Not Implemented Live source controller" should {

    "return a Not Implemented response on GET" in {
      notImplementedSourceTypes.foreach { sourceType =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
        .when()
          .get(s"/$saUtr/$taxYear/${sourceType.name}")
          .thenAssertThat()
          .isNotImplemented
      }
    }

    "return a Not Implemented response on POST" in {
      notImplementedSourceTypes.foreach { sourceType =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
        .when()
          .post(s"/$saUtr/$taxYear/${sourceType.name}", Some(sourceType.example()))
          .thenAssertThat()
          .isNotImplemented
      }
    }

    "return a Not Implemented response on PUT" in {
      notImplementedSourceTypes.foreach { sourceType =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
        .when()
          .post(s"/$saUtr/$taxYear/${sourceType.name}", Some(sourceType.example()))
          .thenAssertThat()
          .isNotImplemented
      }
    }

  }
}
