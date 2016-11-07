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

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.FeatureSwitchAction
import uk.gov.hmrc.selfassessmentapi.controllers.api._

object SourceController extends uk.gov.hmrc.selfassessmentapi.controllers.SourceController with SourceTypeSupport {

  def create(nino: Nino, taxYear: TaxYear, sourceType: SourceType) = FeatureSwitchAction(sourceType).asyncFeatureSwitch {
    request => super.createSource(request, nino, taxYear, sourceType)
  }

  def read(nino: Nino, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId) = FeatureSwitchAction(sourceType).asyncFeatureSwitch {
    super.readSource(nino, taxYear, sourceType, sourceId)
  }

  def update(nino: Nino, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId) = FeatureSwitchAction(sourceType).asyncFeatureSwitch {
    request => super.updateSource(request, nino, taxYear, sourceType, sourceId)
  }

  def delete(nino: Nino, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId) = FeatureSwitchAction(sourceType).asyncFeatureSwitch {
    super.deleteSource(nino, taxYear, sourceType, sourceId)
  }

  def list(nino: Nino, taxYear: TaxYear, sourceType: SourceType) = FeatureSwitchAction(sourceType).asyncFeatureSwitch {
    super.listSources(nino, taxYear, sourceType)
  }
}
