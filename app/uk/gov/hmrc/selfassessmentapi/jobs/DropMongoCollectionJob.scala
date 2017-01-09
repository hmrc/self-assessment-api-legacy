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

package uk.gov.hmrc.selfassessmentapi.jobs

import java.util.concurrent.TimeUnit

import play.Logger
import play.modules.reactivemongo.MongoDbConnection
import reactivemongo.api.collections.bson.BSONCollection
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.play.scheduling.ExclusiveScheduledJob
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.repositories.JobHistoryRepository
import uk.gov.hmrc.selfassessmentapi.repositories.live._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object DropMongoCollectionJob extends ExclusiveScheduledJob with MongoDbConnection {

  override val name = "DropMongoCollectionsJob"

  override lazy val initialDelay = AppContext.dropMongoCollectionJob.getMilliseconds("initialDelay").getOrElse(throw new IllegalStateException("Config key not found: initialDelay")) millisecond

  override lazy val interval = FiniteDuration(180, TimeUnit.DAYS)

  private val reposToBeRecreated = Seq(SelfEmploymentRepository())

  private lazy val mongoDatabase = new MongoDatabase(reposToBeRecreated)

  override lazy val isRunning = super.isRunning.flatMap(isRunning => if (isRunning) Future(true) else mongoDatabase.isLatestJobInProgress)

  override def executeInMutex(implicit ec: ExecutionContext): Future[Result] = {
    mongoDatabase.dropAndRecreateCollections().map { msg =>
      Logger.info(s"Finished $name.")
      Result(msg)
    }
  }


  private class MongoDatabase(reposToBeRecreated: Seq[ReactiveRepository[_, _]]) {
    private val jobRepo = JobHistoryRepository()

    def isLatestJobInProgress: Future[Boolean] = {
      jobRepo.isLatestJobInProgress
    }

    def dropAndRecreateCollections(): Future[String] = {
      Logger.info(s"Starting $name drop and recreated mongo collections.")

      jobRepo.startJob().flatMap { job =>
        val dropUI: Future[Boolean] = dropUnearnedIncomes()
        dropUI.flatMap { dropUI =>
          val result: Future[Seq[Seq[Boolean]]] = recreateCollections()
          result.map { resultSeq =>
            jobRepo.completeJob(job.jobNumber, 0)
            val status = resultSeq.flatten.forall(_ == true)
            if (dropUI && status)
              s"$name Completed. Dropped and recreated mongo collections successfully."
            else
              s"$name Completed. Could not drop and recreate mongo collections."
          }.recover {
            case ex: Throwable =>
              jobRepo.abortJob(job.jobNumber)
              Logger.warn(ex.getMessage)
              ex.getMessage
          }
        }
      }
    }

    private def recreateCollections(): Future[Seq[Seq[Boolean]]] = {
      Future.sequence {
        reposToBeRecreated.map { repo =>
          for {
            _ <- repo.drop
            status <- repo.ensureIndexes
          } yield status
        }
      }
    }

    private def dropUnearnedIncomes(): Future[Boolean] = {
      val db = mongoConnector.db()
      for {
        _ <- db.collection[BSONCollection]("unearnedIncomes").drop()
      } yield true
    }
  }

}
