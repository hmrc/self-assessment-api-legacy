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

package uk.gov.hmrc.selfassessmentapi.controllers.live.selfemployment

import uk.gov.hmrc.play.http.NotImplementedException
import uk.gov.hmrc.selfassessmentapi.controllers.api.SummaryType
import uk.gov.hmrc.selfassessmentapi.controllers.{SourceHandler, SummaryHandler}
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.SourceType.SelfEmployments
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.SummaryTypes.{BalancingCharges, Expenses, GoodsAndServicesOwnUses, Incomes}
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment._
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.repositories.{SourceRepositoryWrapper, SummaryRepositoryWrapper}
import uk.gov.hmrc.selfassessmentapi.repositories.live.SelfEmploymentRepository

object SelfEmploymentSourceHandler extends SourceHandler(SelfEmployment, SelfEmployments.name) {

  override def summaryHandler(summaryType: SummaryType): Option[SummaryHandler[_]] = {
    summaryType match {
      case Incomes =>  Some(SummaryHandler(SummaryRepositoryWrapper(SelfEmploymentRepository().IncomeRepository), Income, Incomes.name))
      case Expenses => Some(SummaryHandler(SummaryRepositoryWrapper(SelfEmploymentRepository().ExpenseRepository), Expense, Expenses.name))
      case BalancingCharges => Some(SummaryHandler(SummaryRepositoryWrapper(SelfEmploymentRepository().BalancingChargeRepository), BalancingCharge, BalancingCharges.name))
      case GoodsAndServicesOwnUses => Some(SummaryHandler(SummaryRepositoryWrapper(SelfEmploymentRepository().GoodsAndServicesOwnUseRepository), GoodsAndServicesOwnUse, GoodsAndServicesOwnUses.name))
      case _ => throw new NotImplementedException(s"${SelfEmployments.name} ${summaryType.name} is not implemented")
    }
  }

  override val repository = SourceRepositoryWrapper(SelfEmploymentRepository())
}
