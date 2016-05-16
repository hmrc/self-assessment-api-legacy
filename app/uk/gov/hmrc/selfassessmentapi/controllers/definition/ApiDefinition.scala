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

package uk.gov.hmrc.selfassessmentapi.controllers.definition

import uk.gov.hmrc.selfassessmentapi.controllers.definition.APIStatus.APIStatus
import uk.gov.hmrc.selfassessmentapi.controllers.definition.AuthType.AuthType
import uk.gov.hmrc.selfassessmentapi.controllers.definition.HttpMethod.HttpMethod
import uk.gov.hmrc.selfassessmentapi.controllers.definition.ResourceThrottlingTier.ResourceThrottlingTier

case class Definition(scopes: Seq[Scope],
                      api: APIDefinition)

case class APIDefinition(
                          name: String,
                          description: String,
                          context: String,
                          versions: Seq[APIVersion],
                          requiresTrust: Option[Boolean]) {

  require(name.nonEmpty, s"name is required")
  require(context.nonEmpty, s"context is required")
  require(description.nonEmpty, s"description is required")
  require(versions.nonEmpty, s"at least one version is required")
  require(uniqueVersions, s"version numbers must be unique")
  versions.foreach(version => {
    require(version.version.nonEmpty, s"version is required")
    require(version.endpoints.nonEmpty, s"at least one endpoint is required")
    version.endpoints.foreach(endpoint => {
      require(endpoint.name.nonEmpty, s"endpointName is required")
      endpoint.queryParameters.getOrElse(Nil).foreach(parameter => {
        require(parameter.name.nonEmpty, "parameter name is required")
      })
      endpoint.authType match {
        case AuthType.USER => require(endpoint.scope.nonEmpty, s"scope is required if authType is USER")
        case _ => ()
      }
    })
  })

  private def uniqueVersions = {
    !versions.map(_.version).groupBy(identity).mapValues(_.size).exists(_._2 > 1)
  }

}

case class Scope(key: String,
                 name: String,
                 description: String)

case class APIVersion(
                       version: String,
                       status: APIStatus,
                       endpoints: Seq[Endpoint])

case class Endpoint(
                     uriPattern: String,
                     name: String,
                     method: HttpMethod,
                     authType: AuthType,
                     throttlingTier: ResourceThrottlingTier,
                     scope: Option[String] = None,
                     queryParameters: Option[Seq[Parameter]] = None)

case class Parameter(name: String, required: Boolean = false)

case class PublishingException(message: String) extends Exception(message)

object APIStatus extends Enumeration {
  type APIStatus = Value
  val PROTOTYPED, PUBLISHED, DEPRECATED, RETIRED = Value
}

object AuthType extends Enumeration {
  type AuthType = Value
  val NONE, APPLICATION, USER = Value
}

object HttpMethod extends Enumeration {
  type HttpMethod = Value
  val GET, POST, PUT, DELETE, OPTIONS = Value
}

object ResourceThrottlingTier extends Enumeration {
  type ResourceThrottlingTier = Value
  val UNLIMITED = Value
}
