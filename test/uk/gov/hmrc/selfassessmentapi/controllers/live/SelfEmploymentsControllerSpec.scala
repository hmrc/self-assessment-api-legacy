/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.controllers.live

import com.github.tomakehurst.wiremock.client.WireMock.{reset => _}
import org.joda.time.LocalDate
import org.mockito.Mockito._
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterEach}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.api.controllers.ErrorNotFound
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.domain.{SelfEmployment, SelfEmploymentId}
import uk.gov.hmrc.selfassessmentapi.services.SelfEmploymentService

import scala.concurrent.Future

class SelfEmploymentsControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfter with Eventually
  with ScalaFutures with IntegrationPatience with BeforeAndAfterEach {

  val saUtr: SaUtr = SaUtr("12345")

  val mockSelfEmploymentService: SelfEmploymentService =
    mock[SelfEmploymentService]

  override def beforeEach() = {
    reset(mockSelfEmploymentService)
  }

  val controller = new uk.gov.hmrc.selfassessmentapi.controllers.SelfEmploymentsController {
    override val selfEmploymentService: SelfEmploymentService =
      mockSelfEmploymentService
    override lazy val context: String = "self-assessment"
  }

  "create" should {
    def fakeRequest(selfEmployment: SelfEmployment) =
      FakeRequest("POST", s"/${saUtr.utr}/self-employments")
        .withJsonBody(Json.toJson(selfEmployment))
        .withHeaders(("Accept", "application/vnd.hmrc.1.0+json"))
      val seId = "1234"

    "invoke the self employment service and return an HTTP 201 and a successful response" in {
      val selfEmployment =
        SelfEmployment(Some(seId), "Awesome Plumbers", LocalDate.now().minusDays(1))
      when(mockSelfEmploymentService.create(selfEmployment))
        .thenReturn(Future.successful(seId))
      val request = fakeRequest(selfEmployment)
      val result = await(call(controller.create(saUtr), request))
      status(result) shouldEqual CREATED

      val jsonResult = contentAsJson(result)

      (jsonResult \ "_links" \ "self" \ "href").as[String] shouldEqual "/self-assessment/12345/self-employments/1234"
    }

    "return error if the json body is invalid" in {
      val selfEmployment =
        SelfEmployment(Some(seId), "Awesome Plumbers", LocalDate.now().plusDays(1))
      when(mockSelfEmploymentService.create(selfEmployment))
        .thenReturn(Future.successful(seId))
      val request = fakeRequest(selfEmployment)
      val result = await(call(controller.create(saUtr), request))
      status(result) shouldEqual BAD_REQUEST

      // todo assert the error response (to be implemented after the validation story is played)
    }
  }


  "update" should {

    val seId = BSONObjectID.generate.stringify
    def fakeRequest(selfEmployment: SelfEmployment) =
      FakeRequest("PUT", s"/${saUtr.utr}/self-employments/$seId")
        .withJsonBody(Json.toJson(selfEmployment))
        .withHeaders(("Accept", "application/vnd.hmrc.1.0+json"))

    "return error if the json body is invalid" in {
      val selfEmployment =
        SelfEmployment(Some(seId), "Awesome Plumbers" * 30, LocalDate.now().minusDays(1))
      when(mockSelfEmploymentService.create(selfEmployment))
        .thenReturn(Future.successful(seId))
      val request = fakeRequest(selfEmployment)
      val result = await(call(controller.create(saUtr), request))
      status(result) shouldEqual BAD_REQUEST

      // todo assert the error response (to be implemented after the validation story is played)
    }
  }

  "findById" should {
    def fakeRequest(seId: SelfEmploymentId) =
      FakeRequest("GET", s"/${saUtr.utr}/self-employments/$seId")
        .withHeaders(("Accept", "application/vnd.hmrc.1.0+json"))

    "invoke the self-employments service with an existing self-employment id, retrieve the self employment and return an HTTP 200" in {
      val seId = BSONObjectID.generate.stringify
      val selfEmployment = SelfEmployment(Some(seId), "Awesome Plumbers", LocalDate.now().minusDays(1))

      when(mockSelfEmploymentService.findBySelfEmploymentId(saUtr, seId)).thenReturn(Future.successful(Some(selfEmployment)))

      val request = fakeRequest(seId)
      val result = await(call(controller.findById(saUtr, seId), request))
      status(result) shouldEqual OK

      val jsonResult = contentAsJson(result)

      jsonResult.as[SelfEmployment] shouldEqual selfEmployment

      (jsonResult \ "_links" \ "self" \ "href").as[String] shouldEqual s"/self-assessment/${saUtr.utr}/self-employments/$seId"
    }

    "return an HTTP 404 when the self-employment id does not exist" in {
      val seId = BSONObjectID.generate.stringify

      when(mockSelfEmploymentService.findBySelfEmploymentId(saUtr, seId)).thenReturn(Future.successful(None))

      val request = fakeRequest(seId)
      val result = await(call(controller.findById(saUtr, seId), request))
      status(result) shouldEqual NOT_FOUND

      val jsonResult = contentAsJson(result)

      (jsonResult \ "message").as[String] shouldEqual ErrorNotFound.message
    }
  }
}
