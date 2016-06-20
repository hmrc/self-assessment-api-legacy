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
import uk.gov.hmrc.selfassessmentapi.domain.TaxYear
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment._
import uk.gov.hmrc.selfassessmentapi.repositories.SourceRepository
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{MongoSelfEmployment, MongoSelfEmploymentIncomeSummary}

import scala.concurrent.ExecutionContext.Implicits.global

class SelfEmploymentRepositorySpec extends MongoEmbeddedDatabase with BeforeAndAfterEach {

  private val mongoRepository = new SelfEmploymentMongoRepository
  private val selfEmploymentRepository: SourceRepository[SelfEmployment] = mongoRepository
  private val incomeRepository: SelfEmploymentIncomesRepository = mongoRepository

  override def beforeEach() {
    await(mongoRepository.drop)
    await(mongoRepository.ensureIndexes)
  }

  val taxYear = TaxYear("2016-17")
  val saUtr = generateSaUtr()

  def selfEmployment(): SelfEmployment = SelfEmployment.example()

  "round trip" should {
    "create and retrieve using generated id" in {
      val source = selfEmployment()
      val id = await(selfEmploymentRepository.create(saUtr, taxYear, source))
      val found: SelfEmployment = await(selfEmploymentRepository.findById(saUtr, taxYear, id)).get

      found.commencementDate shouldBe source.commencementDate
    }
  }

  "delete by Id" should {
    "return true when self employment is deleted" in {
      val source = selfEmployment()
      val id = await(selfEmploymentRepository.create(saUtr, taxYear, source))
      val result = await(selfEmploymentRepository.delete(saUtr, taxYear, id))

      result shouldBe true
    }

    "return false when self employment is not deleted" in {
      val source = selfEmployment()
      val id = await(selfEmploymentRepository.create(saUtr, taxYear, source))
      val result = await(selfEmploymentRepository.delete(generateSaUtr(), taxYear, id))

      result shouldBe false
    }
  }

  "delete by utr and taxYear" should {
    "delete  all self employments for utr/tax year" in {
      val sources = for {
        n <- 1 to 10
        source = selfEmployment()
        id = await(selfEmploymentRepository.create(saUtr, taxYear, source))
      } yield source.copy(id = Some(id))


      await(selfEmploymentRepository.delete(saUtr, taxYear))

      val found: Seq[SelfEmployment] = await(selfEmploymentRepository.list(saUtr, taxYear))

      found shouldBe empty
    }

    "not delete self employments for different utr" in {
      val saUtr2: SaUtr = generateSaUtr()
      val source1 = await(selfEmploymentRepository.create(saUtr, taxYear, selfEmployment()))
      val source2 = await(selfEmploymentRepository.create(saUtr2, taxYear, selfEmployment()))

      await(selfEmploymentRepository.delete(saUtr, taxYear))
      val found: Seq[SelfEmployment] = await(selfEmploymentRepository.list(saUtr2, taxYear))

      found.flatMap(_.id) should contain theSameElementsAs Seq(source2)
    }
  }


  "list" should {
    "retrieve all self employments for utr/tax year" in {
      val sources = for {
        n <- 1 to 10
        source = selfEmployment()
        id = await(selfEmploymentRepository.create(saUtr, taxYear, source))
      } yield source.copy(id = Some(id))


      val found: Seq[SelfEmployment] = await(selfEmploymentRepository.list(saUtr, taxYear))

      found should contain theSameElementsAs sources
    }

    "not include self employments for different utr" in {
      val source1 = await(selfEmploymentRepository.create(saUtr, taxYear, selfEmployment()))
      val source2 = await(selfEmploymentRepository.create(generateSaUtr(), taxYear, selfEmployment()))

      val found: Seq[SelfEmployment] = await(selfEmploymentRepository.list(saUtr, taxYear))

      found.flatMap(_.id) should contain theSameElementsAs Seq(source1)
    }
  }

