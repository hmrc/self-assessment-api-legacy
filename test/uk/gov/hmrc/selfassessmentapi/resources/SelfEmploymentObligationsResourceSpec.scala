/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.resources

import org.joda.time.LocalDate
import play.api.mvc.Result
import play.api.test.FakeRequest
import uk.gov.hmrc.selfassessmentapi.mocks.MockIdGenerator
import uk.gov.hmrc.selfassessmentapi.mocks.connectors.MockObligationsConnector
import uk.gov.hmrc.selfassessmentapi.mocks.services.MockAuditService
import uk.gov.hmrc.selfassessmentapi.models.SourceType
import uk.gov.hmrc.selfassessmentapi.resources.utils.ObligationQueryParams

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SelfEmploymentObligationsResourceSpec extends ResourceSpec
  with MockObligationsConnector with MockAuditService with MockIdGenerator {

  class Setup {
    val resource = new SelfEmploymentObligationsResource(
      mockAppContext,
      mockAuthorisationService,
      mockObligationsConnector,
      mockAuditService,
      cc,
      mockIdGenerator
    )
    mockAPIAction(SourceType.SelfEmployments)
    MockIdGenerator.getCorrelationId.returns("X-123")
  }

  val sourceId = "test-source-id"

  "retrieveObligations" should {
    "return a 500" when {
      "the connector returns a failed future" in new Setup {
        val request: FakeRequest[Unit] = FakeRequest().ignoreBody

        val from: Option[LocalDate] = Some(LocalDate.parse("2017-01-01"))
        val to: Option[LocalDate] = Some(LocalDate.parse("2017-12-31"))

        MockObligationsConnector.get(nino, "ITSB", Some(ObligationQueryParams(from, to)))
          .returns(Future.failed(new RuntimeException("something went wrong")))

        val result: Future[Result] = resource.retrieveObligations(nino, sourceId, ObligationQueryParams(from, to))(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
