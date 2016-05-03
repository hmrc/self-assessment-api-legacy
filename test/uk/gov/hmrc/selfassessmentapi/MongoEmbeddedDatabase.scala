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

package uk.gov.hmrc.selfassessmentapi

import de.flapdoodle.embed.mongo.config.{MongodConfigBuilder, Net}
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.{MongodExecutable, MongodProcess, MongodStarter}
import de.flapdoodle.embed.process.runtime.Network
import reactivemongo.api.FailoverStrategy
import reactivemongo.api.collections.bson.BSONCollection
import uk.gov.hmrc.mongo.MongoConnector

trait MongoEmbeddedDatabase {

  private val starter = MongodStarter.getDefaultInstance
  private var mongodExe: MongodExecutable = null
  private var mongod: MongodProcess = null

  protected val port = 12345

  protected val databaseName = "test-" + this.getClass.getSimpleName

  protected val mongoUri: String = s"mongodb://127.0.0.1:$port/$databaseName"

  implicit val mongoConnectorForTest = new MongoConnector(mongoUri)

  implicit val mongo = mongoConnectorForTest.db

  def bsonCollection(name: String)(failoverStrategy: FailoverStrategy = mongoConnectorForTest.helper.db.failoverStrategy): BSONCollection = {
    mongoConnectorForTest.helper.db(name, failoverStrategy)
  }

  def mongoStart() = {
    mongodExe = starter.prepare(new MongodConfigBuilder()
      .version(Version.Main.PRODUCTION)
      .net(new Net(port, Network.localhostIsIPv6()))
      .build());
    mongod = mongodExe.start()
  }

  def mongoStop() = {
    mongod.stop()
    mongodExe.stop()
  }

}