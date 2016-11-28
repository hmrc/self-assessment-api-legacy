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

package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.FeatureSwitchAction
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.{BaseController, GenericErrorResult, ValidationErrorResult}
import uk.gov.hmrc.selfassessmentapi.resources.models.periods.SelfEmploymentPeriod
import uk.gov.hmrc.selfassessmentapi.resources.models.SelfEmploymentAnnualSummary
import uk.gov.hmrc.selfassessmentapi.domain
import uk.gov.hmrc.selfassessmentapi.services.SelfEmploymentsService

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

object SelfEmploymentsResource extends PeriodResource[SourceId, SelfEmploymentPeriod, domain.SelfEmployment] with BaseController {

  override val context: PeriodId = AppContext.apiGatewayLinkContext

  override val service = SelfEmploymentsService()
  override val sourceType: SourceType = SourceTypes.SelfEmployments
  private val seFeatureSwitch = FeatureSwitchAction(SourceTypes.SelfEmployments)
  private val seAnnualFeatureSwitch = FeatureSwitchAction(SourceTypes.SelfEmployments, "annual")

  def create(nino: Nino): Action[JsValue] = seFeatureSwitch.asyncFeatureSwitch { request =>
    validate[models.SelfEmployment, Option[SourceId]](request.body) { selfEmployment =>
      service.create(nino, selfEmployment)
    } match {
      case Left(errorResult) =>
        Future.successful {
          errorResult match {
            case GenericErrorResult(message) => BadRequest(Json.toJson(Errors.badRequest(message)))
            case ValidationErrorResult(errors) => BadRequest(Json.toJson(Errors.badRequest(errors)))
          }
        }
      case Right(idOption) => idOption.map {
        case Some(id) => Created.withHeaders(LOCATION -> s"/ni/$nino/self-employments/$id")
        case None => InternalServerError
      }
    }
  }

  def update(nino: Nino, id: SourceId): Action[JsValue] = seFeatureSwitch.asyncFeatureSwitch { request =>
    validate[models.SelfEmployment, Boolean](request.body) { selfEmployment =>
      service.update(nino, selfEmployment, id)
    } match {
      case Left(errorResult) =>
        Future.successful {
          errorResult match {
            case GenericErrorResult(message) => BadRequest(Json.toJson(Errors.badRequest(message)))
            case ValidationErrorResult(errors) => BadRequest(Json.toJson(Errors.badRequest(errors)))
          }
        }
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

  def retrieveAll(nino: Nino) = seFeatureSwitch.asyncFeatureSwitch {
    service.retrieveAll(nino) map { seq =>
      Ok(Json.toJson(seq))
    }
  }

  def updateAnnualSummary(nino: Nino, id: SourceId, taxYear: TaxYear): Action[JsValue] = seAnnualFeatureSwitch.asyncFeatureSwitch { request =>
    validate[SelfEmploymentAnnualSummary, Boolean](request.body) {
      service.updateAnnualSummary(nino, id, taxYear, _)
    } match {
      case Left(errorResult) =>
        Future.successful {
          errorResult match {
            case GenericErrorResult(message) => BadRequest(Json.toJson(Errors.badRequest(message)))
            case ValidationErrorResult(errors) => BadRequest(Json.toJson(Errors.badRequest(errors)))
          }
        }
      case Right(result) => result.map {
        case true => NoContent
        case false => NotFound
      }
    }
  }

  def retrieveAnnualSummary(nino: Nino, id: SourceId, taxYear: TaxYear): Action[AnyContent] = seAnnualFeatureSwitch.asyncFeatureSwitch {
    service.retrieveAnnualSummary(id, taxYear, nino).map {
      case Some(summary) => Ok(Json.toJson(summary))
      case None => NotFound
    }
  }
}
