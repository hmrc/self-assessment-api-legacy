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

import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.domain.TaxYear
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{LiabilityResult, MongoLiabilityCalculationError, MongoLiabilityCalculationErrors}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.functional._
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps.SelfAssessment

import scala.util.{Failure, Success, Try}

object LiabilityOrError {
  def apply(saUtr: SaUtr, taxYear: TaxYear, assessment: SelfAssessment): FLiabilityResult = {
    Try(FunctionalLiability.create(saUtr, taxYear, assessment)) match {
      case Success(liability) => liability
      case Failure(ex: LiabilityCalculationException) => FLiabilityCalculationErrors.create(saUtr, taxYear,
        errors = Seq(FLiabilityCalculationError(ex.errorCode, ex.message)))
      case Failure(ex) => throw ex
    }
  }

}
