package uk.gov.hmrc.selfassessmentapi.definition

import uk.gov.hmrc.selfassessmentapi.definition.APIStatus.APIStatus
import uk.gov.hmrc.selfassessmentapi.definition.AuthType.AuthType
import uk.gov.hmrc.selfassessmentapi.definition.HttpMethod.HttpMethod
import uk.gov.hmrc.selfassessmentapi.definition.ResourceThrottlingTier.ResourceThrottlingTier

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
      require(endpoint.endpointName.nonEmpty, s"endpointName is required")
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

case class APIVersion(
                       version: String,
                       status: APIStatus,
                       endpoints: Seq[Endpoint])

case class Endpoint(
                     uriPattern: String,
                     endpointName: String,
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

object SubscriptionThrottlingTier extends Enumeration {
  type ThrottlingTier = Value
  val BRONZE_SUBSCRIPTION = Value
}
