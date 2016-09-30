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

import play.Logger
import uk.gov.hmrc.play.scheduling.ExclusiveScheduledJob
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.repositories.live.{LiabilityMongoRepository, LiabilityRepository}
import uk.gov.hmrc.selfassessmentapi.repositories.{JobHistoryMongoRepository, JobHistoryRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object DropMongoCollectionJob extends ExclusiveScheduledJob {

  override val name = "DropMongoCollectionJob"

  override lazy val initialDelay = AppContext.dropMongoCollectionJob.getMilliseconds("initialDelay").getOrElse(throw new IllegalStateException("Config key not found: initialDelay")) millisecond

  override lazy val interval = AppContext.dropMongoCollectionJob.getMilliseconds("interval").getOrElse(throw new IllegalStateException("Config key not found: interval")) millisecond

  private lazy val dropMongoCollection = new DropMongoLiabilityCollection(LiabilityRepository(), JobHistoryRepository())

  override lazy val isRunning = super.isRunning.flatMap(isRunning => if (isRunning) Future(true) else dropMongoCollection.isLatestJobInProgress)

  override def executeInMutex(implicit ec: ExecutionContext): Future[Result] = {
    dropMongoCollection.dropMongoCollection().map { msg =>
      Logger.info(s"Finished $name.")
      Result(msg)
    }
  }


  private class DropMongoLiabilityCollection(repo: LiabilityMongoRepository, jobRepo: JobHistoryMongoRepository) {

    def isLatestJobInProgress: Future[Boolean] = {
      jobRepo.isLatestJobInProgress
    }

    def dropMongoCollection(): Future[String] = {
      Logger.info(s"Starting $name drop the mongo liability collection.")

      jobRepo.startJob().flatMap { job =>
        repo.drop.map { status =>
          jobRepo.completeJob(job.jobNumber, 0)
          if (status)
            s"$name Completed. Dropped the mongo liability collection successfully."
          else
            s"$name Completed. Could not drop the mongo liability collection."
        }.recover {
          case ex: Throwable =>
            jobRepo.abortJob(job.jobNumber)
            Logger.warn(ex.getMessage)
            ex.getMessage
        }
      }

    }

  }

}
