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

import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.domain.{SelfEmploymentId, SelfEmploymentIncomeId}

trait Links {

  val context: String

  private def createLink(endpointUrl: String) = s"/$context$endpointUrl"

  def discoveryHref(utr: SaUtr): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.SelfAssessmentDiscoveryController.discover(utr).url)

  def liabilityHref(utr: SaUtr, liabilityId: String): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.LiabilityController.retrieveLiability(utr, liabilityId).url)

  def liabilitiesHref(utr: SaUtr): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.LiabilityController.find(utr).url)

  def selfEmploymentHref(utr: SaUtr, seId: String): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.SelfEmploymentsController.findById(utr, seId).url)

  def selfEmploymentsHref(utr: SaUtr): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.SelfEmploymentsController.find(utr).url)

  def selfEmploymentIncomeHref(utr: SaUtr, seId: SelfEmploymentId, seIncomeId: SelfEmploymentIncomeId): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.SelfEmploymentsIncomeController.findById(utr, seId, seIncomeId).url)

  def selfEmploymentIncomeHref(saUtr: SaUtr, seId: SelfEmploymentId): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.SelfEmploymentsIncomeController.find(saUtr, seId).url)
}
