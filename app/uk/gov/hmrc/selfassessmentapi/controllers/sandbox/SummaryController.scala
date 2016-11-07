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

package uk.gov.hmrc.selfassessmentapi.controllers.sandbox
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.FeatureSwitchAction
import uk.gov.hmrc.selfassessmentapi.controllers.api.SourceType
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceId, SummaryId, TaxYear}

object SummaryController extends uk.gov.hmrc.selfassessmentapi.controllers.SummaryController with SourceTypeSupport {

  def create(nino: Nino, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId,
             summaryTypeName: String) = FeatureSwitchAction(sourceType, summaryTypeName).asyncFeatureSwitch {
    request => super.createSummary(request, nino, taxYear, sourceType, sourceId, summaryTypeName)
  }

  def read(nino: Nino, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String,
           summaryId: SummaryId) = FeatureSwitchAction(sourceType, summaryTypeName).asyncFeatureSwitch {
    super.readSummary(nino, taxYear, sourceType, sourceId, summaryTypeName, summaryId)
  }

  def update(nino: Nino, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId,
             summaryTypeName: String, summaryId: SummaryId) = FeatureSwitchAction(sourceType, summaryTypeName).asyncFeatureSwitch {
    request => super.updateSummary(request, nino, taxYear, sourceType, sourceId, summaryTypeName, summaryId)
  }

  def delete(nino: Nino, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String, summaryId: SummaryId) = FeatureSwitchAction(sourceType, summaryTypeName).asyncFeatureSwitch {
    super.deleteSummary(nino, taxYear, sourceType, sourceId, summaryTypeName, summaryId)
  }

  def list(nino: Nino, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String) = FeatureSwitchAction(sourceType, summaryTypeName).asyncFeatureSwitch {
    super.listSummaries(nino, taxYear, sourceType, sourceId, summaryTypeName)
  }
}
