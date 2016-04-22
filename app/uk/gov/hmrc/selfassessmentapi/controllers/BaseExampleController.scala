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

import play.api.Logger
import play.api.libs.json.Json
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.play.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.selfassessmentapi.services.BaseExampleService
import scala.concurrent.ExecutionContext.Implicits.global

trait BaseExampleController extends BaseController with HeaderValidator {
  val service: BaseExampleService
  implicit val hc: HeaderCarrier = HeaderCarrier()

  final def fetchExample(utr: SaUtr) = validateAccept(acceptHeaderValidationRules).async {
    service.fetchExample(utr).map(as => Ok(Json.toJson(as))
    ) recover {
      case ex: NotFoundException => Status(ErrorNotFound.httpStatusCode)(Json.toJson(ErrorNotFound))
      case e: Throwable =>
        Logger.error(s"Internal server error: ${e.getMessage}", e)
        Status(ErrorInternalServerError.httpStatusCode)(Json.toJson(ErrorInternalServerError))
    }
  }
}
