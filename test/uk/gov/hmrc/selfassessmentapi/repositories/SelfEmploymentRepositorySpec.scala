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

package uk.gov.hmrc.selfassessmentapi.repositories

import java.util.UUID

import org.scalatest.BeforeAndAfterEach
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.MongoEmbeddedDatabase
import uk.gov.hmrc.selfassessmentapi.domain.TaxYear
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.{Adjustments, Allowances, IncomeType, SelfEmployment}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{MongoSelfEmployment, MongoSelfEmploymentIncomeSummary}

import scala.concurrent.ExecutionContext.Implicits.global

class SelfEmploymentRepositorySpec extends MongoEmbeddedDatabase with BeforeAndAfterEach {

  private val mongoRepository = new SelfEmploymentMongoRepository
  private val repository: SelfEmploymentRepository = mongoRepository

  override def beforeEach() {
    await(mongoRepository.drop)
    await(mongoRepository.ensureIndexes)
  }

  val taxYear = TaxYear("2016-17")
  val saUtr = generateSaUtr()

  def selfEmployment(): SelfEmployment = SelfEmployment.example

  "round trip" should {
    "create and retrieve using generated id" in {
      val source = selfEmployment()
      val id = await(repository.create(saUtr, taxYear, source))
      val found: SelfEmployment = await(repository.findById(saUtr, taxYear, id)).get

      found.name shouldBe source.name
      found.commencementDate shouldBe source.commencementDate
    }
  }

  "delete" should {
    "return true when self employment is deleted" in {
      val source = selfEmployment()
      val id = await(repository.create(saUtr, taxYear, source))
      val result = await(repository.delete(saUtr, taxYear, id))

      result shouldBe true
    }

    "return false when self employment is not deleted" in {
      val source = selfEmployment()
      val id = await(repository.create(saUtr, taxYear, source))
      val result = await(repository.delete(generateSaUtr(), taxYear, id))

      result shouldBe false
    }
  }

  "list" should {
    "retrieve all self employments for utr/tax year" in {
      val sources = for {
        n <- 1 to 10
        source = selfEmployment()
        id = await(repository.create(saUtr, taxYear, source))
      } yield source.copy(id = Some(id))


      val found: Seq[SelfEmployment] = await(repository.list(saUtr, taxYear))

      found should contain theSameElementsAs sources
    }

    "not include self employments for different utr" in {
      val source1 = await(repository.create(saUtr, taxYear, selfEmployment()))
      val source2 = await(repository.create(generateSaUtr(), taxYear, selfEmployment()))

      val found: Seq[SelfEmployment] = await(repository.list(saUtr, taxYear))

      found.flatMap(_.id) should contain theSameElementsAs Seq(source1)
    }
  }

  "update" should {
    def verifyUpdate(original: SelfEmployment, updated: SelfEmployment) = {
      val sourceId = await(repository.create(saUtr, taxYear, original))
      val result = await(repository.update(saUtr, taxYear, sourceId, updated))
      result shouldEqual true

      val found = await(repository.findById(saUtr, taxYear, sourceId))
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
        name = UUID.randomUUID().toString,
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
      val result = await(repository.update(saUtr, taxYear, UUID.randomUUID().toString, selfEmployment()))
      result shouldEqual false
    }

    "not remove incomes" in {
      val source = MongoSelfEmployment.create(saUtr, taxYear, selfEmployment()).copy(incomes = Seq(MongoSelfEmploymentIncomeSummary(BSONObjectID.generate.stringify, IncomeType.Turnover, 10)))
      val insertResult = await(mongoRepository.insert(source))
      val found = await(mongoRepository.findById(saUtr, taxYear, source.sourceId)).get
      await(repository.update(saUtr, taxYear, source.sourceId, found))

      val found1 = await(mongoRepository.findById(source.id))

      found1.get.incomes should not be empty
    }

    "update last modified" in {
      val source = selfEmployment()
      val sourceId = await(repository.create(saUtr, taxYear, source))
      val found = await(mongoRepository.findById(BSONObjectID(sourceId)))
      await(repository.update(saUtr, taxYear, sourceId, source))

      val found1 = await(mongoRepository.findById(BSONObjectID(sourceId)))

      found1.get.lastModifiedDateTime.isAfter(found.get.lastModifiedDateTime) shouldBe true

    }
  }
}
