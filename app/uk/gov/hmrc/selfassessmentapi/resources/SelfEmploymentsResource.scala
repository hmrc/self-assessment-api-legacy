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

import play.api.libs.json.Json
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.FeatureSwitchAction
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode.ErrorCode
import uk.gov.hmrc.selfassessmentapi.controllers.{BaseController, GenericErrorResult, ValidationErrorResult}
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.resources.models.periods.SelfEmploymentPeriod
import uk.gov.hmrc.selfassessmentapi.resources.models.{SelfEmployment, SelfEmploymentAnnualSummary}
import uk.gov.hmrc.selfassessmentapi.services.SelfEmploymentsService

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

object SelfEmploymentsResource extends BaseController {

  override val context = AppContext.apiGatewayLinkContext

  private val service = SelfEmploymentsService()
  private val featureSwitch = FeatureSwitchAction(SourceTypes.SelfEmployments)

  def create(nino: Nino) = featureSwitch.asyncFeatureSwitch { request =>
    validate[SelfEmployment, Option[SourceId]](request.body) { selfEmployment =>
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
        case Some(id) => Created.withHeaders(LOCATION -> s"/nino/$nino/self-employments/$id")
        case None => InternalServerError
      }
    }
  }

  def update(nino: Nino, id: SourceId) = featureSwitch.asyncFeatureSwitch { request =>
    validate[SelfEmployment, Boolean](request.body) { selfEmployment: SelfEmployment =>
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

  def retrieve(nino: Nino, id: SourceId) = featureSwitch.asyncFeatureSwitch {
    service.retrieve(nino, id) map {
      case Some(selfEmployment) => Ok(Json.toJson(selfEmployment))
      case None => NotFound
    }
  }

  def retrieveAll(nino: Nino) = featureSwitch.asyncFeatureSwitch {
    service.retrieveAll(nino) map {
      case seq if seq.isEmpty => NoContent
      case seq => Ok(Json.toJson(seq))
    }
  }

  def updateAnnualSummary(nino: Nino, id: SourceId, taxYear: TaxYear) = featureSwitch.asyncFeatureSwitch { request =>
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

  def retrieveAnnualSummary(nino: Nino, id: SourceId, taxYear: TaxYear) = featureSwitch.asyncFeatureSwitch {
    service.retrieveAnnualSummary(id, taxYear, nino).map {
      case Some(summary) => Ok(Json.toJson(summary))
      case None => NotFound
    }
  }

  def createPeriod(nino: Nino, id: SourceId) = featureSwitch.asyncFeatureSwitch { request =>
    validate[SelfEmploymentPeriod, Either[ErrorCode, PeriodId]](request.body) {
      service.createPeriod(nino, id, _)
    } match {
      case Left(errorResult) =>
        Future.successful {
          errorResult match {
            case GenericErrorResult(message) => BadRequest(Json.toJson(Errors.badRequest(message)))
            case ValidationErrorResult(errors) => BadRequest(Json.toJson(Errors.badRequest(errors)))
          }
        }
      case Right(result) => result.map {
        case Right(periodId) => Created.withHeaders(LOCATION -> s"/nino/$nino/self-employments/$id/periods/$periodId")
        case Left(error) => if (error == ErrorCode.DUPLICATE_PERIOD) Conflict else InternalServerError
      }
    }
  }

  def updatePeriod(nino: Nino, id: SourceId, periodId: PeriodId) = featureSwitch.asyncFeatureSwitch { request =>
    validate[SelfEmploymentPeriod, Boolean](request.body) {
      service.updatePeriod(nino, id, periodId, _)
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

  def retrievePeriod(nino: Nino, id: SourceId, periodId: PeriodId) = featureSwitch.asyncFeatureSwitch {
    service.retrievePeriod(nino, id, periodId) map {
      case Some(period) => Ok(Json.toJson(period))
      case None => NotFound
    }
  }

  def retrievePeriods(nino: Nino, id: SourceId) = featureSwitch.asyncFeatureSwitch {
    service.retrieveAllPeriods(nino: Nino, id: SourceId).map {
      case seq if seq.isEmpty=> NoContent
      case seq => Ok(Json.toJson(seq))
    }
  }
}
