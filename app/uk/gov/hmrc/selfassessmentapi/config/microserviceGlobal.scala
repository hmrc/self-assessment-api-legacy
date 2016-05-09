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

package uk.gov.hmrc.selfassessmentapi.config

import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.{StringReader, ValueReader}
import play.api.Play._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.{Application, Configuration, Play, Routes}
import uk.gov.hmrc.api.config.{ServiceLocatorConfig, ServiceLocatorRegistration}
import uk.gov.hmrc.api.connector.ServiceLocatorConnector
import uk.gov.hmrc.api.controllers.{ErrorAcceptHeaderInvalid, HeaderValidator}
import uk.gov.hmrc.play.audit.filters.AuditFilter
import uk.gov.hmrc.play.auth.controllers.{AuthConfig, AuthParamsControllerConfig}
import uk.gov.hmrc.play.auth.microservice.connectors.{AccountId, HttpVerb, Regime, ResourceToAuthorise}
import uk.gov.hmrc.play.auth.microservice.filters.AuthorisationFilter
import uk.gov.hmrc.play.config.{AppName, ControllerConfig, RunMode}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.filters.LoggingFilter
import uk.gov.hmrc.play.microservice.bootstrap.DefaultMicroserviceGlobal

import scala.concurrent.Future
import scala.util.matching.Regex

case class ControllerConfigParams(needsHeaderValidation: Boolean = true, needsLogging: Boolean = true,
                                  needsAuditing: Boolean = true, needsAuth: Boolean = true)

object ControllerConfiguration {
  lazy val controllerConfigs = Play.current.configuration.underlying.as[Config]("controllers")
  implicit val regexValueReader: ValueReader[Regex] = StringReader.stringValueReader.map(_.r)

  implicit val controllerParamsReader = ValueReader.relative[ControllerConfigParams] { config =>
    ControllerConfigParams(
      needsHeaderValidation = config.getAs[Boolean]("needsHeaderValidation").getOrElse(true),
      needsLogging = config.getAs[Boolean]("needsLogging").getOrElse(true),
      needsAuditing = config.getAs[Boolean]("needsAuditing").getOrElse(true),
      needsAuth = config.getAs[Boolean]("needsAuth").getOrElse(true)
    )
  }

  def controllerParamsConfig(controllerName: String): ControllerConfigParams = {
    controllerConfigs.as[Option[ControllerConfigParams]](controllerName).getOrElse(ControllerConfigParams())
  }
}


object AuthParamsControllerConfiguration extends AuthParamsControllerConfig {
  lazy val controllerConfigs = ControllerConfiguration.controllerConfigs
}

object MicroserviceAuditFilter extends AuditFilter with AppName {
  override val auditConnector = MicroserviceAuditConnector

  override def controllerNeedsAuditing(controllerName: String) = ControllerConfiguration.controllerParamsConfig(controllerName).needsAuditing
}

object MicroserviceLoggingFilter extends LoggingFilter {
  override def controllerNeedsLogging(controllerName: String) = ControllerConfiguration.controllerParamsConfig(controllerName).needsLogging
}

object MicroserviceAuthFilter extends AuthorisationFilter {
  override lazy val authParamsConfig = AuthParamsControllerConfiguration
  override lazy val authConnector = MicroserviceAuthConnector

  override def extractResource(pathString: String, verb: HttpVerb, authConfig: AuthConfig): Option[ResourceToAuthorise] = {
    authConfig.mode match {
      case "identity" => extractIdentityResource(pathString, verb, authConfig)
      case "passcode" => super.extractResource(pathString, verb, authConfig)
    }
  }

  private def extractIdentityResource(pathString: String, verb: HttpVerb, authConfig: AuthConfig): Option[ResourceToAuthorise] = {
    pathString match {
      case authConfig.pattern(utr) =>
        Some(ResourceToAuthorise(verb, Regime("sa"), AccountId(utr)))
      case _ => None
    }
  }

  override def controllerNeedsAuth(controllerName: String): Boolean = ControllerConfiguration.controllerParamsConfig(controllerName).needsAuth
}

object HeaderValidatorFilter extends Filter with HeaderValidator {
  def apply(next: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
    val controller: Option[String] = rh.tags.get(Routes.ROUTE_CONTROLLER)
    val needsHeaderValidation: Option[String] => Boolean = {
      case Some(name) => ControllerConfiguration.controllerParamsConfig(name).needsHeaderValidation
      case None => true
    }
    if (!needsHeaderValidation(controller) || acceptHeaderValidationRules(rh.headers.get("Accept"))) next(rh)
    else Future.successful(Status(ErrorAcceptHeaderInvalid.httpStatusCode)(Json.toJson(ErrorAcceptHeaderInvalid)))
  }
}

trait MicroserviceRegistration extends ServiceLocatorRegistration with ServiceLocatorConfig {
  override lazy val registrationEnabled: Boolean = AppContext.registrationEnabled
  override val slConnector: ServiceLocatorConnector = ServiceLocatorConnector(WSHttp)
  override implicit val hc: HeaderCarrier = HeaderCarrier()
}


object MicroserviceGlobal extends DefaultMicroserviceGlobal with MicroserviceRegistration with RunMode {
  override val auditConnector = MicroserviceAuditConnector

  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig(s"microservice.metrics")

  override val loggingFilter = MicroserviceLoggingFilter

  override val microserviceAuditFilter = MicroserviceAuditFilter

  override val authFilter = Some(MicroserviceAuthFilter)

  override def microserviceFilters: Seq[EssentialFilter] = Seq(HeaderValidatorFilter) ++ defaultMicroserviceFilters

}
