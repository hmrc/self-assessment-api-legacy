/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models.des.selfemployment

import play.api.libs.json.{Json, Reads, Writes}

case class Deductions(costOfGoods: Option[Deduction] = None,
                      constructionIndustryScheme: Option[Deduction] = None,
                      staffCosts: Option[Deduction] = None,
                      travelCosts: Option[Deduction] = None,
                      premisesRunningCosts: Option[Deduction] = None,
                      maintenanceCosts: Option[Deduction] = None,
                      adminCosts: Option[Deduction] = None,
                      advertisingCosts: Option[Deduction] = None,
                      businessEntertainmentCosts: Option[Deduction] = None,
                      interest: Option[Deduction] = None,
                      financialCharges: Option[Deduction] = None,
                      badDebt: Option[Deduction] = None,
                      professionalFees: Option[Deduction] = None,
                      depreciation: Option[Deduction] = None,
                      other: Option[Deduction] = None,
                      simplifiedExpenses: Option[BigDecimal] = None)

object Deductions {
  implicit val writes: Writes[Deductions] = Json.writes[Deductions]
  implicit val reads: Reads[Deductions] = Json.reads[Deductions]
}

case class Deduction(amount: BigDecimal, disallowableAmount: Option[BigDecimal])

object Deduction {
  implicit val writes: Writes[Deduction] = Json.writes[Deduction]
  implicit val reads: Reads[Deduction] = Json.reads[Deduction]
}
