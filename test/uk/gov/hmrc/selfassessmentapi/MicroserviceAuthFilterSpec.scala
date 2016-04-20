package uk.gov.hmrc.selfassessmentapi

import org.scalatest.{Matchers, WordSpecLike}
import uk.gov.hmrc.play.auth.controllers.AuthConfig
import uk.gov.hmrc.play.auth.microservice.connectors.ConfidenceLevel.L50
import uk.gov.hmrc.play.auth.microservice.connectors.{AuthRequestParameters, HttpVerb}

/**
  * Created by hicnar on 20/04/16.
  */
class MicroserviceAuthFilterSpec extends WordSpecLike with Matchers {

  val underTest = MicroserviceAuthFilter

  "MicroserviceAuthFilter" should {
    "extract resource that builds valid auth url" in {
      val resource = underTest.extractResource("/123456/employments", HttpVerb("GET"), AuthConfig(pattern = "/(\\w+)/.*".r, confidenceLevel = L50))
      resource.get.buildUrl("http://authhost.com/auth", AuthRequestParameters(L50)) shouldBe "http://authhost.com/auth/authorise/read/sa/123456?confidenceLevel=50"
    }
  }
}