  "update" should {
    def verifyUpdate(original: SelfEmployment, updated: SelfEmployment) = {
      val sourceId = await(selfEmploymentRepository.create(saUtr, taxYear, original))
      val result = await(selfEmploymentRepository.update(saUtr, taxYear, sourceId, updated))
      result shouldEqual true

      val found = await(selfEmploymentRepository.findById(saUtr, taxYear, sourceId))
      found shouldEqual Some(updated.copy(id = Some(sourceId)))

    }
    "return true when the self employment exists and has been updated" in {
      val source = selfEmployment()

      val allowances = Allowances(
        annualInvestmentAllowance = Some(BigDecimal(10.00)),
        capitalAllowanceMainPool = Some(BigDecimal(20.00)),
        capitalAllowanceSpecialRatePool = Some(BigDecimal(30.00)),
        restrictedCapitalAllowance = Some(BigDecimal(40.00)),
        businessPremisesRenovationAllowance = Some(BigDecimal(50.00)),
        enhancedCapitalAllowance = Some(BigDecimal(60.00)),
        allowancesOnSales = Some(BigDecimal(70.00)))

      val adjustments = Adjustments(
        includedNonTaxableProfits = Some(BigDecimal(10.00)),
        basisAdjustment = Some(BigDecimal(20.00)),
        overlapReliefUsed = Some(BigDecimal(30.00)),
        accountingAdjustment = Some(BigDecimal(40.00)),
        averagingAdjustment = Some(BigDecimal(50.00)),
        lossBroughtForward = Some(BigDecimal(60.00)),
        outstandingBusinessIncome = Some(BigDecimal(70.00)))

      val updatedSource = source.copy(
        commencementDate = source.commencementDate.minusMonths(1),
        allowances = Some(allowances),
        adjustments = Some(adjustments)
      )

      verifyUpdate(source, updatedSource)
    }

    "set allowances to None if not provided" in {
      val source = selfEmployment()

      val updatedSource = source.copy(
        allowances = None
      )

      verifyUpdate(source, updatedSource)
    }

    "set each allowance to None if not provided" in {
      val source = selfEmployment()

      val updatedSource = source.copy(
        allowances = Some(Allowances())
      )

      verifyUpdate(source, updatedSource)
    }

    "set adjustments to None if not provided" in {
      val source = selfEmployment()

      val updatedSource = source.copy(
        adjustments = None
      )

      verifyUpdate(source, updatedSource)
    }

    "set each adjustment to None if not provided" in {
      val source = selfEmployment()

      val updatedSource = source.copy(
        adjustments = Some(Adjustments())
      )

      verifyUpdate(source, updatedSource)
    }

    "return false when the self employment does not exist" in {
      val result = await(selfEmploymentRepository.update(saUtr, taxYear, UUID.randomUUID().toString, selfEmployment()))
      result shouldEqual false
    }

    "not remove incomes" in {
      val source = MongoSelfEmployment.create(saUtr, taxYear, selfEmployment()).copy(incomes = Seq(MongoSelfEmploymentIncomeSummary(BSONObjectID.generate.stringify, IncomeType.Turnover, 10)))
      await(mongoRepository.insert(source))
      val found = await(mongoRepository.findById(saUtr, taxYear, source.sourceId)).get
      await(selfEmploymentRepository.update(saUtr, taxYear, source.sourceId, found))

      val found1 = await(mongoRepository.findById(source.id))

      found1.get.incomes should not be empty
    }

    "update last modified" in {
      val source = selfEmployment()
      val sourceId = await(selfEmploymentRepository.create(saUtr, taxYear, source))
      val found = await(mongoRepository.findById(BSONObjectID(sourceId)))
      await(selfEmploymentRepository.update(saUtr, taxYear, sourceId, source))

      val found1 = await(mongoRepository.findById(BSONObjectID(sourceId)))

      // Added the equals clauses as it was failing locally once, can fail if the test runs faster and has the same time for create and update
      found1.get.lastModifiedDateTime.isEqual(found.get.lastModifiedDateTime) || found1.get.lastModifiedDateTime.isAfter(found.get.lastModifiedDateTime) shouldBe true
    }
  }

  "create income" should {
    "add an income to an empty list when source exists and return id" in {
      val sourceId = await(selfEmploymentRepository.create(saUtr, taxYear, selfEmployment()))
      val summary = Income(`type` = IncomeType.Turnover, amount = 10)
      val summaryId = await(incomeRepository.createIncome(saUtr, taxYear, sourceId, summary))

      summaryId.isDefined shouldEqual true
      val summaries = await(incomeRepository.listIncomes(saUtr, taxYear, sourceId))

      val found = summaries.get
      found.headOption shouldEqual Some(summary.copy(id = summaryId))
    }

    "add an income to the existing list when source exists and return id" in {
      val sourceId = await(selfEmploymentRepository.create(saUtr, taxYear, selfEmployment()))
      val summary = Income(`type` = IncomeType.Turnover, amount = 10)
      val summary1 = Income(`type` = IncomeType.Other, amount = 10)
      val summaryId = await(incomeRepository.createIncome(saUtr, taxYear, sourceId, summary))
      val summaryId1 = await(incomeRepository.createIncome(saUtr, taxYear, sourceId, summary1))

      val summaries = await(incomeRepository.listIncomes(saUtr, taxYear, sourceId))

      val found = summaries.get
      found should contain theSameElementsAs Seq(summary.copy(id = summaryId), summary1.copy(id = summaryId1))
    }

    "return none when source does not exist" in {
      val summary = Income(`type` = IncomeType.Turnover, amount = 10)
      val summaryId = await(incomeRepository.createIncome(saUtr, taxYear, BSONObjectID.generate.stringify, summary))
      summaryId shouldEqual None
    }
  }

