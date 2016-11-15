/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.selfassessmentapi.controllers.definition
import play.api.http.LazyHttpErrorHandler
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import play.twirl.api.Xml
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.definition.JsonFormatters._
import uk.gov.hmrc.selfassessmentapi.controllers.{BaseController, Links}
import uk.gov.hmrc.selfassessmentapi.views.Helpers

abstract class DocumentationController extends uk.gov.hmrc.api.controllers.DocumentationController(LazyHttpErrorHandler) {

  def apiDefinition: Definition

  override def definition() = Action {
    Ok(Json.toJson(apiDefinition))
  }

  override def documentation(version: String, endpointName: String): Action[AnyContent] = Action {
    Documentation.findDocumentation(endpointName) match {
      case Some(docs) => Ok(docs).withHeaders("Content-Type" -> "application/xml")
      case None => NotFound
    }
  }
}

object DocumentationController extends DocumentationController {

  override val apiDefinition: Definition = AppContext.apiStatus match {
    case "PUBLISHED" => PublishedSelfAssessmentApiDefinition.definition
    case _ => PrototypedSelfAssessmentApiDefinition.definition
  }
}


object Documentation extends BaseController with Links {

  case class EndpointDocumentation(name: String, view: Xml)

  override val context: String = AppContext.apiGatewayLinkContext

  private val sourceId: SourceId = "00d2d32d"
  private val summaryId: SourceId = "00d2d98a"
  private val nino = Nino("AA123456A")
  private val taxYear = TaxYear("2016-17")

  private lazy val updateTaxYearPropertiesPage = if (FeatureSwitchedTaxYearProperties.atLeastOnePropertyIsEnabled) {
    Seq(EndpointDocumentation("Update Tax Year", uk.gov.hmrc.selfassessmentapi.views.xml.updateTaxYear(nino, taxYear)))
  } else Seq.empty[EndpointDocumentation]

  private lazy val sourceAndSummaryDocumentation = Helpers.enabledSourceTypes.toSeq.flatMap { sourceType =>
    val updateEndpoint =  sourceType match  {
      case SourceTypes.Benefits | SourceTypes.Banks | SourceTypes.Dividends =>  Nil
      case _ => Seq(EndpointDocumentation(s"Update ${sourceType.documentationName}", uk.gov.hmrc.selfassessmentapi.views.xml.updateSource(nino, taxYear, sourceType, sourceId)))
    }
    Seq(
      EndpointDocumentation(s"Create ${sourceType.documentationName}", uk.gov.hmrc.selfassessmentapi.views.xml.createSource(nino, taxYear, sourceType, sourceId)),
      EndpointDocumentation(s"Retrieve ${sourceType.documentationName}", uk.gov.hmrc.selfassessmentapi.views.xml.readSource(nino, taxYear, sourceType, sourceId))
    ) ++ updateEndpoint ++
    Seq(
      EndpointDocumentation(s"Delete ${sourceType.documentationName}", uk.gov.hmrc.selfassessmentapi.views.xml.deleteSource(nino, taxYear, sourceType, sourceId)),
      EndpointDocumentation(s"Retrieve All ${sourceType.documentationName}", uk.gov.hmrc.selfassessmentapi.views.xml.listSources(nino, taxYear, sourceType, sourceId))
    ) ++ summaryDocumentation(sourceType)
  }

  private lazy val summaryDocumentation: SourceType => Seq[EndpointDocumentation] = { sourceType =>
    Helpers.enabledSummaries(sourceType).toSeq.flatMap { summaryType =>
      Seq(
        EndpointDocumentation(s"Create ${sourceType.documentationName} ${summaryType.documentationName}", uk.gov.hmrc.selfassessmentapi.views.xml.createSummary(nino, taxYear, sourceType, summaryType, sourceId, summaryId)),
        EndpointDocumentation(s"Retrieve ${sourceType.documentationName} ${summaryType.documentationName}", uk.gov.hmrc.selfassessmentapi.views.xml.readSummary(nino, taxYear, sourceType, summaryType, sourceId, summaryId)),
        EndpointDocumentation(s"Update ${sourceType.documentationName} ${summaryType.documentationName}", uk.gov.hmrc.selfassessmentapi.views.xml.updateSummary(nino, taxYear, sourceType, summaryType, sourceId, summaryId)),
        EndpointDocumentation(s"Delete ${sourceType.documentationName} ${summaryType.documentationName}", uk.gov.hmrc.selfassessmentapi.views.xml.deleteSummary(nino, taxYear, sourceType, summaryType, sourceId, summaryId)),
        EndpointDocumentation(s"Retrieve All ${sourceType.documentationName} ${summaryType.documentationName}", uk.gov.hmrc.selfassessmentapi.views.xml.listSummaries(nino, taxYear, sourceType, summaryType, sourceId, summaryId))
      )
    }
  }

  private lazy val documentation =  Seq(EndpointDocumentation("Resolve Taxpayer", uk.gov.hmrc.selfassessmentapi.views.xml.resolveTaxpayer(nino)),
      EndpointDocumentation("Discover Tax Years", uk.gov.hmrc.selfassessmentapi.views.xml.discoverTaxYears(nino, taxYear)),
      EndpointDocumentation("Discover Tax Year", uk.gov.hmrc.selfassessmentapi.views.xml.discoverTaxYear(nino, taxYear))) ++ updateTaxYearPropertiesPage   ++ sourceAndSummaryDocumentation ++
      Seq(EndpointDocumentation("Request Liability", uk.gov.hmrc.selfassessmentapi.views.xml.createLiability(nino, taxYear)),
      EndpointDocumentation("Retrieve Liability", uk.gov.hmrc.selfassessmentapi.views.xml.readLiability(nino, taxYear)))

  private lazy val documentationByName = documentation.map(x => x.name -> x.view).toMap

  def findDocumentation(endpointName: String): Option[Xml] = documentationByName.get(endpointName)
}
