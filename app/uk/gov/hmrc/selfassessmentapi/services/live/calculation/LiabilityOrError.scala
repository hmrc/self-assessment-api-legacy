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

package uk.gov.hmrc.selfassessmentapi.services.live.calculation

import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode.INVALID_EMPLOYMENT_TAX_PAID
import uk.gov.hmrc.selfassessmentapi.domain.UkTaxPaidForEmployment
import uk.gov.hmrc.selfassessmentapi.repositories.domain.functional._

object LiabilityOrError {
  def apply(liability: FunctionalLiability): FLiabilityResult = {
    liability.taxDeducted.ukTaxesPaidForEmployments match  {
      case taxesPaidForEmployments if doesntContainsAnyPositiveTaxPaid(taxesPaidForEmployments) =>
        FLiabilityCalculationErrors.create(liability.saUtr, liability.taxYear,
          errors = Seq(FLiabilityCalculationError(INVALID_EMPLOYMENT_TAX_PAID,
          "The UK tax paid must be positive for at least one employment source")))
      case _ => liability
    }
  }

  private def doesntContainsAnyPositiveTaxPaid(taxesPaidForEmployments: Seq[UkTaxPaidForEmployment]): Boolean = {
    taxesPaidForEmployments.nonEmpty && taxesPaidForEmployments.count(_.taxPaid >= 0) == 0
  }
}
