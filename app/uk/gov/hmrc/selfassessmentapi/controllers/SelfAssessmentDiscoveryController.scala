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
import play.api.mvc.hal._
import uk.gov.hmrc.api.controllers.HeaderValidator
import uk.gov.hmrc.domain.SaUtr

trait SelfAssessmentDiscoveryController
    extends BaseController with HeaderValidator with Links {

  final def discover(utr: SaUtr) =
    validateAccept(acceptHeaderValidationRules) { request =>
      val links = Seq(HalLink("self", discoveryHref(utr)),
                      HalLink("self-employments", selfEmploymentsHref(utr)),
                      HalLink("liabilities", liabilitiesHref(utr)))
      Ok(halResource(JsObject(Nil), links))
    }
}
