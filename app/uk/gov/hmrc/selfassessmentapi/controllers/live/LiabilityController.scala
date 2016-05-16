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

package uk.gov.hmrc.selfassessmentapi.controllers.live

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.controllers.ErrorNotImplemented

import scala.concurrent.Future

object LiabilityController extends uk.gov.hmrc.selfassessmentapi.controllers.LiabilityController {

  override val context: String = AppContext.apiGatewayContext

  override def requestLiability(utr: SaUtr, taxYear: Option[String]) = Action.async { request =>
    Future.successful(NotImplemented(Json.toJson(ErrorNotImplemented)))
  }

  override def retrieveLiability(utr: SaUtr, liabilityId: String) = Action.async { request =>
    Future.successful(NotImplemented(Json.toJson(ErrorNotImplemented)))
  }

  override def deleteLiability(utr: SaUtr, liabilityId: String) = Action.async { request =>
    Future.successful(NotImplemented(Json.toJson(ErrorNotImplemented)))
  }

  override def find(utr: SaUtr): Action[AnyContent] = Action.async { request =>
    Future.successful(NotImplemented(Json.toJson(ErrorNotImplemented)))
  }
}
