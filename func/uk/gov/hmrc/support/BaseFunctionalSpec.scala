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

package uk.gov.hmrc.support

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import org.joda.time.LocalDate
import org.json.{JSONArray, JSONObject}
import org.scalatest.Assertion
import org.skyscreamer.jsonassert.JSONAssert.assertEquals
import org.skyscreamer.jsonassert.JSONCompareMode.LENIENT
import play.api.http.Status._
import play.api.libs.json._
import uk.gov.hmrc.api.controllers.ErrorNotFound
import uk.gov.hmrc.utils.{Nino, TaxYear}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.r2.selfassessmentapi.models.ErrorResponses.ErrorNotImplemented
import uk.gov.hmrc.selfassessmentapi.models.obligations.ObligationsQueryParams
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.selfassessmentapi.models.Period
import uk.gov.hmrc.selfassessmentapi.resources.DesJsons
import uk.gov.hmrc.selfassessmentapi.{NinoGenerator, TestApplication}

import scala.collection.mutable
import scala.util.matching.Regex

trait BaseFunctionalSpec extends TestApplication with HttpComponent {

  protected val nino: Nino = NinoGenerator().nextNino()

  class Assertions(request: String, response: HttpResponse)(implicit urlPathVariables: mutable.Map[String, String])
    extends UrlInterpolation {
    def jsonBodyIsEmptyObject(): Assertion = response.json shouldBe Json.obj()

    def jsonBodyIsEmptyArray(): Assertion = response.json shouldBe JsArray()

    def responseContainsHeader(name: String, pattern: Regex): Assertions = {
      response.header(name) match {
        case Some(h) => h should fullyMatch regex pattern
        case _ => fail(s"Header [$name] not found in the response headers")
      }
      this
    }

    if (request.startsWith("POST")) {
      response.header("Location").map { location =>
        if (location.contains("/periods")) {
          urlPathVariables += ("periodLocation" -> location.replaceFirst("/self-assessment", ""))
        } else {
          urlPathVariables += ("sourceLocation" -> location.replaceFirst("/self-assessment", ""))
        }
      }
    }

    def when() = new HttpVerbs()

    def butResponseHasNo(sourceName: String, summaryName: String = ""): Assertions = {
      val jsvOpt =
      // FIXME: use \\
        if (summaryName.isEmpty) (response.json \ "_embedded" \ sourceName).toOption
        else (response.json \ "_embedded" \ sourceName \ summaryName).toOption

      jsvOpt match {
        case Some(v) =>
          v.asOpt[List[String]] match {
            case Some(list) => list.isEmpty shouldBe true
            case _ =>
          }
        case None => ()
      }
      this
    }

    def bodyIsError(code: String): Assertions = body(_ \ "code").is(code)

    def isValidationError(error: (String, String)): Assertions = isValidationError(error._1, error._2)

    def isValidationError(path: String, code: String): Assertions = {
      statusIs(BAD_REQUEST).contentTypeIsJson().body(_ \ "code").is("INVALID_REQUEST")

      val errors = (response.json \ "errors").toOption
      errors match {
        case None => fail("didn't find 'errors' element in the json response")
        case Some(e) =>
          (e(0) \ "path").toOption shouldBe Some(JsString(path))
          (e(0) \ "code").toOption shouldBe Some(JsString(code))
      }
      this
    }

    def isBadRequest(path: String, code: String): Assertions = {
      statusIs(BAD_REQUEST).contentTypeIsJson().body(_ \ "path").is(path).body(_ \ "code").is(code)
      this
    }

    def isBadRequest(code: String): Assertions = {
      statusIs(BAD_REQUEST).contentTypeIsJson().body(_ \ "code").is(code)
      this
    }

    def isBadRequest: Assertions = {
      isBadRequest("INVALID_REQUEST")
    }

    def isNotFound: Assertions = {
      statusIs(NOT_FOUND).contentTypeIsJson().bodyIsError(ErrorNotFound.errorCode)
      this
    }

    def isNotImplemented: Assertions = {
      statusIs(501).contentTypeIsJson().bodyIsError(ErrorNotImplemented.errorCode)
      this
    }

    def contentTypeIsXml(): Assertions = contentTypeIs("application/xml")

    def contentTypeIsJson(): Assertions = contentTypeIs("application/json")

    def contentTypeIsHalJson(): Assertions = contentTypeIs("application/hal+json")

    def noInteractionsWithExternalSystems(): Unit = {
      verify(0, RequestPatternBuilder.allRequests())
    }

    def bodyIs(expectedBody: String): Assertions = {
      response.body shouldBe expectedBody
      this
    }

    def bodyIs(expectedBody: JsValue): Assertions = {
      (response.json match {
        case JsObject(_) => response.json.as[JsObject] - "_links" - "id"
        case json => json
      }) shouldEqual expectedBody
      this
    }

    def bodyIsLike(expectedBody: String): Assertions = {
      response.json match {
        case JsArray(_) => assertEquals(expectedBody, new JSONArray(response.body), LENIENT)
        case _ => assertEquals(expectedBody, new JSONObject(response.body), LENIENT)
      }
      this
    }

    def bodyHasLink(rel: String, href: String): Assertions = {
      getLinkFromBody(rel) shouldEqual Some(interpolated(href))
      this
    }

    def bodyHasPath[T](path: String, value: T)(implicit reads: Reads[T]): Assertions = {
      extractPathElement(path) shouldEqual Some(value)
      this
    }

    def bodyHasPath(path: String, valuePattern: Regex): Assertions = {
      extractPathElement[String](path) match {
        case Some(x) =>
          valuePattern findFirstIn x match {
            case Some(_) =>
            case None => fail(s"$x did not match pattern")
          }
        case None => fail(s"No value found for $path")
      }
      this
    }

    def bodyDoesNotHavePath[T](path: String)(implicit reads: Reads[T]): Assertions = {
      extractPathElement[T](path) match {
        case Some(x) => fail(s"$x match found")
        case None =>
      }
      this
    }

    private def extractPathElement[T](path: String)(implicit reads: Reads[T]): Option[T] = {
      val pathSeq = path.filter(!_.isWhitespace).split('\\').toSeq.filter(_.nonEmpty)

      def op(js: Option[JsValue], pathElement: String): Option[JsValue] = {
        val pattern = """(.*)\((\d+)\)""".r
        js match {
          case Some(v) =>
            pathElement match {
              case pattern(arrayName, index) =>
                js match {
                  case Some(v) =>
                    if (arrayName.isEmpty) Some(v(index.toInt)) else Some((v \ arrayName) (index.toInt))
                  case None => None
                }
              case _ => (v \ pathElement).toOption
            }
          case None => None
        }
      }

      pathSeq.foldLeft(Some(response.json): Option[JsValue])(op).flatMap(jsValue => jsValue.asOpt[T])
    }

    private def getLinkFromBody(rel: String): Option[String] =
      if (response.body.isEmpty) None
      else
        (for {
          links <- (response.json \ "_links").toOption
          link <- (links \ rel).toOption
          href <- (link \ "href").toOption

        } yield href.asOpt[String]).flatten

    def bodyHasLink(rel: String, hrefPattern: Regex): Assertions = {
      getLinkFromBody(rel) match {
        case Some(href) =>
          interpolated(hrefPattern).r findFirstIn href match {
            case Some(_) =>
            case None => fail(s"$href did not match pattern")
          }
        case None => fail(s"No href found for $rel")
      }
      this
    }

    def bodyHasString(content: String): Assertions = {
      response.body.contains(content) shouldBe true
      this
    }

    def bodyDoesNotHaveString(content: String): Assertions = {
      response.body.contains(content) shouldBe false
      this
    }

    def statusIs(statusCode: Regex): Assertions = {
      withClue(s"expected $request to return $statusCode; but got ${response.body}\n") {
        response.status.toString should fullyMatch regex statusCode
      }
      this
    }

    def statusIs(statusCode: Int): Assertions = {
      withClue(s"expected $request to return $statusCode; but got ${response.body}\n") {
        response.status shouldBe statusCode
      }
      this
    }

    private def contentTypeIs(contentType: String) = {
      response.header("Content-Type") shouldEqual Some(contentType)
      this
    }

    def body(myQuery: JsValue => JsLookupResult): BodyAssertions = {
      new BodyAssertions(myQuery(response.json).toOption, this)
    }

    def selectFields(myQuery: JsValue => Seq[JsValue]): BodyListAssertions = {
      new BodyListAssertions(myQuery(response.json), this)
    }

    class BodyAssertions(content: Option[JsValue], assertions: Assertions) {
      def is(value: String): Assertions = {
        content match {
          case Some(v) =>
            v.asOpt[String] match {
              case Some(actualValue) => actualValue shouldBe value
              case _ => "" shouldBe value
            }
          case None => fail(s"no value found matching $value")
        }
        assertions
      }

      def isAbsent: Assertions = {
        content shouldBe None
        assertions
      }

      def is(value: BigDecimal): Assertions = {
        content match {
          case Some(v) => v.as[BigDecimal] shouldBe value
          case None => fail()
        }
        assertions
      }
    }

    class BodyListAssertions(content: Seq[JsValue], assertions: Assertions) {
      def isLength(n: Int): BodyListAssertions = {
        content.size shouldBe n
        this
      }

      def matches(matcher: Regex): Assertions = {
        content.map(_.as[String]).forall {
          case matcher(_*) => true
          case _ => false
        } shouldBe true

        assertions
      }

      def is(value: String*): Assertions = {
        content.map(con => con.as[String]) should contain theSameElementsAs value
        assertions
      }
    }

  }

