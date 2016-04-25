package uk.gov.hmrc.support

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers}
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatestplus.play.OneServerPerSuite
import play.api.libs.json.{JsObject, JsValue}
import play.api.test.FakeHeaders
import uk.gov.hmrc.domain.{SaUtr, SaUtrGenerator}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.test.UnitSpec

trait BaseFunctionalSpec extends UnitSpec with Matchers with OneServerPerSuite with Eventually with ScalaFutures
  with BeforeAndAfterEach with IntegrationPatience with BeforeAndAfterAll with MockitoSugar {

  val WIREMOCK_PORT = 22222
  val stubHost = "localhost"
  private val saUtrGenerator = new SaUtrGenerator()

  def generateSaUtr(): SaUtr = saUtrGenerator.nextSaUtr

  protected val wiremockBaseUrl: String = s"http://$stubHost:$WIREMOCK_PORT"

  private val wireMockServer = new WireMockServer(wireMockConfig().port(WIREMOCK_PORT))

  override def beforeAll() = {
    wireMockServer.stop()
    wireMockServer.start()
    WireMock.configureFor(stubHost, WIREMOCK_PORT)
  }

  override def beforeEach() = {
    WireMock.reset()
  }

  class Assertions(response: HttpResponse) {
    def bodyIs(expectedBody: String) = {
      response.body shouldBe expectedBody
      this
    }

    def bodyIs(expectedBody: JsValue) = {
      response.json.as[JsObject] - "_links" shouldEqual expectedBody
      this
    }

    def bodyHasLink(rel: String, href: String) = {
      val links = response.json \ "_links"
      val link = links \ rel
      (link \ "href").asOpt[String] shouldEqual Some(href)
      this
    }

    def statusIs(statusCode: Int) = {
      response.status shouldBe statusCode
      this
    }

    def contentTypeIs(contentType: String) = {
      response.header("Content-Type") shouldEqual Some(contentType)
      this
    }

    def body(myQuery: JsValue => JsValue) = {
      new BodyAssertions(myQuery(response.json), this)
    }

    class BodyAssertions(content: JsValue, assertions: Assertions) {
      def is(value: String) = {
        content.as[String] shouldBe value
        assertions
      }
    }

  }

  class HttpVerbs {
    var addAcceptHeader = true
    var method = ""
    var url = ""
    var hc = HeaderCarrier()

    def get(path: String) = {
      assert(path.startsWith("/"), "please provide only a path starting with '/'")
      this.url = s"http://localhost:$port$path"
      this.method = "GET"
      this
    }

    private def withHeader(name : String, value: String) = {
      hc.withExtraHeaders((name, value))
      this
    }

    def withoutAcceptHeader() = {
      this.addAcceptHeader = false
      this
    }

    def thenAssertThat() = {
      if (addAcceptHeader) hc = hc.withExtraHeaders(("Accept", "application/vnd.hmrc.1.0+json"))
      method match {
        case "GET" => new Assertions(Http.get(url)(hc))
      }
    }

    def withAcceptHeader() = {
      addAcceptHeader = true
      this
    }
  }

  class Givens {
    def when() = new HttpVerbs()

    def userIsNotAuthorisedForTheResource(utr: SaUtr) = {
      stubFor(get(urlPathEqualTo(s"/authorise/read/sa/$utr")).willReturn(aResponse().withStatus(401).withHeader("Content-Length", "0")))
      this
    }

    def userIsAuthorisedForTheResource(utr: SaUtr) = {
      stubFor(get(urlPathEqualTo(s"/authorise/read/sa/$utr")).willReturn(aResponse().withStatus(200)))
      this
    }

    def userIsEnrolledInSa(utr: SaUtr) = {
      val json =
        s"""
           |{
           |    "accounts": {
           |        "sa": {
           |            "link": "/sa/individual/$utr",
           |            "utr": "$utr"
           |        }
           |    },
           |    "confidenceLevel": 500
           |}
      """.stripMargin

      stubFor(get(urlPathEqualTo(s"/auth/authority")).willReturn(aResponse().withBody(json).withStatus(200).withHeader("Content-Type", "application/json")))
      this
    }

    def userIsNotEnrolledInSa = {
      val json =
        s"""
           |{
           |    "accounts": {
           |    },
           |    "confidenceLevel": 500
           |}
      """.stripMargin

      stubFor(get(urlPathEqualTo(s"/auth/authority")).willReturn(aResponse().withBody(json).withStatus(200).withHeader("Content-Type", "application/json")))
      this
    }
  }

  def given() = new Givens()

}


