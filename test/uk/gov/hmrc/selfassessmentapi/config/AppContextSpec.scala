package uk.gov.hmrc.selfassessmentapi.config

import org.scalatestplus.play.OneServerPerSuite
import uk.gov.hmrc.play.test.UnitSpec

class AppContextSpec extends UnitSpec with OneServerPerSuite {

  "AppContext" should {
    "be initialized with featureSwitch" in {
      val _ = AppContext.featureSwitch
      AppContext.featureSwitch shouldBe 1
    }
  }
}
