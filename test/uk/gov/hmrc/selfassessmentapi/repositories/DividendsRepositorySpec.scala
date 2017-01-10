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

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.{MongoEmbeddedDatabase, UnitSpec}
import uk.gov.hmrc.selfassessmentapi.controllers.util.NinoGenerator
import uk.gov.hmrc.selfassessmentapi.domain.Dividends
import uk.gov.hmrc.selfassessmentapi.resources.models
import uk.gov.hmrc.selfassessmentapi.resources.models.TaxYear

class DividendsRepositorySpec extends MongoEmbeddedDatabase {
  private val repository = new DividendsRepository

  private val nino = NinoGenerator().nextNino()
  private val exampleDividend =
    Dividends(BSONObjectID.generate, nino, Map(TaxYear("2016-17") -> models.dividends.Dividends(Some(500.25))))


  "update" should {
    "create a dividends object the first time the user inserts a record" in {
      await(repository.update(nino, exampleDividend)) shouldBe true

      val result = await(repository.retrieve(nino))
      result.isDefined shouldBe true
      result.get.dividends.get(TaxYear("2016-17")) shouldBe Some(models.dividends.Dividends(Some(500.25)))
    }

    "overwrite an existing dividend" in {
      val updatedDividend = exampleDividend.copy(dividends =
        exampleDividend.dividends.updated(TaxYear("2016-17"), models.dividends.Dividends(None)))

      await(repository.update(nino, exampleDividend)) shouldBe true
      await(repository.update(nino, updatedDividend)) shouldBe true

      val result = await(repository.retrieve(nino))
      result.isDefined shouldBe true
      result.get.dividends.get(TaxYear("2016-17")) shouldBe Some(models.dividends.Dividends(None))
    }
  }

  "retrieve" should {
    "return None if the user has no dividends" in {
      await(repository.retrieve(nino)) shouldBe None
    }
  }
}
