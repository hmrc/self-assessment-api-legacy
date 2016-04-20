package uk.gov.hmrc.support

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures, Eventually}
import org.scalatestplus.play.OneServerPerSuite
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.test.UnitSpec

trait BaseFunctionalSpec extends UnitSpec with Matchers with OneServerPerSuite with Eventually with ScalaFutures
with BeforeAndAfterEach with IntegrationPatience with BeforeAndAfterAll with MockitoSugar {

  val WIREMOCK_PORT = 22222
  val stubHost = "localhost"

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
    def bodyIs(expectedBody: String): Unit = {
      response.body shouldBe expectedBody
    }

    def statusIs(statusCode: Int) = {
      response.status shouldBe statusCode
      this
    }
    def thenAssertThat() = this
  }

  class HttpVerbs {
    implicit val hc = HeaderCarrier()
    def get(path: String) = {
      assert(path.startsWith("/"), "please provide only a path starting with '/'")
      new Assertions(Http.get(s"http://localhost:$port$path"))
    }
  }

  class Givens {
    def when() = new HttpVerbs()
    def userIsNotAuthorisedForTheResource(utr: String) = {
      stubFor(get(urlPathEqualTo(s"/authorise/read/sa/$utr")).willReturn(aResponse().withStatus(401).withHeader("Content-Length", "0")))
      this
    }
    def userIsAuthorisedForTheResource(utr: String) = {
      stubFor(get(urlPathEqualTo(s"/authorise/read/sa/$utr")).willReturn(aResponse().withStatus(200)))
      this
    }
  }

  def given() = new Givens()

}
