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
import play.api.libs.json.Json._
import play.api.mvc.{AnyContent, Action}
import play.api.mvc.hal._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.domain._
import scala.concurrent.Future

trait SelfEmploymentsGoodsAndServicesController extends BaseController with Links {

  override lazy val context: String = AppContext.apiGatewayContext

  def create(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId) = Action.async(parse.json) { implicit request =>
    withJsonBody[GoodsAndServicesOwnUse] { _ =>
      val goodsAndServiceOwnUseId = BSONObjectID.generate.stringify
      Future.successful(Created(halResource(obj(), Seq(HalLink("self", selfEmploymentSummaryTypeIdHref(saUtr, taxYear, seId, SummaryType.`goods-and-services-own-use`, goodsAndServiceOwnUseId))))))
    }
  }

  def findById(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId, goodsAndServiceOwnUseId: GoodsAndServicesOwnUseId) = Action.async { implicit request =>
    val goodsAndServiceOwnUse = GoodsAndServicesOwnUse(Some(goodsAndServiceOwnUseId), BigDecimal("1000"))
    Future.successful(Ok(halResource(toJson(goodsAndServiceOwnUse), Seq(HalLink("self", selfEmploymentSummaryTypeIdHref(saUtr, taxYear, seId, SummaryType.`goods-and-services-own-use`, goodsAndServiceOwnUseId))))))
  }

  def find(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId): Action[AnyContent] = Action { request =>
    val goodsAndServiceOwnUse = Seq(GoodsAndServicesOwnUse(Some("1234"), BigDecimal("1000")),
                                    GoodsAndServicesOwnUse(Some("5678"), BigDecimal("2000")))

    val goodsAndServiceOwnUseJson = toJson(goodsAndServiceOwnUse.map(gs => halResource(obj(),
      Seq(HalLink("self", selfEmploymentSummaryTypeIdHref(saUtr, taxYear, seId, SummaryType.`goods-and-services-own-use`, gs.id.get))))))

    Ok(halResourceList("goods-and-services-own-use", goodsAndServiceOwnUseJson, selfEmploymentSummaryTypeHref(saUtr, taxYear, seId, SummaryType.`goods-and-services-own-use`)))
  }

  def update(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId, goodsAndServiceOwnUseId: GoodsAndServicesOwnUseId) = Action.async(parse.json) { implicit request =>
    withJsonBody[GoodsAndServicesOwnUse] { _ =>
      Future.successful(Ok(halResource(obj(), Seq(HalLink("self", selfEmploymentSummaryTypeIdHref(saUtr, taxYear, seId, SummaryType.`goods-and-services-own-use`, goodsAndServiceOwnUseId))))))
    }
  }

  def delete(saUtr: SaUtr, taxYear: TaxYear, seId: SelfEmploymentId, goodsAndServiceOwnUseId: GoodsAndServicesOwnUseId) = Action.async { implicit request =>
    Future.successful(NoContent)
  }

}
