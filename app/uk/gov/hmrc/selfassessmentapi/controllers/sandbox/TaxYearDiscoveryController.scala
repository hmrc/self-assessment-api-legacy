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

import play.api.libs.json.Json._
import play.api.mvc.Action
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.domain.{TaxYear, TaxYearProperties}
import uk.gov.hmrc.selfassessmentapi.domain.TaxYearProperties._
import uk.gov.hmrc.selfassessmentapi.views.Helpers._
import play.api.mvc.hal._
import uk.gov.hmrc.selfassessmentapi.controllers.{BaseController, Links}

import scala.concurrent.Future

object TaxYearDiscoveryController extends BaseController with Links {
  override val context: String = AppContext.apiGatewayContext

  final def discoverTaxYear(utr: SaUtr, taxYear: TaxYear) = Action.async { request =>
    Future.successful(Ok(halResource(toJson(TaxYearProperties.example()), discoveryLinks(utr, taxYear))))
  }

  final def update(utr: SaUtr, taxYear: TaxYear) = Action.async(parse.json) { implicit request =>
    withJsonBody[TaxYearProperties] {
      taxYearProperties =>
        Future.successful(Ok(halResource(obj(), discoveryLinks(utr, taxYear))))
    }
  }
}
