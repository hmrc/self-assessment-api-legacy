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

import org.joda.time.{DateTime, DateTimeUtils}
import reactivemongo.bson.BSONObjectID
import reactivemongo.core.errors.DatabaseException
import uk.gov.hmrc.selfassessmentapi.MongoEmbeddedDatabase
import uk.gov.hmrc.selfassessmentapi.controllers.util.NinoGenerator
import uk.gov.hmrc.selfassessmentapi.domain.Properties
import uk.gov.hmrc.selfassessmentapi.resources.models.AccountingType

import scala.concurrent.ExecutionContext.Implicits.global

class PropertiesRepositorySpec extends MongoEmbeddedDatabase {

  private val repo = new PropertiesRepository
  private val nino = NinoGenerator().nextNino()
  private val properties = Properties(BSONObjectID.generate, nino, AccountingType.CASH)

  "create" should {
    "persist a properties object" in {
      await(repo.create(properties))

      val result = await(repo.retrieve(nino)).get
      result.nino shouldBe nino
      result.lastModifiedDateTime should not be null

    }

    "fail if customer tries to create a second properties business" in {
      await(repo.create(properties))

      a [DatabaseException] shouldBe thrownBy (await(repo.create(properties)))

    }
  }

  "update" should {
    "update a persisted object" in {
      await(repo.create(properties))

      await(repo.update(nino, properties.copy(accountingType = AccountingType.ACCRUAL)))

      val updatedProperties = await(repo.retrieve(nino)).get

      updatedProperties.accountingType shouldBe AccountingType.ACCRUAL

    }

    "update the lastModifiedDateTime on the persisted object" in {
      await(repo.create(properties))

      val creationDateTime = await(repo.retrieve(nino)).get.lastModifiedDateTime

      DateTimeUtils.setCurrentMillisFixed(DateTime.now().plusDays(1).getMillis)

      await(repo.update(nino, properties.copy(accountingType = AccountingType.ACCRUAL)))

      val lastModifiedDateTime = await(repo.retrieve(nino)).get.lastModifiedDateTime

      lastModifiedDateTime.isAfter(creationDateTime) shouldBe true

      DateTimeUtils.setCurrentMillisSystem()

    }
  }

}
