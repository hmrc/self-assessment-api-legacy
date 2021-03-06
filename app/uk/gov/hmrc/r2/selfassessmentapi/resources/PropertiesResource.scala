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

package uk.gov.hmrc.r2.selfassessmentapi.resources

import cats.implicits._
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.utils.Nino
import uk.gov.hmrc.r2.selfassessmentapi.config.AppContext
import uk.gov.hmrc.r2.selfassessmentapi.connectors.PropertiesConnector
import uk.gov.hmrc.r2.selfassessmentapi.models._
import uk.gov.hmrc.r2.selfassessmentapi.models.properties.NewProperties
import uk.gov.hmrc.r2.selfassessmentapi.services.AuthorisationService
import uk.gov.hmrc.utils.IdGenerator

import javax.inject.Inject
import scala.concurrent.ExecutionContext


class PropertiesResource @Inject()(
                                    override val appContext: AppContext,
                                    override val authService: AuthorisationService,
                                    propertiesConnector: PropertiesConnector,
                                    cc: ControllerComponents,
                                    val idGenerator: IdGenerator
                                  )(implicit ec: ExecutionContext) extends BaseResource(cc) {

  def create(nino: Nino): Action[JsValue] =
    APIAction(nino, SourceType.Properties).async(parse.json) {
      implicit request => {
        implicit val correlationID: String = idGenerator.getCorrelationId
        logger.warn(message = s"[PropertiesAnnualSummaryResource][create property] " +
          s"Update property annual summary with correlationId : $correlationID")
        for {
          props <- validateJson[NewProperties](request.body)
          response <- execute { _ => propertiesConnector.create(nino, props) }
        } yield response
      } onDesSuccess { response =>
        response.filter {
          case 200 => Created.withHeaders(LOCATION -> response.createLocationHeader(nino))
          case 403 => Conflict.withHeaders(LOCATION -> s"/self-assessment/ni/${nino.nino}/uk-properties")
        }
      } recoverWith exceptionHandling
    }
}
