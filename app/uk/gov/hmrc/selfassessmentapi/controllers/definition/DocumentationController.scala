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

import play.api.libs.json.{JsValue, Json}
import play.api.libs.json.Json._
import play.api.mvc.{Action, AnyContent}
import JsonFormatters._
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.controllers.{BaseController, Links}
import uk.gov.hmrc.selfassessmentapi.domain._
import play.api.hal.Hal._
import play.api.hal.HalLink

import scala.xml.{Node, NodeSeq, PCData}

trait DocumentationController extends uk.gov.hmrc.api.controllers.DocumentationController {

  def apiDefinition: Definition

  override def definition() = Action {
    Ok(Json.toJson(apiDefinition))
  }

  lazy val supportedDocs: Map[String, Node] = Map(
    "Create-Summary" -> SummaryDocumentation.createSummary,
    "Update-Summary" -> SummaryDocumentation.updateSummary,
    "Retrieve-Summary" -> SummaryDocumentation.readSummary
  )

  override def documentation(version: String, endpointName: String): Action[AnyContent] = {
    supportedDocs.get(endpointName) match {
      case Some(docs) => Action { Ok(docs).withHeaders("Content-Type" -> "application/xml") }
      case None => super.at(s"/public/api/documentation/$version", s"$endpointName.xml")
    }
  }
}

object DocumentationController extends DocumentationController {

  override val apiDefinition: Definition = AppContext.apiStatus match {
    case "PUBLISHED" => PublishedSelfAssessmentApiDefinition.definition
    case _ => PrototypedSelfAssessmentApiDefinition.definition
  }
}


object SummaryDocumentation extends BaseController with Links {

  override val context: String = AppContext.apiGatewayContext

  val sourceId: SourceId = "5728b53c4800005100d2d32d"
  val summaryId: SourceId = "5728b53c4800005100d2d98a"
  val utr = SaUtr("2234567890")
  val taxYear = TaxYear("2016-17")

  def pathParams(includeSummaryId: Boolean) =
    <section id="path-parameters">
      <title>Path Parameters</title>
      <table>
        <headings>
          <cell>Name</cell>
          <cell>Type</cell>
          <cell>Example</cell>
          <cell>Description</cell>
        </headings>
        <rows>
          <row>
            <cell>
              <code>utr</code>
            </cell>
            <cell>
              <code>String</code>
            </cell>
            <cell>
              <code>
                {utr.utr}
              </code>
            </cell>
            <cell>The Self Assessment UTR of the customer</cell>
          </row>
          <row>
            <cell>
              <code>taxYear</code>
            </cell>
            <cell>
              <code>String</code>
            </cell>
            <cell>
              <code>
                {taxYear.taxYear}
              </code>
            </cell>
            <cell>The tax year the data applies to</cell>
          </row>
          <row>
            <cell>
              <code>source</code>
            </cell>
            <cell>
              <code>String</code>
            </cell>
            <cell>
              <code>self-employments</code>
            </cell>
            <cell>The type of source</cell>
          </row>
          <row>
            <cell>
              <code>sourceId</code>
            </cell>
            <cell>
              <code>String</code>
            </cell>
            <cell>
              <code>
                {sourceId}
              </code>
            </cell>
            <cell>The source id</cell>
          </row>
            <row>
              <cell>
                <code>summary</code>
              </cell>
              <cell>
                <code>String</code>
              </cell>
              <cell>
                <code>incomes</code>
              </cell>
              <cell>The type of summary</cell>
            </row>
          {if(includeSummaryId){
          <row>
              <cell>
                <code>summaryId</code>
              </cell>
              <cell>
                <code>String</code>
              </cell>
              <cell>
                <code>{summaryId}</code>
              </cell>
              <cell>The summary id</cell>
            </row>
        }}
        </rows>
      </table>

      Summary types are specific to the sources:

      <table>
        <headings>
          <cell>Source</cell>
          <cell>Summary types</cell>
        </headings>
        <rows>
          {SourceTypes.types.map { sourceType =>
          <row>
            <cell>
              <code>
                {sourceType.name}
              </code>
            </cell>
            <cell>
              <code>
                {sourceType.summaryTypes.map(_.name).mkString(", ")}
              </code>
            </cell>
          </row>
        }}
        </rows>
      </table>
    </section>

  def requestHeaders(includeContentType: Boolean) =
    <section id="request-headers">
      <title>Request Headers</title>
      <table>
        <headings>
          <cell>Name</cell>
          <cell>Value</cell>
        </headings>
        <rows>
          <row>
            <cell>
              <code>Accept</code>
            </cell>
            <cell>
              <code>application/vnd.hmrc.1.0+json</code>
            </cell>
          </row>
          {if(includeContentType) {
          <row>
            <cell>
              <code>Content-Type</code>
            </cell>
            <cell>
              <code>application/json</code>
            </cell>
          </row>
        }}
        </rows>
      </table>
    </section>

