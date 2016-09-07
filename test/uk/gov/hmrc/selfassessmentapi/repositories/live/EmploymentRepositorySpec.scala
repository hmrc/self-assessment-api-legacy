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

package uk.gov.hmrc.selfassessmentapi.repositories.live

import java.util.UUID

import org.scalatest.BeforeAndAfterEach
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.MongoEmbeddedDatabase
import uk.gov.hmrc.selfassessmentapi.controllers.api.JsonMarshaller
import uk.gov.hmrc.selfassessmentapi.controllers.api.employment._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{MongoEmployment, MongoEmploymentIncomeSummary}
import uk.gov.hmrc.selfassessmentapi.repositories.{SourceRepository, SummaryRepository}

import scala.concurrent.ExecutionContext.Implicits.global

class EmploymentRepositorySpec extends MongoEmbeddedDatabase with BeforeAndAfterEach {

  private val mongoRepository = new EmploymentMongoRepository
  private val employmentRepository: SourceRepository[Employment] = mongoRepository
  private val summariesMap: Map[JsonMarshaller[_], SummaryRepository[_]] = Map(
      Income -> mongoRepository.IncomeRepository,
      Expense -> mongoRepository.ExpenseRepository,
      Benefit -> mongoRepository.BenefitRepository,
      UkTaxPaid -> mongoRepository.UkTaxPaidRepository)

  override def beforeEach() {
    await(mongoRepository.drop)
    await(mongoRepository.ensureIndexes)
  }

  val saUtr = generateSaUtr()

  def employment(): Employment = Employment.example()

  "delete by Id" should {
    "return true when employment is deleted" in {
      val source = employment()
      val id = await(employmentRepository.create(saUtr, taxYear, source))
      val result = await(employmentRepository.delete(saUtr, taxYear, id))

      result shouldBe true
    }

    "return false when employment is not deleted" in {
      val source = employment()
      val id = await(employmentRepository.create(saUtr, taxYear, source))
      val result = await(employmentRepository.delete(generateSaUtr(), taxYear, id))

      result shouldBe false
    }
  }

  "delete by utr and taxYear" should {
    "delete  all employments for utr/tax year" in {
      for {
        n <- 1 to 10
        source = employment()
        id = await(employmentRepository.create(saUtr, taxYear, source))
      } yield source.copy(id = Some(id))

      await(employmentRepository.delete(saUtr, taxYear))

      val found: Seq[Employment] = await(employmentRepository.list(saUtr, taxYear))

      found shouldBe empty
    }

    "not delete employments for different utr" in {
      val saUtr2: SaUtr = generateSaUtr()
      await(employmentRepository.create(saUtr, taxYear, employment()))
      val source2 = await(employmentRepository.create(saUtr2, taxYear, employment()))

      await(employmentRepository.delete(saUtr, taxYear))
      val found: Seq[Employment] = await(employmentRepository.list(saUtr2, taxYear))

      found.flatMap(_.id) should contain theSameElementsAs Seq(source2)
    }
  }

  "list" should {
    "retrieve all employments for utr/tax year" in {
      val sources = for {
        n <- 1 to 10
        source = employment()
        id = await(employmentRepository.create(saUtr, taxYear, source))
      } yield source.copy(id = Some(id))

      val found: Seq[Employment] = await(employmentRepository.list(saUtr, taxYear))

      found should contain theSameElementsAs sources
    }

    "not include employments for different utr" in {
      val source1 = await(employmentRepository.create(saUtr, taxYear, employment()))
      await(employmentRepository.create(generateSaUtr(), taxYear, employment()))

      val found: Seq[Employment] = await(employmentRepository.list(saUtr, taxYear))

      found.flatMap(_.id) should contain theSameElementsAs Seq(source1)
    }
  }

    "return false when the employment does not exist" in {
      val result = await(employmentRepository.update(saUtr, taxYear, UUID.randomUUID().toString, employment()))
      result shouldEqual false
    }

  "update" should {

    "not remove incomes" in {
      val source = MongoEmployment.create(saUtr, taxYear, employment()).copy(incomes = Seq(MongoEmploymentIncomeSummary(BSONObjectID.generate.stringify, IncomeType.Salary, 1000)))
      await(mongoRepository.insert(source))
      val found = await(mongoRepository.findById(saUtr, taxYear, source.sourceId)).get
      await(employmentRepository.update(saUtr, taxYear, source.sourceId, found))

      val found1 = await(mongoRepository.findById(source.id))

      found1.get.incomes should not be empty
    }

    "update last modified" in {
      val source = employment()
      val sourceId = await(employmentRepository.create(saUtr, taxYear, source))
      val found = await(mongoRepository.findById(BSONObjectID(sourceId)))
      await(employmentRepository.update(saUtr, taxYear, sourceId, source))

      val found1 = await(mongoRepository.findById(BSONObjectID(sourceId)))

      // Added the equals clauses as it was failing locally once, can fail if the test runs faster and has the same time for create and update
      found1.get.lastModifiedDateTime.isEqual(found.get.lastModifiedDateTime) || found1.get.lastModifiedDateTime.isAfter(found.get.lastModifiedDateTime) shouldBe true
    }
  }

  def cast[A](a: Any): A = a.asInstanceOf[A]

