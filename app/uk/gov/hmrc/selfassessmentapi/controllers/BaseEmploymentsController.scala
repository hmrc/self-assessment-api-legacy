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

package uk.gov.hmrc.selfassessmentapi.controllers

import play.api.libs.json.Json
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.domain.Employment

import scala.concurrent.Future
import play.api.mvc.hal._
import uk.gov.hmrc.api.controllers.HeaderValidator

trait BaseEmploymentsController extends BaseController with HeaderValidator {

  def getEmployments(utr: SaUtr) = validateAccept(acceptHeaderValidationRules).async { implicit request =>
		val message = s"Employments for utr: $utr"
		val employment = Employment(message)
    val links = Seq(Link("self", selfLink(utr)))
		Future.successful(Ok(halResource(Json.toJson(employment), links)))
	}

  private def selfLink(utr: SaUtr): String = {
    val endpointUrl = uk.gov.hmrc.selfassessmentapi.controllers.live.routes.EmploymentsController.getEmployments(utr).url
    s"$context$endpointUrl"
  }
}
