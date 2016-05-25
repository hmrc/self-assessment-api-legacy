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

package uk.gov.hmrc.selfassessmentapi.views

import play.api.hal.Hal._
import play.api.hal.HalLink
import play.api.libs.json.Json._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.controllers.{HalSupport, Links}
import uk.gov.hmrc.selfassessmentapi.domain._

import scala.xml.PCData

object Helpers extends HalSupport with Links {

  override val context: String = AppContext.apiGatewayContext

  def sourceTypeAndSummaryTypeResponse(utr: SaUtr, taxYear: TaxYear,  sourceId: SourceId, summaryId: SummaryId) =
    sourceTypeAndSummaryTypeIdResponse(obj(), utr, taxYear, SelfEmploymentsSourceType, sourceId, IncomesSummaryType, summaryId)

  def sourceTypeAndSummaryTypeIdResponse(jsValue: JsValue, utr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryType: SummaryType, summaryId: SummaryId) = {
    val hal = halResource(jsValue, Seq(HalLink("self", sourceTypeAndSummaryTypeIdHref(utr, taxYear, sourceType, sourceId, summaryType, summaryId))))
    prettyPrint(hal.json)
  }

  def sourceLinkResponse(utr: SaUtr, taxYear: TaxYear, sourceId: SourceId) = {
    sourceModelResponse(obj(), utr, taxYear, SelfEmploymentsSourceType, sourceId)
  }

  def sourceModelResponse(jsValue: JsValue, utr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId) = {
    val hal = halResource(jsValue, sourceLinks(utr, taxYear, sourceType, sourceId))
    prettyPrint(hal.json)
  }

  def sourceTypeAndSummaryTypeIdListResponse(utr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryType: SummaryType, summaryId: SummaryId) = {
    val json = toJson(Seq(summaryId, summaryId, summaryId).map(id => halResource(obj(),
      Seq(HalLink("self", sourceTypeAndSummaryTypeIdHref(utr, taxYear, sourceType, sourceId, summaryType, id))))))
    val hal = halResourceList(summaryType.name, json, sourceTypeAndSummaryTypeHref(utr, taxYear, sourceType, sourceId, summaryType))
    PCData(Json.prettyPrint(hal.json))
  }

  def sourceTypeIdListResponse(utr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId) = {
    val json = toJson(Seq(sourceId, sourceId, sourceId).map(id => halResource(obj(),
      Seq(HalLink("self", sourceIdHref(utr, taxYear, sourceType, id))))))
    val hal = halResourceList(sourceType.name, json, sourceHref(utr, taxYear, sourceType))
    prettyPrint(hal.json)
  }

  def resolveCustomerResponse(utr: SaUtr) = {
    val hal = halResource(obj(), Seq(HalLink("self-assessment", discoverTaxYearsHref(utr))))
    prettyPrint(hal.json)
  }

  def discoverTaxYearsResponse(utr: SaUtr, taxYear: TaxYear) = {
    val hal = halResource(obj(), Seq(HalLink("self", discoverTaxYearsHref(utr)), HalLink(taxYear.taxYear, discoverTaxYearHref(utr, taxYear))))
    prettyPrint(hal.json)
  }

  def discoverTaxYearResponse(utr: SaUtr, taxYear: TaxYear) = {
    val sourceLinks = SourceTypes.types.map(sourceType => HalLink(sourceType.name, sourceHref(utr, taxYear, sourceType)))
    val links = sourceLinks :+ HalLink("liabilities", liabilitiesHref(utr, taxYear)) :+ HalLink("self", discoverTaxYearHref(utr, taxYear))
    val hal = halResource(obj(), links)
    prettyPrint(hal.json)
  }

  def prettyPrint(jsValue: JsValue): PCData =
    PCData(Json.prettyPrint(jsValue))

}
