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
import play.api.libs.json.JsValue
import play.api.mvc.Result
import play.api.test.FakeRequest
import uk.gov.hmrc.selfassessmentapi.mocks.MockIdGenerator
import uk.gov.hmrc.selfassessmentapi.fixtures.selfemployment.SelfEmploymentPeriodFixture
import uk.gov.hmrc.selfassessmentapi.mocks.connectors.MockSelfEmploymentPeriodConnector
import uk.gov.hmrc.selfassessmentapi.mocks.services.MockAuditService
import uk.gov.hmrc.selfassessmentapi.models.SourceType
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.SelfEmploymentPeriod

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SelfEmploymentPeriodResourceSpec extends ResourceSpec
  with MockSelfEmploymentPeriodConnector
  with MockAuditService
  with MockIdGenerator {

  class Setup {
    val resource = new SelfEmploymentPeriodResource(
      mockAppContext,
      mockAuthorisationService,
      mockSelfEmploymentPeriodConnector,
      mockAuditService,
      cc,
      mockIdGenerator
    )
    mockAPIAction(SourceType.SelfEmployments)
    MockIdGenerator.getCorrelationId.returns("X-123")
  }

  val sourceId = "test-source-id"

  val from: LocalDate = LocalDate.parse("2017-04-06")
  val to: LocalDate = LocalDate.parse("2018-04-05")

  val selfEmploymentPeriod: SelfEmploymentPeriod = SelfEmploymentPeriodFixture.period(None, from, to)
  val selfEmploymentPeriodJson: JsValue = Jsons.SelfEmployment.period(Some(from.toString), Some(to.toString))

  "createPeriod" should {
    "return a 500" when {
      "the connector returns a failed future" in new Setup {
        val request: FakeRequest[JsValue] = FakeRequest().withBody[JsValue](selfEmploymentPeriodJson)

        MockSelfEmploymentPeriodConnector.create(nino, sourceId, selfEmploymentPeriod)
          .returns(Future.failed(new RuntimeException("something went wrong")))

        val result: Future[Result] = resource.createPeriod(nino, sourceId)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result) shouldBe None
      }
    }
  }
}
