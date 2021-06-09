/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.support.WireMockMethods
import uk.gov.hmrc.utils.Nino

object MtdIdLookupStub extends WireMockMethods {

  private def lookupUrl(nino: String): String = s".*/registration/business-details/nino/$nino"

  def ninoFound(nino: Nino): StubMapping = {
    when(method = GET, uri = lookupUrl(nino.nino))
      .thenReturnWithHeaders(status = OK, Map("Content-Type" -> "application/json"), body = Json.obj("mtdbsa" -> "12345678"))
  }

  def unauthorised(nino: String): StubMapping = {
    when(method = GET, uri = lookupUrl(nino))
      .thenReturn(status = FORBIDDEN, body = Json.obj())
  }

  def notFound(nino: String): StubMapping = {
    when(method = GET, uri = lookupUrl(nino))
      .thenReturn(status = NOT_FOUND, body = Json.obj())
  }

  def badRequest(nino: String): StubMapping = {
    when(method = GET, uri = lookupUrl(nino))
      .thenReturn(status = BAD_REQUEST, body = Json.obj())
  }

  def internalServerError(nino: String): StubMapping = {
    when(method = GET, uri = lookupUrl(nino))
      .thenReturn(status = INTERNAL_SERVER_ERROR, body = Json.obj())
  }

  def serviceUnavailableError(nino: String): StubMapping = {
    when(method = GET, uri = lookupUrl(nino))
      .thenReturn(status = SERVICE_UNAVAILABLE, body = Json.obj())
  }

}
