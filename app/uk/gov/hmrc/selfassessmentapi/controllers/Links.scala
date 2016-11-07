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

package uk.gov.hmrc.selfassessmentapi.controllers

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceId, SourceType, TaxYear}

trait Links {

  val context: String

  private def createLink(endpointUrl: String) = s"$context$endpointUrl"

  def discoverTaxYearsHref(nino: Nino): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.TaxYearsDiscoveryController.discoverTaxYears(nino).url)

  def discoverTaxYearHref(nino: Nino, taxYear: TaxYear): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.TaxYearDiscoveryController.discoverTaxYear(nino, taxYear).url)

  def liabilityHref(nino: Nino, taxYear: TaxYear): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.LiabilityController.retrieveLiability(nino, taxYear).url)

  def sourceIdHref(nino: Nino, taxYear: TaxYear, sourceType: SourceType, seId: SourceId): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.SourceController.read(nino, taxYear, sourceType, seId).url)

  def sourceHref(nino: Nino, taxYear: TaxYear, sourceType: SourceType): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.SourceController.list(nino, taxYear, sourceType).url)

  def sourceTypeAndSummaryTypeHref(nino: Nino, taxYear: TaxYear, sourceType: SourceType, seId: SourceId, summaryTypeName: String): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.SummaryController.list(nino, taxYear, sourceType, seId, summaryTypeName).url)

  def sourceTypeAndSummaryTypeIdHref(nino: Nino, taxYear: TaxYear, sourceType: SourceType, seId: SourceId, summaryTypeName: String, id: String): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.SummaryController.read(nino, taxYear, sourceType, seId, summaryTypeName, id).url)
}
