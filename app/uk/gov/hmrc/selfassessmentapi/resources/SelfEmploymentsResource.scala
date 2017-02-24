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
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.selfassessmentapi.resources.models._
import uk.gov.hmrc.selfassessmentapi.resources.models.Errors.BusinessError
import uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment.{SelfEmployment, SelfEmploymentUpdate}
import uk.gov.hmrc.selfassessmentapi.services.SelfEmploymentsService

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

object SelfEmploymentsResource extends BaseController {
  private lazy val seFeatureSwitch = FeatureSwitchAction(SourceType.SelfEmployments)
  private val service = SelfEmploymentsService

  def create(nino: Nino): Action[JsValue] = seFeatureSwitch.asyncJsonFeatureSwitch { request =>
    validate[SelfEmployment, Either[BusinessError, Option[SourceId]]](request.body) { selfEmployment =>
      service.create(nino, selfEmployment)
    } match {
      case Left(errorResult) => Future.successful(handleValidationErrors(errorResult))
      case Right(idOption) => idOption.map {
        case Left(err) => Forbidden(Json.toJson(err))
        case Right(Some(id)) => Created.withHeaders(LOCATION -> s"/self-assessment/ni/$nino/self-employments/$id")
        case Right(None) => InternalServerError
      }
    }
  }

  def update(nino: Nino, id: SourceId): Action[JsValue] = seFeatureSwitch.asyncJsonFeatureSwitch { request =>
    validate[SelfEmploymentUpdate, Boolean](request.body) { selfEmployment =>
      service.update(nino, selfEmployment, id)
    } match {
      case Left(errorResult) => Future.successful(handleValidationErrors(errorResult))
      case Right(result) => result.map {
        case true => NoContent
        case false => NotFound
      }
    }
  }

  def retrieve(nino: Nino, id: SourceId): Action[AnyContent] = seFeatureSwitch.asyncFeatureSwitch {
    service.retrieve(nino, id) map {
      case Some(selfEmployment) => Ok(Json.toJson(selfEmployment))
      case None => NotFound
    }
  }

  def retrieveAll(nino: Nino): Action[AnyContent] = seFeatureSwitch.asyncFeatureSwitch {
    service.retrieveAll(nino) map { seq =>
      Ok(Json.toJson(seq))
    }
  }

}
