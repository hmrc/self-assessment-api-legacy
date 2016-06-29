package uk.gov.hmrc.selfassessmentapi.live

import org.joda.time.LocalDate
import play.api.libs.json.Json._
import uk.gov.hmrc.selfassessmentapi.controllers.live.SummaryController
import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SourceType.SelfEmployments
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SummaryTypes.Incomes
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.{Income, SelfEmployment}
import uk.gov.hmrc.selfassessmentapi.repositories.SummaryRepository
import uk.gov.hmrc.selfassessmentapi.repositories.live.SelfEmploymentMongoRepository
import uk.gov.hmrc.support.BaseFunctionalSpec

class SummaryControllerSpec extends BaseFunctionalSpec {

  private val seRepository: SelfEmploymentMongoRepository = new SelfEmploymentMongoRepository
  private val seIncomeRepository: SummaryRepository[Income] = seRepository.IncomeRepository
  private val supportedSummaryTypes = SummaryController.supportedSummaryTypes

  lazy val createSource: Map[SourceType, String] = Map(
    SelfEmployments -> await(seRepository.create(saUtr, TaxYear(taxYear), SelfEmployment.example()))
  )

  def createSummary(summaryType: SummaryType, sourceId: SourceId) = Map[SummaryType, Option[String]](
    Incomes -> await(seIncomeRepository.create(saUtr, TaxYear(taxYear), sourceId, Income.example()))
  )(summaryType)



//  val errorScenarios : Map[SourceType, Map[SummaryType, Set[Scenario]]] = Map(
//    SelfEmployments -> Scenario( input = toJson(SelfEmployment.example().copy(commencementDate = LocalDate.now().plusDays(1))),
//      output = Output(
//        body = parse(
//          s"""
//             |[
//             | {
//             |   "path":"/commencementDate",
//             |   "code": "${COMMENCEMENT_DATE_NOT_IN_THE_PAST}",
//             |   "message":"commencement date should be in the past"
//             | }
//             |]
//          """.stripMargin),
//        code = 400
//      )
//    )
//  )
//


  "Live summary controller" should {

    "return a 404 error when summary is not found" in {
     supportedSummaryTypes.foreach {
        case (sourceType, summaryTypes) =>

          val sourceId = createSource(sourceType)

          summaryTypes.foreach { summaryType =>

            given().userIsAuthorisedForTheResource(saUtr)
              .when()
              .get(s"/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}/23452345")
              .thenAssertThat()
              .statusIs(404)

            given().userIsAuthorisedForTheResource(saUtr)
              .when()
              .put(s"/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}/23452345", Some(summaryType.example()))
              .thenAssertThat()
              .statusIs(404)

            given().userIsAuthorisedForTheResource(saUtr)
              .when()
              .delete(s"/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}/23452345")
              .thenAssertThat()
              .statusIs(404)
          }
      }
    }

    "return a 201 response with links if POST is successful" in {
      supportedSummaryTypes.foreach {
        case (sourceType, summaryTypes) =>
          val sourceId = createSource(sourceType)
          summaryTypes.foreach { summaryType =>
            given().userIsAuthorisedForTheResource(saUtr)
            when()
              .post(s"/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}", Some(summaryType.example()))
              .thenAssertThat()
              .statusIs(201)
              .contentTypeIsHalJson()
              .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}/.+".r)
          }
      }
    }

//    "return 400 and an error response if the data for POST is invalid" in {
//      supportedSummaryTypes.foreach {
//        case (sourceType, summaryTypes) =>
//        given().userIsAuthorisedForTheResource(saUtr)
//        when()
//          .post(s"/$saUtr/$taxYear/${sourceType.name}", Some(errorScenarios(sourceType).input))
//          .thenAssertThat()
//          .statusIs(errorScenarios(sourceType).output.code)
//          .bodyIs(errorScenarios(sourceType).output.body)
//      }
//    }

    //
    //    "return 200 code with links if PUT is successful" in {
    //      supportedSourceTypes.foreach { sourceType =>
    //        val seId = createSource(sourceType)
    //        given().userIsAuthorisedForTheResource(saUtr)
    //        when()
    //          .put(s"/$saUtr/$taxYear/${sourceType.name}/$seId", Some(toJson(SelfEmployment.example().copy(commencementDate = LocalDate.now().minusDays(1)))))
    //          .thenAssertThat()
    //          .statusIs(200)
    //          .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/.+".r)
    //          .bodyHasSummaryLinks(sourceType, saUtr, taxYear)
    //      }
    //    }
    //
    //    "return 204 code when DELETE is successful" in {
    //      supportedSourceTypes.foreach { sourceType =>
    //        val seId = createSource(sourceType)
    //        given().userIsAuthorisedForTheResource(saUtr)
    //        when()
    //          .delete(s"/$saUtr/$taxYear/${sourceType.name}/$seId")
    //          .thenAssertThat()
    //          .statusIs(204)
    //      }
    //    }
    //
    //    "return 400 and an error response if the data for PUT is invalid" in {
    //      supportedSourceTypes.foreach { sourceType =>
    //        val seId = createSource(sourceType)
    //        given().userIsAuthorisedForTheResource(saUtr)
    //        when()
    //          .put(s"/$saUtr/$taxYear/${sourceType.name}/$seId", Some(errorScenarios(sourceType).input))
    //          .thenAssertThat()
    //          .statusIs(errorScenarios(sourceType).output.code)
    //          .bodyIs(errorScenarios(sourceType).output.body)
    //      }
    //    }
  }
}
