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

package uk.gov.hmrc.selfassessmentapi.resources

import javax.inject.Inject
import play.api.libs.json.{JsArray, JsValue, Json, Writes}
import play.api.mvc.{Action, ControllerComponents, Result}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.connectors.SelfEmploymentConnector
import uk.gov.hmrc.selfassessmentapi.models.Errors.Error
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.models.des.selfemployment.Business
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.SelfEmployment
import uk.gov.hmrc.selfassessmentapi.resources.wrappers._
import uk.gov.hmrc.selfassessmentapi.services.AuthorisationService
import uk.gov.hmrc.utils.IdGenerator

import scala.concurrent.ExecutionContext.Implicits._

class SelfEmploymentsResource @Inject()(
                                         override val appContext: AppContext,
                                         override val authService: AuthorisationService,
                                         connector: SelfEmploymentConnector,
                                         cc: ControllerComponents,
                                         val idGenerator: IdGenerator
                                       ) extends BaseResource(cc) {

  def create(nino: Nino): Action[JsValue] =
    APIAction(nino, SourceType.SelfEmployments).async(parse.json) { implicit request =>
      implicit val correlationID: String = idGenerator.getCorrelationId
      logger.warn(message = s"[SelfEmploymentsResource][create] " +
        s"create self-employment with correlationId : $correlationID")

      validate[SelfEmployment, SelfEmploymentResponse](request.body) { selfEmployment =>
        connector.create(nino, Business.from(selfEmployment))
      } map {
        case Left(errorResult) => handleErrors(errorResult)
        case Right(response) =>
          response.filter {
            case 200 => logger.warn(message = s"[SelfEmploymentsResource][create] " +
              s"Success response with correlationId : ${correlationId(response)}")
              Created.withHeaders(LOCATION -> response.createLocationHeader(nino).getOrElse(""))
            case 403 =>
              logger.warn(message = s"[SelfEmploymentsResource][create] " +
                s"Error response TOO_MANY_SOURCES with correlationId : ${correlationId(response)}")
              Forbidden(
                Json.toJson(
                  Errors.businessError(Error(ErrorCode.TOO_MANY_SOURCES.toString,
                    s"The maximum number of Self-Employment incomes sources is 1",
                    Some("")))))
          }
      } recoverWith exceptionHandling
    }

  // Removed def update as it has been disabled since Bravo update (in 2017)

  def retrieve(nino: Nino, id: SourceId): Action[Unit] =
    APIAction(nino, SourceType.SelfEmployments).async(parse.empty) { implicit request =>
      implicit val correlationID: String = idGenerator.getCorrelationId
      logger.warn(message = s"[SelfEmploymentsResource][retrieve] " +
        s"retrieve self-employment with correlationId : $correlationID")
      connector.get(nino).map { response =>
        response.filter {
          case 200 => logger.warn(message = s"[SelfEmploymentsResource][retrieve] " +
            s"Success response with correlationId : ${correlationId(response)}")
            handleRetrieve(response.selfEmployment(id), NotFound)
        }
      } recoverWith exceptionHandling
    }

  def retrieveAll(nino: Nino): Action[Unit] =
    APIAction(nino, SourceType.SelfEmployments).async(parse.empty) { implicit request =>
      implicit val correlationID: String = idGenerator.getCorrelationId
      logger.warn(message = s"[SelfEmploymentsResource][retrieveAll] " +
        s"retrieveAll self-employments with correlationId : $correlationID")

      connector.get(nino).map { response =>
        response.filter {
          case 200 => logger.warn(message = s"[SelfEmploymentsResource][retrieveAll] " +
            s"Success response with correlationId : ${correlationId(response)}")
            handleRetrieve(response.listSelfEmployment, Ok(JsArray()))
        }
      } recoverWith exceptionHandling
    }

  private def handleRetrieve[T](selfEmployments: Either[DesTransformError, T], resultOnEmptyData: Result)(
    implicit w: Writes[T]): Result =
    selfEmployments match {
      case error@Left(EmptyBusinessData(_) | EmptySelfEmployments(_)) =>
        logger.warn(error.left.get.msg)
        resultOnEmptyData
      case Left(UnmatchedIncomeId(msg)) =>
        logger.warn(msg)
        NotFound
      case error@Left(DesTransformError(msg)) =>
        error match {
          case Left(ParseError(_)) => logger.warn(msg)
          case _ => logger.warn(msg)
        }
        InternalServerError(Json.toJson(Errors.InternalServerError))
      case Right(se) => Ok(Json.toJson(se))
    }
}
