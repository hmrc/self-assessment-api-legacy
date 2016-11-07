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

import play.api.hal.HalLink
import play.api.libs.json.Json
import play.api.libs.json.Json._
import play.api.mvc.Action
import play.api.mvc.hal._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.config.{AppContext, FeatureConfig}
import uk.gov.hmrc.selfassessmentapi.controllers._
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.services.live.TaxYearPropertiesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object TaxYearDiscoveryController extends TaxYearDiscoveryController {
  override val context: String = AppContext.apiGatewayLinkContext
  private val taxYearPropertiesService = TaxYearPropertiesService()

  final def discoverTaxYear(nino: Nino, taxYear: TaxYear) = Action.async { request =>
    val halLinks = buildSourceHalLinks(nino, taxYear) + HalLink("self", discoverTaxYearHref(nino, taxYear))
    taxYearPropertiesService.findTaxYearProperties(nino, taxYear).map(taxYearProperties =>
        Ok(halResource(taxYearProperties match {
          case Some(t) => toJson(t)
          case None => obj()
        }, halLinks))
    )
  }

  private def buildSourceHalLinks(nino: Nino, taxYear: TaxYear) = {
    SourceTypes.types.filter { source =>
      AppContext.featureSwitch.exists { config =>
        FeatureConfig(config).isSourceEnabled(source.name)
      }
    } map { source =>
      HalLink(source.name, sourceHref(nino, taxYear, source))
    }
  }

  final def updateTaxYearProperties(nino: Nino, taxYear: TaxYear) =
    Action.async(parse.json) {
      implicit request =>
        if (FeatureSwitchedTaxYearProperties.atLeastOnePropertyIsEnabled)
          withJsonBody[TaxYearProperties] { taxYearProperties =>
            validateRequest(taxYearProperties, taxYear.taxYear) match {
              case Some(invalidPart) => Future.successful(BadRequest(Json.toJson(InvalidRequest(ErrorCode.INVALID_REQUEST, "Invalid request", Seq(invalidPart)))))
              case None => taxYearPropertiesService.updateTaxYearProperties(nino, taxYear, taxYearProperties).map { updated =>
                if (updated) Ok(halResource(obj(), buildSourceHalLinks(nino, taxYear)))
                else BadRequest(Json.toJson(ErrorFeatureSwitched))
              }
            }
          }
        else Future.successful(NotImplemented(Json.toJson(ErrorNotImplemented)))
    }
}