  class HttpRequest(method: String, path: String, body: Option[JsValue], hc: HeaderCarrier = HeaderCarrier())(
    implicit urlPathVariables: mutable.Map[String, String])
    extends UrlInterpolation {

    private val interpolatedPath: String = interpolated(path)
    assert(interpolatedPath.startsWith("/"), "please provide only a path starting with '/'")

    val url = s"http://localhost:$port$interpolatedPath"
    var addAcceptHeader = true
    var acceptHeader = "application/vnd.hmrc.1.0+json"

    def thenAssertThat(): Assertions = {
      implicit val carrier: HeaderCarrier =
        if (addAcceptHeader) HeaderCarrier(extraHeaders = Seq("Accept" -> "application/vnd.hmrc.1.0+json", "Authorization" -> hc.authorization.map(_.value).getOrElse("")) ++ hc.extraHeaders) else hc

      withClue(s"Request $method $url") {
        method match {
          case "GET" => new Assertions(s"GET@$url", Http.get(url))
          case "DELETE" => new Assertions(s"DELETE@$url", Http.delete(url))
          case "POST" =>
            body match {
              case Some(jsonBody) => new Assertions(s"POST@$url", Http.postJson(url, jsonBody))
              case None => new Assertions(s"POST@$url", Http.postEmpty(url))
            }
          case "PUT" =>
            val jsonBody = body.getOrElse(throw new RuntimeException("Body for PUT must be provided"))
            new Assertions(s"PUT@$url", Http.putJson(url, jsonBody))
        }
      }
    }

    def withAcceptHeader(): HttpRequest = {
      addAcceptHeader = true
      this
    }

    def withoutAcceptHeader(): HttpRequest = {
      addAcceptHeader = false
      this
    }

    def withHeaders(header: String, value: String): HttpRequest = {
      new HttpRequest(method, path, body, hc.withExtraHeaders(header -> value))
    }
  }

  class HttpPostBodyWrapper(method: String, body: Option[JsValue])(
    implicit urlPathVariables: mutable.Map[String, String]) {
    def to(url: String): HttpRequest = new HttpRequest(method, url, body)
  }

  class HttpPutBodyWrapper(method: String, body: Option[JsValue])(
    implicit urlPathVariables: mutable.Map[String, String]) {
    def at(url: String): HttpRequest = new HttpRequest(method, url, body)
  }

  class HttpVerbs()(implicit urlPathVariables: mutable.Map[String, String] = mutable.Map()) {

    def post(body: JsValue): HttpPostBodyWrapper = {
      new HttpPostBodyWrapper("POST", Some(body))
    }

    def put(body: JsValue): HttpPutBodyWrapper = {
      new HttpPutBodyWrapper("PUT", Some(body))
    }

    def get(path: String): HttpRequest = {
      new HttpRequest("GET", path, None)
    }

    def delete(path: String): HttpRequest = {
      new HttpRequest("DELETE", path, None)
    }

    def post(path: String, body: Option[JsValue] = None): HttpRequest = {
      new HttpRequest("POST", path, body)
    }

    def put(path: String, body: Option[JsValue]): HttpRequest = {
      new HttpRequest("PUT", path, body)
    }

  }

  class Givens {

    implicit val urlPathVariables: mutable.Map[String, String] = mutable.Map()

    def when(): HttpVerbs = new HttpVerbs()

    def stubAudit: Givens = {
      stubFor(post(urlPathMatching(s"/write/audit.*"))
        .willReturn(
          aResponse()
            .withStatus(NO_CONTENT)))
      this
    }

    def userIsSubscribedToMtdFor(nino: Nino, mtdId: String = "abc"): Givens = {
      stubFor(any(urlMatching(s".*/registration/business-details/nino/${nino.nino}"))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withHeader("Content-Type", "application/json")
            .withBody(DesJsons.SelfEmployment(nino, mtdId))))

      this
    }

    def userIsNotSubscribedToMtdFor(nino: Nino): Givens = {
      stubFor(any(urlMatching(s".*/registration/business-details/nino/${nino.nino}"))
        .willReturn(
          aResponse()
            .withStatus(NOT_FOUND)
            .withHeader("Content-Type", "application/json")
            .withBody(DesJsons.Errors.ninoNotFound)))

      this
    }

    def businessDetailsLookupReturns500Error(nino: Nino): Givens = {
      stubFor(any(urlMatching(s".*/registration/business-details/nino/${nino.nino}"))
        .willReturn(
          aResponse()
            .withStatus(INTERNAL_SERVER_ERROR)
            .withHeader("Content-Type", "application/json")
            .withBody(DesJsons.Errors.serverError)))

      this
    }

    def businessDetailsLookupReturns503Error(nino: Nino): Givens = {
      stubFor(any(urlMatching(s".*/registration/business-details/nino/${nino.nino}"))
        .willReturn(
          aResponse()
            .withStatus(SERVICE_UNAVAILABLE)
            .withHeader("Content-Type", "application/json")
            .withBody(DesJsons.Errors.serviceUnavailable)))

      this
    }

