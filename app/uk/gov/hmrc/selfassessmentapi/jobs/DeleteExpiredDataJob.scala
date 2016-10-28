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

import com.codahale.metrics.Gauge
import com.kenshoo.play.metrics.Metrics
import org.joda.time.{DateTime, LocalTime}
import play.Logger
import play.api.{Configuration, Play}
import uk.gov.hmrc.play.scheduling.ExclusiveScheduledJob
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.repositories.{JobHistoryMongoRepository, JobHistoryRepository}
import uk.gov.hmrc.selfassessmentapi.services.DeleteExpiredDataService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object DeleteExpiredDataJob extends ExclusiveScheduledJob {

  private lazy val config = new DeleteExpiredDataJobConfig(AppContext.deleteExpiredDataJob)

  override val name = "DeleteExpiredDataJob"

  override lazy val initialDelay = config.jobInitialDelay

  override lazy val interval = config.jobInterval

  private lazy val deleteExpiredData = new DeleteExpiredData(DeleteExpiredDataService(), JobHistoryRepository(), config)

  override lazy val isRunning = super.isRunning.flatMap(isRunning => if (isRunning) Future(true) else deleteExpiredData.isLatestJobInProgress)

  override def executeInMutex(implicit ec: ExecutionContext): Future[Result] = {
    deleteExpiredData.deleteExpiredData(config.latestModifiedDate).map { msg =>
      Logger.info(s"Finished $name.")
      Result(msg)
    }
  }


  private[jobs] class DeleteExpiredDataJobConfig(config: Configuration) {
    def latestModifiedDate = DateTime.now.minusHours(config.getInt("timeToLiveInHours").getOrElse(throw new IllegalStateException("Config key not found: timeToLiveInHours")))

    lazy val jobInterval = config.getMilliseconds("interval").getOrElse(throw new IllegalStateException("Config key not found: interval")) millisecond

    lazy val jobInitialDelay = config.getMilliseconds("initialDelay").getOrElse(calculateJobInitialDelay(stringFromConfig("startAt"))) milliseconds

    private def stringFromConfig(key: String) = config.getString(key).getOrElse(throw new IllegalStateException(s"Config key not found: $key"))

    private def calculateJobInitialDelay(startAt: String): Long = {
      val startTimeToday = LocalTime.parse(startAt).toDateTimeToday
      val nowMillis = DateTime.now.getMillis

      if (startTimeToday.isAfter(nowMillis))
        startTimeToday.getMillis - nowMillis
      else
        startTimeToday.plusDays(1).getMillis - nowMillis
    }
  }

  private[jobs] class DeleteExpiredData(service: DeleteExpiredDataService, jobRepo: JobHistoryMongoRepository, config: DeleteExpiredDataJobConfig) {

    class IntegerGauge(var value: Int) extends Gauge[Int] {
      def update(numberOfEntities: Int) = this.value = numberOfEntities

      def getValue: Int = value
    }

    object JobSuccess extends Gauge[Int] {
      val getValue = 1
    }

    object JobFailure extends Gauge[Int] {
      val getValue = 0
    }

    def isLatestJobInProgress: Future[Boolean] = {
      jobRepo.isLatestJobInProgress
    }

    def deleteExpiredData(latestModifiedDate: DateTime): Future[String] = {
      Logger.info(s"Starting $name to delete expired data older than [$latestModifiedDate]")
      service.deleteExpiredData(config.latestModifiedDate).map { recordsDeleted =>
        monitorGauge("Gauge-RowsDeleted", new IntegerGauge(recordsDeleted))
        monitorGauge("Gauge-JobStatus", JobSuccess)

        if (recordsDeleted > 0)
          s"$name Completed. Total count of self assessment records deleted [$recordsDeleted]"
        else
          s"$name Completed. No expired records found."
      }.recover {
        case ex: Throwable =>
          monitorGauge("Gauge-JobStatus", JobFailure)
          Logger.warn(ex.getMessage)
          ex.getMessage
      }
    }

    private def monitorGauge(name: String, gauge: Gauge[_]) = {
      val metrics = Play.current.injector.instanceOf[Metrics]

      val defaultRegistry = metrics.defaultRegistry
      defaultRegistry.remove(name)
      defaultRegistry.register(name, gauge)
    }
  }

}
