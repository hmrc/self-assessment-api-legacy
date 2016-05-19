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

package uk.gov.hmrc.selfassessmentapi.services.sandbox

import org.joda.time.LocalDate
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.domain.{SelfEmploymentAdjustments, SelfEmploymentAllowances, SelfEmployment, SelfEmploymentId}

import scala.concurrent.Future

object SelfEmploymentService extends uk.gov.hmrc.selfassessmentapi.services.SelfEmploymentService {

  override def create(selfEmployment: SelfEmployment): Future[SelfEmploymentId] = Future.successful(BSONObjectID.generate.stringify)

  override def findBySelfEmploymentId(utr: SaUtr, selfEmploymentId: SelfEmploymentId): Future[Option[SelfEmployment]] =
    Future.successful(Some(
      SelfEmployment(
        Some(selfEmploymentId),
        "Awesome Bakers",
        LocalDate.now,
        Some(SelfEmploymentAllowances(
          annualInvestmentAllowance = Some(BigDecimal(1000.00)),
          capitalAllowanceMainPool = Some(BigDecimal(150.00)),
          capitalAllowanceSpecialRatePool = Some(BigDecimal(5000.50)),
          restrictedCapitalAllowance = Some(BigDecimal(400.00)),
          businessPremisesRenovationAllowance = Some(BigDecimal(600.00)),
          enhancedCapitalAllowance = Some(BigDecimal(50.00)),
          allowancesOnSales = Some(BigDecimal(3399.99)))),
        Some(SelfEmploymentAdjustments(
          includedNonTaxableProfits = Some(BigDecimal(50.00)),
          basisAdjustment = Some(BigDecimal(20.10)),
          overlapReliefUsed = Some(BigDecimal(500.00)),
          accountingAdjustment = Some(BigDecimal(10.50)),
          averagingAdjustment = Some(BigDecimal(-400.99)),
          lossBroughtForward = Some(BigDecimal(10000.00)),
          outstandingBusinessIncome = Some(BigDecimal(50.00))))
      )))

  override def find(saUtr: SaUtr): Future[Seq[SelfEmployment]] =
    Future.successful(Seq(SelfEmployment(Some("1234"), "Awesome Plumbers", new LocalDate(2015, 1, 1)),
      SelfEmployment(Some("5678"), "Awesome Bakers", new LocalDate(2015, 10, 1)),
      SelfEmployment(Some("9101"), "Average Accountants", new LocalDate(2015, 10, 11))))

  override def update(selfEmployment: SelfEmployment, utr: SaUtr, selfEmploymentId: SelfEmploymentId): Future[Unit] =
    Future.successful(())

  override def delete(utr: SaUtr, selfEmploymentId: SelfEmploymentId): Future[Boolean] =
    Future.successful(true)
}
