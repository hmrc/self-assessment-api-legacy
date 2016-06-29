package uk.gov.hmrc.selfassessmentapi.live

import play.api.libs.json.JsValue
import play.api.libs.json.Json.{parse, toJson}
import uk.gov.hmrc.selfassessmentapi.controllers.live.SummaryController
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SelfEmployment
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SourceType.SelfEmployments
import uk.gov.hmrc.selfassessmentapi.repositories.live.SelfEmploymentMongoRepository
import uk.gov.hmrc.support.BaseFunctionalSpec

class SummaryControllerSpec extends BaseFunctionalSpec {

  private val seRepository = new SelfEmploymentMongoRepository
  private val supportedSummaryTypes = SummaryController.supportedSummaryTypes

  private lazy val createSource: Map[SourceType, String] = Map(
    SelfEmployments -> await(seRepository.create(saUtr, TaxYear(taxYear), SelfEmployment.example()))
  )

  private def createSummary(sourceType: SourceType, summaryType: SummaryType, sourceId: SourceId) =
   Map[SourceType, Map[SummaryType, Option[String]]] (
      SelfEmployments -> Map(selfemployment.SummaryTypes.Incomes -> await(seRepository.IncomeRepository.create(saUtr, TaxYear(taxYear), sourceId, selfemployment.Income.example())),
                             selfemployment.SummaryTypes.Expenses -> await(seRepository.ExpenseRepository.create(saUtr, TaxYear(taxYear), sourceId, selfemployment.Expense.example())))
   )(sourceType)(summaryType)


  private def modifiedSummaryFor(sourceType: SourceType,summaryType: SummaryType) =
    Map[SourceType, Map[SummaryType, JsValue]](
      SelfEmployments -> Map(selfemployment.SummaryTypes.Incomes -> toJson(selfemployment.Income.example().copy(amount = 7000)),
                             selfemployment.SummaryTypes.Expenses -> toJson(selfemployment.Expense.example().copy(amount = 7000))
                         )
    )(sourceType)(summaryType)


  private val errorScenarios = Map[SourceType, Map[SummaryType, Set[Scenario]]](
    SelfEmployments -> Map(
      selfemployment.SummaryTypes.Incomes -> Set(Scenario(input = parse(
                                                  s"""
                                                     |{
                                                     | "type" : "TurnRover",
                                                     | "amount" : 1234.05
                                                     |}
                                                   """.stripMargin),
                                                  output = Output(parse(
                                                    s"""
                                                       |[
                                                       |  {
                                                       |    "path": "/type",
                                                       |    "code": "NO_VALUE_FOUND",
                                                       |    "message": "Self Employment Income type is invalid"
                                                       |  }
                                                       |]
                                                     """.stripMargin),
                                                    code = 400))
      ),

      selfemployment.SummaryTypes.Expenses -> Set(Scenario(input = parse(
                                                    s"""
                                                       |{
                                                       | "type" : "StaffCostaDrinks",
                                                       | "amount" : 1234.05
                                                       |}
                                                     """.stripMargin),
                                                    output = Output(parse(
                                                      s"""
                                                         |[
                                                         |  {
                                                         |    "path": "/type",
                                                         |    "code": "NO_VALUE_FOUND",
                                                         |    "message": "Self Employment Expense type is invalid"
                                                         |  }
                                                         |]
                                                       """.stripMargin),
                                                      code = 400))
      )
    )
  )


  "Live summary controller" should {

    "return 404 error when summary is not found" in {
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

    "return 201 response with links if POST is successful" in {
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

    "return 400 and an error response if the data for POST is invalid" in {
      supportedSummaryTypes.foreach {
        case (sourceType, summaryTypes) =>
          val sourceId = createSource(sourceType)
          summaryTypes.foreach { summaryType =>
            errorScenarios(sourceType)(summaryType).foreach { scenario =>
              given().userIsAuthorisedForTheResource(saUtr)
              when()
                .post(s"/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}", Some(scenario.input))
                .thenAssertThat()
                .statusIs(scenario.output.code)
                .bodyIs(scenario.output.body)
            }
          }
      }
    }

    "return 400 and an error response if the data for PUT is invalid" in {
      supportedSummaryTypes.foreach {
        case (sourceType, summaryTypes) =>
          val sourceId = createSource(sourceType)
          summaryTypes.foreach { summaryType =>
            errorScenarios(sourceType)(summaryType).foreach { scenario =>
              createSummary(sourceType, summaryType, sourceId) match {
                case Some(summaryId) =>
                  given().userIsAuthorisedForTheResource(saUtr)
                    .when()
                    .put(s"/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}/$summaryId", Some(scenario.input))
                    .thenAssertThat()
                    .statusIs(400)
                    .bodyIs(scenario.output.body)
                case None => fail(s"Unable to create summary ${summaryType.name} for source: ${sourceType.name}")
              }
            }
          }
      }
    }

    "return 200 a response with summary details for GET " in {
      supportedSummaryTypes.foreach {
        case (sourceType, summaryTypes) =>
          val sourceId = createSource(sourceType)
          summaryTypes.foreach { summaryType =>
            createSummary(sourceType, summaryType, sourceId) match {
              case Some(summaryId) =>
                given().userIsAuthorisedForTheResource(saUtr)
                  .when()
                  .get(s"/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}/$summaryId")
                  .thenAssertThat()
                  .statusIs(200)
              case None => fail(s"Unable to create summary ${summaryType.name} for source: ${sourceType.name}")
            }
          }
      }
    }

    "return 200 code with links if PUT is successful" in {
      supportedSummaryTypes.foreach {
        case (sourceType, summaryTypes) =>
          val sourceId = createSource(sourceType)
          summaryTypes.foreach { summaryType =>
            createSummary(sourceType, summaryType, sourceId) match {
              case Some(summaryId) =>
                given().userIsAuthorisedForTheResource(saUtr)
                  .when()
                  .put(s"/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}/$summaryId", Some(modifiedSummaryFor(sourceType, summaryType)))
                  .thenAssertThat()
                  .statusIs(200)
                  .contentTypeIsHalJson()
                  .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}/$summaryId")
              case None => fail(s"Unable to create summary ${summaryType.name} for source: ${sourceType.name}")
            }
          }
      }
    }

    "return 204 code when DELETE is successful" in {
      supportedSummaryTypes.foreach {
        case (sourceType, summaryTypes) =>
          val sourceId = createSource(sourceType)
          summaryTypes.foreach { summaryType =>
            createSummary(sourceType, summaryType, sourceId) match {
              case Some(summaryId) =>
                given().userIsAuthorisedForTheResource(saUtr)
                  .when()
                  .delete(s"/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}/$summaryId")
                  .thenAssertThat()
                  .statusIs(204)
              case None => fail(s"Unable to create summary ${summaryType.name} for source: ${sourceType.name}")
            }
          }
      }
    }
  }
}
