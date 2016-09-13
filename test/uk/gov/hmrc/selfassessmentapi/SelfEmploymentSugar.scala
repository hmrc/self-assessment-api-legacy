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

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.BalancingChargeType._
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.ExpenseType._
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.IncomeType._
import uk.gov.hmrc.selfassessmentapi.repositories.domain._
import uk.gov.hmrc.selfassessmentapi.SelfAssessmentSugar._

object SelfEmploymentSugar {

  def aSelfEmployment(id: SourceId = BSONObjectID.generate.stringify,
                      saUtr: SaUtr = generateSaUtr(),
                      taxYear: TaxYear = taxYear) =
    SelfEmployment(BSONObjectID.generate, id, saUtr, taxYear, now, now, now.toLocalDate)

  def anIncome(`type`: IncomeType, amount: BigDecimal) =
    SelfEmploymentIncomeSummary(BSONObjectID.generate.stringify, `type`, amount)

  def anExpense(`type`: ExpenseType, amount: BigDecimal) =
    SelfEmploymentExpenseSummary(BSONObjectID.generate.stringify, `type`, amount)

  def aBalancingCharge(`type`: BalancingChargeType, amount: BigDecimal) =
    SelfEmploymentBalancingChargeSummary(BSONObjectID.generate.stringify, `type`, amount)

  def aGoodsAndServices(amount: BigDecimal) =
    SelfEmploymentGoodsAndServicesOwnUseSummary(BSONObjectID.generate.stringify, amount)

}
