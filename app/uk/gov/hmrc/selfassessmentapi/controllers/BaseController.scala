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

import play.api.hal.{Hal, HalLink, HalResource}
import play.api.libs.json.JsValue
import play.api.mvc.Request
import uk.gov.hmrc.play.http.HeaderCarrier

case class Link(name: String, href: String)

trait BaseController extends uk.gov.hmrc.play.microservice.controller.BaseController {

  val context: String
  def hc(request: Request[Any]): HeaderCarrier = HeaderCarrier.fromHeadersAndSession(request.headers, None)

  def halResource(jsValue: JsValue, links: Seq[Link]): HalResource = {
    val halState = Hal.state(jsValue)
    links.foldLeft(halState)((res, link) => res ++ HalLink(link.name, link.href))
  }
}
