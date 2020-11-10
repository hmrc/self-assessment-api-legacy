/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.mocks.connectors

import org.joda.time.LocalDate
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Suite
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.connectors.SelfEmploymentPeriodConnector
import uk.gov.hmrc.selfassessmentapi.mocks.Mock
import uk.gov.hmrc.selfassessmentapi.models.SourceId
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.{SelfEmploymentPeriod, SelfEmploymentPeriodUpdate}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.SelfEmploymentPeriodResponse

import scala.concurrent.Future

trait MockSelfEmploymentPeriodConnector extends Mock {
  _: Suite =>

  val mockSelfEmploymentPeriodConnector: SelfEmploymentPeriodConnector = mock[SelfEmploymentPeriodConnector]

  object MockSelfEmploymentPeriodConnector {
    def create(nino: Nino, id: SourceId, selfEmploymentPeriod: SelfEmploymentPeriod): OngoingStubbing[Future[SelfEmploymentPeriodResponse]] = {
      when(mockSelfEmploymentPeriodConnector.create(eqTo(nino), eqTo(id), eqTo(selfEmploymentPeriod))(any(), any(), any()))
    }

    def get(nino: Nino, id: SourceId, from: LocalDate, to: LocalDate): OngoingStubbing[Future[SelfEmploymentPeriodResponse]] = {
      when(mockSelfEmploymentPeriodConnector.get(eqTo(nino), eqTo(id), eqTo(from), eqTo(to))(any(), any(), any()))
    }

    def getAll(nino: Nino, id: SourceId): OngoingStubbing[Future[SelfEmploymentPeriodResponse]] = {
      when(mockSelfEmploymentPeriodConnector.getAll(eqTo(nino), eqTo(id))(any(), any(), any()))
    }

    def update(nino: Nino, id: SourceId, from: LocalDate, to: LocalDate, update: SelfEmploymentPeriodUpdate): OngoingStubbing[Future[SelfEmploymentPeriodResponse]] = {
      when(mockSelfEmploymentPeriodConnector.update(eqTo(nino), eqTo(id), eqTo(from), eqTo(to), eqTo(update))(any(), any(), any()))
    }
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSelfEmploymentPeriodConnector)
  }
}
