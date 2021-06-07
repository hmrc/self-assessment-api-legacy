/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.support.WireMockMethods

object AuthStub extends WireMockMethods {

  private val authoriseUri: String = "/auth/authorise"

  private val authResponse = Json.parse(s"""
                                |{
                                |  "internalId": "some-id",
                                |  "affinityGroup": "Individual",
                                |  "confidenceLevel": 200,
                                |  "loginTimes": {
                                |     "currentLogin": "2016-11-27T09:00:00.000Z",
                                |     "previousLogin": "2016-11-01T12:00:00.000Z"
                                |  },
                                |  "authorisedEnrolments": [
                                |   {
                                |         "key":"HMRC-AS-AGENT",
                                |         "identifiers":[
                                |            {
                                |               "key":"AgentReferenceNumber",
                                |               "value":"1000051409"
                                |            }
                                |         ],
                                |         "state":"Activated"
                                |      }
                                |  ]
                                |}
          """.stripMargin)

  def authorised(): StubMapping = {
    when(method = POST, uri = authoriseUri)
      .thenReturn(status = OK, body = authResponse)
  }

  def unauthorisedNotLoggedIn(): StubMapping = {
    when(method = POST, uri = authoriseUri)
      .thenReturn(status = UNAUTHORIZED, headers = Map("WWW-Authenticate" -> """MDTP detail="MissingBearerToken""""))
  }

  def unauthorisedOther(): StubMapping = {
    when(method = POST, uri = authoriseUri)
      .thenReturn(status = UNAUTHORIZED, headers = Map("WWW-Authenticate" -> """MDTP detail="InvalidBearerToken""""))
  }

}
