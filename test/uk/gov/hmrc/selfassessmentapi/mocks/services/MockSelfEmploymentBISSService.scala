/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.mocks.services

import org.scalatest.Suite
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.mocks.Mock
import uk.gov.hmrc.selfassessmentapi.models.TaxYear
import uk.gov.hmrc.selfassessmentapi.services.SelfEmploymentBISSService

trait MockSelfEmploymentBISSService extends Mock {
  _: Suite =>

  val mockSelfEmploymentBISSService = mock[SelfEmploymentBISSService]

  object MockSelfEmploymentBISSService {
    def getSummary(nino: Nino, taxYear: TaxYear, selfEmploymentId: String) = {
      when(mockSelfEmploymentBISSService.getSummary(eqTo(nino), eqTo(taxYear), eqTo(selfEmploymentId))(any(), any()))
    }
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSelfEmploymentBISSService)
  }
}
