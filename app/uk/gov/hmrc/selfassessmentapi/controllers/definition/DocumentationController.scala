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

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import play.twirl.api.Xml
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.controllers.definition.JsonFormatters._
import uk.gov.hmrc.selfassessmentapi.controllers.{BaseController, Links}
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.api.TaxYearProperties

trait DocumentationController extends uk.gov.hmrc.api.controllers.DocumentationController {

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
  private val utr = SaUtr("2234567890")
  private val taxYear = TaxYear("2016-17")

  private lazy val updateTaxYearPropertiesPage = if (TaxYearProperties.atLeastOnePropertyIsEnabled) {
    Seq(EndpointDocumentation("Update Tax Year", uk.gov.hmrc.selfassessmentapi.views.xml.updateTaxYear(utr, taxYear)))
  } else Seq.empty[EndpointDocumentation]

  private lazy val documentation = updateTaxYearPropertiesPage ++ Seq(
      EndpointDocumentation("Resolve Customer", uk.gov.hmrc.selfassessmentapi.views.xml.resolveCustomer(utr)),

      EndpointDocumentation("Discover Tax Years", uk.gov.hmrc.selfassessmentapi.views.xml.discoverTaxYears(utr, taxYear)),
      EndpointDocumentation("Discover Tax Year", uk.gov.hmrc.selfassessmentapi.views.xml.discoverTaxYear(utr, taxYear)),

      EndpointDocumentation("Create Summary", uk.gov.hmrc.selfassessmentapi.views.xml.createSummary(utr, taxYear, sourceId, summaryId)),
      EndpointDocumentation("Retrieve Summary", uk.gov.hmrc.selfassessmentapi.views.xml.readSummary(utr, taxYear, sourceId, summaryId)),
      EndpointDocumentation("Update Summary", uk.gov.hmrc.selfassessmentapi.views.xml.updateSummary(utr, taxYear, sourceId, summaryId)),
      EndpointDocumentation("Delete Summary", uk.gov.hmrc.selfassessmentapi.views.xml.deleteSummary(utr, taxYear, sourceId, summaryId)),
      EndpointDocumentation("Retrieve Summaries", uk.gov.hmrc.selfassessmentapi.views.xml.listSummaries(utr, taxYear, sourceId, summaryId)),

      EndpointDocumentation("Create Source", uk.gov.hmrc.selfassessmentapi.views.xml.createSource(utr, taxYear, sourceId)),
      EndpointDocumentation("Retrieve Source", uk.gov.hmrc.selfassessmentapi.views.xml.readSource(utr, taxYear, sourceId)),
      EndpointDocumentation("Update Source", uk.gov.hmrc.selfassessmentapi.views.xml.updateSource(utr, taxYear, sourceId)),
      EndpointDocumentation("Delete Source", uk.gov.hmrc.selfassessmentapi.views.xml.deleteSource(utr, taxYear, sourceId)),
      EndpointDocumentation("Retrieve Sources", uk.gov.hmrc.selfassessmentapi.views.xml.listSources(utr, taxYear, sourceId)),

      EndpointDocumentation("Request Liability", uk.gov.hmrc.selfassessmentapi.views.xml.createLiability(utr, taxYear)),
      EndpointDocumentation("Retrieve Liability", uk.gov.hmrc.selfassessmentapi.views.xml.readLiability(utr, taxYear)))

  private lazy val documentationByName = documentation.map(x => x.name -> x.view).toMap

  def findDocumentation(endpointName: String): Option[Xml] = documentationByName.get(endpointName)
}