  def authorisation =
    <section id="authorisation">
      <title>Authorisation</title>
      <authorisation>
        <type>USER</type>
        <scope>write:self-assessment</scope>
      </authorisation>
    </section>

  def responseLink = response(obj(), SelfEmploymentsSourceType, IncomesSummaryType)

  def response(jsValue: JsValue, sourceType: SourceType, summaryType: SummaryType) = {
    val hal = halResource(jsValue, Seq(HalLink("self", sourceTypeAndSummaryTypeIdHref(utr, taxYear, sourceType, sourceId, summaryType, summaryId))))
    PCData(Json.prettyPrint(hal.json))
  }


  val createSummary =
    <endpoint>
      <name>Create Summary</name>
      <description>This endpoint creates a summary for the specified source</description>
      <section id="resource">
        <title>Resource</title>
        <resource><![CDATA[POST /self-assessment/{utr}/{taxYear}/{source}/{sourceId}/{summary}]]></resource>
      </section>
      {pathParams(false)}
      {requestHeaders(true)}
      {authorisation}
      {SourceTypes.types.map { sourceType =>
      sourceType.summaryTypes.map { summaryType =>
        summaryWriteRequest("POST", utr, taxYear, sourceType, sourceId, summaryType, None)
      }
    }}
      <section id="sample-response">
        <title>Sample Response</title>
        <httpStatus>201 (CREATED)</httpStatus>
        <json>{responseLink}</json>
      </section>
    </endpoint>

  val updateSummary =
    <endpoint>
      <name>Update Summary</name>
      <description>This endpoint updates a summary for the specified source</description>
      <section id="resource">
        <title>Resource</title>
        <resource>
          <![CDATA[PUT /self-assessment/{utr}/{taxYear}/{source}/{sourceId}/{summary}/{summaryId}]]>
        </resource>
      </section>
      {pathParams(false)}
      {requestHeaders(true)}
      {authorisation}
      {SourceTypes.types.map { sourceType =>
      <section id={s"${sourceType.name}-requests"}>
        {sourceType.summaryTypes.map { summaryType =>
        summaryWriteRequest("PUT", utr, taxYear, sourceType, sourceId, summaryType, Some(summaryId))
      }}
      </section>
    }}
      <section id="sample-response">
        <title>Sample Response</title>
        <httpStatus>200 (OK)</httpStatus>
        <json>{responseLink}</json>
      </section>
    </endpoint>

  val readSummary =
    <endpoint>
      <name>Retrieve Summary</name>
      <description>This endpoint rerieves a summary for the specified source</description>
      <section id="resource">
        <title>Resource</title>
        <resource><![CDATA[GET /self-assessment/{utr}/{taxYear}/{source}/{sourceId}/{summary}/{summaryId}]]></resource>
      </section>
      {pathParams(true)}
      {requestHeaders(false)}
      {authorisation}
      {SourceTypes.types.map { sourceType =>
      sourceType.summaryTypes.map { summaryType =>
        summaryGetResponse(utr, taxYear, sourceType, sourceId, summaryType)
      }
    }}
    </endpoint>

  def summaryWriteRequest(method: String, utr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryType: SummaryType, summaryId: Option[String]) =
    <section id={s"sample-request-${sourceType.name}-${summaryType.name}"}>
      <title>{summaryType.title} Request</title>
      <resource>{method} /self-assessment/{utr.utr}/{taxYear.taxYear}/{sourceType.name}/{sourceId}/{summaryType.name}{summaryId.map(x => s"/$x").getOrElse("")}</resource>
      <description>{summaryType.description(if(method=="POST") "Creates" else "Updates")}</description>
      <json>{PCData(Json.prettyPrint(summaryType.example))}</json>{if (summaryType.fieldDescriptions.nonEmpty) {
      <table>
        <headings>
          <cell>Source</cell>
          <cell>Name</cell>
          <cell>Type</cell>
          <cell>Example</cell>
          <cell>Description</cell>
          <cell>Optional</cell>
        </headings>
        <rows>
          {summaryType.fieldDescriptions.foldLeft[NodeSeq](NodeSeq.Empty)((acc, cur) =>
          acc ++
            <row>
              <cell><code>{cur.source}</code></cell>
              <cell><code>{cur.name}</code></cell>
              <cell><code>{cur.`type`}</code></cell>
              <cell><code>{cur.example}</code></cell>
              <cell><code>{cur.description}</code></cell>
              <cell><code>{cur.optional}</code></cell>
            </row>
        )}
        </rows>
      </table>
    }}
    </section>

  def summaryGetResponse(utr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryType: SummaryType) =
    <section id={s"sample-response-${sourceType.name}-${summaryType.name}"}>
      <title>{summaryType.title} Response</title>
      <httpStatus>200 (OK)</httpStatus>

      <json>{response(summaryType.example, sourceType, summaryType)}</json>
    </section>
}