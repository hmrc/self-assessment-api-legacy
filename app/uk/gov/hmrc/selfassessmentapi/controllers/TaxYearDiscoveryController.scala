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
import play.api.libs.json.JsObject
import play.api.mvc.Action
import play.api.mvc.hal._
import uk.gov.hmrc.api.controllers.HeaderValidator
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.domain.{SourceTypes, TaxYear}

import scala.concurrent.Future

trait TaxYearDiscoveryController
  extends BaseController with HeaderValidator with Links {

  final def discoverTaxYear(utr: SaUtr, taxYear: TaxYear) = Action.async { request =>
    val links = Seq(
      HalLink("self", discoverTaxYearHref(utr, taxYear)),
      HalLink("self-employments", sourceHref(utr, taxYear, SourceTypes.SelfEmployments)),
      HalLink("furnished-holiday-lettings", sourceHref(utr, taxYear, SourceTypes.FurnishedHolidayLettings)),
      HalLink("liabilities", liabilitiesHref(utr, taxYear)))
    Future.successful(Ok(halResource(JsObject(Nil), links)))
  }
}
