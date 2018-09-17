package uk.gov.hmrc.support

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import org.joda.time.LocalDate
import org.json.{JSONArray, JSONObject}
import org.skyscreamer.jsonassert.JSONAssert.assertEquals
import org.skyscreamer.jsonassert.JSONCompareMode.LENIENT
import play.api.libs.json._
import uk.gov.hmrc.api.controllers.ErrorNotFound
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.selfassessmentapi.models.obligations.ObligationsQueryParams
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.selfassessmentapi.models.{ErrorNotImplemented, Period, TaxYear}
import uk.gov.hmrc.selfassessmentapi.resources.DesJsons
import uk.gov.hmrc.selfassessmentapi.{NinoGenerator, TestApplication}

import scala.collection.mutable
import scala.util.matching.Regex

trait BaseFunctionalSpec extends TestApplication {

  protected val nino = NinoGenerator().nextNino()

  class Assertions(request: String, response: HttpResponse)(implicit urlPathVariables: mutable.Map[String, String])
    extends UrlInterpolation {
    def jsonBodyIsEmptyObject() = response.json shouldBe Json.obj()

    def jsonBodyIsEmptyArray() = response.json shouldBe JsArray()

    def responseContainsHeader(name: String, pattern: Regex): Assertions = {
      response.header(name) match {
        case Some(h) => h should fullyMatch regex pattern
        case _ => fail(s"Header [$name] not found in the response headers")
      }
      this
    }

    if (request.startsWith("POST")) {
      response.header("Location").map { location =>
        location.contains("/periods") match {
          case true => urlPathVariables += ("periodLocation" -> location.replaceFirst("/self-assessment", ""))
          case false => urlPathVariables += ("sourceLocation" -> location.replaceFirst("/self-assessment", ""))
        }
      }
    }

    def when() = new HttpVerbs()

    def butResponseHasNo(sourceName: String, summaryName: String = "") = {
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

    def bodyIsError(code: String) = body(_ \ "code").is(code)

    def isValidationError(error: (String, String)): Assertions = isValidationError(error._1, error._2)

    def isValidationError(path: String, code: String) = {
      statusIs(400).contentTypeIsJson().body(_ \ "code").is("INVALID_REQUEST")

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
      statusIs(400).contentTypeIsJson().body(_ \ "path").is(path).body(_ \ "code").is(code)
      this
    }

    def isBadRequest(code: String): Assertions = {
      statusIs(400).contentTypeIsJson().body(_ \ "code").is(code)
      this
    }

    def isBadRequest: Assertions = {
      isBadRequest("INVALID_REQUEST")
    }

    def isNotFound = {
      statusIs(404).contentTypeIsJson().bodyIsError(ErrorNotFound.errorCode)
      this
    }

    def isNotImplemented = {
      statusIs(501).contentTypeIsJson().bodyIsError(ErrorNotImplemented.errorCode)
      this
    }

    def contentTypeIsXml() = contentTypeIs("application/xml")

    def contentTypeIsJson() = contentTypeIs("application/json")

    def contentTypeIsHalJson() = contentTypeIs("application/hal+json")

    def noInteractionsWithExternalSystems() = {
      verify(0, RequestPatternBuilder.allRequests())
    }

    def bodyIs(expectedBody: String) = {
      response.body shouldBe expectedBody
      this
    }

    def bodyIs(expectedBody: JsValue) = {
      (response.json match {
        case JsObject(fields) => response.json.as[JsObject] - "_links" - "id"
        case json => json
      }) shouldEqual expectedBody
      this
    }

    def bodyIsLike(expectedBody: String) = {
      response.json match {
        case JsArray(_) => assertEquals(expectedBody, new JSONArray(response.body), LENIENT)
        case _ => assertEquals(expectedBody, new JSONObject(response.body), LENIENT)
      }
      this
    }

    def bodyHasLink(rel: String, href: String) = {
      getLinkFromBody(rel) shouldEqual Some(interpolated(href))
      this
    }

    def bodyHasPath[T](path: String, value: T)(implicit reads: Reads[T]): Assertions = {
      extractPathElement(path) shouldEqual Some(value)
      this
    }

    def bodyHasPath(path: String, valuePattern: Regex) = {
      extractPathElement[String](path) match {
        case Some(x) =>
          valuePattern findFirstIn x match {
            case Some(v) =>
            case None => fail(s"$x did not match pattern")
          }
        case None => fail(s"No value found for $path")
      }
      this
    }

    def bodyDoesNotHavePath[T](path: String)(implicit reads: Reads[T]) = {
      extractPathElement[T](path) match {
        case Some(x) => fail(s"$x match found")
        case None =>
      }
      this
    }

    private def extractPathElement[T](path: String)(implicit reads: Reads[T]): Option[T] = {
      val pathSeq = path.filter(!_.isWhitespace).split('\\').toSeq.filter(!_.isEmpty)

      def op(js: Option[JsValue], pathElement: String): Option[JsValue] = {
        val pattern = """(.*)\((\d+)\)""".r
        js match {
          case Some(v) =>
            pathElement match {
              case pattern(arrayName, index) =>
                js match {
                  case Some(v) =>
                    if (arrayName.isEmpty) v(index.toInt).toOption else (v \ arrayName) (index.toInt).toOption
                  case None => None
                }
              case _ => (v \ pathElement).toOption
            }
          case None => None
        }
      }

      pathSeq.foldLeft(Some(response.json): Option[JsValue])(op).map(jsValue => jsValue.asOpt[T]).getOrElse(None)
    }

    private def getLinkFromBody(rel: String): Option[String] =
      if (response.body.isEmpty) None
      else
        (for {
          links <- (response.json \ "_links").toOption
          link <- (links \ rel).toOption
          href <- (link \ "href").toOption

        } yield href.asOpt[String]).getOrElse(None)

    def bodyHasLink(rel: String, hrefPattern: Regex) = {
      getLinkFromBody(rel) match {
        case Some(href) =>
          interpolated(hrefPattern).r findFirstIn href match {
            case Some(v) =>
            case None => fail(s"$href did not match pattern")
          }
        case None => fail(s"No href found for $rel")
      }
      this
    }

    def bodyHasString(content: String) = {
      response.body.contains(content) shouldBe true
      this
    }

    def bodyDoesNotHaveString(content: String) = {
      response.body.contains(content) shouldBe false
      this
    }

    def statusIs(statusCode: Regex) = {
      withClue(s"expected $request to return $statusCode; but got ${response.body}\n") {
        response.status.toString should fullyMatch regex statusCode
      }
      this
    }

    def statusIs(statusCode: Int) = {
      withClue(s"expected $request to return $statusCode; but got ${response.body}\n") {
        response.status shouldBe statusCode
      }
      this
    }

    private def contentTypeIs(contentType: String) = {
      response.header("Content-Type") shouldEqual Some(contentType)
      this
    }

    def body(myQuery: JsValue => JsLookupResult) = {
      new BodyAssertions(myQuery(response.json).toOption, this)
    }

    def selectFields(myQuery: JsValue => Seq[JsValue]) = {
      new BodyListAssertions(myQuery(response.json), this)
    }

    class BodyAssertions(content: Option[JsValue], assertions: Assertions) {
      def is(value: String) = {
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

      def isAbsent() = {
        content shouldBe None
        assertions
      }

      def is(value: BigDecimal) = {
        content match {
          case Some(v) => v.as[BigDecimal] shouldBe value
          case None => fail()
        }
        assertions
      }
    }

    class BodyListAssertions(content: Seq[JsValue], assertions: Assertions) {
      def isLength(n: Int) = {
        content.size shouldBe n
        this
      }

      def matches(matcher: Regex) = {
        content.map(_.as[String]).forall {
          case matcher(_*) => true
          case _ => false
        } shouldBe true

        assertions
      }

      def is(value: String*) = {
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

    def thenAssertThat(): Assertions = {
      implicit val carrier =
        if (addAcceptHeader) hc.withExtraHeaders("Accept" -> "application/vnd.hmrc.1.0+json") else hc

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
    def to(url: String) = new HttpRequest(method, url, body)
  }

  class HttpPutBodyWrapper(method: String, body: Option[JsValue])(
    implicit urlPathVariables: mutable.Map[String, String]) {
    def at(url: String) = new HttpRequest(method, url, body)
  }

  class HttpVerbs()(implicit urlPathVariables: mutable.Map[String, String] = mutable.Map()) {

    def post(body: JsValue) = {
      new HttpPostBodyWrapper("POST", Some(body))
    }

    def put(body: JsValue) = {
      new HttpPutBodyWrapper("PUT", Some(body))
    }

    def get(path: String) = {
      new HttpRequest("GET", path, None)
    }

    def delete(path: String) = {
      new HttpRequest("DELETE", path, None)
    }

    def post(path: String, body: Option[JsValue] = None) = {
      new HttpRequest("POST", path, body)
    }

    def put(path: String, body: Option[JsValue]) = {
      new HttpRequest("PUT", path, body)
    }

  }

  class Givens {

    implicit val urlPathVariables: mutable.Map[String, String] = mutable.Map()

    def when() = new HttpVerbs()

    def userIsSubscribedToMtdFor(nino: Nino, mtdId: String = "abc"): Givens = {
      stubFor(any(urlMatching(s".*/registration/business-details/nino/$nino"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(DesJsons.SelfEmployment(nino, mtdId))))

      this
    }

    def userIsNotSubscribedToMtdFor(nino: Nino): Givens = {
      stubFor(any(urlMatching(s".*/registration/business-details/nino/$nino"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json")
            .withBody(DesJsons.Errors.ninoNotFound)))

      this
    }

    def businessDetailsLookupReturns500Error(nino: Nino): Givens = {
      stubFor(any(urlMatching(s".*/registration/business-details/nino/$nino"))
        .willReturn(
          aResponse()
            .withStatus(500)
            .withHeader("Content-Type", "application/json")
            .withBody(DesJsons.Errors.serverError)))

      this
    }

    def businessDetailsLookupReturns503Error(nino: Nino): Givens = {
      stubFor(any(urlMatching(s".*/registration/business-details/nino/$nino"))
        .willReturn(
          aResponse()
            .withStatus(503)
            .withHeader("Content-Type", "application/json")
            .withBody(DesJsons.Errors.serviceUnavailable)))

      this
    }

    def missingBearerToken: Givens = {
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .willReturn(aResponse()
          .withStatus(401)
          .withHeader("Content-Length", "0")
          .withHeader("WWW-Authenticate", "MDTP detail=\"MissingBearerToken\"")))

      this
    }

    def upstream502BearerTokenDecryptionError: Givens = {
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .willReturn(aResponse()
          .withStatus(502)
          .withHeader("Content-Type", "application/json")
          .withBody("""{"statusCode":500,"message":"Unable to decrypt value"}""")))

      this
    }

    def upstream5xxError: Givens = {
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .willReturn(aResponse()
          .withStatus(500)))

      this
    }

    def upstream4xxError: Givens = {
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .willReturn(aResponse()
          .withStatus(403)))

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
          .withStatus(401)
          .withHeader("Content-Length", "0")
          .withHeader("WWW-Authenticate", "MDTP detail=\"InsufficientEnrolments\"")))

      // The user is an 'Individual/Group', so the affinity check for 'Agent' should fail.
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .withRequestBody(containing("Agent"))
        .willReturn(aResponse()
          .withStatus(401)
          .withHeader("Content-Length", "0")
          .withHeader("WWW-Authenticate", "MDTP detail=\"UnsupportedAffinityGroup\"")))

      this
    }

    def userIsPartiallyAuthorisedForTheResource: Givens = {

      // First call to auth to check if fully authorised should fail.
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .withRequestBody(containing("HMRC-MTD-IT"))
        .willReturn(aResponse()
          .withStatus(401)
          .withHeader("Content-Length", "0")
          .withHeader("WWW-Authenticate", "MDTP detail=\"InsufficientEnrolments\"")))

      // Second call to auth to check affinity is equal to 'Agent' should succeed.
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .withRequestBody(containing("Agent"))
        .willReturn(aResponse().withStatus(200).withBody(
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
        .willReturn(aResponse().withStatus(200).withBody(
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
          .withStatus(401)
          .withHeader("Content-Length", "0")
          .withHeader("WWW-Authenticate", "MDTP detail=\"InsufficientEnrolments\"")))

      // Second call to auth to check affinity is equal to 'Agent' should succeed.
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .withRequestBody(containing("Agent"))
        .willReturn(aResponse().withStatus(200).withBody(
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
          .withStatus(401)
          .withHeader("Content-Length", "0")
          .withHeader("WWW-Authenticate", "MDTP detail=\"InsufficientEnrolments\"")))

      this
    }

    def userIsPartiallyAuthorisedForTheResourceNoAgentCode: Givens = {

      // First call to auth to check if fully authorised should fail.
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .withRequestBody(containing("HMRC-MTD-IT"))
        .willReturn(aResponse()
          .withStatus(401)
          .withHeader("Content-Length", "0")
          .withHeader("WWW-Authenticate", "MDTP detail=\"InsufficientEnrolments\"")))

      // Second call to auth to check affinity is equal to 'Agent' should succeed.
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .withRequestBody(containing("Agent"))
        .willReturn(aResponse().withStatus(200).withBody(
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
        .willReturn(aResponse().withStatus(200).withBody(
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

    def clientIsFullyAuthorisedForTheResource: Givens = {
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .willReturn(aResponse().withStatus(200).withBody(
          """
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

    def agentIsFullyAuthorisedForTheResource: Givens = {
      stubFor(post(urlPathEqualTo(s"/auth/authorise"))
        .willReturn(aResponse().withStatus(200).withBody(
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
        .willReturn(aResponse().withStatus(200).withBody(
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
        .willReturn(aResponse().withStatus(200).withBody(
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
        stubFor(any(urlMatching(s".*/(calculation-data|nino)/$nino.*"))
          .willReturn(
            aResponse()
              .withStatus(418)))

        givens
      }

      def invalidBusinessIdFor(nino: Nino): Givens = {
        stubFor(
          any(urlMatching(s".*/nino/$nino.*"))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.invalidBusinessId)
            ))

        givens
      }

      def invalidOriginatorIdFor(nino: Nino): Givens = {
        stubFor(
          any(urlMatching(s".*/nino/$nino.*"))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.invalidOriginatorId)
            ))

        givens
      }

      def serviceUnavailableFor(nino: Nino): Givens = {
        stubFor(any(urlMatching(s".*/nino/$nino.*"))
          .willReturn(
            aResponse()
              .withStatus(503)
              .withHeader("Content-Type", "application/json")
              .withBody(DesJsons.Errors.serviceUnavailable)
          ))

        givens
      }

      def serverErrorFor(nino: Nino): Givens = {
        stubFor(any(urlMatching(s".*/nino/$nino.*"))
          .willReturn(
            aResponse()
              .withStatus(500)
              .withHeader("Content-Type", "application/json")
              .withBody(DesJsons.Errors.serverError)
          ))

        givens
      }

      def ninoNotFoundFor(nino: Nino): Givens = {
        stubFor(any(urlMatching(s".*/nino/$nino.*"))
          .willReturn(
            aResponse()
              .withStatus(404)
              .withHeader("Content-Type", "application/json")
              .withBody(DesJsons.Errors.ninoNotFound)))

        givens
      }

      def invalidNinoFor(nino: Nino): Givens = {
        stubFor(any(urlMatching(s".*/nino/$nino.*"))
          .willReturn(
            aResponse()
              .withStatus(400)
              .withHeader("Content-Type", "application/json")
              .withBody(DesJsons.Errors.invalidNino)))

        givens
      }

      def payloadFailsValidationFor(nino: Nino): Givens = {
        stubFor(any(urlMatching(s".*/nino/$nino/.*"))
          .willReturn(
            aResponse()
              .withStatus(400)
              .withHeader("Content-Type", "application/json")
              .withBody(DesJsons.Errors.invalidPayload)))

        givens
      }

      object selfEmployment {
        def annualSummaryNotFoundFor(nino: Nino): Givens = {
          stubFor(any(urlMatching(s".*/income-store/nino/$nino/self-employments/.*"))
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.ninoNotFound)))

          givens
        }

        def invalidOriginatorIdFor(nino: Nino): Givens = {
          stubFor(any(urlMatching(s"/income-store/nino/$nino/self-employments/.*"))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.invalidOriginatorId)
            ))

          givens
        }

        def tooManySourcesFor(nino: Nino): Givens = {
          stubFor(post(urlEqualTo(s"/income-tax-self-assessment/nino/$nino/business"))
            .willReturn(
              aResponse()
                .withStatus(403)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.tooManySources)
            ))

          givens
        }

        def failsTradingName(nino: Nino): Givens = {
          stubFor(any(urlEqualTo(s"/income-tax-self-assessment/nino/$nino/business"))
            .willReturn(
              aResponse()
                .withStatus(409)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.tradingNameConflict)
            ))

          givens
        }

        def willBeCreatedFor(nino: Nino, id: String = "abc", mtdId: String = "123"): Givens = {
          stubFor(post(urlEqualTo(s"/income-tax-self-assessment/nino/$nino/business"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.SelfEmployment.createResponse(id, mtdId))))
          givens
        }

        def willBeReturnedFor(nino: Nino, mtdId: String = "123", id: String = "abc", accPeriodStart: String = "2017-04-06", accPeriodEnd: String = "2018-04-05"): Givens = {
          stubFor(get(urlEqualTo(s"/registration/business-details/nino/$nino"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.SelfEmployment(nino, mtdId, id, accPeriodStart = accPeriodStart, accPeriodEnd = accPeriodEnd))))

          givens
        }

        def periodWillBeCreatedFor(nino: Nino, id: String = "abc"): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/periodic-summaries"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.SelfEmployment.Period.createResponse())))

          givens
        }

        def periodWillBeNotBeCreatedFor(nino: Nino, id: String = "abc"): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/periodic-summaries"))
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.notFound)))

          givens
        }

        def periodsWillBeReturnedFor(nino: Nino, id: String = "abc"): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/periodic-summaries"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.SelfEmployment.Period.periods)))

          givens
        }

        def invalidPeriodsJsonFor(nino: Nino, id: String = "abc"): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/periodic-summaries"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(Json.obj().toString())))

          givens
        }

        def periodWillBeReturnedFor(nino: Nino, id: String = "abc", from: String, to: String): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/periodic-summary-detail?from=$from&to=$to"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.SelfEmployment.Period())))

          givens
        }

        def periodWillBeUpdatedFor(nino: Nino, id: String = "abc", from: String, to: String): Givens = {
          stubFor(put(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/periodic-summaries?from=$from&to=$to"))
            .willReturn(
              aResponse()
                .withStatus(200)))

          givens
        }

        def periodWillNotBeUpdatedFor(nino: Nino, id: String = "abc", from: String, to: String): Givens = {
          stubFor(put(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/periodic-summaries?from=$from&to=$to"))
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.ninoNotFound)))

          givens
        }

        def noPeriodFor(nino: Nino, id: String = "abc", from: String, to: String): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/periodic-summary-detail?from=$from&to=$to"))
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.ninoNotFound)))

          givens
        }

        def invalidDateFrom(nino: Nino, id: String = "abc", from: String, to: String): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/periodic-summary-detail?from=$from&to=$to"))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.invalidDateFrom)))

          givens
        }

        def invalidDateTo(nino: Nino, id: String = "abc", from: String, to: String): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/periodic-summary-detail?from=$from&to=$to"))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.invalidDateTo)))

          givens
        }

        def noPeriodsFor(nino: Nino, id: String = "abc"): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/periodic-summaries"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.SelfEmployment.Period.emptyPeriods)))

          givens
        }

        def overlappingPeriodFor(nino: Nino, id: String = "abc"): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/periodic-summaries"))
            .willReturn(
              aResponse()
                .withStatus(409)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.overlappingPeriod)))

          givens
        }

        def nonContiguousPeriodFor(nino: Nino, id: String = "abc"): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/periodic-summaries"))
            .willReturn(
              aResponse()
                .withStatus(409)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.nonContiguousPeriod)))

          givens
        }

        def misalignedPeriodFor(nino: Nino, id: String = "abc"): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/periodic-summaries"))
            .willReturn(
              aResponse()
                .withStatus(409)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.misalignedPeriod)))

          givens
        }

        def annualSummaryWillBeUpdatedFor(nino: Nino, id: String = "abc", taxYear: TaxYear = TaxYear("2017-18")): Givens = {
          stubFor(put(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/annual-summaries/${taxYear.toDesTaxYear}"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(DesJsons.SelfEmployment.AnnualSummary.response)))

          givens
        }

        def annualSummaryWillNotBeUpdatedFor(nino: Nino, id: String = "abc", taxYear: TaxYear = TaxYear("2017-18")): Givens = {
          stubFor(put(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/annual-summaries/${taxYear.toDesTaxYear}"))
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.ninoNotFound)))

          givens
        }

        def annualSummaryWillNotBeReturnedFor(nino: Nino, id: String = "abc", taxYear: TaxYear = TaxYear("2017-18")): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/annual-summaries/${taxYear.toDesTaxYear}"))
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.ninoNotFound)))

          givens
        }

        def annualSummaryWillBeReturnedFor(nino: Nino, id: String = "abc", taxYear: TaxYear = TaxYear("2017-18")): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/annual-summaries/${taxYear.toDesTaxYear}"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.SelfEmployment.AnnualSummary())))

          givens
        }

        def noAnnualSummaryFor(nino: Nino, id: String = "abc", taxYear: TaxYear = TaxYear("2017-18")): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/annual-summaries/${taxYear.toDesTaxYear}"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(Json.obj().toString)))

          givens
        }

        def willBeUpdatedFor(nino: Nino, id: String = "abc"): Givens = {
          stubFor(put(urlEqualTo(s"/income-tax-self-assessment/nino/$nino/incomeSourceId/$id/regime/ITSA"))
            .willReturn(
              aResponse()
                .withStatus(200)))

          givens
        }

        def willNotBeUpdatedFor(nino: Nino): Givens = {
          stubFor(put(urlMatching(s"/income-tax-self-assessment/nino/$nino/business/.*"))
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.ninoNotFound)))

          givens
        }

        def doesNotExistPeriodFor(nino: Nino, id: String = "abc"): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/periodic-summaries"))
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.ninoNotFound)))

          givens
        }

        def noneFor(nino: Nino, mtdId: String = "123"): Givens = {
          stubFor(get(urlEqualTo(s"/registration/business-details/nino/$nino"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.SelfEmployment.emptySelfEmployment(nino, mtdId))))

          givens
        }

        def noContentTypeFor(nino: Nino, mtdId: String = "123"): Givens = {
          stubFor(get(urlEqualTo(s"/registration/business-details/nino/$nino"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(DesJsons.SelfEmployment.emptySelfEmployment(nino, mtdId))))

          givens
        }

        def incomeIdNotFoundFor(nino: Nino, mtdId: String = "123"): Givens = {
          stubFor(get(urlEqualTo(s"/registration/business-details/nino/$nino"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.SelfEmployment(nino, mtdId, id = "inexistent"))))

          givens
        }

        def invalidJson(nino: Nino, mtdId: String = "123"): Givens = {
          stubFor(get(urlEqualTo(s"/registration/business-details/nino/$nino"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""{ "businessData": 1 }""")))

          givens
        }

        def endOfYearStatementReadyToBeFinalised(nino: Nino, start: LocalDate, end: LocalDate, id: String = "abc"): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/accounting-periods/${start}_${end}/statement"))
            .willReturn(
              aResponse()
                .withStatus(204)
                .withBody("")))

          givens
        }

        def endOfYearStatementMissingPeriod(nino: Nino, start: LocalDate, end: LocalDate, id: String = "abc"): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/accounting-periods/${start}_${end}/statement"))
            .willReturn(
              aResponse()
                .withStatus(403)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.periodicUpdateMissing)))

          givens
        }

