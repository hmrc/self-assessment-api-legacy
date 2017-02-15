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
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.selfassessmentapi.resources.models._
import uk.gov.hmrc.selfassessmentapi.services.SelfEmploymentObligationsService

import scala.concurrent.ExecutionContext.Implicits.global

object SelfEmploymentObligationsResource extends BaseController {
  private val featureSwitch = FeatureSwitchAction(SourceType.SelfEmployments, "obligations")
  private val service = SelfEmploymentObligationsService

  def retrieveObligations(nino: Nino, id: SourceId): Action[Unit] = featureSwitch.asyncEmptyFeatureSwitch { request =>
    service.retrieveObligations(nino, id, request.headers.get(GovTestScenarioHeader)) map {
      case Some(obligationsOrError) =>
        obligationsOrError match {
          case Left(error) => BadRequest(Json.toJson(error))
          case Right(obligations) => Ok(Json.toJson(obligations))
        }
      case None => NotFound
    }
  }
}
