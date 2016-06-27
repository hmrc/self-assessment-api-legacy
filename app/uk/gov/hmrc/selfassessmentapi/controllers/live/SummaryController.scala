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

package uk.gov.hmrc.selfassessmentapi.controllers.live

import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.{FeatureSwitchAction, domain}
import uk.gov.hmrc.selfassessmentapi.controllers.ErrorNotImplemented
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SourceType.SelfEmployments

import scala.concurrent.Future
object SummaryController extends uk.gov.hmrc.selfassessmentapi.controllers.SummaryController with SourceTypeSupport {

  // this whole implementation can be deleted (defaulted to the super class implementation) once all sources are supported

  private val supportedLiveTypes =
    Map[SourceType, Set[String]](SelfEmployments -> Set(domain.selfemployment.SummaryTypes.Incomes.name)).withDefaultValue(Set())

  private def toJsValue(sourceType: SourceType, summaryTypeName: String)(f: => Action[JsValue]) = {
    supportedLiveTypes(sourceType).contains(summaryTypeName) match {
      case true  => f()
      case false => FeatureSwitchAction(sourceType, summaryTypeName).async(parse.json) {
        _ => Future.successful(NotImplemented(toJson(ErrorNotImplemented)))
      }
    }
  }

  private def toAnyContent(sourceType: SourceType, summaryTypeName: String)(f: => Action[AnyContent]) = {
    supportedLiveTypes(sourceType).contains(summaryTypeName) match {
      case true  => f()
      case false => FeatureSwitchAction(sourceType, summaryTypeName).async {
        _ => Future.successful(NotImplemented(toJson(ErrorNotImplemented)))
      }
    }
  }

  override def create(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String) =
    toJsValue(sourceType, summaryTypeName) {
      super.create(saUtr, taxYear, sourceType, sourceId, summaryTypeName)
    }

  override def read(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String, summaryId: SummaryId) =
    toAnyContent(sourceType, summaryTypeName) {
      super.read(saUtr, taxYear, sourceType, sourceId, summaryTypeName, summaryId)
    }

  override def update(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String, summaryId: SummaryId) =
    toJsValue(sourceType, summaryTypeName) {
      super.update(saUtr, taxYear, sourceType, sourceId, summaryTypeName, summaryId)
    }

  override def delete(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String, summaryId: SummaryId) =
    toAnyContent(sourceType, summaryTypeName) {
      super.delete(saUtr, taxYear, sourceType, sourceId, summaryTypeName, summaryId)
    }

  override def list(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String) =
    toAnyContent(sourceType, summaryTypeName) {
      super.list(saUtr, taxYear, sourceType, sourceId, summaryTypeName)
    }
}
