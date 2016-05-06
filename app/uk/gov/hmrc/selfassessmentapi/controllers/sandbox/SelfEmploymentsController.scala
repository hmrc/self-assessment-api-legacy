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

package uk.gov.hmrc.selfassessmentapi.controllers.sandbox

import org.joda.time.LocalDate
import play.api.hal.HalLink
import play.api.libs.json.{JsObject, Json}
import play.api.libs.json.Json.{obj, toJson}
import play.api.mvc.Action
import play.api.mvc.hal._
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.services.sandbox.SelfEmploymentService

import scala.concurrent.Future

object SelfEmploymentsController extends uk.gov.hmrc.selfassessmentapi.controllers.SelfEmploymentsController {
  override val selfEmploymentService = SelfEmploymentService

  override def update(saUtr: SaUtr, seId: SelfEmploymentId) = Action.async(parse.json) { implicit request =>
    withJsonBody[SelfEmployment] { selfEmployment =>
      Future.successful(Ok(halResource(obj(), Seq(HalLink("self", selfEmploymentHref(saUtr, seId))))))
    }
  }

  override def find(saUtr: SaUtr, page: Int, pageSize: Int) = Action { request =>

    val result= Seq(SelfEmployment(Some("1234"), "Awesome Plumbers", new LocalDate(2015, 1,1)),
                    SelfEmployment(Some("5678"), "Awesome Bakers", new LocalDate(2015, 10,1)),
                    SelfEmployment(Some("9101"), "Average Accountants", new LocalDate(2015, 10,11)))

    val selfEmployments = toJson(
      result.map(res => halResource(obj(), Seq(HalLink("self", selfEmploymentHref(saUtr, res.id.get)))))
    )

    Ok(halResource(
      JsObject(
        Seq(
          "_embedded" -> JsObject(
            Seq("selfEmployments" -> selfEmployments))
        )
      ),
      Seq(HalLink("self", selfEmploymentsHref(saUtr, page, pageSize))))
    )
  }
}
