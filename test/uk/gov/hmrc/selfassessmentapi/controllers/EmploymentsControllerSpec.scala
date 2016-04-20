package uk.gov.hmrc.selfassessmentapi.controllers

import play.api.http.Status
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class EmploymentsControllerSpec extends UnitSpec with WithFakeApplication{

  val fakeRequest = FakeRequest("GET", "/")

  "GET /" should {
    "return 200" in {
      val result = EmploymentsController.getEmployments("123456")(fakeRequest)
      status(result) shouldBe Status.OK
    }
  }


}
