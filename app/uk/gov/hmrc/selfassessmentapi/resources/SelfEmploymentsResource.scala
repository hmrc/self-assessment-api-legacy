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

import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Results.NotFound
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.api.controllers.ErrorNotFound
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.selfassessmentapi.connectors.SelfEmploymentConnector
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.models.Errors
import uk.gov.hmrc.selfassessmentapi.models.Errors.Error
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.{SelfEmployment, SelfEmploymentUpdate}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.SelfEmploymentResponse

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

object SelfEmploymentsResource extends BaseController {
  private val logger = Logger(SelfEmploymentsResource.getClass)
  private lazy val seFeatureSwitch = FeatureSwitchAction(SourceType.SelfEmployments)
  private val connector = SelfEmploymentConnector

  def create(nino: Nino): Action[JsValue] = seFeatureSwitch.asyncJsonFeatureSwitch { request =>
    validate[SelfEmployment, SelfEmploymentResponse](request.body) { selfEmployment =>
      connector.create(nino, des.Business.from(selfEmployment))
    } match {
      case Left(errorResult) => Future.successful(handleValidationErrors(errorResult))
      case Right(response) => response.map { response =>
        if (response.status == 200) Created.withHeaders(LOCATION -> response.createLocationHeader(nino).getOrElse(""))
        else if (response.status == 403) Forbidden(Json.toJson(Errors.businessError(Error(ErrorCode.TOO_MANY_SOURCES.toString, s"The maximum number of Self-Employment incomes sources is 1", ""))))
        else if (response.status == 400 || response.status == 409) BadRequest(Error.from(response.json))
        else if (response.status == 404) NotFound
        else unhandledResponse(response.status, logger)
      }
    }
  }

  // TODO: DES spec for this method is currently unavailable. This method should be updated once it is available.
  def update(nino: Nino, id: SourceId): Action[JsValue] = seFeatureSwitch.asyncJsonFeatureSwitch { request =>
    validate[SelfEmploymentUpdate, SelfEmploymentResponse](request.body) { selfEmployment =>
      connector.update(nino, des.SelfEmploymentUpdate.from(selfEmployment), id)
    } match {
      case Left(errorResult) => Future.successful(handleValidationErrors(errorResult))
      case Right(result) => result.map { response =>
        if (response.status == 204) NoContent
        else if (response.status == 400) BadRequest(Error.from(response.json))
        else if (response.status == 404) NotFound
        else unhandledResponse(response.status, logger)
      }
    }
  }

  def retrieve(nino: Nino, id: SourceId): Action[AnyContent] = seFeatureSwitch.asyncFeatureSwitch {
    connector.get(nino).map { response =>
      if (response.status == 200) response.selfEmployment(id) match {
        case Some(se) => Ok(Json.toJson(se))
        case None => NotFound
      }
      else if (response.status == 404) NotFound
      else if (response.status == 400) BadRequest(Error.from(response.json))
      else unhandledResponse(response.status, logger)
    }
  }

  def retrieveAll(nino: Nino): Action[AnyContent] = seFeatureSwitch.asyncFeatureSwitch {
    connector.get(nino).map { response =>
      if (response.status == 200) Ok(Json.toJson(response.listSelfEmployment))
      else if (response.status == 404) NotFound
      else if (response.status == 400) BadRequest(Error.from(response.json))
      else unhandledResponse(response.status, logger)
    }
  }

}
