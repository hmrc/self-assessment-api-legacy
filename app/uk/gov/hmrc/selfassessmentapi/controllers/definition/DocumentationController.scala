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
import uk.gov.hmrc.selfassessmentapi.domain._

trait DocumentationController extends uk.gov.hmrc.api.controllers.DocumentationController {

  def apiDefinition: Definition

  override def definition() = Action {
    Ok(Json.toJson(apiDefinition))
  }

  lazy val supportedDocs: Map[String, Xml] = Map(
    "Create Summary" -> Documentation.createSummary,
    "Retrieve Summary" -> Documentation.readSummary,
    "Update Summary" -> Documentation.updateSummary,
    "Delete Summary" -> Documentation.deleteSummary,
    "Retrieve Summaries" -> Documentation.listSummaries,
    "Create Source" -> Documentation.createSource,
    "Retrieve Source" -> Documentation.readSource,
    "Delete Source" -> Documentation.deleteSource,
    "Update Source" -> Documentation.updateSource,
    "Retrieve Sources" -> Documentation.listSources,
    "Resolve Customer" -> Documentation.resolveCustomer,
    "Discover Tax Years" -> Documentation.discoverTaxYears,
    "Discover Tax Year" -> Documentation.discoverTaxYear
  )

  override def documentation(version: String, endpointName: String): Action[AnyContent] = {
    supportedDocs.get(endpointName) match {
      case Some(docs) => Action { Ok(docs).withHeaders("Content-Type" -> "application/xml") }
      case None => super.at(s"/public/api/documentation/$version", s"${endpointName.replaceAll(" ", "-")}.xml")
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

  override val context: String = AppContext.apiGatewayContext

  val sourceId: SourceId = "5728b53c4800005100d2d32d"
  val summaryId: SourceId = "5728b53c4800005100d2d98a"
  val utr = SaUtr("2234567890")
  val taxYear = TaxYear("2016-17")


  val resolveCustomer: Xml = uk.gov.hmrc.selfassessmentapi.views.xml.resolveCustomer(utr)
  val discoverTaxYears: Xml = uk.gov.hmrc.selfassessmentapi.views.xml.discoverTaxYears(utr, taxYear)
  val discoverTaxYear: Xml = uk.gov.hmrc.selfassessmentapi.views.xml.discoverTaxYear(utr, taxYear)

  val createSummary: Xml = uk.gov.hmrc.selfassessmentapi.views.xml.createSummary(utr, taxYear, sourceId, summaryId)
  val updateSummary: Xml = uk.gov.hmrc.selfassessmentapi.views.xml.updateSummary(utr, taxYear, sourceId, summaryId)
  val readSummary: Xml = uk.gov.hmrc.selfassessmentapi.views.xml.readSummary(utr, taxYear, sourceId, summaryId)
  val deleteSummary: Xml = uk.gov.hmrc.selfassessmentapi.views.xml.deleteSummary(utr, taxYear, sourceId, summaryId)
  val listSummaries: Xml = uk.gov.hmrc.selfassessmentapi.views.xml.listSummaries(utr, taxYear, sourceId, summaryId)


  val createSource: Xml = uk.gov.hmrc.selfassessmentapi.views.xml.createSource(utr, taxYear, sourceId)
  val readSource: Xml = uk.gov.hmrc.selfassessmentapi.views.xml.readSource(utr, taxYear, sourceId)
  val updateSource: Xml = uk.gov.hmrc.selfassessmentapi.views.xml.updateSource(utr, taxYear, sourceId)
  val deleteSource: Xml = uk.gov.hmrc.selfassessmentapi.views.xml.deleteSource(utr, taxYear, sourceId)
  val listSources: Xml = uk.gov.hmrc.selfassessmentapi.views.xml.listSources(utr, taxYear, sourceId)

}