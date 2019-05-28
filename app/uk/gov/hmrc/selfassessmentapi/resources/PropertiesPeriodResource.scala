/*
 * Copyright 2019 HM Revenue & Customs
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
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.connectors.PropertiesPeriodConnector
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.selfassessmentapi.models.properties._
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.ResponseMapper
import uk.gov.hmrc.selfassessmentapi.services.{AuditService, AuthorisationService}

class PropertiesPeriodResource @Inject()(
                                          override val appContext: AppContext,
                                          override val authService: AuthorisationService,
                                          connector: PropertiesPeriodConnector,
                                          auditService: AuditService
                                        ) extends BaseResource {

  def retrievePeriods(nino: Nino, id: PropertyType): Action[AnyContent] =
    APIAction(nino, SourceType.Properties, Some("periods")).async { implicit request =>
      connector.retrieveAll(nino, id).map { response =>
        response.filter {
          case 200 =>
            id match {
              case PropertyType.FHL =>
                ResponseMapper[FHL.Properties, des.properties.FHL.Properties]
                  .allPeriods(response)
                  .map(seq => Ok(Json.toJson(seq)))
                  .getOrElse(InternalServerError)
              case PropertyType.OTHER =>
                ResponseMapper[Other.Properties, des.properties.Other.Properties]
                  .allPeriods(response)
                  .map(seq => Ok(Json.toJson(seq)))
                  .getOrElse(InternalServerError)
            }
        }
      } recoverWith exceptionHandling
    }
}
