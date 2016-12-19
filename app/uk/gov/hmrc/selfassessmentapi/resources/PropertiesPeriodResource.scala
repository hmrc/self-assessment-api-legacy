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
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.selfassessmentapi.FeatureSwitchAction
import uk.gov.hmrc.selfassessmentapi.resources.models.Errors.Error
import uk.gov.hmrc.selfassessmentapi.resources.models._
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.{PropertiesPeriod, PropertiesPeriodicData}
import uk.gov.hmrc.selfassessmentapi.services.PropertiesPeriodService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object PropertiesPeriodResource extends BaseController {

  lazy val featureSwitch = FeatureSwitchAction(SourceType.Properties, "periods")

  private val service = PropertiesPeriodService

  def createPeriod(nino: Nino, id: PropertyType): Action[JsValue] = featureSwitch.asyncFeatureSwitch { request =>
    validate[PropertiesPeriod, Either[Error, PeriodId]](request.body) { period =>
      service.createPeriod(nino, id, period)
    } match {
      case Left(errorResult) => Future.successful(handleValidationErrors(errorResult))
      case Right(result) => result.map {
        case Right(periodId) =>
          Created.withHeaders(LOCATION -> s"/self-assessment/ni/$nino/${SourceType.Properties.toString}/${id.toString}/periods/$periodId")
        case Left(error) =>
          if (error.code == ErrorCode.NOT_FOUND.toString) NotFound
          else if (error.code == ErrorCode.ALREADY_EXISTS.toString)
            Conflict.withHeaders(LOCATION -> s"/self-assessment/ni/$nino/${SourceType.Properties.toString}/${id.toString}/periods/${error.path}")
          else Forbidden(Json.toJson(Errors.businessError(error)))
      }
    }
  }

  def updatePeriod(nino: Nino, id: PropertyType, periodId: PeriodId): Action[JsValue] = featureSwitch.asyncFeatureSwitch { request =>
    validate[PropertiesPeriodicData, Boolean](request.body) {
      service.updatePeriod(nino, id, periodId, _)
    } match {
      case Left(errorResult) => Future.successful(handleValidationErrors(errorResult))
      case Right(result) => result.map {
        case true => NoContent
        case false => NotFound
      }
    }
  }

  def retrievePeriod(nino: Nino, id: PropertyType, periodId: PeriodId): Action[AnyContent] = featureSwitch.asyncFeatureSwitch {
    service.retrievePeriod(nino, id, periodId).map {
      case Some(period) => Ok(Json.toJson(period))
      case None => NotFound
    }
  }

  def retrievePeriods(nino: Nino, id: PropertyType): Action[AnyContent] = featureSwitch.asyncFeatureSwitch {
    service.retrieveAllPeriods(nino, id).map {
      case Some(periods) => Ok(Json.toJson(periods))
      case None => NotFound
    }
  }
}
