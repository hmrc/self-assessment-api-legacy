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

package uk.gov.hmrc.selfassessmentapi.services

import org.joda.time.DateTime
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.MongoEmbeddedDatabase
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.{SelfEmployment => _}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.JobStatus._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{Employment, JobHistory, SelfAssessment, SelfEmployment, FurnishedHolidayLettings, UKProperties}
import uk.gov.hmrc.selfassessmentapi.repositories.live._
import uk.gov.hmrc.selfassessmentapi.repositories.{JobHistoryMongoRepository, SelfAssessmentMongoRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class DeleteExpiredDataServiceSpec extends MongoEmbeddedDatabase with MockitoSugar with ScalaFutures {

  private val saRepo = new SelfAssessmentMongoRepository
  private val seRepo = new SelfEmploymentMongoRepository
  private val uiRepo = new UnearnedIncomeMongoRepository
  private val empRepo = new EmploymentMongoRepository
  private val fhlRepo = new FurnishedHolidayLettingsMongoRepository
  private val ukPropertyRepo = new UKPropertiesMongoRepository
  private val jobRepo = new JobHistoryMongoRepository

  val saUtr = generateSaUtr()
  val lastModifiedDate = DateTime.now().minusWeeks(1)

  implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds), interval = Span(300, Millis))

  "deleteExpiredData" should {

    "delete only the expired records (older than the lastModifiedDate) and not the latest records which have not expired" ignore {
      val lastModifiedDate = DateTime.now.minusWeeks(1)
      val saUtr2 = generateSaUtr()
      val saUtr3 = generateSaUtr()

      val sa1 = SelfAssessment(BSONObjectID.generate, saUtr, taxYear, DateTime.now().minusMonths(1), DateTime.now().minusMonths(1))
      val sa2 = SelfAssessment(BSONObjectID.generate, saUtr2, taxYear, DateTime.now().minusMonths(2), DateTime.now().minusMonths(2))
      val latestSa3 = SelfAssessment(BSONObjectID.generate, saUtr3, taxYear, DateTime.now().minusDays(1), DateTime.now().minusDays(1))

      val se1 = SelfEmployment.create(saUtr, taxYear, selfemployment.SelfEmployment.example())
      val se2 = SelfEmployment.create(saUtr2, taxYear, selfemployment.SelfEmployment.example())
      val latestSe3 = SelfEmployment.create(saUtr3, taxYear, selfemployment.SelfEmployment.example())

      val emp1 = Employment.create(saUtr, taxYear, employment.Employment.example())
      val emp2 = Employment.create(saUtr2, taxYear, employment.Employment.example())
      val latestEmp3 = Employment.create(saUtr3, taxYear, employment.Employment.example())

      val fhl1 = FurnishedHolidayLettings.create(saUtr, taxYear, furnishedholidaylettings.FurnishedHolidayLetting.example())
      val fhl2 = FurnishedHolidayLettings.create(saUtr2, taxYear, furnishedholidaylettings.FurnishedHolidayLetting.example())
      val latestFhl3 = FurnishedHolidayLettings.create(saUtr3, taxYear, furnishedholidaylettings.FurnishedHolidayLetting.example())

      val property1 = UKProperties.create(saUtr, taxYear, ukproperty.UKProperty.example())
      val property2 = UKProperties.create(saUtr2, taxYear, ukproperty.UKProperty.example())
      val latestProperty3 = UKProperties.create(saUtr3, taxYear, ukproperty.UKProperty.example())


      insertSelfAssessmentRecords(sa1, sa2, latestSa3)
      insertSelfEmploymentRecords(se1, se2, latestSe3)
      insertEmploymentRecords(emp1, emp2, latestEmp3)
      insertFHLRecords(fhl1, fhl2, latestFhl3)
      insertUKPropertyRecords(property1, property2, latestProperty3)

      val service = new DeleteExpiredDataService(saRepo, seRepo, uiRepo, empRepo, fhlRepo, ukPropertyRepo, jobRepo)

      val nrDeleted = service.deleteExpiredData(lastModifiedDate)

      whenReady(nrDeleted) { _ =>

        val saRecords = saRepo.findAll()

        whenReady(saRecords) { _ =>
          val seRecords = seRepo.findAll()
          val empRecords = empRepo.findAll()
          val fhlRecords = fhlRepo.findAll()
          val propertyRecords = ukPropertyRepo.findAll()

          whenReady(seRecords) { _ =>

            saRecords.size shouldBe 1
            saRecords.head.saUtr == latestSa3.saUtr && saRecords.head.taxYear == latestSa3.taxYear shouldBe true

            seRecords.size shouldBe 1
            seRecords.head.saUtr == latestSe3.saUtr && seRecords.head.taxYear == latestSe3.taxYear shouldBe true
          }

          whenReady(empRecords) { _ =>
            empRecords.size shouldBe 1
            empRecords.head.saUtr == latestEmp3.saUtr && empRecords.head.taxYear == latestEmp3.taxYear shouldBe true
          }

          whenReady(fhlRecords) { _ =>
            fhlRecords.size shouldBe 1
            fhlRecords.head.saUtr == latestFhl3.saUtr && fhlRecords.head.taxYear == latestFhl3.taxYear shouldBe true
          }

          whenReady(propertyRecords) { _ =>
            propertyRecords.size shouldBe 1
            propertyRecords.head.saUtr == latestProperty3.saUtr && propertyRecords.head.taxYear == latestProperty3.taxYear shouldBe true
          }
        }
      }
    }


    "mark job as failed if there is an exception when trying to delete records from self assessment" in {
      val sa1 = SelfAssessment(BSONObjectID.generate, saUtr, taxYear, DateTime.now().minusMonths(1), DateTime.now().minusMonths(1))
      val saRepo = mock[SelfAssessmentMongoRepository]
      when(saRepo.findOlderThan(any[DateTime]())).thenReturn(Future.successful(Seq(sa1)))
      when(saRepo.delete(any[SaUtr](), any[TaxYear]())).thenThrow(new RuntimeException("something wrong"))

      val service = new DeleteExpiredDataService(saRepo, seRepo, uiRepo, empRepo, fhlRepo, ukPropertyRepo, jobRepo)

      an[RuntimeException] should be thrownBy await(service.deleteExpiredData(DateTime.now))

      await(jobRepo.find().head).status shouldBe Failed
      verify(saRepo).delete(any[SaUtr](), any[TaxYear]())
    }

    "mark job as failed if there is an exception when trying to delete records from self employment" in {
      val sa1 = SelfAssessment(BSONObjectID.generate, saUtr, taxYear, DateTime.now().minusMonths(1), DateTime.now().minusMonths(1))
      val seRepo = mock[SelfEmploymentMongoRepository]
      await(saRepo.insert(sa1))
      when(seRepo.delete(any[SaUtr](), any[TaxYear]())).thenThrow(new RuntimeException("something wrong"))

      val service = new DeleteExpiredDataService(saRepo, seRepo, uiRepo, empRepo, fhlRepo, ukPropertyRepo, jobRepo)

      an[RuntimeException] should be thrownBy await(service.deleteExpiredData(DateTime.now))

      await(jobRepo.find().head).status shouldBe Failed
      verify(seRepo).delete(any[SaUtr](), any[TaxYear]())
    }

    "mark job as failed if there is an exception when trying to delete records from employment" in {
      val sa1 = SelfAssessment(BSONObjectID.generate, saUtr, taxYear, DateTime.now().minusMonths(1), DateTime.now().minusMonths(1))
      val empRepo = mock[EmploymentMongoRepository]
      await(saRepo.insert(sa1))
      when(empRepo.delete(any[SaUtr](), any[TaxYear]())).thenThrow(new RuntimeException("something wrong"))

      val service = new DeleteExpiredDataService(saRepo, seRepo, uiRepo, empRepo, fhlRepo, ukPropertyRepo, jobRepo)

      an[RuntimeException] should be thrownBy await(service.deleteExpiredData(DateTime.now))

      await(jobRepo.find().head).status shouldBe Failed
      verify(empRepo).delete(any[SaUtr](), any[TaxYear]())
    }

    "mark job as failed if there is an exception when trying to delete records from furnished holiday lettings" in {
      val sa1 = SelfAssessment(BSONObjectID.generate, saUtr, taxYear, DateTime.now().minusMonths(1), DateTime.now().minusMonths(1))
      val fhlRepo = mock[FurnishedHolidayLettingsMongoRepository]
      await(saRepo.insert(sa1))
      when(fhlRepo.delete(any[SaUtr](), any[TaxYear]())).thenThrow(new RuntimeException("something wrong"))

      val service = new DeleteExpiredDataService(saRepo, seRepo, uiRepo, empRepo, fhlRepo, ukPropertyRepo, jobRepo)

      an[RuntimeException] should be thrownBy await(service.deleteExpiredData(DateTime.now))

      await(jobRepo.find().head).status shouldBe Failed
      verify(fhlRepo).delete(any[SaUtr](), any[TaxYear]())
    }

    "mark job as failed if there is an exception when trying to delete records from uk properties" in {
      val sa1 = SelfAssessment(BSONObjectID.generate, saUtr, taxYear, DateTime.now().minusMonths(1), DateTime.now().minusMonths(1))
      val ukPropertyRepo = mock[UKPropertiesMongoRepository]
      await(saRepo.insert(sa1))
      when(ukPropertyRepo.delete(any[SaUtr](), any[TaxYear]())).thenThrow(new RuntimeException("something wrong"))

      val service = new DeleteExpiredDataService(saRepo, seRepo, uiRepo, empRepo, fhlRepo, ukPropertyRepo, jobRepo)

      an[RuntimeException] should be thrownBy await(service.deleteExpiredData(DateTime.now))

      await(jobRepo.find().head).status shouldBe Failed
      verify(ukPropertyRepo).delete(any[SaUtr](), any[TaxYear]())
    }

    "mark job as failed if there is an exception when trying to delete records from unearned incomes" in {
      val sa1 = SelfAssessment(BSONObjectID.generate, saUtr, taxYear, DateTime.now().minusMonths(1), DateTime.now().minusMonths(1))
      val uiRepo = mock[UnearnedIncomeMongoRepository]
      await(saRepo.insert(sa1))
      when(uiRepo.delete(any[SaUtr](), any[TaxYear]())).thenThrow(new RuntimeException("something wrong"))

      val service = new DeleteExpiredDataService(saRepo, seRepo, uiRepo, empRepo, fhlRepo, ukPropertyRepo, jobRepo)

      an[RuntimeException] should be thrownBy await(service.deleteExpiredData(DateTime.now))

      await(jobRepo.find().head).status shouldBe Failed
      verify(uiRepo).delete(any[SaUtr](), any[TaxYear]())
    }


    "mark job as failed if there is an exception when trying to mark job as completed" in {
      val jobRepo = mock[JobHistoryMongoRepository]
      when(jobRepo.startJob()).thenReturn(Future(JobHistory(1, InProgress)))
      when(jobRepo.completeJob(1, 0)).thenThrow(new RuntimeException("something wrong"))

      val service = new DeleteExpiredDataService(saRepo, seRepo, uiRepo, empRepo, fhlRepo, ukPropertyRepo, jobRepo)

      an[RuntimeException] should be thrownBy await(service.deleteExpiredData(DateTime.now))

      await(saRepo.findAll()).size shouldBe 0
      await(seRepo.findAll()).size shouldBe 0
      verify(jobRepo).abortJob(1)
    }

  }

  private def insertSelfAssessmentRecords(records: SelfAssessment*) = {
    records.foreach {
      record =>
        val futureWrite = saRepo.insert(record)
        whenReady(futureWrite)(identity)
    }
  }

  private def insertSelfEmploymentRecords(records: SelfEmployment*) = {
    records.foreach {
      record =>
        val futureWrite = seRepo.insert(record)
        whenReady(futureWrite)(identity)
    }
  }

  private def insertEmploymentRecords(records: Employment*) = {
    records.foreach { record =>
      val futureWrite = empRepo.insert(record)
      whenReady(futureWrite)(identity)
    }
  }

  private def insertFHLRecords(records: FurnishedHolidayLettings*) = {
    records.foreach { record =>
      val futureWrite = fhlRepo.insert(record)
      whenReady(futureWrite)(identity)
    }
  }

  private def insertUKPropertyRecords(records: UKProperties*) = {
    records.foreach { record =>
      val futureWrite = ukPropertyRepo.insert(record)
      whenReady(futureWrite)(identity)
    }
  }
}
