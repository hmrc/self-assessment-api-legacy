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

import java.lang.Integer._

import org.joda.time.LocalDate
import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.controllers.api.TaxYearProperties

trait TaxYearDiscoveryController extends BaseController with Links {

  protected def taxYearValidationErrors(path: String, yearFromBody: LocalDate, yearFromUrl: String) = {
    val startYear: Int = parseInt(yearFromUrl.split("-")(0))
    val startOfTaxYear = new LocalDate(startYear, 4, 6)
    val endOfTaxYear = new LocalDate(startYear + 1, 4, 5)

    (yearFromBody.isBefore(startOfTaxYear), yearFromBody.isAfter(endOfTaxYear)) match {
      case (true, _) => Some(InvalidPart(BENEFIT_STOPPED_DATE_INVALID, s"The dateBenefitStopped must be after the start of the tax year: $yearFromUrl", path))
      case (_, true) => Some(InvalidPart(BENEFIT_STOPPED_DATE_INVALID, s"The dateBenefitStopped must be before the end of the tax year: $yearFromUrl", path))
      case _ => None
    }
  }

  protected def validateRequest(taxYearProperties: TaxYearProperties, taxYear: String) = {
    for {
      childBenefit <- taxYearProperties.childBenefit
      dateBenefitStopped <- childBenefit.dateBenefitStopped
      taxYearValidationResult <- taxYearValidationErrors("/taxYearProperties/childBenefit/dateBenefitStopped",
        dateBenefitStopped, taxYear)
    } yield taxYearValidationResult
  }


}
