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
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.DividendType.{apply => _, _}
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.SavingsIncomeType._
import uk.gov.hmrc.selfassessmentapi.domain.{SourceId, SummaryId, TaxYear}
import uk.gov.hmrc.selfassessmentapi.repositories.domain._

trait UnearnedIncomesSugar extends SelfAssessmentSugar {

  this: UnitSpec =>

  def anUnearnedIncomes(id: SourceId = BSONObjectID.generate.stringify,
                        saUtr: SaUtr = generateSaUtr(),
                        taxYear: TaxYear = taxYear) =
    MongoUnearnedIncome(BSONObjectID.generate, id, saUtr, taxYear, now, now)

  def anUnearnedInterestIncomeSummary(summaryId: SummaryId = BSONObjectID.generate.stringify,
                                      `type`: SavingsIncomeType = InterestFromBanksUntaxed,
                                      amount: BigDecimal) =
    MongoUnearnedIncomesSavingsIncomeSummary(summaryId, `type`, amount)

  def anUnearnedDividendIncomeSummary(summaryId: SummaryId = BSONObjectID.generate.stringify,
                                      `type`: DividendType = FromUKCompanies,
                                      amount: BigDecimal) =
    MongoUnearnedIncomesDividendSummary(summaryId, `type`, amount)
}
