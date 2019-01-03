/*
 * Copyright 2019 HM Revenue & Customs
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

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.models.SourceId
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentsResourceSpec extends BaseFunctionalSpec {

  "create" should {
    "return code 201 containing a location header when creating a valid a self-employment source of income" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.willBeCreatedFor(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader("Location", s"/self-assessment/ni/$nino/self-employments/\\w+".r)
    }

    "return code 400 (INVALID_REQUEST) when attempting to create a self-employment with an invalid dates in the accountingPeriod" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .post(Jsons.SelfEmployment(accPeriodStart = "01-01-2017", accPeriodEnd = "02-01-2017")).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.invalidRequest(("INVALID_DATE", "/accountingPeriod/start"), ("INVALID_DATE", "/accountingPeriod/end")))
    }

    "return code 400 (INVALID_VALUE) when attempting to create a self-employment with an invalid accounting type" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .post(Jsons.SelfEmployment(accountingType = "INVALID_ACC_TYPE")).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.invalidRequest(("INVALID_VALUE", "/accountingType")))
    }

    "return code 400 when attempting to create a self-employment that fails DES validation" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().payloadFailsValidationFor(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(Jsons.Errors.invalidRequest)
    }

    "return code 400 when attempting to create a self-employment that fails DES nino validation" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().invalidNinoFor(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(Jsons.Errors.ninoInvalid)
    }

    "return code 500 when DES is experiencing issues" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().serverErrorFor(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "return code 500 when systems that DES is dependant on are experiencing issues" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().serviceUnavailableFor(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "return code 403 Unauthorized when attempting to create more than one self-employment source" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.tooManySourcesFor(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsLike(Jsons.Errors.businessError("TOO_MANY_SOURCES" -> ""))
    }

    "return code 500 when we receive a status code from DES that we do not handle" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().isATeapotFor(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(500)
    }
  }

  "retrieve" should {
    "return code 200 when retrieving a self-employment resource that exists" in {
      val expectedSelfEmployment = Jsons.SelfEmployment(cessationDate = None, businessDescription = None)

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.willBeReturnedFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/abc")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedSelfEmployment.toString())
        .bodyDoesNotHavePath[SourceId]("id")
    }

    "return code 400 when attempting to retrieve a self-employment that fails DES nino validation" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().invalidNinoFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/sourceId")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(Jsons.Errors.ninoInvalid)
    }

    "return code 404 when retrieving a self-employment resource that does not exist" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.noneFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/invalidSourceId")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 404 when retrieving a self-employment resource with an unmatched income id" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.incomeIdNotFoundFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/invalidSourceId")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 400 when retrieving a self-employment that fails nino validation" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/teapot/self-employments/invalidSourceId")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(Jsons.Errors.ninoInvalid)
    }

    "return code 404 when retrieving a self-employment for a nino that does not exist" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().ninoNotFoundFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/invalidSourceId")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 500 when DES returns invalid Json" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.invalidJson(nino)
        .when()
        .get(s"/ni/$nino/self-employments/abc")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "return code 500 when DES is experiencing issues" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().serverErrorFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/invalidSourceId")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "return code 500 when systems that DES is dependant on are experiencing issues" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().serviceUnavailableFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/invalidSourceId")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "return code 500 when we receive a status code from DES that we do not handle" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().isATeapotFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/abc")
        .thenAssertThat()
        .statusIs(500)
    }
  }

  "retrieveAll" should {
    "return code 200 when retrieving self-employments that exist" in {

      val expectedBody =
        s"""
           |[
           |  ${Jsons.SelfEmployment(cessationDate = None, businessDescription = None).toString()}
           |]
         """.stripMargin

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.willBeReturnedFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
        .selectFields(_ \\ "id").isLength(1).matches("\\w+".r)
    }

    "return code 200 with an empty body when the user has no self-employment sources" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.noneFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(200)
        .jsonBodyIsEmptyArray()
    }

    "return code 400 when attempting to retrieve self-employments that fails DES nino validation" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().invalidNinoFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(Jsons.Errors.ninoInvalid)
    }

    "return code 404 when attempting to retrieve self-employments for a nino that does not exist" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().ninoNotFoundFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 500 when DES returns invalid Json" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.invalidJson(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "return code 500 when we receive a status code from DES that we do not handle" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().isATeapotFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(500)
    }
  }

}
