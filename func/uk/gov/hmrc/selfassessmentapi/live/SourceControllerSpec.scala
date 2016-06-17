package uk.gov.hmrc.selfassessmentapi.live

import org.joda.time.LocalDate
import play.api.libs.json.JsValue
import play.api.libs.json.Json.{parse, toJson}
import uk.gov.hmrc.selfassessmentapi.MongoEmbeddedDatabase
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SelfEmployment
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SelfEmployment._
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SourceType.SelfEmployments
import uk.gov.hmrc.support.BaseFunctionalSpec


class SourceControllerSpec extends BaseFunctionalSpec with MongoEmbeddedDatabase {


  val supportedSourceTypes = Set(SelfEmployments)

  val scenarios = Map(SelfEmployments -> Scenario( input = toJson(SelfEmployment.example().copy(commencementDate = LocalDate.now().plusDays(1))),
                                                         output = parse(
                                                         """
                                                          |[
                                                          | {
                                                          |   "path":"/commencementDate",
                                                          |   "code":"COMMENCEMENT_DATE_NOT_IN_THE_PAST",
                                                          |   "message":"commencement date should be in the past"
                                                          | }
                                                          |]
                                                         """.stripMargin)
                                                )
  )

  override def beforeEach() {
    super.baseBeforeEach()
  }

  override def beforeAll = {
    super.mongoBeforeAll()
    super.baseBeforeAll()
  }

  override def afterAll: Unit = {
    super.mongoAfterAll()
  }

  "Live source controller" should {

    "return a 404 error when source type is invalid" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear/blah")
        .thenAssertThat()
        .statusIs(404)
    }

    "return a 404 error when source with given id does not exist" in {
      supportedSourceTypes.foreach { sourceType =>
        given().userIsAuthorisedForTheResource(saUtr)
          .when()
          .get(s"/$saUtr/$taxYear/${sourceType.name}/asdfasdf")
          .thenAssertThat()
          .statusIs(404)
      }
    }

    "return a 201 response with links to newly created source" in {
      supportedSourceTypes.foreach { sourceType =>
        given().userIsAuthorisedForTheResource(saUtr)
        when()
          .post(s"/$saUtr/$taxYear/${sourceType.name}", Some(sourceType.example()))
          .thenAssertThat()
          .statusIs(201)
          .contentTypeIsHalJson()
          .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/.+".r)
          .bodyHasSummaryLinks(sourceType, saUtr, taxYear)
      }
    }

//    "return a 400 response if the data is invalid" in {
//      invalidExamples.foreach {
//        case (sourceType, example) =>
//          given().userIsAuthorisedForTheResource(saUtr)
//          when()
//            .post(s"/$saUtr/$taxYear/${sourceType.name}", Some(example.input))
//            .thenAssertThat()
//            .statusIs(400)
//            .bodyIs(example.output)
//
//      }
//    }
  }

  case class Scenario(input: JsValue, output: JsValue)
}
