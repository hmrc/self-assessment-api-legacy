/*
 * Copyright 2017 HM Revenue & Customs
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

import play.api.libs.json.Json
import play.api.mvc.Action
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.connectors.ObligationsConnector
import uk.gov.hmrc.selfassessmentapi.models.{Errors, SourceType}

import scala.concurrent.ExecutionContext.Implicits.global

object PropertiesObligationsResource extends BaseResource {
  private val connector = ObligationsConnector

  def retrieveObligations(nino: Nino): Action[Unit] =
    APIAction(nino, SourceType.Properties, Some("obligations")).async(parse.empty) { implicit request =>
      connector.get(nino).map { response =>
        response.filter {
          case 200 =>
            logger.debug("Properties obligations from DES = " + Json.stringify(response.json))
            response.obligations("ITSP") match {
              case Right(obj) =>  obj.map(x => Ok(Json.toJson(x))).getOrElse(NotFound)
              case Left(ex) =>
                logger.warn(ex.msg)
                InternalServerError(Json.toJson(Errors.InternalServerError))
            }
          case 400 if response.isInvalidNino => BadRequest(Json.toJson(Errors.NinoInvalid))
          case 404 => NotFound
          case _ => unhandledResponse(response.status, logger)
        }
      }
    }
}
