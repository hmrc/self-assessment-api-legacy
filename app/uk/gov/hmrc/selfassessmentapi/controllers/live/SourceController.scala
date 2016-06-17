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
import play.api.libs.json.Json.toJson
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.FeatureSwitchAction
import uk.gov.hmrc.selfassessmentapi.controllers.ErrorNotImplemented
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SourceType.SelfEmployments
import uk.gov.hmrc.selfassessmentapi.domain.{SourceId, SourceType, TaxYear}

import scala.concurrent.Future

object SourceController extends uk.gov.hmrc.selfassessmentapi.controllers.SourceController with SourceTypeSupport {

  // this whole implementation can be deleted (defaulted to the super class implementation) once all sources are supported

  val supportedLiveSourceTypes: Set[SourceType] = Set(SelfEmployments)

  private def toJsValue(sourceType: SourceType)(f: => Action[JsValue]) = {
    if (supportedLiveSourceTypes.contains(sourceType)) f()
    else FeatureSwitchAction(sourceType).async(parse.json) {request => Future.successful(NotImplemented(toJson(ErrorNotImplemented)))}
  }

  private def toAnyContent(sourceType: SourceType)(f: => Action[AnyContent]) = {
    if (supportedLiveSourceTypes.contains(sourceType)) f()
    else FeatureSwitchAction(sourceType).async {Future.successful(NotImplemented(toJson(ErrorNotImplemented)))}
  }

  override def create(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType) = toJsValue(sourceType) {
    super.create(saUtr, taxYear, sourceType)
  }

  override def update(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId) = toJsValue(sourceType) {
    super.update(saUtr, taxYear, sourceType, sourceId)
  }

  override def read(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId) = toAnyContent(sourceType) {
    super.read(saUtr, taxYear, sourceType, sourceId)
  }

  override def delete(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId) = toAnyContent(sourceType) {
    super.delete(saUtr, taxYear, sourceType, sourceId)
  }

  override def list(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType) = toAnyContent(sourceType) {
    super.list(saUtr, taxYear, sourceType)
  }
}
