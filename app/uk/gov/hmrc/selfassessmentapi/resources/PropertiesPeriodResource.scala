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
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.connectors.PropertiesPeriodConnector
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.ResponseMapper
import uk.gov.hmrc.selfassessmentapi.services.{AuditService, AuthorisationService}
import uk.gov.hmrc.utils.IdGenerator

import scala.concurrent.ExecutionContext

class PropertiesPeriodResource @Inject()(
                                          override val appContext: AppContext,
                                          override val authService: AuthorisationService,
                                          connector: PropertiesPeriodConnector,
                                          auditService: AuditService,
                                          cc: ControllerComponents,
                                          val idGenerator: IdGenerator
                                        )(implicit ec: ExecutionContext) extends BaseResource(cc) {

  def retrievePeriods(nino: Nino, id: PropertyType): Action[AnyContent] =
    APIAction(nino, SourceType.Properties, Some("periods")).async { implicit request =>
      implicit val correlationID: String = idGenerator.getCorrelationId
      logger.warn(message = s"[PropertiesPeriodResource][retrievePeriods] " +
        s"retrieve property obligations with correlationId : $correlationID")

      connector.retrieveAll(nino, id).map { response =>
        response.filter {
          case 200 =>
            logger.warn(message = s"[PropertiesPeriodResource][retrievePeriods] " +
              s"Success response with correlationId : $correlationID")

            ResponseMapper
              .allPeriods(response)
              .map(seq => Ok(Json.toJson(seq)))
              .getOrElse(InternalServerError)
        }
      } recoverWith exceptionHandling
    }
}