        def endOfYearStatementIsEarly(nino: Nino, start: LocalDate, end: LocalDate, id: String = "abc"): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/accounting-periods/${start}_${end}/statement"))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.earlySubmission)))

          givens
        }

        def endOfYearStatementIsLate(nino: Nino, start: LocalDate, end: LocalDate, id: String = "abc"): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/accounting-periods/${start}_${end}/statement"))
            .willReturn(
              aResponse()
                .withStatus(403)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.lateSubmission)))

          givens
        }

        def endOfYearStatementDoesNotMatchPeriod(nino: Nino, start: LocalDate, end: LocalDate, id: String = "abc"): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/accounting-periods/${start}_${end}/statement"))
            .willReturn(
              aResponse()
                .withStatus(403)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.nonMatchingPeriod)))

          givens
        }

        def endOfYearStatementAlreadySubmitted(nino: Nino, start: LocalDate, end: LocalDate, id: String = "abc"): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/accounting-periods/${start}_${end}/statement"))
            .willReturn(
              aResponse()
                .withStatus(403)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.alreadySubmitted)))

          givens
        }

      }

      object crystallisation {
        def intentToCrystallise(nino: Nino, taxYear: TaxYear = TaxYear("2017-18")): Givens = {
          stubFor(post(urlEqualTo(s"/income-tax/nino/$nino/taxYear/${taxYear.toDesTaxYear}/tax-calculation?crystallise=true"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Crystallisation.intentToCrystallise())))

          givens
        }

        def intentToCrystalliseRequiredEndOfPeriodStatement(nino: Nino, taxYear: TaxYear) = {
          stubFor(post(urlEqualTo(s"/income-tax/nino/$nino/taxYear/${taxYear.toDesTaxYear}/tax-calculation?crystallise=true"))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.requiredEndOfPeriodStatement)))
          givens
        }

        def crystallise(nino: Nino, taxYear: TaxYear = TaxYear("2017-18"), calcId: String): Givens = {
          stubFor(post(urlMatching(s"/income-tax/calculation/nino/$nino/taxYear/${taxYear.toDesTaxYear}/$calcId/crystallise"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(s"""
                             |{
                             |}
                          """.stripMargin
                )))

          givens
        }

        def crystalliseError(nino: Nino, taxYear: TaxYear = TaxYear("2017-18"), calcId: String)(responseStatus: Int, responseBody: String): Givens = {
          stubFor(post(urlMatching(s"/income-tax/calculation/nino/$nino/taxYear/${taxYear.toDesTaxYear}/$calcId/crystallise"))
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
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Obligations.crystallisationObligations(nino.nino, taxYear))))
          givens
        }
      }

      object taxCalculation {
        def isReadyFor(nino: Nino, calcId: String = "abc"): Givens = {
          stubFor(get(urlMatching(s"/calculation-store/02.00.00/calculation-data/$nino/calcId/$calcId"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.TaxCalculation())))

          givens
        }

        def isAcceptedFor(nino: Nino, taxYear: TaxYear = TaxYear("2017-18")): Givens = {
          stubFor(post(urlMatching(s"/income-tax-self-assessment/nino/$nino/taxYear/${taxYear.toDesTaxYear}/tax-calculation"))
            .willReturn(
              aResponse()
                .withStatus(202)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.TaxCalculation.createResponse())))

          givens
        }

        def isNotReadyFor(nino: Nino, calcId: String = "abc"): Givens = {
          stubFor(get(urlMatching(s"/calculation-store/02.00.00/calculation-data/$nino/calcId/$calcId"))
            .willReturn(
              aResponse()
                .withStatus(204)))

          givens
        }

        def doesNotExistFor(nino: Nino, calcId: String = "abc"): Givens = {
          stubFor(get(urlMatching(s"/calculation-store/02.00.00/calculation-data/$nino/calcId/$calcId"))
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.notFound)))

          givens
        }

        def invalidCalculationIdFor(nino: Nino, calcId: String = "abc"): Givens = {
          stubFor(get(urlMatching(s"/calculation-store/02.00.00/calculation-data/$nino/calcId/$calcId"))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.invalidCalcId)))

          givens
        }
      }

      object obligations {
        def obligationNotFoundFor(nino: Nino): Givens = {
          stubFor(get(urlEqualTo(s"/enterprise/obligation-data/nino/$nino/ITSA?${ObligationsQueryParams().from}&to=${ObligationsQueryParams().to}"))
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.notFound)))

          givens
        }

        def returnObligationsFor(nino: Nino): Givens = {
          stubFor(get(urlEqualTo(s"/enterprise/obligation-data/nino/$nino/ITSA?from=${ObligationsQueryParams().from}&to=${ObligationsQueryParams().to}"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withHeader("CorrelationId", "abc")
                .withBody(DesJsons.Obligations())))

          givens
        }

        def returnObligationsWithNoIdentificationFor(nino: Nino):Givens = {
          stubFor(get(urlEqualTo(s"/enterprise/obligation-data/nino/$nino/ITSA?from=${ObligationsQueryParams().from}&to=${ObligationsQueryParams().to}"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withHeader("CorrelationId", "abc")
                .withBody(DesJsons.Obligations.obligationsNoIdentification)))
          givens
        }

        def returnEopsObligationsWithNoIdentificationFor(nino: Nino):Givens = {
          stubFor(get(urlMatching(s"/enterprise/obligation-data/nino/$nino/ITSA/.*"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withHeader("CorrelationId", "abc")
                .withBody(DesJsons.Obligations.obligationsNoIdentification)))
          givens
        }


        def returnEndOfPeriodObligationsFor(nino: Nino, refNo: String): Givens = {
          stubFor(get(urlMatching(s"/enterprise/obligation-data/nino/$nino/ITSA/.*"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withHeader("CorrelationId", "abc")
                .withBody(DesJsons.Obligations.eopsObligations(refNo))))
          givens
        }

        def returnEopsObligationsErrorFor(nino: Nino, refNo: String)(status: Int, body: String): Givens = {

          def error(code: String): String = {
            s"""
               |{
               |  "code": "$code",
               |  "reason": ""
               |}
            """.stripMargin
          }

          stubFor(get(urlMatching(s"/enterprise/obligation-data/nino/$nino/ITSA/.*"))
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
            get(urlEqualTo(s"/enterprise/obligation-data/nino/$nino/ITSA?from=${ObligationsQueryParams().from}&to=${ObligationsQueryParams().to}"))
              .withHeader("Gov-Test-Scenario", matching(headerValue))
              .willReturn(
                aResponse()
                  .withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withHeader("CorrelationId", "abc")
                  .withBody(DesJsons.Obligations())))

          givens
        }
      }

      object properties {

        def willBeCreatedFor(nino: Nino): Givens = {
          stubFor(
            post(urlEqualTo(s"/income-tax-self-assessment/nino/$nino/properties"))
              .willReturn(
                aResponse()
                  .withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Properties.createResponse)))
          givens
        }

        def willConflict(nino: Nino): Givens = {
          stubFor(
            post(urlEqualTo(s"/income-tax-self-assessment/nino/$nino/properties"))
              .willReturn(
                aResponse()
                  .withStatus(403)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Errors.propertyConflict)
              ))
          givens
        }

        def notFoundIncomeSourceFor(nino: Nino): Givens = {
          stubFor(
            post(urlEqualTo(s"/income-tax-self-assessment/nino/$nino/properties"))
              .willReturn(
                aResponse()
                  .withStatus(404)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Errors.notFoundIncomeSource)
              ))
          givens
        }

        def willBeReturnedFor(nino: Nino): Givens = {
          stubFor(
            get(urlEqualTo(s"/registration/business-details/nino/$nino"))
              .willReturn(
                aResponse()
                  .withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Properties.retrieveProperty)))
          givens
        }

        def willReturnNone(nino: Nino): Givens = {
          stubFor(
            get(urlEqualTo(s"/registration/business-details/nino/$nino"))
              .willReturn(
                aResponse()
                  .withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Properties.retrieveNoProperty)))
          givens
        }

        def returnObligationsFor(nino: Nino, id: String = "abc"): Givens = {
          stubFor(
            get(urlEqualTo(s"/ni/$nino/self-employments/$id/obligations"))
              .willReturn(
                aResponse()
                  .withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Obligations())))

          givens
        }

        def annualSummaryWillBeUpdatedFor(nino: Nino,
                                          propertyType: PropertyType,
                                          taxYear: TaxYear = TaxYear("2017-18")): Givens = {
          stubFor(
            put(urlEqualTo(
              s"/income-store/nino/$nino/uk-properties/$propertyType/annual-summaries/${taxYear.toDesTaxYear}"))
              .willReturn(aResponse()
                .withStatus(200)
                .withBody(DesJsons.Properties.AnnualSummary.response)))

          givens
        }

        def annualSummaryWillNotBeReturnedFor(nino: Nino,
                                              propertyType: PropertyType,
                                              taxYear: TaxYear = TaxYear("2017-18")): Givens = {
          stubFor(
            any(urlEqualTo(
              s"/income-store/nino/$nino/uk-properties/$propertyType/annual-summaries/${taxYear.toDesTaxYear}"))
              .willReturn(
                aResponse()
                  .withStatus(404)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Errors.ninoNotFound)))

          givens
        }

        def annualSummaryWillBeReturnedFor(nino: Nino,
                                           propertyType: PropertyType,
                                           taxYear: TaxYear = TaxYear("2017-18"),
                                           response: String = ""): Givens = {
          stubFor(
            get(urlEqualTo(
              s"/income-store/nino/$nino/uk-properties/$propertyType/annual-summaries/${taxYear.toDesTaxYear}"))
              .willReturn(
                aResponse()
                  .withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(response)))

          givens
        }

        def noAnnualSummaryFor(nino: Nino, propertyType: PropertyType, taxYear: TaxYear = TaxYear("2017-18")): Givens = {
          stubFor(
            get(urlEqualTo(
              s"/income-store/nino/$nino/uk-properties/$propertyType/annual-summaries/${taxYear.toDesTaxYear}"))
              .willReturn(
                aResponse()
                  .withStatus(404)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Errors.notFoundProperty)))

          givens
        }

        def periodWillBeCreatedFor(nino: Nino, propertyType: PropertyType): Givens = {
          stubFor(
            post(urlEqualTo(s"/income-store/nino/$nino/uk-properties/$propertyType/periodic-summaries"))
              .willReturn(
                aResponse()
                  .withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Properties.Period.createResponse())))

          givens
        }

        def overlappingPeriodFor(nino: Nino, propertyType: PropertyType): Givens = {
          stubFor(
            post(urlEqualTo(s"/income-store/nino/$nino/uk-properties/$propertyType/periodic-summaries"))
              .willReturn(
                aResponse()
                  .withStatus(409)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Errors.overlappingPeriod)))

          givens
        }

        def misalignedPeriodFor(nino: Nino, propertyType: PropertyType): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/$nino/uk-properties/$propertyType/periodic-summaries"))
            .willReturn(
              aResponse()
                .withStatus(409)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.misalignedPeriod)))

          givens
        }

        def periodWillBeNotBeCreatedFor(nino: Nino, propertyType: PropertyType): Givens = {
          stubFor(
            post(urlEqualTo(s"/income-store/nino/$nino/uk-properties/$propertyType/periodic-summaries"))
              .willReturn(
                aResponse()
                  .withStatus(404)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Errors.notFound)))

          givens
        }

        def periodWillBeReturnedFor(nino: Nino, propertyType: PropertyType, periodId: String = "2017-04-06_2018-04-05"): Givens = {
          val periodAsJsonString = propertyType match {
            case PropertyType.FHL =>
              DesJsons.Properties.Period.fhl(
                transactionReference = periodId,
                from = "2017-04-05",
                to = "2018-04-04",
                rentIncome = 200.00,
                premisesRunningCosts = 200.00,
                repairsAndMaintenance = 200.00,
                financialCosts = 200.00,
                professionalFees = 200.00,
                costOfServices = 200.00,
                other = 200.00)
                .toString()
            case PropertyType.OTHER =>
              DesJsons.Properties.Period.other(
                transactionReference = periodId,
                from = "2017-04-05",
                to = "2018-04-04",
                rentIncome = 200.00,
                premiumsOfLeaseGrant = Some(200.00),
                reversePremiums = Some(200.00),
                otherPropertyIncome = Some(200.00),
                premisesRunningCosts = Some(200.00),
                repairsAndMaintenance = Some(200.00),
                financialCosts = Some(200.00),
                professionalFees = Some(200.00),
                costOfServices = Some(200.00),
                residentialFinancialCost = Some(200.00),
                other = Some(200.00))
                .toString()
          }

          periodId match {
            case Period(from, to) =>
              stubFor(
                get(urlEqualTo(s"/income-store/nino/$nino/uk-properties/$propertyType/periodic-summary-detail?from=$from&to=$to"))
                  .willReturn(
                    aResponse()
                      .withStatus(200)
                      .withHeader("Content-Type", "application/json")
                      .withBody(periodAsJsonString)))
              givens
            case _ => fail(s"Invalid period ID: $periodId.")
          }
        }

        def periodWillBeNotBeCreatedForInexistentIncomeSource(nino: Nino, propertyType: PropertyType): Givens = {
          stubFor(
            post(urlEqualTo(s"/income-store/nino/$nino/uk-properties/$propertyType/periodic-summaries"))
              .willReturn(
                aResponse()
                  .withStatus(403)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Errors.invalidIncomeSource)))

          givens
        }

        def periodsWillBeReturnedFor(nino: Nino, propertyType: PropertyType): Givens = {
          stubFor(
            get(urlEqualTo(s"/income-store/nino/$nino/uk-properties/$propertyType/periodic-summaries"))
              .willReturn(
                aResponse()
                  .withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Properties.Period.periodsSummary)))

          givens
        }

        def invalidPeriodsJsonFor(nino: Nino, propertyType: PropertyType): Givens = {
          stubFor(
            get(urlEqualTo(s"/income-store/nino/$nino/uk-properties/$propertyType/periodic-summaries"))
              .willReturn(
                aResponse()
                  .withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(Json.obj().toString)))

          givens
        }

        def noPeriodFor(nino: Nino, propertyType: PropertyType, periodId: String = "2017-04-06_2018-04-05"): Givens = {
          periodId match {
            case Period(from, to) =>
              stubFor(
                get(urlEqualTo(s"/income-store/nino/$nino/uk-properties/$propertyType/periodic-summary-detail?from=$from&to=$to"))
                  .willReturn(
                    aResponse()
                      .withStatus(404)
                      .withHeader("Content-Type", "application/json")
                      .withBody(DesJsons.Errors.ninoNotFound)))
              givens
            case _ => fail(s"Invalid period ID: $periodId.")
          }
        }

        def noPeriodsFor(nino: Nino, propertyType: PropertyType): Givens = {
          stubFor(
            get(urlEqualTo(s"/income-store/nino/$nino/uk-properties/$propertyType/periodic-summaries"))
              .willReturn(
                aResponse()
                  .withStatus(404)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Errors.notFoundProperty)))

          givens
        }

        def invalidDateFrom(nino: Nino, propertyType: PropertyType, periodId: String = "2017-04-06_2018-04-05"): Givens = {
          periodId match {
            case Period(from, to) =>
              stubFor(
                get(urlEqualTo(s"/income-store/nino/$nino/uk-properties/$propertyType/periodic-summary-detail?from=$from&to=$to"))
                  .willReturn(
                    aResponse()
                      .withStatus(400)
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
                get(urlEqualTo(s"/income-store/nino/$nino/uk-properties/$propertyType/periodic-summary-detail?from=$from&to=$to"))
                  .willReturn(
                    aResponse()
                      .withStatus(400)
                      .withHeader("Content-Type", "application/json")
                      .withBody(DesJsons.Errors.invalidDateTo)))
              givens
            case _ => fail(s"Invalid period ID: $periodId.")
          }
        }

        def doesNotExistPeriodFor(nino: Nino, propertyType: PropertyType): Givens = {
          stubFor(
            get(urlEqualTo(s"/income-store/nino/$nino/uk-properties/$propertyType/periodic-summaries"))
              .willReturn(
                aResponse()
                  .withStatus(404)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Errors.ninoNotFound)))

          givens
        }

        def periodWillBeUpdatedFor(nino: Nino, propertyType: PropertyType, from: String = "2017-04-06", to: String = "2018-04-05"): Givens = {
          stubFor(put(urlEqualTo(s"/income-store/nino/$nino/uk-properties/$propertyType/periodic-summaries?from=$from&to=$to"))
            .willReturn(
              aResponse()
                .withStatus(204)))

          givens
        }

        def periodWillNotBeUpdatedFor(nino: Nino, propertyType: PropertyType, from: String = "2017-04-06", to: String = "2018-04-05"): Givens = {
          stubFor(put(urlEqualTo(s"/income-store/nino/$nino/uk-properties/$propertyType/periodic-summaries?from=$from&to=$to"))
            .willReturn(
              aResponse()
                .withStatus(404)))

          givens
        }

        def invalidPeriodUpdateFor(nino: Nino, propertyType: PropertyType, from: String = "2017-04-06", to: String = "2018-04-05"): Givens = {
          stubFor(put(urlEqualTo(s"/income-store/nino/$nino/uk-properties/$propertyType/periodic-summaries?from=$from&to=$to"))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.invalidPeriod)))

          givens
        }

        def createWithNotAllowedConsolidatedExpenses(nino: Nino, propertyType: PropertyType): Givens = {
          stubFor(
            post(urlEqualTo(s"/income-store/nino/$nino/uk-properties/$propertyType/periodic-summaries"))
              .willReturn(
                aResponse()
                  .withStatus(409)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Errors.notAllowedConsolidatedExpenses)))

          givens
        }

        def updateWithNotAllowedConsolidatedExpenses(nino: Nino, propertyType: PropertyType, from: String = "2017-04-06", to: String = "2018-04-05"): Givens = {
          stubFor(
            put(urlEqualTo(s"/income-store/nino/$nino/uk-properties/$propertyType/periodic-summaries?from=$from&to=$to"))
              .willReturn(
                aResponse()
                  .withStatus(409)
                  .withHeader("Content-Type", "application/json")
                  .withBody(DesJsons.Errors.notAllowedConsolidatedExpenses)))

          givens
        }

        def endOfYearStatementReadyToBeFinalised(nino: Nino, start: LocalDate, end: LocalDate): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/$nino/uk-properties/accounting-periods/${start}_${end}/statement"))
            .willReturn(
              aResponse()
                .withStatus(204)
                .withBody("")))

          givens
        }

        def endOfYearStatementMissingPeriod(nino: Nino, start: LocalDate, end: LocalDate): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/$nino/uk-properties/accounting-periods/${start}_${end}/statement"))
            .willReturn(
              aResponse()
                .withStatus(403)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.periodicUpdateMissing)))

          givens
        }

        def endOfYearStatementIsEarly(nino: Nino, start: LocalDate, end: LocalDate): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/$nino/uk-properties/accounting-periods/${start}_${end}/statement"))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.earlySubmission)))

          givens
        }

        def endOfYearStatementIsLate(nino: Nino, start: LocalDate, end: LocalDate): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/$nino/uk-properties/accounting-periods/${start}_${end}/statement"))
            .willReturn(
              aResponse()
                .withStatus(403)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.lateSubmission)))

          givens
        }

        def endOfYearStatementDoesNotMatchPeriod(nino: Nino, start: LocalDate, end: LocalDate): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/$nino/uk-properties/accounting-periods/${start}_${end}/statement"))
            .willReturn(
              aResponse()
                .withStatus(403)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.nonMatchingPeriod)))

          givens
        }

        def endOfYearStatementAlreadySubmitted(nino: Nino, start: LocalDate, end: LocalDate): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/$nino/uk-properties/accounting-periods/${start}_${end}/statement"))
            .willReturn(
              aResponse()
                .withStatus(403)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.alreadySubmitted)))

          givens
        }

      }

      object GiftAid {

        def updatePayments(nino: Nino, taxYear: TaxYear): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/$nino/charitable-giving/${taxYear.toDesTaxYear}"))
              .willReturn(
                aResponse()
                  .withStatus(204)
                .withBody("")))
          givens
        }

        def updatePaymentsWithNinoNotAvailable(nino: Nino, taxYear: TaxYear): Givens = {
          stubFor(post(urlEqualTo(s"/income-store/nino/$nino/charitable-giving/${taxYear.toDesTaxYear}"))
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.ninoNotFound)))

          givens
        }

        def retrievePayments(nino: Nino, taxYear: TaxYear): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/$nino/charitable-giving/${taxYear.toDesTaxYear}"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.CharitableGivings())))

          givens
        }

        def retrievePaymentsWithInvalidNino(nino: Nino, taxYear: TaxYear): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/$nino/charitable-giving/${taxYear.toDesTaxYear}"))
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.ninoNotFound)))

          givens
        }
      }

      object PropertiesBISS {
        def getSummary(nino: Nino, taxYear: TaxYear): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/$nino/uk-properties/income-source-summary/${taxYear.toDesTaxYear}"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.PropertiesBISS.summary)))

          givens
        }

        def getSummaryErrorResponse(nino: Nino, taxYear: TaxYear, status: Int, errorCode: String): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/$nino/uk-properties/income-source-summary/${taxYear.toDesTaxYear}"))
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
          stubFor(get(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/income-source-summary/${taxYear.toDesTaxYear}"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.SelfEmploymentBISS.summary)))

          givens
        }

        def getSummaryErrorResponse(nino: Nino, taxYear: TaxYear, id: String, status: Int, errorCode: String): Givens = {
          stubFor(get(urlEqualTo(s"/income-store/nino/$nino/self-employments/$id/income-source-summary/${taxYear.toDesTaxYear}"))
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

  def given() = new Givens()

  def when() = new HttpVerbs()

}
