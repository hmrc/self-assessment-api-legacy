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

package uk.gov.hmrc.selfassessmentapi.repositories

import _root_.reactivemongo.api.DB
import org.joda.time.DateTime
import play.api.libs.json.Json
import play.modules.reactivemongo.MongoDbConnection
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.selfassessmentapi.domain.JobHistory
import uk.gov.hmrc.selfassessmentapi.domain.JobStatus._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object JobHistoryRepository extends MongoDbConnection {
  private lazy val repository = new JobHistoryMongoRepository

  def apply(): JobHistoryMongoRepository = repository
}

class JobHistoryMongoRepository(implicit mongo: () => DB)
  extends ReactiveRepository[JobHistory, BSONObjectID](
  collectionName = "jobHistory", mongo = mongo, domainFormat = JobHistory.mongoFormats){


  override def indexes: Seq[Index] = Seq(
    Index(key = Seq("jobNumber" -> IndexType.Ascending), name = Some("job_number"), unique = true))

  def startJob(): Future[JobHistory] =
    findLatestJob.flatMap {
      case Some(latestJob) if latestJob.isInProgress => throw JobAlreadyInProgressException()
      case latestJob =>
        val nextJobNumber = latestJob.map(_.jobNumber + 1).getOrElse(1)
        val mongoJobHistory = JobHistory(nextJobNumber, InProgress)
        insert(mongoJobHistory).map {
          case result if result.n == 0 => throw CannotStartJobException(result.getCause)
          case _ => mongoJobHistory
        }
    }

  def updateJobProgress(jobNumber: Int, recordsDeleted: Int): Future[Unit] =
    collection
      .update(Json.obj("jobNumber" -> jobNumber), Json.obj("$inc" -> Json.obj("recordsDeleted" -> recordsDeleted)))
      .map(writeResult => if (writeResult.n == 0) throw JobNotFoundException(jobNumber))

  def completeJob(jobNumber: Int, recordsDeleted: Int): Future[Unit] =
    collection
      .update(Json.obj("jobNumber" -> jobNumber), Json.obj("$set" -> Json.obj("status" ->  Success, "finishedAt" -> DateTime.now, "recordsDeleted" -> recordsDeleted)))
      .map(writeResult => if (writeResult.n == 0) throw JobNotFoundException(jobNumber))

  def abortJob(jobNumber: Int): Future[Unit] = {
    collection
      .update(Json.obj("jobNumber" -> jobNumber), Json.obj("$set" -> Json.obj("status" -> Failed, "finishedAt" -> DateTime.now)))
      .map(writeResult => if (writeResult.n == 0) throw JobNotFoundException(jobNumber))
  }

  def isLatestJobInProgress: Future[Boolean] = findLatestJob.map(latestJob => latestJob.exists(_.isInProgress))

  private def findLatestJob: Future[Option[JobHistory]] = {
    collection
      .find(Json.obj())
      .sort(Json.obj("jobNumber" -> -1))
      .one[JobHistory]
  }

}

case class JobNotFoundException(jobNumber: Int) extends RuntimeException(s"Job with number [$jobNumber] not found")

case class CannotStartJobException(t: Throwable) extends RuntimeException("Cannot start a new job", t)

case class JobAlreadyInProgressException() extends RuntimeException("Job not started as previous job is still running")
