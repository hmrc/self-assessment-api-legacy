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
import uk.gov.hmrc.selfassessmentapi.domain._

object SummaryController extends uk.gov.hmrc.selfassessmentapi.controllers.SummaryController with SourceTypeSupport {

  def create(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String) =
    FeatureSwitchAction(sourceType, summaryTypeName).asyncFeatureSwitch {
      request => super.createSummary(request, saUtr, taxYear, sourceType, sourceId, summaryTypeName)
    }

  def read(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String, summaryId: SummaryId) =
    FeatureSwitchAction(sourceType, summaryTypeName).asyncFeatureSwitch {
      super.readSummary(saUtr, taxYear, sourceType, sourceId, summaryTypeName, summaryId)
    }

  def update(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String, summaryId: SummaryId) =
    FeatureSwitchAction(sourceType, summaryTypeName).asyncFeatureSwitch {
      request => super.updateSummary(request, saUtr, taxYear, sourceType, sourceId, summaryTypeName, summaryId)
    }

  def delete(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String, summaryId: SummaryId) =
    FeatureSwitchAction(sourceType, summaryTypeName).asyncFeatureSwitch {
      super.deleteSummary(saUtr, taxYear, sourceType, sourceId, summaryTypeName, summaryId)
    }

  def list(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String) =
    FeatureSwitchAction(sourceType, summaryTypeName).asyncFeatureSwitch {
      super.listSummaries(saUtr, taxYear, sourceType, sourceId, summaryTypeName)
    }
}
