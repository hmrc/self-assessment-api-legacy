package uk.gov.hmrc.selfassessmentapi.live

import uk.gov.hmrc.selfassessmentapi.controllers.live.SummaryController
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SourceType.SelfEmployments
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SummaryTypes.Incomes
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.{Income, SelfEmployment}
import uk.gov.hmrc.selfassessmentapi.repositories.live.SelfEmploymentMongoRepository
import uk.gov.hmrc.support.BaseFunctionalSpec

class SummaryControllerSpec extends BaseFunctionalSpec {

  private val seRepository = new SelfEmploymentMongoRepository
  private val seIncomeRepository = seRepository.IncomeRepository
  private val supportedSummaryTypes = SummaryController.supportedSummaryTypes

  private lazy val createSource: Map[SourceType, String] = Map(
    SelfEmployments -> await(seRepository.create(saUtr, TaxYear(taxYear), SelfEmployment.example()))
  )

  private def createSummary(summaryType: SummaryType, sourceId: SourceId) = Map[SummaryType, Option[String]](
    Incomes -> await(seIncomeRepository.create(saUtr, TaxYear(taxYear), sourceId, Income.example()))
  )(summaryType)



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
  }
}
