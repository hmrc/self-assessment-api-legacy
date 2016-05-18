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


package object domain {

  type SelfEmploymentId = String
  type LiabilityId = String
  type SelfEmploymentIncomeId = String
  type SelfEmploymentExpenseId = String
  type GoodsAndServicesOwnUseId = String
  val amountValidator = Reads.of[BigDecimal].filter(ValidationError("amount should be non-negative number up to 2 decimal values",
    ErrorCode("INVALID_MONETARY_AMOUNT")))(x => x >= 0 && x.scale < 3)

  val amountNoPenceValidator = Reads.of[BigDecimal].filter(ValidationError("amount should be non-negative number and rounded to pounds",
    ErrorCode("INVALID_MONETARY_AMOUNT_NO_PENCE")))(x => x >= 0 && x.scale == 0)

}
