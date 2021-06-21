/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.resources

import javax.inject.Inject
import play.api.libs.json.Json.toJson
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.utils.{Nino, TaxYear}
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.models.{Errors, SourceType}
import uk.gov.hmrc.selfassessmentapi.services.AuthorisationService

import scala.concurrent.ExecutionContext

class SelfEmploymentBISSResource @Inject()(
                                            override val appContext: AppContext,
                                            override val authService: AuthorisationService,
                                            cc: ControllerComponents
                                          )(implicit ec: ExecutionContext) extends BaseResource(cc) {

  def getSummary(nino: Nino, taxYear: TaxYear, selfEmploymentId: String): Action[AnyContent] =
    APIAction(nino, SourceType.SelfEmployments, Some("BISS")) {
        logger.info(s"[SelfEmploymentBISSResource][getSummary] Get BISS for NI number : $nino with selfEmploymentId: $selfEmploymentId")
        logger.warn(message = "[SelfEmploymentBISSResource][getSummary] - Using deprecated resource.  Should be using BISS API")
        Gone(toJson(Errors.ResourceGone))
    }
}
