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

import org.joda.time.DateTime
import play.api.Logger
import uk.gov.hmrc.selfassessmentapi.repositories._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class DeleteExpiredDataService(seRepo: SelfEmploymentsRepository,
                               propsRepo: PropertiesRepository,
                               divRepo: DividendsRepository,
                               jobRepo: JobHistoryMongoRepository) {

  def deleteExpiredData(lastModifiedDate: DateTime): Future[Int] = {
    Logger.info(s"Deleting records older than lastModifiedDate: $lastModifiedDate ")

    jobRepo.startJob().flatMap { job =>
      val result = for {
        nRemoved <- deleteRecords(lastModifiedDate)
        _ <- jobRepo.completeJob(job.jobNumber, nRemoved)
      } yield nRemoved

      result.recover {
        case t => Await.result(abortJob(job.jobNumber, t), Duration.Inf)
      }
    }
  }

  private def deleteRecords(lastModifiedDate: DateTime): Future[Int] =
      for {
        seModified <- seRepo.deleteAllBeforeDate(lastModifiedDate)
        propModified <- propsRepo.deleteAllBeforeDate(lastModifiedDate)
        divModified <- divRepo.deleteAllBeforeDate(lastModifiedDate)
      } yield seModified + propModified + divModified

  private def abortJob(jobNumber: Int, t: Throwable) =
    for {
      _ <- jobRepo.abortJob(jobNumber)
    } yield throw t
}

object DeleteExpiredDataService {
  def apply() =
    new DeleteExpiredDataService(
      SelfEmploymentsRepository(),
      PropertiesRepository(),
      DividendsRepository(),
      JobHistoryRepository())
}
