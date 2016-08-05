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

import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.FeatureSwitchAction
import uk.gov.hmrc.selfassessmentapi.domain.{SourceId, SourceType, TaxYear}

object SourceController extends uk.gov.hmrc.selfassessmentapi.controllers.SourceController with SourceTypeSupport {

  def create(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType) = FeatureSwitchAction(sourceType).async(parse.json) {
    implicit request => super.createSource(request, saUtr, taxYear, sourceType)
  }

  def read(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId) = FeatureSwitchAction(sourceType).async {
    super.readSource(saUtr, taxYear, sourceType, sourceId)
  }

  def update(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId) = FeatureSwitchAction(sourceType).async(parse.json) {
    request => super.updateSource(request, saUtr, taxYear, sourceType, sourceId)
  }

  def delete(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId) = FeatureSwitchAction(sourceType).async {
    super.deleteSource(saUtr, taxYear, sourceType, sourceId)
  }

  def list(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType) = FeatureSwitchAction(sourceType).async {
    super.listSources(saUtr, taxYear, sourceType)
  }
}
