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

package uk.gov.hmrc.selfassessmentapi

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import uk.gov.hmrc.selfassessmentapi.models.Errors
import uk.gov.hmrc.selfassessmentapi.models.Errors.{InvalidDate, InvalidDateRange_2}
import uk.gov.hmrc.selfassessmentapi.resources.{DesJsons, Jsons}
import uk.gov.hmrc.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}
import uk.gov.hmrc.support.IntegrationBaseSpec
import uk.gov.hmrc.utils.Nino

class SelfAssessmentEndOfPeriodObligationsNewISpec extends IntegrationBaseSpec {

  private trait Test {

    protected val nino: Nino = NinoGenerator().nextNino()

    val regime = "ITSB"
    val correlationId: String = "X-ID"
    val from = "2017-01-01"
    val to = "2017-12-31"
    val testRefNo = "abc"
    val validSelfEmploymentId = "AABB12345678912"

    def uri: String = s"/ni/${nino.nino}/self-employments/$validSelfEmploymentId/end-of-period-statements/obligations"

    def desUrl: String = s"/enterprise/obligation-data/nino/${nino.nino}/ITSA"

    val queryParams: Map[String, String] = Map("from" -> "2017-01-01", "to" -> "2017-12-31")

    def desResponse(res: String): JsValue = Json.parse(res)

    def setupStubs(): StubMapping

    def request: WSRequest = {
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

  "Retrieving end-of-period statement obligations" should {
    "return status code 200 " when {
      "a valid request is received" in new Test {

        val expectedJson: JsValue = Jsons.Obligations.eops

        def mtdQueryParams: Seq[(String, String)] =
          Seq(
            ("from", from),
            ("to", to)
          )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, queryParams, OK, desResponse(DesJsons.Obligations.eopsObligations(validSelfEmploymentId)))
        }

        private val response = await(request(mtdQueryParams).get)
        response.status shouldBe OK
        response.json shouldBe expectedJson
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return status code 400 " when {
      "self-employment-id is invalid" in new Test {

        val expectedJson: JsValue = Json.toJson(Errors.SelfEmploymentIDInvalid)

        def mtdQueryParams: Seq[(String, String)] =
          Seq(
            ("from", from),
            ("to", to)
          )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        override def uri: String = s"/ni/${nino.nino}/self-employments/$testRefNo/end-of-period-statements/obligations"

        private val response = await(request(mtdQueryParams).get)
        response.status shouldBe BAD_REQUEST
        response.json shouldBe expectedJson
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return status code 404 " when {
      "obligations with no 'identification' data is returned" in new Test {

        val expectedJson: JsValue = Json.toJson(Errors.SelfEmploymentIDInvalid)

        def mtdQueryParams: Seq[(String, String)] =
          Seq(
            ("from", from),
            ("to", to)
          )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, queryParams, OK, desResponse(DesJsons.Obligations.eopsObligations(testRefNo)))
        }

        private val response = await(request(mtdQueryParams).get)
        response.status shouldBe NOT_FOUND
      }

      "INVALID_BPKEY error is received from DES" in new Test {

        val expectedJson: JsValue = Json.toJson(Errors.SelfEmploymentIDInvalid)

        val desErrorJson: String =
          s"""
             |{
             |  "code": "ERROR_EOPS_INVALID_DATE_RANGE",
             |  "reason": ""
             |}
            """.stripMargin

        def mtdQueryParams: Seq[(String, String)] =
          Seq(
            ("from", from),
            ("to", to)
          )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onError(DesStub.GET, desUrl, NOT_FOUND, desErrorJson)
        }

        private val response = await(request(mtdQueryParams).get)
        response.status shouldBe NOT_FOUND
      }
    }

    "return validation error according to spec" when {
      def validationErrorTest(requestNino: String, mtdQueryParams: Seq[(String, String)], expectedStatus: Int, expectedBody: Errors.Error): Unit = {
        s"validation fails with ${expectedBody.code} error" in new Test {

          override val nino: Nino = Nino(requestNino)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request(mtdQueryParams).get)
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val input = Seq(
        ("AA123456A", Seq(("from", "201-01-01"), ("to", "2017-12-31")), BAD_REQUEST, InvalidDate),
        ("AA123456A", Seq(("from", "2017-01-01"), ("to", "2016-12-31")), BAD_REQUEST, InvalidDateRange_2))

      input.foreach(args => (validationErrorTest _).tupled(args))
    }

    "des service error" when {
      def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: Errors.Error): Unit = {
        s"des returns an $desCode error and status $desStatus" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onError(DesStub.GET, desUrl, desStatus, errorBody(desCode))
          }

          val response: WSResponse = await(request.get)
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      def errorBody(code: String): String =
        s"""
           |{
           |   "code": "$code",
           |   "reason": "des message"
           |}
            """.stripMargin

      val input = Seq(
        (BAD_REQUEST, "INVALID_STATUS", INTERNAL_SERVER_ERROR, Errors.InternalServerError),
        (BAD_REQUEST, "INVALID_REGIME", INTERNAL_SERVER_ERROR, Errors.InternalServerError),
        (BAD_REQUEST, "INVALID_IDTYPE", INTERNAL_SERVER_ERROR, Errors.InternalServerError),
        (BAD_REQUEST, "INVALID_DATE_TO", BAD_REQUEST, Errors.InvalidDate),
        (BAD_REQUEST, "INVALID_DATE_FROM", BAD_REQUEST, Errors.InvalidDate),
        (BAD_REQUEST, "INVALID_DATE_RANGE", BAD_REQUEST, Errors.InvalidDateRange_2),
        (BAD_REQUEST, "INVALID_IDNUMBER", BAD_REQUEST, Errors.NinoInvalid))

      input.foreach(args => (serviceErrorTest _).tupled(args))
    }
  }
}
