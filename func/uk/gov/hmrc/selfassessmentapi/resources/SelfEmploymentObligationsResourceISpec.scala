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

package uk.gov.hmrc.selfassessmentapi.resources

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, OK}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSRequest
import uk.gov.hmrc.selfassessmentapi.NinoGenerator
import uk.gov.hmrc.selfassessmentapi.models.obligations.ObligationsQueryParams
import uk.gov.hmrc.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}
import uk.gov.hmrc.support.IntegrationBaseSpec
import uk.gov.hmrc.utils.Nino

class SelfEmploymentObligationsResourceISpec extends IntegrationBaseSpec {

  private trait Test {

    protected val nino: Nino = NinoGenerator().nextNino()

    val regime = "ITSB"
    val correlationId: String = "X-ID"

    def uri: String = s"/ni/${nino.nino}/self-employments/abc/obligations"

    def desUrl: String = s"/enterprise/obligation-data/nino/${nino.nino}/ITSA"

    val queryParams: Map[String, String] = Map("from" -> ObligationsQueryParams().from.toString, "to" -> ObligationsQueryParams().to.toString)

    def desResponse(res: String): JsValue = Json.parse(res)

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }

    def request(mtdQueryParams: Seq[(String, String)]): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .addQueryStringParameters(mtdQueryParams: _*)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "retrieveObligations" should {
    "return status code 200 containing a set of canned obligations" when {
      "a valid request is received" in new Test {

        val expectedJson: JsValue = Jsons.Obligations()

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, queryParams, OK, desResponse(DesJsons.Obligations()))
        }

        private val response = await(request().get)
        response.status shouldBe OK
        response.json shouldBe expectedJson
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return status code 400" when {
      "self employment id does not exist" in new Test {

        val expectedJson: JsValue = Jsons.Obligations()

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, queryParams, OK, desResponse(DesJsons.Obligations()))
        }

        private val response = await(request().get)
        response.status shouldBe OK
        response.json shouldBe expectedJson
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return code 404 when attempting to retrieve obligations for the self-employment business that does not exist" when {
      "a valid request is received" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        private val response = await(request().get)
        response.status shouldBe NOT_FOUND
      }
    }

    "return code 404 when obligations with no 'identification' data is returned" when {
      "a valid request is received" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, queryParams, OK, desResponse(DesJsons.Obligations.obligationsNoIdentification))
        }

        private val response = await(request().get)
        response.status shouldBe NOT_FOUND
      }
    }

    "return code 404 when self-employment id does not exist" when {
      "a valid request is received" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, queryParams, NOT_FOUND, desResponse(DesJsons.Errors.notFound))
        }

        private val response = await(request().get)
        response.status shouldBe NOT_FOUND
      }
    }

    "return status code 400 with NINO_INVALID error" when {
      "a request with invalid nino is received" in new Test {

        val expectedJson: JsValue = Json.parse(Jsons.Errors.ninoInvalid)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, queryParams, BAD_REQUEST, desResponse(DesJsons.Errors.invalidNino))
        }

        private val response = await(request().get)
        response.status shouldBe BAD_REQUEST
        response.json shouldBe expectedJson
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return code 400 with FORMAT_TO_DATE error" when {
      "an invalid from date request is received" in new Test {

        val expectedJson: JsValue = Json.parse(s"""
                                                  |{
                                                  |  "code": "FORMAT_FROM_DATE",
                                                  |  "message": "The provided 'from' date is invalid"
                                                  |}""".stripMargin)

        def mtdQueryParams: Seq[(String, String)] =
          Seq(
            ("from", "201-01-01"),
            ("to", ObligationsQueryParams().to.toString)
          )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        private val response = await(request(mtdQueryParams).get)
        response.status shouldBe BAD_REQUEST
        response.json shouldBe expectedJson
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return status code 400 with FORMAT_TO_DATE error" when {
      "an invalid to date request is received" in new Test {

        val expectedJson: JsValue = Json.parse(s"""
                                                  |{
                                                  |  "code": "FORMAT_TO_DATE",
                                                  |  "message": "The provided 'to' date is invalid"
                                                  |}""".stripMargin)

        def mtdQueryParams: Seq[(String, String)] =
          Seq(
            ("from", "2017-01-01"),
            ("to", "201-03-31")
          )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        private val response = await(request(mtdQueryParams).get)
        response.status shouldBe BAD_REQUEST
        response.json shouldBe expectedJson
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return status code 400 with RULE_DATE_PARAMETER error" when {
      "'to' date is supplied with no 'from' date" in new Test {

        val expectedJson: JsValue = Json.parse(s"""
                                                  |{
                                                  |  "code": "RULE_DATE_PARAMETER",
                                                  |  "message": "The 'from' query parameter supplied without the `to` query parameter, or vice-versa"
                                                  |}""".stripMargin)

        def mtdQueryParams: Seq[(String, String)] =
          Seq(
            ("to", "2017-03-31")
          )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        private val response = await(request(mtdQueryParams).get)
        response.status shouldBe BAD_REQUEST
        response.json shouldBe expectedJson
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "'from' date is supplied with no 'to' date" in new Test {

        val expectedJson: JsValue = Json.parse(s"""
                                                  |{
                                                  |  "code": "RULE_DATE_PARAMETER",
                                                  |  "message": "The 'from' query parameter supplied without the `to` query parameter, or vice-versa"
                                                  |}""".stripMargin)

        def mtdQueryParams: Seq[(String, String)] =
          Seq(
            ("from", "2017-03-31")
          )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        private val response = await(request(mtdQueryParams).get)
        response.status shouldBe BAD_REQUEST
        response.json shouldBe expectedJson
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return status code 400 with RANGE_TO_DATE_BEFORE_FROM_DATE error" when {
      "to is before from date" in new Test {

        val expectedJson: JsValue = Json.parse(s"""
                                                  |{
                                                  |  "code": "RANGE_TO_DATE_BEFORE_FROM_DATE",
                                                  |  "message": "The 'to' date is less than the 'from' date"
                                                  |}""".stripMargin)

        def mtdQueryParams: Seq[(String, String)] =
          Seq(
            ("from", "2017-12-01"),
            ("to", "2017-03-31")
          )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        private val response = await(request(mtdQueryParams).get)
        response.status shouldBe BAD_REQUEST
        response.json shouldBe expectedJson
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return status code 400 with RANGE_DATE_TOO_LONG error" when {
      "to is before from date" in new Test {

        val expectedJson: JsValue = Json.parse(s"""
                                                  |{
                                                  |  "code": "RANGE_DATE_TOO_LONG",
                                                  |  "message": "The specified date range is too big"
                                                  |}""".stripMargin)

        def mtdQueryParams: Seq[(String, String)] =
          Seq(
            ("from", "2017-01-01"),
            ("to", "2018-01-02")
          )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        private val response = await(request(mtdQueryParams).get)
        response.status shouldBe BAD_REQUEST
        response.json shouldBe expectedJson
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }
  }
}
