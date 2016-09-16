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

import org.joda.time.{DateTime, DateTimeZone}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.controllers._
import uk.gov.hmrc.selfassessmentapi.controllers.api.{TaxDeducted => _, TaxYearProperties => _, _}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{Liability, SelfAssessment => _, _}

object SelfAssessmentSugar extends UnitSpec {

  def now = DateTime.now(DateTimeZone.UTC)

  def aTaxYearProperty = TaxYearProperties(BSONObjectID.generate, generateSaUtr(), taxYear, now, now)

  def aSelfAssessment(employments: Seq[Employment] = Nil,
                      selfEmployments: Seq[SelfEmployment] = Nil,
                      unearnedIncomes: Seq[UnearnedIncome] = Nil,
                      ukProperties: Seq[UKProperties] = Nil,
                      taxYearProperties: Option[api.TaxYearProperties] = None,
                      furnishedHolidayLettings: Seq[FurnishedHolidayLettings] = Nil) =
    SelfAssessment(employments,
                   selfEmployments,
                   unearnedIncomes,
                   ukProperties,
                   taxYearProperties,
                   furnishedHolidayLettings)

}
