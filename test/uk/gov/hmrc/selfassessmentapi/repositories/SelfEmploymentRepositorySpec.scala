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

import org.joda.time.LocalDate
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.selfassessmentapi.MongoEmbeddedDatabase
import uk.gov.hmrc.selfassessmentapi.domain.SelfEmployment

import scala.concurrent.ExecutionContext.Implicits.global

class SelfEmploymentRepositorySpec extends UnitSpec with MongoEmbeddedDatabase with BeforeAndAfterEach with BeforeAndAfterAll {

  private val repository = new SelfEmploymentMongoRepository

  override def beforeAll: Unit = {
    mongoStart()
  }

  override def afterAll: Unit = {
    mongoStop()
  }

  override def beforeEach() {
    await(repository.drop)
    await(repository.ensureIndexes)
  }

  "self employment repository" should {
    "create a database record with generated id" in {

      val cd = LocalDate.now()
      val name: String = "Awesome Blacksmiths"
      val selfEmployment: SelfEmployment = SelfEmployment(name = name, commencementDate = cd)
      val id = await(repository.create(selfEmployment))
      val found: SelfEmployment = await(repository.findById(BSONObjectID(id))).get.toSelfEmployment

      found.name shouldBe name
      found.commencementDate shouldBe cd
    }
  }
}
