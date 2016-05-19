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

package uk.gov.hmrc.selfassessmentapi

import play.api.data.validation.ValidationError
import play.api.libs.json.Reads
import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode._


package object domain {

  type SelfEmploymentId = String
  type SourceId = String
  type LiabilityId = String
  type SelfEmploymentIncomeId = String
  type SelfEmploymentExpenseId = String
  type SelfEmploymentBalancingChargeId = String
  type GoodsAndServicesOwnUseId = String
  def amountValidator(fieldName: String) = Reads.of[BigDecimal].filter(ValidationError(s"$fieldName should be non-negative number up to 2 decimal values",
    INVALID_MONETARY_AMOUNT))(x => x >= 0 && x.scale < 3)
}