  "create summary" should {
    "add a summary to an empty list when source exists and return id" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(employmentRepository.create(saUtr, taxYear, employment()))
        val summary = summaryItem.example()
        val summaryId = await(repo.create(saUtr, taxYear, sourceId, cast(summary)))

        summaryId.isDefined shouldEqual true
        val dbSummaries = await(repo.list(saUtr, taxYear, sourceId))

        val found = dbSummaries.get
        found.headOption shouldEqual Some(summaryItem.example(id = summaryId))
      }
    }

    "add a summary to the existing list when source exists and return id" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(employmentRepository.create(saUtr, taxYear, employment()))
        val summary = summaryItem.example()
        val summary1 = summaryItem.example()
        val summaryId = await(repo.create(saUtr, taxYear, sourceId, cast(summary)))
        val summaryId1 = await(repo.create(saUtr, taxYear, sourceId, cast(summary1)))

        val summaries = await(repo.list(saUtr, taxYear, sourceId))

        val found = summaries.get
        found should contain theSameElementsAs Seq(summaryItem.example(id = summaryId),
                                                   summaryItem.example(id = summaryId1))
      }
    }

    "return none when source does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        val summary = summaryItem.example()
        val summaryId = await(repo.create(saUtr, taxYear, BSONObjectID.generate.stringify, cast(summary)))
        summaryId shouldEqual None
      }
    }
  }

  "find summary by id" should {
    "return none if the source does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        await(repo.findById(saUtr, taxYear, BSONObjectID.generate.stringify, BSONObjectID.generate.stringify)) shouldEqual None
      }
    }

    "return none if the summary does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(employmentRepository.create(saUtr, taxYear, employment()))
        await(repo.findById(saUtr, taxYear, sourceId, BSONObjectID.generate.stringify)) shouldEqual None
      }
    }

    "return the summary if found" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(employmentRepository.create(saUtr, taxYear, employment()))
        val summary = summaryItem.example()
        val summaryId = await(repo.create(saUtr, taxYear, sourceId, cast(summary))).get
        val found = await(repo.findById(saUtr, taxYear, sourceId, summaryId))

        found shouldEqual Some(summaryItem.example(id = Some(summaryId)))
      }
    }
  }

  "list summaries" should {
    "return empty list when source has no summaries" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(employmentRepository.create(saUtr, taxYear, employment()))
        await(repo.list(saUtr, taxYear, sourceId)) shouldEqual Some(Seq.empty)
      }
    }

    "return none when source does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        await(repo.list(saUtr, taxYear, BSONObjectID.generate.stringify)) shouldEqual None
      }
    }
  }

  "delete summary" should {
    "return true when the summary has been deleted" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(employmentRepository.create(saUtr, taxYear, employment()))
        val summary = summaryItem.example()
        val summaryId = await(repo.create(saUtr, taxYear, sourceId, cast(summary))).get
        await(repo.delete(saUtr, taxYear, sourceId, summaryId)) shouldEqual true
      }
    }

    "only delete the specified summary" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(employmentRepository.create(saUtr, taxYear, employment()))
        val summary = summaryItem.example()
        val summaryId = await(repo.create(saUtr, taxYear, sourceId, cast(summary))).get
        val summaryId1 = await(repo.create(saUtr, taxYear, sourceId, cast(summary)))
        await(repo.delete(saUtr, taxYear, sourceId, summaryId))

        val found = await(repo.list(saUtr, taxYear, sourceId)).get
        found.size shouldEqual 1
        found.head shouldEqual summaryItem.example(id = summaryId1)
      }
    }

    "return false when the source does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        await(repo.delete(saUtr, taxYear, BSONObjectID.generate.stringify, BSONObjectID.generate.stringify)) shouldEqual false
      }
    }

    "return false when the summary does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(employmentRepository.create(saUtr, taxYear, employment()))
        await(repo.delete(saUtr, taxYear, sourceId, BSONObjectID.generate.stringify)) shouldEqual false
      }
    }
  }

  "update income" should {
    "return true when the income has been updated" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(employmentRepository.create(saUtr, taxYear, employment()))
        val summary = summaryItem.example()
        val summaryId = await(repo.create(saUtr, taxYear, sourceId, cast(summary))).get

        val summaryToUpdate = summaryItem.example()
        await(repo.update(saUtr, taxYear, sourceId, summaryId, cast(summaryToUpdate))) shouldEqual true

        val found = await(repo.findById(saUtr, taxYear, sourceId, summaryId))

        found shouldEqual Some(summaryItem.example(id = Some(summaryId)))
      }
    }

    "only update the specified income" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(employmentRepository.create(saUtr, taxYear, employment()))
        val summary1 = summaryItem.example()
        val summaryId1 = await(repo.create(saUtr, taxYear, sourceId, cast(summary1))).get
        val summary2 = summaryItem.example()
        val summaryId2 = await(repo.create(saUtr, taxYear, sourceId, cast(summary2))).get

        val summaryToUpdate = summaryItem.example()
        await(repo.update(saUtr, taxYear, sourceId, summaryId2, cast(summaryToUpdate))) shouldEqual true

        val found = await(repo.list(saUtr, taxYear, sourceId)).get

        found should contain theSameElementsAs Seq(summaryItem.example(id = Some(summaryId1)),
                                                   summaryItem.example(id = Some(summaryId2)))
      }
    }

    "return false when the source does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        await(
            repo.update(saUtr,
                        taxYear,
                        BSONObjectID.generate.stringify,
                        BSONObjectID.generate.stringify,
                        cast(summaryItem.example()))) shouldEqual false
      }
    }

    "return false when the income does not exist" in {
      val sourceId = await(employmentRepository.create(saUtr, taxYear, employment()))
      for ((summaryItem, repo) <- summariesMap) {
        await(repo.update(saUtr, taxYear, sourceId, BSONObjectID.generate.stringify, cast(summaryItem.example()))) shouldEqual false
      }
    }
  }

}