    def missingBearerToken: Givens = {
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .willReturn(aResponse()
          .withStatus(UNAUTHORIZED)
          .withHeader("Content-Length", "0")
          .withHeader("WWW-Authenticate", "MDTP detail=\"MissingBearerToken\"")))

      this
    }

    def upstream502BearerTokenDecryptionError: Givens = {
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .willReturn(aResponse()
          .withStatus(BAD_GATEWAY)
          .withHeader("Content-Type", "application/json")
          .withBody("""{"statusCode":500,"message":"Unable to decrypt value"}""")))

      this
    }

    def upstream5xxError: Givens = {
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .willReturn(aResponse()
          .withStatus(INTERNAL_SERVER_ERROR)))

      this
    }

    def upstream4xxError: Givens = {
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .willReturn(aResponse()
          .withStatus(FORBIDDEN)))

      this
    }

    def upstreamNonFatal: Givens = {
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .willReturn(aResponse()
          .withStatus(509))) // very brittle test that relies on how http-verbs.HttpErrorFunctions maps upstream status codes

      this
    }

    def userIsNotAuthorisedForTheResource: Givens = {
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .willReturn(aResponse()
          .withStatus(UNAUTHORIZED)
          .withHeader("Content-Length", "0")
          .withHeader("WWW-Authenticate", "MDTP detail=\"InsufficientEnrolments\"")))

      // The user is an 'Individual/Group', so the affinity check for 'Agent' should fail.
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .withRequestBody(containing("Agent"))
        .willReturn(aResponse()
          .withStatus(UNAUTHORIZED)
          .withHeader("Content-Length", "0")
          .withHeader("WWW-Authenticate", "MDTP detail=\"UnsupportedAffinityGroup\"")))

      this
    }

    def userIsPartiallyAuthorisedForTheResource: Givens = {

      // First call to auth to check if fully authorised should fail.
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .withRequestBody(containing("HMRC-MTD-IT"))
        .willReturn(aResponse()
          .withStatus(UNAUTHORIZED)
          .withHeader("Content-Length", "0")
          .withHeader("WWW-Authenticate", "MDTP detail=\"InsufficientEnrolments\"")))

      // Second call to auth to check affinity is equal to 'Agent' should succeed.
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .withRequestBody(containing("Agent"))
        .willReturn(aResponse().withStatus(OK).withBody(
          """
            |{
            |  "internalId": "some-id",
            |  "affinityGroup": "Agent",
            |  "agentCode": "some-agent-code",
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
          """.stripMargin)))

      // Third call to auth to check FOA subscription status should succeed.
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .withRequestBody(containing("HMRC-AS-AGENT"))
        .willReturn(aResponse().withStatus(OK).withBody(
          """
            |{
            |  "internalId": "some-id",
            |  "affinityGroup": "Agent",
            |  "agentCode": "some-agent-code",
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
          """.stripMargin)))

      this
    }

    def userIsNotPartiallyAuthorisedForTheResource: Givens = {
      // First call to auth to check if fully authorised should fail.
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .withRequestBody(containing("HMRC-MTD-IT"))
        .willReturn(aResponse()
          .withStatus(UNAUTHORIZED)
          .withHeader("Content-Length", "0")
          .withHeader("WWW-Authenticate", "MDTP detail=\"InsufficientEnrolments\"")))

      // Second call to auth to check affinity is equal to 'Agent' should succeed.
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .withRequestBody(containing("Agent"))
        .willReturn(aResponse().withStatus(OK).withBody(
          """
            |{
            |  "internalId": "some-id",
            |  "affinityGroup": "Agent",
            |  "loginTimes": {
            |     "currentLogin": "2016-11-27T09:00:00.000Z",
            |     "previousLogin": "2016-11-01T12:00:00.000Z"
            |  }
            |}
          """.stripMargin)))

      // Third call to auth to check FOA subscription status should fail.
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .withRequestBody(containing("HMRC-AS-AGENT"))
        .willReturn(aResponse()
          .withStatus(UNAUTHORIZED)
          .withHeader("Content-Length", "0")
          .withHeader("WWW-Authenticate", "MDTP detail=\"InsufficientEnrolments\"")))

      this
    }

    def userIsPartiallyAuthorisedForTheResourceNoAgentCode: Givens = {

      // First call to auth to check if fully authorised should fail.
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .withRequestBody(containing("HMRC-MTD-IT"))
        .willReturn(aResponse()
          .withStatus(UNAUTHORIZED)
          .withHeader("Content-Length", "0")
          .withHeader("WWW-Authenticate", "MDTP detail=\"InsufficientEnrolments\"")))

      // Second call to auth to check affinity is equal to 'Agent' should succeed.
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .withRequestBody(containing("Agent"))
        .willReturn(aResponse().withStatus(OK).withBody(
          """
            |{
            |  "internalId": "some-id",
            |  "affinityGroup": "Agent",
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
          """.stripMargin)))

      // Third call to auth to check FOA subscription status should succeed.
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .withRequestBody(containing("HMRC-AS-AGENT"))
        .willReturn(aResponse().withStatus(OK).withBody(
          """
            |{
            |  "internalId": "some-id",
            |  "affinityGroup": "Agent",
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
          """.stripMargin)))

      this
    }

    def clientIsAuthorisedForTheResourceWithoutCL200: Givens = {
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .willReturn(aResponse().withStatus(OK).withBody(
          s"""
            |{
            |  "internalId": "some-id",
            |  "affinityGroup": "Individual",
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
          """.stripMargin)))

      this
    }

    def clientIsFullyAuthorisedForTheResource: Givens = {
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .willReturn(aResponse().withStatus(OK).withBody(
          s"""
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
          """.stripMargin)))

      this
    }

    def agentIsFullyAuthorisedForTheResource: Givens = {
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .willReturn(aResponse().withStatus(OK).withBody(
          """
            |{
            |  "internalId": "some-id",
            |  "affinityGroup": "Agent",
            |  "agentCode": "some-agent-code",
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
          """.stripMargin)))

      this
    }

    def agentIsFullyAuthorisedForTheResourceNoAgentCode: Givens = {
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .willReturn(aResponse().withStatus(OK).withBody(
          """
            |{
            |  "internalId": "some-id",
            |  "affinityGroup": "Agent",
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
          """.stripMargin)))

      this
    }

    def userIsFullyAuthorisedForTheResource: Givens = {
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .willReturn(aResponse().withStatus(OK).withBody(
          """
            |{
            |  "internalId": "some-id",
            |  "loginTimes": {
            |     "currentLogin": "2016-11-27T09:00:00.000Z",
            |     "previousLogin": "2016-11-01T12:00:00.000Z"
            |  }
            |}
          """.stripMargin)))

      this
    }

    class Des(givens: Givens) {
      def isATeapotFor(nino: Nino): Givens = {
        stubFor(any(urlMatching(s".*/(calculation-data|nino)/${nino.nino}.*"))
          .willReturn(
            aResponse()
              .withStatus(418)))

        givens
      }

      def invalidBusinessIdFor(nino: Nino): Givens = {
        stubFor(
          any(urlMatching(s".*/nino/${nino.nino}.*"))
            .willReturn(
              aResponse()
                .withStatus(BAD_REQUEST)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.invalidBusinessId)
            ))

        givens
      }

      def invalidOriginatorIdFor(nino: Nino): Givens = {
        stubFor(
          any(urlMatching(s".*/nino/${nino.nino}.*"))
            .willReturn(
              aResponse()
                .withStatus(BAD_REQUEST)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.invalidOriginatorId)
            ))

        givens
      }

      def serviceUnavailableFor(nino: Nino): Givens = {
        stubFor(any(urlMatching(s".*/nino/${nino.nino}.*"))
          .willReturn(
            aResponse()
              .withStatus(SERVICE_UNAVAILABLE)
              .withHeader("Content-Type", "application/json")
              .withBody(DesJsons.Errors.serviceUnavailable)
          ))

        givens
      }

      def serverErrorFor(nino: Nino): Givens = {
        stubFor(any(urlMatching(s".*/nino/${nino.nino}.*"))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
              .withHeader("Content-Type", "application/json")
              .withBody(DesJsons.Errors.serverError)
          ))

        givens
      }

      def ninoNotFoundFor(nino: Nino): Givens = {
        stubFor(any(urlMatching(s".*/nino/${nino.nino}.*"))
          .willReturn(
            aResponse()
              .withStatus(NOT_FOUND)
              .withHeader("Content-Type", "application/json")
              .withBody(DesJsons.Errors.ninoNotFound)))

        givens
      }

      def invalidNinoFor(nino: Nino): Givens = {
        stubFor(any(urlMatching(s".*/nino/${nino.nino}.*"))
          .willReturn(
            aResponse()
              .withStatus(BAD_REQUEST)
              .withHeader("Content-Type", "application/json")
              .withBody(DesJsons.Errors.invalidNino)))

        givens
      }

      def payloadFailsValidationFor(nino: Nino): Givens = {
        stubFor(any(urlMatching(s".*/nino/${nino.nino}/.*"))
          .willReturn(
            aResponse()
              .withStatus(BAD_REQUEST)
              .withHeader("Content-Type", "application/json")
              .withBody(DesJsons.Errors.invalidPayload)))

        givens
      }

      object selfEmployment {
        def annualSummaryNotFoundFor(nino: Nino): Givens = {
          stubFor(any(urlMatching(s".*/income-store/nino/${nino.nino}/self-employments/.*"))
            .willReturn(
              aResponse()
                .withStatus(NOT_FOUND)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.ninoNotFound)))

          givens
        }

        def invalidOriginatorIdFor(nino: Nino): Givens = {
          stubFor(any(urlMatching(s"/income-store/nino/${nino.nino}/self-employments/.*"))
            .willReturn(
              aResponse()
                .withStatus(BAD_REQUEST)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.invalidOriginatorId)
            ))

          givens
        }

        def tooManySourcesFor(nino: Nino): Givens = {
          stubFor(post(urlEqualTo(s"/income-tax-self-assessment/nino/${nino.nino}/business"))
            .willReturn(
              aResponse()
                .withStatus(FORBIDDEN)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.tooManySources)
            ))

          givens
        }

        def failsTradingName(nino: Nino): Givens = {
          stubFor(any(urlEqualTo(s"/income-tax-self-assessment/nino/${nino.nino}/business"))
            .willReturn(
              aResponse()
                .withStatus(409)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.tradingNameConflict)
            ))

          givens
        }

        def willBeCreatedFor(nino: Nino, id: String = "abc", mtdId: String = "123"): Givens = {
          stubFor(post(urlEqualTo(s"/income-tax-self-assessment/nino/${nino.nino}/business"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.SelfEmployment.createResponse(id, mtdId))))
          givens
        }

        def willBeReturnedFor(nino: Nino, mtdId: String = "123", id: String = "abc", accPeriodStart: String = "2017-04-06", accPeriodEnd: String = "2018-04-05"): Givens = {
          stubFor(get(urlEqualTo(s"/registration/business-details/nino/${nino.nino}"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.SelfEmployment(nino, mtdId, id, accPeriodStart = accPeriodStart, accPeriodEnd = accPeriodEnd))))

          givens
        }

        def periodWillBeCreatedFor(nino: Nino, id: String = "abc"): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/${nino.nino}/self-employments/$id/periodic-summaries"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.SelfEmployment.Period.createResponse())))

          givens
        }

        def periodWillBeNotBeCreatedFor(nino: Nino, id: String = "abc"): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/${nino.nino}/self-employments/$id/periodic-summaries"))
            .willReturn(
              aResponse()
                .withStatus(NOT_FOUND)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.notFound)))

          givens
        }

        def periodsWillBeReturnedFor(nino: Nino, id: String = "abc"): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/${nino.nino}/self-employments/$id/periodic-summaries"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.SelfEmployment.Period.periods)))

          givens
        }

        def invalidPeriodsJsonFor(nino: Nino, id: String = "abc"): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/${nino.nino}/self-employments/$id/periodic-summaries"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(Json.obj().toString())))

          givens
        }

        def periodWillBeReturnedFor(nino: Nino, id: String = "abc", from: String, to: String): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/${nino.nino}/self-employments/$id/periodic-summary-detail?from=$from&to=$to"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.SelfEmployment.Period())))

          givens
        }

        def periodWithNegativeBadDebtsWillBeReturnedFor(nino: Nino, id: String = "abc", from: String, to: String): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/${nino.nino}/self-employments/$id/periodic-summary-detail?from=$from&to=$to"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.SelfEmployment.Period.withNegativeBadDebts())))

          givens
        }

        def periodWillBeUpdatedFor(nino: Nino, id: String = "abc", from: String, to: String): Givens = {
          stubFor(put(urlEqualTo(s"/income-store/nino/${nino.nino}/self-employments/$id/periodic-summaries?from=$from&to=$to"))
            .willReturn(
              aResponse()
                .withStatus(OK)))

          givens
        }

        def periodWillNotBeUpdatedFor(nino: Nino, id: String = "abc", from: String, to: String): Givens = {
          stubFor(put(urlEqualTo(s"/income-store/nino/${nino.nino}/self-employments/$id/periodic-summaries?from=$from&to=$to"))
            .willReturn(
              aResponse()
                .withStatus(NOT_FOUND)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.ninoNotFound)))

          givens
        }

        def noPeriodFor(nino: Nino, id: String = "abc", from: String, to: String): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/${nino.nino}/self-employments/$id/periodic-summary-detail?from=$from&to=$to"))
            .willReturn(
              aResponse()
                .withStatus(NOT_FOUND)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.ninoNotFound)))

          givens
        }

        def invalidDateFrom(nino: Nino, id: String = "abc", from: String, to: String): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/${nino.nino}/self-employments/$id/periodic-summary-detail?from=$from&to=$to"))
            .willReturn(
              aResponse()
                .withStatus(BAD_REQUEST)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.invalidDateFrom)))

          givens
        }

        def invalidDateTo(nino: Nino, id: String = "abc", from: String, to: String): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/${nino.nino}/self-employments/$id/periodic-summary-detail?from=$from&to=$to"))
            .willReturn(
              aResponse()
                .withStatus(BAD_REQUEST)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.invalidDateTo)))

          givens
        }

        def noPeriodsFor(nino: Nino, id: String = "abc"): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/${nino.nino}/self-employments/$id/periodic-summaries"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.SelfEmployment.Period.emptyPeriods)))

          givens
        }

        def overlappingPeriodFor(nino: Nino, id: String = "abc"): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/${nino.nino}/self-employments/$id/periodic-summaries"))
            .willReturn(
              aResponse()
                .withStatus(409)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.overlappingPeriod)))

          givens
        }

        def nonContiguousPeriodFor(nino: Nino, id: String = "abc"): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/${nino.nino}/self-employments/$id/periodic-summaries"))
            .willReturn(
              aResponse()
                .withStatus(409)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.nonContiguousPeriod)))

          givens
        }

        def misalignedPeriodFor(nino: Nino, id: String = "abc"): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/${nino.nino}/self-employments/$id/periodic-summaries"))
            .willReturn(
              aResponse()
                .withStatus(409)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.misalignedPeriod)))

          givens
        }

        def annualSummaryWillBeUpdatedFor(nino: Nino, id: String = "abc", taxYear: TaxYear = TaxYear("2017-18")): Givens = {
          stubFor(put(urlEqualTo(s"/income-store/nino/${nino.nino}/self-employments/$id/annual-summaries/${taxYear.toDesTaxYear}"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withBody(DesJsons.SelfEmployment.AnnualSummary.response)))

          givens
        }

        def annualSummaryWillNotBeUpdatedFor(nino: Nino, id: String = "abc", taxYear: TaxYear = TaxYear("2017-18")): Givens = {
          stubFor(put(urlEqualTo(s"/income-store/nino/${nino.nino}/self-employments/$id/annual-summaries/${taxYear.toDesTaxYear}"))
            .willReturn(
              aResponse()
                .withStatus(NOT_FOUND)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.ninoNotFound)))

          givens
        }

        def annualSummaryWillNotBeReturnedFor(nino: Nino, id: String = "abc", taxYear: TaxYear = TaxYear("2017-18")): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/${nino.nino}/self-employments/$id/annual-summaries/${taxYear.toDesTaxYear}"))
            .willReturn(
              aResponse()
                .withStatus(NOT_FOUND)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.ninoNotFound)))

          givens
        }

        def annualSummaryWillBeReturnedFor(nino: Nino, id: String = "abc", taxYear: TaxYear = TaxYear("2017-18")): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/${nino.nino}/self-employments/$id/annual-summaries/${taxYear.toDesTaxYear}"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.SelfEmployment.AnnualSummary())))

          givens
        }

        def noAnnualSummaryFor(nino: Nino, id: String = "abc", taxYear: TaxYear = TaxYear("2017-18")): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/${nino.nino}/self-employments/$id/annual-summaries/${taxYear.toDesTaxYear}"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(Json.obj().toString)))

          givens
        }

        def willBeUpdatedFor(nino: Nino, id: String = "abc"): Givens = {
          stubFor(put(urlEqualTo(s"/income-tax-self-assessment/nino/${nino.nino}/incomeSourceId/$id/regime/ITSA"))
            .willReturn(
              aResponse()
                .withStatus(OK)))

          givens
        }

        def willNotBeUpdatedFor(nino: Nino): Givens = {
          stubFor(put(urlMatching(s"/income-tax-self-assessment/nino/${nino.nino}/business/.*"))
            .willReturn(
              aResponse()
                .withStatus(NOT_FOUND)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.ninoNotFound)))

          givens
        }

        def doesNotExistPeriodFor(nino: Nino, id: String = "abc"): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/${nino.nino}/self-employments/$id/periodic-summaries"))
            .willReturn(
              aResponse()
                .withStatus(NOT_FOUND)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.ninoNotFound)))

          givens
        }

        def noneFor(nino: Nino, mtdId: String = "123"): Givens = {
          stubFor(get(urlEqualTo(s"/registration/business-details/nino/${nino.nino}"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.SelfEmployment.emptySelfEmployment(nino, mtdId))))

          givens
        }

        def noContentTypeFor(nino: Nino, mtdId: String = "123"): Givens = {
          stubFor(get(urlEqualTo(s"/registration/business-details/nino/${nino.nino}"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withBody(DesJsons.SelfEmployment.emptySelfEmployment(nino, mtdId))))

          givens
        }

        def incomeIdNotFoundFor(nino: Nino, mtdId: String = "123"): Givens = {
          stubFor(get(urlEqualTo(s"/registration/business-details/nino/${nino.nino}"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.SelfEmployment(nino, mtdId, id = "inexistent"))))

          givens
        }

        def invalidJson(nino: Nino, mtdId: String = "123"): Givens = {
          stubFor(get(urlEqualTo(s"/registration/business-details/nino/${nino.nino}"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody("""{ "businessData": 1 }""")))

          givens
        }

        def endOfYearStatementReadyToBeFinalised(nino: Nino, start: LocalDate, end: LocalDate, id: String = "abc"): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/${nino.nino}/self-employments/$id/accounting-periods/${start}_$end/statement"))
            .willReturn(
              aResponse()
                .withStatus(NO_CONTENT)
                .withBody("")))

          givens
        }

        def endOfYearStatementMissingPeriod(nino: Nino, start: LocalDate, end: LocalDate, id: String = "abc"): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/${nino.nino}/self-employments/$id/accounting-periods/${start}_$end/statement"))
            .willReturn(
              aResponse()
                .withStatus(FORBIDDEN)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.periodicUpdateMissing)))

          givens
        }

        def endOfYearStatementIsEarly(nino: Nino, start: LocalDate, end: LocalDate, id: String = "abc"): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/${nino.nino}/self-employments/$id/accounting-periods/${start}_$end/statement"))
            .willReturn(
              aResponse()
                .withStatus(BAD_REQUEST)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.earlySubmission)))

          givens
        }

        def endOfYearStatementIsLate(nino: Nino, start: LocalDate, end: LocalDate, id: String = "abc"): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/${nino.nino}/self-employments/$id/accounting-periods/${start}_$end/statement"))
            .willReturn(
              aResponse()
                .withStatus(FORBIDDEN)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.lateSubmission)))

          givens
        }

        def endOfYearStatementDoesNotMatchPeriod(nino: Nino, start: LocalDate, end: LocalDate, id: String = "abc"): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/${nino.nino}/self-employments/$id/accounting-periods/${start}_$end/statement"))
            .willReturn(
              aResponse()
                .withStatus(FORBIDDEN)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.nonMatchingPeriod)))

          givens
        }

        def endOfYearStatementAlreadySubmitted(nino: Nino, start: LocalDate, end: LocalDate, id: String = "abc"): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/${nino.nino}/self-employments/$id/accounting-periods/${start}_$end/statement"))
            .willReturn(
              aResponse()
                .withStatus(FORBIDDEN)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.alreadySubmitted)))

          givens
        }

      }

      object crystallisation {
        def intentToCrystallise(nino: Nino, taxYear: TaxYear = TaxYear("2017-18")): Givens = {
          stubFor(post(urlEqualTo(s"/income-tax/nino/${nino.nino}/taxYear/${taxYear.toDesTaxYear}/tax-calculation?crystallise=true"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Crystallisation.intentToCrystallise())))

          givens
        }

        def intentToCrystalliseRequiredEndOfPeriodStatement(nino: Nino, taxYear: TaxYear): Givens = {
          stubFor(post(urlEqualTo(s"/income-tax/nino/${nino.nino}/taxYear/${taxYear.toDesTaxYear}/tax-calculation?crystallise=true"))
            .willReturn(
              aResponse()
                .withStatus(BAD_REQUEST)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.requiredEndOfPeriodStatement)))
          givens
        }

        def crystallise(nino: Nino, taxYear: TaxYear = TaxYear("2017-18"), calcId: String): Givens = {
          stubFor(post(urlMatching(s"/income-tax/calculation/nino/${nino.nino}/${taxYear.toDesTaxYear}/$calcId/crystallise"))
            .willReturn(
              aResponse()
                .withStatus(NO_CONTENT)
                .withHeader("Content-Type", "application/json")
                .withBody(
                  s"""
                     |{
                     |}
                          """.stripMargin
                )))

          givens
        }

        def crystalliseError(nino: Nino, taxYear: TaxYear = TaxYear("2017-18"), calcId: String)(responseStatus: Int, responseBody: String): Givens = {
          stubFor(post(urlMatching(s"/income-tax/calculation/nino/${nino.nino}/${taxYear.toDesTaxYear}/$calcId/crystallise"))
            .willReturn(
              aResponse()
                .withStatus(responseStatus)
                .withHeader("Content-Type", "application/json")
                .withBody(responseBody)))
          givens
        }

        def crystallisationObligation(nino: Nino, taxYear: TaxYear = TaxYear("2017-18")): Givens = {
          stubFor(get(urlEqualTo(s"/enterprise/obligation-data/nino/${nino.nino}/ITSA?from=${taxYear.taxYearFromDate}&to=${taxYear.taxYearToDate}"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Obligations.crystallisationObligations(nino.nino, taxYear))))
          givens
        }
      }

      object obligations {
        def obligationNotFoundFor(nino: Nino): Givens = {
          stubFor(get(urlEqualTo(s"/enterprise/obligation-data/nino/${nino.nino}/ITSA?${ObligationsQueryParams().from}&to=${ObligationsQueryParams().to}"))
            .willReturn(
              aResponse()
                .withStatus(NOT_FOUND)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.notFound)))

          givens
        }

        def returnObligationsFor(nino: Nino): Givens = {
          stubFor(get(urlEqualTo(s"/enterprise/obligation-data/nino/${nino.nino}/ITSA?from=${ObligationsQueryParams().from}&to=${ObligationsQueryParams().to}"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withHeader("CorrelationId", "abc")
                .withBody(DesJsons.Obligations())))

          givens
        }

        def returnObligationsWithNoIdentificationFor(nino: Nino): Givens = {
          stubFor(get(urlEqualTo(s"/enterprise/obligation-data/nino/${nino.nino}/ITSA?from=${ObligationsQueryParams().from}&to=${ObligationsQueryParams().to}"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withHeader("CorrelationId", "abc")
                .withBody(DesJsons.Obligations.obligationsNoIdentification)))
          givens
        }

        def returnEopsObligationsWithNoIdentificationFor(nino: Nino): Givens = {
          stubFor(get(urlMatching(s"/enterprise/obligation-data/nino/${nino.nino}/ITSA.*"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withHeader("CorrelationId", "abc")
                .withBody(DesJsons.Obligations.obligationsNoIdentification)))
          givens
        }


        def returnEndOfPeriodObligationsFor(nino: Nino, refNo: String): Givens = {
          stubFor(get(urlMatching(s"/enterprise/obligation-data/nino/${nino.nino}/ITSA.*"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withHeader("CorrelationId", "abc")
                .withBody(DesJsons.Obligations.eopsObligations(refNo))))
          givens
        }

        def returnEopsObligationsErrorFor(nino: Nino)(status: Int, body: String): Givens = {

          def error(code: String): String = {
            s"""
               |{
               |  "code": "$code",
               |  "reason": ""
               |}
            """.stripMargin
          }

          stubFor(get(urlMatching(s"/enterprise/obligation-data/nino/${nino.nino}/ITSA.*"))
            .willReturn(
              aResponse()
                .withStatus(status)
                .withHeader("Content-Type", "application/json")
                .withHeader("CorrelationId", "abc")
                .withBody(error(body))))
          givens
        }

        def receivesObligationsTestHeader(nino: Nino, headerValue: String): Givens = {
          stubFor(
            get(urlEqualTo(s"/enterprise/obligation-data/nino/${nino.nino}/ITSA?from=${ObligationsQueryParams().from}&to=${ObligationsQueryParams().to}"))
              .withHeader("Gov-Test-Scenario", matching(headerValue))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withHeader("CorrelationId", "abc")
                  .withBody(DesJsons.Obligations())))

          givens
        }
      }

      object properties {

        def willBeCreatedFor(nino: Nino): Givens = {
          stubFor(
            post(urlEqualTo(s"/income-tax-self-assessment/nino/${nino.nino}/properties"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Properties.createResponse)))
          givens
        }

        def willConflict(nino: Nino): Givens = {
          stubFor(
            post(urlEqualTo(s"/income-tax-self-assessment/nino/${nino.nino}/properties"))
              .willReturn(
                aResponse()
                  .withStatus(FORBIDDEN)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Errors.propertyConflict)
              ))
          givens
        }

        def notFoundIncomeSourceFor(nino: Nino): Givens = {
          stubFor(
            post(urlEqualTo(s"/income-tax-self-assessment/nino/${nino.nino}/properties"))
              .willReturn(
                aResponse()
                  .withStatus(NOT_FOUND)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Errors.notFoundIncomeSource)
              ))
          givens
        }

        def willBeReturnedFor(nino: Nino): Givens = {
          stubFor(
            get(urlEqualTo(s"/registration/business-details/nino/${nino.nino}"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Properties.retrieveProperty)))
          givens
        }

        def willReturnNone(nino: Nino): Givens = {
          stubFor(
            get(urlEqualTo(s"/registration/business-details/nino/${nino.nino}"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Properties.retrieveNoProperty)))
          givens
        }

        def returnObligationsFor(nino: Nino, id: String = "abc"): Givens = {
          stubFor(
            get(urlEqualTo(s"/ni/${nino.nino}/self-employments/$id/obligations"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Obligations())))

          givens
        }

        def annualSummaryWillBeUpdatedFor(nino: Nino,
                                          propertyType: PropertyType,
                                          taxYear: TaxYear = TaxYear("2017-18")): Givens = {
          stubFor(
            put(urlEqualTo(
              s"/income-store/nino/${nino.nino}/uk-properties/$propertyType/annual-summaries/${taxYear.toDesTaxYear}"))
              .willReturn(aResponse()
                .withStatus(OK)
                .withBody(DesJsons.Properties.AnnualSummary.response)))

          givens
        }

        def annualSummaryWillNotBeReturnedDueToNotFoundProperty(nino: Nino,
                                                                propertyType: PropertyType,
                                                                taxYear: TaxYear = TaxYear("2017-18")): Givens = {
          stubFor(
            any(urlEqualTo(
              s"/income-store/nino/${nino.nino}/uk-properties/$propertyType/annual-summaries/${taxYear.toDesTaxYear}"))
              .willReturn(
                aResponse()
                  .withStatus(NOT_FOUND)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Errors.notFoundProperty)))

          givens
        }

        def annualSummaryWillNotBeReturnedFor(nino: Nino,
                                              propertyType: PropertyType,
                                              taxYear: TaxYear = TaxYear("2017-18")): Givens = {
          stubFor(
            any(urlEqualTo(
              s"/income-store/nino/${nino.nino}/uk-properties/$propertyType/annual-summaries/${taxYear.toDesTaxYear}"))
              .willReturn(
                aResponse()
                  .withStatus(NOT_FOUND)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Errors.ninoNotFound)))

          givens
        }

        def annualSummaryWillNotBeReturnedDueToNotFoundPeriod(nino: Nino,
                                                              propertyType: PropertyType,
                                                              taxYear: TaxYear = TaxYear("2017-18")): Givens = {
          stubFor(
            any(urlEqualTo(
              s"/income-store/nino/${nino.nino}/uk-properties/$propertyType/annual-summaries/${taxYear.toDesTaxYear}"))
              .willReturn(
                aResponse()
                  .withStatus(NOT_FOUND)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Errors.notFoundPeriod)))

          givens
        }

        def annualSummaryWillBeReturnedFor(nino: Nino,
                                           propertyType: PropertyType,
                                           taxYear: TaxYear = TaxYear("2017-18"),
                                           response: String = ""): Givens = {
          stubFor(
            get(urlEqualTo(
              s"/income-store/nino/${nino.nino}/uk-properties/$propertyType/annual-summaries/${taxYear.toDesTaxYear}"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(response)))

          givens
        }

        def noAnnualSummaryFor(nino: Nino, propertyType: PropertyType, taxYear: TaxYear = TaxYear("2017-18")): Givens = {
          stubFor(
            get(urlEqualTo(
              s"/income-store/nino/${nino.nino}/uk-properties/$propertyType/annual-summaries/${taxYear.toDesTaxYear}"))
              .willReturn(
                aResponse()
                  .withStatus(NOT_FOUND)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Errors.notFoundProperty)))

          givens
        }

        def periodWillBeCreatedFor(nino: Nino, propertyType: PropertyType): Givens = {
          stubFor(
            post(urlEqualTo(s"/income-store/nino/${nino.nino}/uk-properties/$propertyType/periodic-summaries"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Properties.Period.createResponse())))

          givens
        }

        def overlappingPeriodFor(nino: Nino, propertyType: PropertyType): Givens = {
          stubFor(
            post(urlEqualTo(s"/income-store/nino/${nino.nino}/uk-properties/$propertyType/periodic-summaries"))
              .willReturn(
                aResponse()
                  .withStatus(409)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Errors.overlappingPeriod)))

          givens
        }

        def misalignedPeriodFor(nino: Nino, propertyType: PropertyType): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/${nino.nino}/uk-properties/$propertyType/periodic-summaries"))
            .willReturn(
              aResponse()
                .withStatus(409)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.misalignedPeriod)))

          givens
        }

        def periodWillBeNotBeCreatedFor(nino: Nino, propertyType: PropertyType): Givens = {
          stubFor(
            post(urlEqualTo(s"/income-store/nino/${nino.nino}/uk-properties/$propertyType/periodic-summaries"))
              .willReturn(
                aResponse()
                  .withStatus(NOT_FOUND)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Errors.notFound)))

          givens
        }

        def propertyPeriodPostError(nino: Nino, propertyType: PropertyType)(status: Int, code: String): Givens = {
          stubFor(
            post(urlEqualTo(s"/income-store/nino/${nino.nino}/uk-properties/$propertyType/periodic-summaries"))
              .willReturn(
                aResponse()
                  .withStatus(status)
                  .withHeader("Content-Type", "application/json")
                  .withBody(code)))
          givens
        }

        def periodsWillBeReturnedFor(nino: Nino, propertyType: PropertyType): Givens = {
          stubFor(
            get(urlEqualTo(s"/income-tax/nino/${nino.nino}/uk-properties/$propertyType/periodic-summaries"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Properties.Period.periodsSummary)))

          givens
        }

        def emptyPeriodsWillBeReturnedFor(nino: Nino, propertyType: PropertyType): Givens = {

          val emptyPeriodJson =
            s"""
               |{
               |  "periods": []
               |}""".stripMargin

          stubFor(
            get(urlEqualTo(s"/income-tax/nino/${nino.nino}/uk-properties/$propertyType/periodic-summaries"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(emptyPeriodJson)))

          givens
        }

        def invalidPeriodsJsonFor(nino: Nino, propertyType: PropertyType): Givens = {
          stubFor(
            get(urlEqualTo(s"/income-tax/nino/${nino.nino}/uk-properties/$propertyType/periodic-summaries"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(Json.obj().toString)))

          givens
        }

        def noPeriodFor(nino: Nino, propertyType: PropertyType, periodId: String = "2017-04-06_2018-04-05"): Givens = {
          periodId match {
            case Period(from, to) =>
              stubFor(
                get(urlEqualTo(s"/income-store/nino/${nino.nino}/uk-properties/$propertyType/periodic-summary-detail?from=$from&to=$to"))
                  .willReturn(
                    aResponse()
                      .withStatus(NOT_FOUND)
                      .withHeader("Content-Type", "application/json")
                      .withBody(DesJsons.Errors.ninoNotFound)))
              givens
            case _ => fail(s"Invalid period ID: $periodId.")
          }
        }

        def noPeriodsFor(nino: Nino, propertyType: PropertyType): Givens = {
          stubFor(
            get(urlEqualTo(s"/income-store/nino/${nino.nino}/uk-properties/$propertyType/periodic-summaries"))
              .willReturn(
                aResponse()
                  .withStatus(NOT_FOUND)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Errors.notFoundProperty)))

          givens
        }

        def invalidDateFrom(nino: Nino, propertyType: PropertyType, periodId: String = "2017-04-06_2018-04-05"): Givens = {
          periodId match {
            case Period(from, to) =>
              stubFor(
                get(urlEqualTo(s"/income-store/nino/${nino.nino}/uk-properties/$propertyType/periodic-summary-detail?from=$from&to=$to"))
                  .willReturn(
                    aResponse()
                      .withStatus(BAD_REQUEST)
                      .withHeader("Content-Type", "application/json")
                      .withBody(DesJsons.Errors.invalidDateFrom)))
              givens
            case _ => fail(s"Invalid period ID: $periodId.")
          }
        }

        def invalidDateTo(nino: Nino, propertyType: PropertyType, periodId: String = "2017-04-06_2018-04-05"): Givens = {
          periodId match {
            case Period(from, to) =>
              stubFor(
                get(urlEqualTo(s"/income-store/nino/${nino.nino}/uk-properties/$propertyType/periodic-summary-detail?from=$from&to=$to"))
                  .willReturn(
                    aResponse()
                      .withStatus(BAD_REQUEST)
                      .withHeader("Content-Type", "application/json")
                      .withBody(DesJsons.Errors.invalidDateTo)))
              givens
            case _ => fail(s"Invalid period ID: $periodId.")
          }
        }

        def doesNotExistPeriodFor(nino: Nino, propertyType: PropertyType): Givens = {
          stubFor(
            get(urlEqualTo(s"/income-store/nino/${nino.nino}/uk-properties/$propertyType/periodic-summaries"))
              .willReturn(
                aResponse()
                  .withStatus(NOT_FOUND)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Errors.ninoNotFound)))

          givens
        }

        def periodWillBeUpdatedFor(nino: Nino, propertyType: PropertyType, from: String = "2017-04-06", to: String = "2018-04-05"): Givens = {
          stubFor(put(urlEqualTo(s"/income-store/nino/${nino.nino}/uk-properties/$propertyType/periodic-summaries?from=$from&to=$to"))
            .willReturn(
              aResponse()
                .withStatus(OK)))

          givens
        }

        def periodWillNotBeUpdatedFor(nino: Nino, propertyType: PropertyType, from: String = "2017-04-06", to: String = "2018-04-05"): Givens = {
          stubFor(put(urlEqualTo(s"/income-store/nino/${nino.nino}/uk-properties/$propertyType/periodic-summaries?from=$from&to=$to"))
            .willReturn(
              aResponse()
                .withStatus(NOT_FOUND)))

          givens
        }

        def invalidPeriodUpdateFor(nino: Nino, propertyType: PropertyType, from: String = "2017-04-06", to: String = "2018-04-05"): Givens = {
          stubFor(put(urlEqualTo(s"/income-store/nino/${nino.nino}/uk-properties/$propertyType/periodic-summaries?from=$from&to=$to"))
            .willReturn(
              aResponse()
                .withStatus(BAD_REQUEST)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.invalidPeriod)))

          givens
        }

        def amendPropertyUpdateError(nino: Nino, propertyType: PropertyType, from: String = "2017-04-06", to: String = "2018-04-05")
                                    (status: Int, code: String): Givens = {
          stubFor(put(urlEqualTo(s"/income-store/nino/${nino.nino}/uk-properties/$propertyType/periodic-summaries?from=$from&to=$to"))
            .willReturn(
              aResponse()
                .withStatus(status)
                .withHeader("Content-Type", "Application/json")
                .withBody(code)
            ))

          givens
        }

        def createWithNotAllowedConsolidatedExpenses(nino: Nino, propertyType: PropertyType): Givens = {
          stubFor(
            post(urlEqualTo(s"/income-store/nino/${nino.nino}/uk-properties/$propertyType/periodic-summaries"))
              .willReturn(
                aResponse()
                  .withStatus(409)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Errors.notAllowedConsolidatedExpenses)))

          givens
        }

        def updateWithNotAllowedConsolidatedExpenses(nino: Nino, propertyType: PropertyType, from: String = "2017-04-06", to: String = "2018-04-05"): Givens = {
          stubFor(
            put(urlEqualTo(s"/income-store/nino/${nino.nino}/uk-properties/$propertyType/periodic-summaries?from=$from&to=$to"))
              .willReturn(
                aResponse()
                  .withStatus(409)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Errors.notAllowedConsolidatedExpenses)))

          givens
        }

        def endOfYearStatementReadyToBeFinalised(nino: Nino, start: LocalDate, end: LocalDate): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/${nino.nino}/uk-properties/accounting-periods/${start}_$end/statement"))
            .willReturn(
              aResponse()
                .withStatus(NO_CONTENT)
                .withBody("")))

          givens
        }

        def endOfYearStatementMissingPeriod(nino: Nino, start: LocalDate, end: LocalDate): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/${nino.nino}/uk-properties/accounting-periods/${start}_$end/statement"))
            .willReturn(
              aResponse()
                .withStatus(FORBIDDEN)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.periodicUpdateMissing)))

          givens
        }

        def endOfYearStatementIsEarly(nino: Nino, start: LocalDate, end: LocalDate): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/${nino.nino}/uk-properties/accounting-periods/${start}_$end/statement"))
            .willReturn(
              aResponse()
                .withStatus(BAD_REQUEST)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.earlySubmission)))

          givens
        }

        def endOfYearStatementIsLate(nino: Nino, start: LocalDate, end: LocalDate): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/${nino.nino}/uk-properties/accounting-periods/${start}_$end/statement"))
            .willReturn(
              aResponse()
                .withStatus(FORBIDDEN)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.lateSubmission)))

          givens
        }

        def endOfYearStatementDoesNotMatchPeriod(nino: Nino, start: LocalDate, end: LocalDate): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/${nino.nino}/uk-properties/accounting-periods/${start}_$end/statement"))
            .willReturn(
              aResponse()
                .withStatus(FORBIDDEN)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.nonMatchingPeriod)))

          givens
        }

        def endOfYearStatementAlreadySubmitted(nino: Nino, start: LocalDate, end: LocalDate): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/${nino.nino}/uk-properties/accounting-periods/${start}_$end/statement"))
            .willReturn(
              aResponse()
                .withStatus(FORBIDDEN)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.alreadySubmitted)))

          givens
        }

      }

      object GiftAid {

        def updatePayments(nino: Nino, taxYear: TaxYear): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/${nino.nino}/charitable-giving/${taxYear.toDesTaxYear}"))
            .willReturn(
              aResponse()
                .withStatus(NO_CONTENT)
                .withBody("")))
          givens
        }

        def updatePaymentsWithNinoNotAvailable(nino: Nino, taxYear: TaxYear): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/${nino.nino}/charitable-giving/${taxYear.toDesTaxYear}"))
            .willReturn(
              aResponse()
                .withStatus(NOT_FOUND)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.ninoNotFound)))

          givens
        }

        def retrievePayments(nino: Nino, taxYear: TaxYear): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/${nino.nino}/charitable-giving/${taxYear.toDesTaxYear}"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.CharitableGivings())))

          givens
        }

        def retrievePaymentsWithInvalidNino(nino: Nino, taxYear: TaxYear): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/${nino.nino}/charitable-giving/${taxYear.toDesTaxYear}"))
            .willReturn(
              aResponse()
                .withStatus(NOT_FOUND)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.ninoNotFound)))

          givens
        }
      }

      object PropertiesBISS {
        def getSummary(nino: Nino, taxYear: TaxYear): Givens = {
          stubFor(get(urlEqualTo(s"/income-tax/income-sources/nino/${nino.nino}/uk-property/${taxYear.toDesTaxYear}/biss"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.PropertiesBISS.summary)))

          givens
        }

        def getSummaryErrorResponse(nino: Nino, taxYear: TaxYear, status: Int, errorCode: String): Givens = {
          stubFor(get(urlEqualTo(s"/income-tax/income-sources/nino/${nino.nino}/uk-property/${taxYear.toDesTaxYear}/biss"))
            .willReturn(
              aResponse()
                .withStatus(status)
                .withHeader("Content-Type", "application/json")
                .withBody(errorCode)))

          givens
        }
      }

      object SelfEmploymentBISS {
        def getSummary(nino: Nino, taxYear: TaxYear, id: String): Givens = {
          stubFor(get(urlEqualTo(s"/income-tax/income-sources/nino/${nino.nino}/self-employment/${taxYear.toDesTaxYear}/biss?incomesourceid=$id"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.SelfEmploymentBISS.summary)))

          givens
        }

        def getSummaryErrorResponse(nino: Nino, taxYear: TaxYear, id: String, status: Int, errorCode: String): Givens = {
          stubFor(get(urlEqualTo(s"/income-tax/income-sources/nino/${nino.nino}/self-employment/${taxYear.toDesTaxYear}/biss?incomesourceid=$id"))
            .willReturn(
              aResponse()
                .withStatus(status)
                .withHeader("Content-Type", "application/json")
                .withBody(errorCode)))

          givens
        }
      }

    }

    def des() = new Des(this)

  }

  def given(): Givens = new Givens()

  def when(): HttpVerbs = new HttpVerbs()

}
