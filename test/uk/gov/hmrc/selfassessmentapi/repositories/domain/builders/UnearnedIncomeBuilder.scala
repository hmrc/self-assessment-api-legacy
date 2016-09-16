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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.builders

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.SelfAssessmentSugar._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{UnearnedIncome, UnearnedIncomesDividendSummary, UnearnedIncomesSavingsIncomeSummary}

case class UnearnedIncomeBuilder(objectID: BSONObjectID = BSONObjectID.generate) {
  import uk.gov.hmrc.selfassessmentapi.controllers.api.unearnedincome.DividendType._
  import uk.gov.hmrc.selfassessmentapi.controllers.api.unearnedincome.SavingsIncomeType._

  private var unearnedIncomes: UnearnedIncome = UnearnedIncome(objectID, objectID.stringify, generateSaUtr(), taxYear, now, now)

  def withSavings(savings: (SavingsIncomeType, BigDecimal)*) = {
    unearnedIncomes = unearnedIncomes.copy(savings = savings.map(saving => UnearnedIncomesSavingsIncomeSummary("", saving._1, saving._2)))
    this
  }

  def withDividends(dividends: (DividendType, BigDecimal)*) = {
    unearnedIncomes = unearnedIncomes.copy(dividends = dividends.map(dividend => UnearnedIncomesDividendSummary("", dividend._1, dividend._2)))
    this
  }

  def create() = unearnedIncomes

}
