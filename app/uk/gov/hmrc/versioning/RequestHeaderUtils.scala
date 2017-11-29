/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.versioning

import play.api.http.HeaderNames.ACCEPT
import play.api.mvc.RequestHeader

object RequestHeaderUtils {

  private val acceptHeaderRegex = "application/vnd\\.hmrc\\.(.*)\\+json".r

  private val uriRegex = "(/[a-zA-Z0-9-_]*)/?.*$".r

  def extractUriContext(requestHeader: RequestHeader) = {
    (uriRegex.findFirstMatchIn(requestHeader.uri) map (_.group(1))).get
  }

  def getVersionedRequest(originalRequest: RequestHeader) = {
    val version = getVersion(originalRequest)
    originalRequest.copy(
      uri = versionedUri(originalRequest.uri, version),
      path = versionedUri(originalRequest.path, version)
    )
  }

  private def getVersion(originalRequest: RequestHeader) =
    originalRequest.headers.get(ACCEPT) flatMap { acceptHeaderValue =>
      acceptHeaderRegex.findFirstMatchIn(acceptHeaderValue) map (_.group(1))
    } getOrElse "1.0"

  private def versionedUri(urlPath: String, version: String) = {
    urlPath match {
      case "/" => s"/v$version"
      case _ => s"/v$version$urlPath"
    }
  }
}
