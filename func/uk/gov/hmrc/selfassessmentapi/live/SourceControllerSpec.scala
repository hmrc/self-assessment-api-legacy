package uk.gov.hmrc.selfassessmentapi.live

import org.joda.time.LocalDate
import play.api.libs.json.JsValue
import play.api.libs.json.Json.{parse, toJson}
import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode.COMMENCEMENT_DATE_NOT_IN_THE_PAST
import uk.gov.hmrc.selfassessmentapi.domain.SourceTypes
import uk.gov.hmrc.selfassessmentapi.domain.employment.SourceType.Employments
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SelfEmployment
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SelfEmployment._
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SourceType.SelfEmployments
import uk.gov.hmrc.support.BaseFunctionalSpec

case class Output(body: JsValue, code: Int)

case class Scenario(input: JsValue, output: Output)

class SourceControllerSpec extends BaseFunctionalSpec {

  val supportedSourceTypes = Set(SelfEmployments)
  val notImplementedSourceTypes = Set(Employments, SourceTypes.FurnishedHolidayLettings, SourceTypes.UKProperties,
    SourceTypes.UnearnedIncomes)

  val errorScenarios = Map(
    SelfEmployments -> Scenario(input = toJson(SelfEmployment.example().copy(commencementDate = LocalDate.now().plusDays(1))),
      output = Output(
        body = parse(
          s"""
             |[
             | {
             |   "path":"/commencementDate",
             |   "code": "$COMMENCEMENT_DATE_NOT_IN_THE_PAST",
             |   "message":"commencement date should be in the past"
             | }
             |]
                                          """.stripMargin),
        code = 400
      )
    )
  )

  "I" should {
    "be able to create, update and delete a self assessment source" in {
      supportedSourceTypes.foreach { sourceType =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
        .when()
          .get(s"/$saUtr/$taxYear/${sourceType.name}")
          .thenAssertThat()
          .statusIs(200)
          .butResponseHasNo(sourceType.name)
        when()
          .post(Some(sourceType.example())).to(s"/$saUtr/$taxYear/${sourceType.name}")
          .thenAssertThat()
          .statusIs(201)
          .contentTypeIsHalJson()
          .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/.+".r)
          .bodyHasSummaryLinks(sourceType, saUtr, taxYear)
        .when()
          .get(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%")
          .thenAssertThat()
          .statusIs(200)
          .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/%sourceId%")
        .when()
          .put(Some(toJson(SelfEmployment.example().copy(commencementDate = LocalDate.now()
            .minusDays(1))))).at(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%")
          .thenAssertThat()
          .statusIs(200)
          .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/%sourceId%")
        .when()
          .get(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%")
          .thenAssertThat()
          .statusIs(200)
          .body(_ \ "commencementDate").is(LocalDate.now().minusDays(1).toString("yyyy-MM-dd"))
        .when()
          .delete(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%")
          .thenAssertThat()
          .statusIs(204)
        .when()
          .get(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%")
          .thenAssertThat()
          .statusIs(404)
      }
    }
  }

  "I" should {
    "not be able to get a invalid source type" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
      .when()
        .get(s"/$saUtr/$taxYear/blah")
        .thenAssertThat()
        .statusIs(404)
    }

    "not be able to get a non-existent source" in {
      supportedSourceTypes.foreach { sourceType =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
        .when()
          .get(s"/$saUtr/$taxYear/${sourceType.name}/asdfasdf")
          .thenAssertThat()
          .statusIs(404)

        given()
          .userIsAuthorisedForTheResource(saUtr)
        .when()
          .put(s"/$saUtr/$taxYear/${sourceType.name}/asdfasdf", Some(sourceType.example()))
          .thenAssertThat()
          .statusIs(404)

        given()
            .userIsAuthorisedForTheResource(saUtr)
          .when()
            .delete(s"/$saUtr/$taxYear/${sourceType.name}/asdfasdf")
            .thenAssertThat()
            .statusIs(404)

      }
    }

    "not be able to create a source with invalid data" in {
      supportedSourceTypes.foreach { sourceType =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
        .when()
          .post(s"/$saUtr/$taxYear/${sourceType.name}", Some(errorScenarios(sourceType).input))
          .thenAssertThat()
          .statusIs(errorScenarios(sourceType).output.code)
          .bodyIs(errorScenarios(sourceType).output.body)
      }
    }

    "not be able to update a source with invalid data" in {
      supportedSourceTypes.foreach { sourceType =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
        .when()
          .post(s"/$saUtr/$taxYear/${sourceType.name}", Some(sourceType.example()))
          .thenAssertThat()
          .statusIs(201)
        .when()
          .put(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%", Some(errorScenarios(sourceType).input))
          .thenAssertThat()
          .statusIs(errorScenarios(sourceType).output.code)
          .bodyIs(errorScenarios(sourceType).output.body)
      }
    }

    "not be able to update a non-existent" in {
      supportedSourceTypes.foreach { sourceType =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
        .when()
          .put(s"/$saUtr/$taxYear/${sourceType.name}/non-existent-source", Some(errorScenarios(sourceType).input))
          .thenAssertThat()
          .statusIs(400)
      }
    }

    "not be able to delete a non-existent" in {
      supportedSourceTypes.foreach { sourceType =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
        .when()
          .delete(s"/$saUtr/$taxYear/${sourceType.name}/non-existent-source")
          .thenAssertThat()
          .statusIs(404)
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
          .resourceIsNotImplemented()
      }
    }

    "return a Not Implemented response on POST" in {
      notImplementedSourceTypes.foreach { sourceType =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
        .when()
          .post(s"/$saUtr/$taxYear/${sourceType.name}", Some(sourceType.example()))
          .thenAssertThat()
          .resourceIsNotImplemented()
      }
    }

    "return a Not Implemented response on PUT" in {
      notImplementedSourceTypes.foreach { sourceType =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
        .when()
          .post(s"/$saUtr/$taxYear/${sourceType.name}", Some(sourceType.example()))
          .thenAssertThat()
          .resourceIsNotImplemented()
      }
    }

  }
}
