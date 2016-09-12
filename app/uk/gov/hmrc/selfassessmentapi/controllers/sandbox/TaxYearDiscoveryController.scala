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

package uk.gov.hmrc.selfassessmentapi.controllers.sandbox

import java.lang.Integer._

import org.joda.time.LocalDate
import play.api.libs.json.Json
import play.api.libs.json.Json._
import play.api.mvc.Action
import play.api.mvc.hal._
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.controllers._
import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode
import ErrorCode._
import uk.gov.hmrc.selfassessmentapi.controllers.api.TaxYear
import uk.gov.hmrc.selfassessmentapi.domain.TaxYearProperties
import uk.gov.hmrc.selfassessmentapi.services.sandbox.TaxYearPropertiesService
import uk.gov.hmrc.selfassessmentapi.views.Helpers._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object TaxYearDiscoveryController extends BaseController with Links {
  override val context: String = AppContext.apiGatewayLinkContext
  private val service = TaxYearPropertiesService()

  final def discoverTaxYear(utr: SaUtr, taxYear: TaxYear) = Action.async { request =>
    Future.successful(Ok(halResource(toJson(service.findTaxYearProperties(utr, taxYear)), discoveryLinks(utr, taxYear))))
  }

  private def taxYearValidationErrors(path: String, yearFromBody : LocalDate, yearFromUrl: String) = {
    val endOfTaxYear = new LocalDate(parseInt(yearFromUrl.split("-")(0)) + 1, 4, 5)
    if (yearFromBody.isAfter(endOfTaxYear)) {
      Some(InvalidPart(BENEFIT_STOPPED_DATE_INVALID, s"The dateBenefitStopped must be before the end of the tax year: $yearFromUrl", path))
    } else None
  }

  private def validateRequest(taxYearProperties: TaxYearProperties, taxYear: String) = {
    for {
        childBenefit <- taxYearProperties.childBenefit
        dateBenefitStopped <- childBenefit.dateBenefitStopped
        taxYearValidationResult <- taxYearValidationErrors("/taxYearProperties/childBenefit/dateBenefitStopped",
                                                           dateBenefitStopped, taxYear)
    } yield taxYearValidationResult
  }

  final def update(utr: SaUtr, taxYear: TaxYear) = Action.async(parse.json) { implicit request =>
    withJsonBody[TaxYearProperties] {
      taxYearProperties =>
        validateRequest(taxYearProperties, taxYear.taxYear) match {
          case Some(invalidPart) => Future.successful(BadRequest(Json.toJson(InvalidRequest(ErrorCode.INVALID_REQUEST, "Invalid request", Seq(invalidPart)))))
          case None => service.updateTaxYearProperties(utr, taxYear, taxYearProperties).map { updated =>
            if (updated) Ok(halResource(obj(), discoveryLinks(utr, taxYear)))
            else NotImplemented(Json.toJson(ErrorFeatureSwitched))
          }
        }
    }
  }
}
