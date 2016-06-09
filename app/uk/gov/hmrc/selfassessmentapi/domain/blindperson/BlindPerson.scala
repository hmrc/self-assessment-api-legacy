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

package uk.gov.hmrc.selfassessmentapi.domain.blindperson

import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.domain.CountryCodes.{apply => _}
import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.domain.UkCountryCodes.{apply => _, _}
import uk.gov.hmrc.selfassessmentapi.domain.{BaseDomain, _}

case class BlindPerson(country: UkCountryCode,
                       registrationAuthority: Option[String] = None,
                       spouseSurplusAllowance: Option[BigDecimal] = None,
                       wantSpouseToUseSurplusAllowance: Boolean)

object BlindPerson extends BaseDomain[BlindPerson] {

  override implicit val writes = Json.writes[BlindPerson]

  override implicit val reads = (
    (__ \ "country").read[UkCountryCode] and
      (__ \ "registrationAuthority").readNullable[String](lengthValidator) and
      (__ \ "spouseSurplusAllowance").readNullable[BigDecimal](positiveAmountValidator("spouseSurplusAllowance") keepAnd maxAmountValidator("spouseSurplusAllowance", BigDecimal(2290.00))) and
      (__ \ "wantSpouseToUseSurplusAllowance").read[Boolean]
    ) (BlindPerson.apply _).filter(ValidationError("If the country is England or Wales, registrationAuthority is mandatory", MISSING_REGISTRATION_AUTHORITY)) {
    blindPerson =>
      if (blindPerson.country == England || blindPerson.country == Wales)
        blindPerson.registrationAuthority != None
      else true
  }

  override def example(id: Option[String] = None) =
    BlindPerson(
      country = Wales,
      registrationAuthority = Some("Registrar"),
      spouseSurplusAllowance = Some(2000.05),
      wantSpouseToUseSurplusAllowance = true
    )
}
