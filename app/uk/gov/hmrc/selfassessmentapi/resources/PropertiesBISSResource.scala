/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.models.{SourceType, TaxYear}
import uk.gov.hmrc.selfassessmentapi.services.AuthorisationService

import scala.concurrent.{ExecutionContext, Future}


class PropertiesBISSResource @Inject()(
                                        override val appContext: AppContext,
                                        override val authService: AuthorisationService,
                                        cc: ControllerComponents
                                      )(implicit ec: ExecutionContext) extends BaseResource(cc) {

  def getSummary(nino: Nino, taxYear: TaxYear): Action[AnyContent] =
    APIAction(nino, SourceType.Properties, Some("BISS")).async {
      implicit request =>
         Future.successful(Gone)
    }
}
