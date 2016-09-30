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

package uk.gov.hmrc.selfassessmentapi.jobs

import play.api.test.FakeApplication
import uk.gov.hmrc.selfassessmentapi.TestApplication
import uk.gov.hmrc.selfassessmentapi.config.MicroserviceGlobal


class JobDisabledSpec extends TestApplication {

  override lazy val app = FakeApplication(additionalConfiguration = Map("Test.scheduling.deleteExpiredDataJob.enabled" -> false,
    "Test.scheduling.dropMongoCollectionJob.enabled" -> false))

  "DeleteExpiredDataJob" should {

    "Not be added to the scheduler jobs when disabled" in {
      MicroserviceGlobal.createScheduledJobs().size shouldBe 0
    }

  }

  "DropMongoCollectionJob" should {

    "Not be added to the scheduler jobs when disabled" in {
      MicroserviceGlobal.createScheduledJobs().size shouldBe 0
    }

  }

}
