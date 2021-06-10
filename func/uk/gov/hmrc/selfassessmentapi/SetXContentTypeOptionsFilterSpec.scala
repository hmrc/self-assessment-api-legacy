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

import uk.gov.hmrc.selfassessmentapi.filters.SetXContentTypeOptionsFilter
import uk.gov.hmrc.selfassessmentapi.resources.Jsons
import uk.gov.hmrc.support.BaseFunctionalSpec

class SetXContentTypeOptionsFilterSpec extends BaseFunctionalSpec {

  "SetXContentTypeOptionsFilter  filter should" should {

    "be applied when returning an HTTP 406 without accept header e.g.: creating a self-employment" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.willBeCreatedFor(nino)
        .when()
        .post(Jsons.SelfEmployment())
        .to(s"/ni/${nino.nino}/self-employments")
        .withoutAcceptHeader()
        .thenAssertThat()
        .statusIs(406)
        .responseContainsHeader(SetXContentTypeOptionsFilter.xContentTypeOptionsHeader, "nosniff".r)
    }
  }
}
