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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Request
import play.api.mvc.hal._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceId, SourceType, TaxYear}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait SourceController extends BaseController with Links with SourceTypeSupport {

  override lazy val context: String = AppContext.apiGatewayLinkContext

  protected def createSource(request: Request[JsValue], nino: Nino, taxYear: TaxYear, sourceType: SourceType) = {
    sourceHandler(sourceType).create(nino, taxYear, request.body) match {
      case Left(errorResult) =>
        Future.successful {
          errorResult match {
            case GenericErrorResult(message) => BadRequest(Json.toJson(invalidRequest(message)))
            case ValidationErrorResult(errors) => BadRequest(Json.toJson(invalidRequest(errors)))
          }
        }
      case Right(id) => id.map { sourceId => Created(halResource(obj(), sourceLinks(nino, taxYear, sourceType, sourceId))) }
    }
  }

  protected def readSource(nino: Nino, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId) = {
    sourceHandler(sourceType).findById(nino, taxYear, sourceId) map {
      case Some(summary) => Ok(halResource(summary, sourceLinks(nino, taxYear, sourceType, sourceId)))
      case None => notFound
    }
  }

  protected def updateSource(request: Request[JsValue], nino: Nino, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId) = {
    sourceHandler(sourceType).update(nino, taxYear, sourceId, request.body) match {
      case Left(errorResult) =>
        Future.successful {
          errorResult match {
            case GenericErrorResult(message) => BadRequest(Json.toJson(invalidRequest(message)))
            case ValidationErrorResult(errors) => BadRequest(Json.toJson(invalidRequest(errors)))
          }
        }
      case Right(result) => result.map {
        case true => Ok(halResource(obj(), sourceLinks(nino, taxYear, sourceType, sourceId)))
        case false => notFound
      }
    }
  }

  protected def deleteSource(nino: Nino, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId) = {
    sourceHandler(sourceType).delete(nino, taxYear, sourceId) map {
      case true => NoContent
      case false => notFound
    }
  }

  protected def listSources(nino: Nino, taxYear: TaxYear, sourceType: SourceType) = {
    val svc = sourceHandler(sourceType)
    svc.find(nino, taxYear) map { sources =>
      val json = toJson(sources.map(source => halResource(source.json,
        Set(HalLink("self", sourceIdHref(nino, taxYear, sourceType, source.id))))))
      Ok(halResourceList(svc.listName, json, sourceHref(nino, taxYear, sourceType)))
    }
  }

}