  "find income by id" should {
    "return none if the source does not exist" in {
      await(incomeRepository.findIncomeById(saUtr, taxYear, BSONObjectID.generate.stringify, BSONObjectID.generate.stringify)) shouldEqual None
    }

    "return none if the income does not exist" in {
      val sourceId = await(selfEmploymentRepository.create(saUtr, taxYear, selfEmployment()))
      await(incomeRepository.findIncomeById(saUtr, taxYear, sourceId, BSONObjectID.generate.stringify)) shouldEqual None
    }

    "return the income if found" in {
      val sourceId = await(selfEmploymentRepository.create(saUtr, taxYear, selfEmployment()))
      val summary = Income(`type` = IncomeType.Turnover, amount = 10)
      val summaryId = await(incomeRepository.createIncome(saUtr, taxYear, sourceId, summary)).get
      val found = await(incomeRepository.findIncomeById(saUtr, taxYear, sourceId, summaryId))

      found shouldEqual Some(summary.copy(id = Some(summaryId)))
    }
  }

  "list incomes" should {
    "return empty list when source has no incomes" in {
      val sourceId = await(selfEmploymentRepository.create(saUtr, taxYear, selfEmployment()))
      await(incomeRepository.listIncomes(saUtr, taxYear, sourceId)) shouldEqual Some(Seq.empty)
    }

    "return none when source does not exist" in {
      await(incomeRepository.listIncomes(saUtr, taxYear, BSONObjectID.generate.stringify)) shouldEqual None
    }
  }

  "delete income" should {
    "return true when the income has been deleted" in {
      val sourceId = await(selfEmploymentRepository.create(saUtr, taxYear, selfEmployment()))
      val summary = Income(`type` = IncomeType.Turnover, amount = 10)
      val summaryId = await(incomeRepository.createIncome(saUtr, taxYear, sourceId, summary)).get
      await(incomeRepository.deleteIncome(saUtr, taxYear, sourceId, summaryId)) shouldEqual true
    }

    "only delete the specified income" in {
      val sourceId = await(selfEmploymentRepository.create(saUtr, taxYear, selfEmployment()))
      val summary = Income(`type` = IncomeType.Turnover, amount = 10)
      val summaryId = await(incomeRepository.createIncome(saUtr, taxYear, sourceId, summary)).get
      val summaryId1 = await(incomeRepository.createIncome(saUtr, taxYear, sourceId, summary))
      await(incomeRepository.deleteIncome(saUtr, taxYear, sourceId, summaryId))

      val found = await(incomeRepository.listIncomes(saUtr, taxYear, sourceId)).get
      found.size shouldEqual 1
      found.map(_.id).head shouldEqual summaryId1
    }

    "return false when the source does not exist" in {
      await(incomeRepository.deleteIncome(saUtr, taxYear, BSONObjectID.generate.stringify, BSONObjectID.generate.stringify)) shouldEqual false
    }

    "return false when the income does not exist" in {
      val sourceId = await(selfEmploymentRepository.create(saUtr, taxYear, selfEmployment()))
      await(incomeRepository.deleteIncome(saUtr, taxYear, sourceId, BSONObjectID.generate.stringify)) shouldEqual false
    }
  }

  "update income" should {
    "return true when the income has been updated" in {
      val sourceId = await(selfEmploymentRepository.create(saUtr, taxYear, selfEmployment()))
      val summary = Income(`type` = IncomeType.Turnover, amount = 10)
      val summaryId = await(incomeRepository.createIncome(saUtr, taxYear, sourceId, summary)).get

      val summaryToUpdate = summary.copy(`type` = IncomeType.Other, amount = 20)
      await(incomeRepository.updateIncome(saUtr, taxYear, sourceId, summaryId, summaryToUpdate)) shouldEqual true

      val found = await(incomeRepository.findIncomeById(saUtr, taxYear, sourceId, summaryId))

      found shouldEqual Some(summaryToUpdate.copy(id = Some(summaryId)))
    }

    "only update the specified income" in {
      val sourceId = await(selfEmploymentRepository.create(saUtr, taxYear, selfEmployment()))
      val summary1 = Income(`type` = IncomeType.Turnover, amount = 10)
      val summaryId1 = await(incomeRepository.createIncome(saUtr, taxYear, sourceId, summary1)).get
      val summary2 = Income(`type` = IncomeType.Turnover, amount = 50)
      val summaryId2 = await(incomeRepository.createIncome(saUtr, taxYear, sourceId, summary2)).get

      val summaryToUpdate = summary2.copy(`type` = IncomeType.Other, amount = 20)
      await(incomeRepository.updateIncome(saUtr, taxYear, sourceId, summaryId2, summaryToUpdate)) shouldEqual true

      val found = await(incomeRepository.listIncomes(saUtr, taxYear, sourceId)).get

      found should contain theSameElementsAs Seq(summary1.copy(id = Some(summaryId1)), summaryToUpdate.copy(id = Some(summaryId2)))
    }

    "return false when the source does not exist" in {
      await(incomeRepository.updateIncome(saUtr, taxYear, BSONObjectID.generate.stringify, BSONObjectID.generate.stringify, Income.example())) shouldEqual false
    }

    "return false when the income does not exist" in {
      val sourceId = await(selfEmploymentRepository.create(saUtr, taxYear, selfEmployment()))
      await(incomeRepository.updateIncome(saUtr, taxYear, sourceId, BSONObjectID.generate.stringify, Income.example())) shouldEqual false
    }
  }
}
