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

import play.api.hal.HalLink
import play.api.libs.json.Json
import play.api.libs.json.Json.obj
import play.api.mvc.hal._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.api.controllers.ErrorNotFound
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.domain.{SelfEmployment, SelfEmploymentId}
import uk.gov.hmrc.selfassessmentapi.services.SelfEmploymentService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait SelfEmploymentsController extends BaseController with Links {

  override lazy val context: String = AppContext.apiGatewayContext

  val selfEmploymentService: SelfEmploymentService

  def findById(utr: SaUtr, seId: SelfEmploymentId) = Action.async { request =>
    for (selfEmployment <- selfEmploymentService.findBySelfEmploymentId(utr, seId)) yield {
      selfEmployment match {
        case Some(se) => Ok(halResource(Json.toJson(selfEmployment), Seq(HalLink("self", selfEmploymentHref(utr, seId)))))
        case None => NotFound(Json.toJson(ErrorNotFound))
      }
    }
  }

  def create(saUtr: SaUtr) = Action.async(parse.json) { implicit request =>
    withJsonBody[SelfEmployment] { selfEmployment =>
      for (seId <- selfEmploymentService.create(selfEmployment)) yield {
        Created(halResource(obj(), Seq(HalLink("self", selfEmploymentHref(saUtr, seId)))))
      }
    }
  }

  def update(saUtr: SaUtr, seId: SelfEmploymentId) = Action.async(parse.json) { implicit request =>
    Future.successful(NotImplemented)
  }

  def find(saUtr: SaUtr, page: Int, pageSize: Int) : Action[AnyContent] = Action { request =>
    NotImplemented
  }
}
