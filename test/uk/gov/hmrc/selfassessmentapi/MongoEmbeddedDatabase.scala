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
import org.scalatest.BeforeAndAfterAll
import play.api.Logger
import uk.gov.hmrc.mongo.MongoConnector

import scala.util.Try

trait MongoEmbeddedDatabase extends UnitSpec with BeforeAndAfterAll {

  private var mongodExe: MongodExecutable = null
  private var mongod: MongodProcess = null

  private val embeddedPort = 12345
  private val mongoUri: String = sys.env.getOrElse("MONGO_TEST_URI", "mongodb://localhost:12345/self-assessment-api")
  private lazy val useEmbeddedMongo = mongoUri.contains(embeddedPort.toString)

  implicit val mongo = new MongoConnector(mongoUri).db

  protected def mongoStart() = {
    if (useEmbeddedMongo) {
      mongodExe = MongodStarter.getDefaultInstance.prepare(new MongodConfigBuilder()
        .version(Version.Main.PRODUCTION)
        .net(new Net(embeddedPort, Network.localhostIsIPv6()))
        .build())
      mongod = mongodExe.start()
    }
  }

  protected def mongoStop() = {
    if (useEmbeddedMongo) {
      mongod.stop()
      mongodExe.stop()
    }
  }

  override def beforeAll = {
    mongoStart()
  }

  override def afterAll = {
    Try(mongoStop()) recover {
      case ex: Throwable => Logger.info(s"MONGO_STOP_FAILED: Couldn't kill mongod process! : $ex")
    }
  }

}