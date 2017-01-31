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

package uk.gov.hmrc.selfassessmentapi.services

import org.joda.time.LocalDate
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.repositories.SelfEmploymentsRepository
import uk.gov.hmrc.selfassessmentapi.resources.models.Errors.{BadRequest, Error}
import uk.gov.hmrc.selfassessmentapi.resources.models._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait SelfEmploymentObligationsService {
  val repository: SelfEmploymentsRepository

  def retrieveObligations(nino: Nino, id: SourceId, testScenario: Option[String]): Future[Option[Either[BadRequest, Obligations]]] = {
    repository.retrieve(id, nino) map {
      case Some(employment) => Some(simulateResponse(testScenario)(employment.accountingPeriod))
      case None => None
    }
  }

  private def simulateResponse(header: Option[String])(implicit period: AccountingPeriod): Either[BadRequest, Obligations] = {
    header match {
      case Some("NONE_MET") =>
        standardYearCannedResponse()
      case Some("ALL_MET") =>
        standardYearCannedResponse(firstMet = true, secondMet = true, thirdMet = true, fourthMet = true)
      case Some(_) | None =>
        accountingPeriodDynamicCannedResponse({
          val sixMonthsFromStartDate = period.start.plusMonths(6).minusDays(1)
          period.end.isAfter(sixMonthsFromStartDate)
        }, "Invalid Accounting period, should be greater than 6 months period.")
    }
  }

  private def standardYearCannedResponse(firstMet: Boolean = false, secondMet: Boolean = false, thirdMet: Boolean = false,
                                         fourthMet: Boolean = false): Either[BadRequest, Obligations] = {
    Right(
      Obligations(
        Seq(
          Obligation(LocalDate.parse("2017-04-06"), LocalDate.parse("2017-07-05"), firstMet),
          Obligation(LocalDate.parse("2017-07-06"), LocalDate.parse("2017-10-05"), secondMet),
          Obligation(LocalDate.parse("2017-10-06"), LocalDate.parse("2018-01-05"), thirdMet),
          Obligation(LocalDate.parse("2018-01-06"), LocalDate.parse("2018-04-05"), fourthMet)
        ).sorted)
    )
  }

  private def accountingPeriodDynamicCannedResponse(validation: => Boolean, errorMessage: String)
                                                   (implicit period: AccountingPeriod): Either[BadRequest, Obligations] = {
    if (validation) {
      Right(Obligations(calculateDynamicObligations(period.start, period.end, Seq())))
    }
    else {
      Left(Errors.badRequest(errorMessage))
    }
  }

  private def calculateDynamicObligations(start: LocalDate, end: LocalDate, obligations: Seq[Obligation]): Seq[Obligation] = {
    if (start.isAfter(end))
      obligations
    else {
      val endDate = if (end.isBefore(start.plusMonths(3))) end else start.plusMonths(3).minusDays(1)
      val obligation = Obligation(start, endDate, obligations.isEmpty)
      calculateDynamicObligations(start.plusMonths(3), end, obligations ++ Seq(obligation))
    }
  }
}

object SelfEmploymentObligationsService extends SelfEmploymentObligationsService {
  override val repository: SelfEmploymentsRepository = SelfEmploymentsRepository()
}
