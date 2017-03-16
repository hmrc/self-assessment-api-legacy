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

package uk.gov.hmrc.selfassessmentapi.resources.wrappers

import play.api.Logger
import play.api.libs.json.JsValue
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.models.{PeriodSummary, SourceId, SourceType, des}
import uk.gov.hmrc.selfassessmentapi.models.des.{DesError, DesErrorCode}
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.SelfEmploymentPeriod

class SelfEmploymentPeriodResponse(underlying: HttpResponse) {

  private val logger: Logger = Logger(classOf[SelfEmploymentResponse])

  val status: Int = underlying.status
  def json: JsValue = underlying.json

  def createLocationHeader(nino: Nino, id: SourceId): Option[String] = {
    (json \ "transactionReference").asOpt[String] match {
      case Some(periodId) =>
        Some(s"/self-assessment/ni/$nino/${SourceType.SelfEmployments.toString}/$id/periods/$periodId")
      case None => {
        logger.error("The 'transactionReference' field was not found in the response from DES.")
        None
      }
    }
  }

  def containsOverlappingPeriod: Boolean = {
    json.asOpt[DesError] match {
      case Some(err) => err.code == DesErrorCode.INVALID_PERIOD
      case None => {
        logger.error("The response from DES does not match the expected error format.")
        false
      }
    }
  }

  def period: Option[SelfEmploymentPeriod] = {
    json.asOpt[des.SelfEmploymentPeriod] match {
      case Some(desPeriod) => Some(SelfEmploymentPeriod.from(desPeriod))
      case None => {
        logger.error("The response from DES does not match the expected self-employment period format.")
        None
      }
    }
  }

  def allPeriods: Seq[PeriodSummary] = {
    json.asOpt[Seq[des.SelfEmploymentPeriod]] match {
      case Some(desPeriods) => desPeriods.map(period => SelfEmploymentPeriod.from(period).asSummary)
      case None => {
        logger.error("The response from DES does not match the expected self-employment period format.")
        Seq.empty
      }
    }
  }
}

object SelfEmploymentPeriodResponse {
  def apply(response: HttpResponse): SelfEmploymentPeriodResponse = new SelfEmploymentPeriodResponse(response)
}
