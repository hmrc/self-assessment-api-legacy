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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.connectors.PropertiesConnector
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.PropertiesResponse

import play.api.libs.concurrent.Execution.Implicits._

object PropertiesResource extends BaseResource {
  private val connector = PropertiesConnector

  def create(nino: Nino): Action[JsValue] =
    APIAction(nino, SourceType.Properties).async(parse.json) { implicit request =>
      validate[properties.Properties, PropertiesResponse](request.body) { props =>
        connector.create(nino, props)
      } map {
        case Left(errorResult) =>
          handleErrors(errorResult)
        case Right(response) =>
          response.filter {
            case 200 => Created.withHeaders(LOCATION -> response.createLocationHeader(nino))
            case 403 => Conflict.withHeaders(LOCATION -> s"/self-assessment/ni/$nino/uk-properties")
          }
      }
    }

  def retrieve(nino: Nino): Action[AnyContent] =
    APIAction(nino, SourceType.Properties).async { implicit request =>
      connector.retrieve(nino).map { response =>
        response.filter {
          case 200 =>
            response.property match {
              case Some(property) => Ok(Json.toJson(property))
              case None => NotFound
            }
        }
      }
    }
}
