/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.resources.wrappers

import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.models.des
import uk.gov.hmrc.selfassessmentapi.models.giftaid.GiftAidPayments

case class GiftAidPaymentsResponse(underlying: HttpResponse) extends Response { self =>

  def payments: Option[GiftAidPayments] = {
    json.asOpt[des.giftaid.GiftAidPayments] match {
      case Some(giftAidPayments) =>
        Some(GiftAidPayments.from(giftAidPayments))
      case None =>
        logger.error(s"The response from DES does not match the expected format. JSON: [$json]")
        None
    }
  }
}