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

package uk.gov.hmrc.selfassessmentapi.controllers.api.blindperson

import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.api.CountryCodes.{apply => _}
import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.controllers.api.UkCountryCodes.{apply => _, _}
import uk.gov.hmrc.selfassessmentapi.controllers.api._

case class BlindPerson(country: Option[UkCountryCode] = None,
                       registrationAuthority: Option[String] = None,
                       spouseSurplusAllowance: Option[BigDecimal] = None,
                       wantSpouseToUseSurplusAllowance: Option[Boolean] = None)

object BlindPerson extends JsonMarshaller[BlindPerson] {


  override implicit val writes = Json.writes[BlindPerson]

  override implicit val reads = (
    (__ \ "country").readNullable[UkCountryCode] and
      (__ \ "registrationAuthority").readNullable[String](lengthValidator) and
      (__ \ "spouseSurplusAllowance").readNullable[BigDecimal](positiveAmountValidator("spouseSurplusAllowance")
        keepAnd maxAmountValidator("spouseSurplusAllowance", BigDecimal(2290.00))) and
      (__ \ "wantSpouseToUseSurplusAllowance").readNullable[Boolean]
    ) (BlindPerson.apply _)
    .filter(ValidationError("If the country is England or Wales, registrationAuthority is mandatory", MISSING_REGISTRATION_AUTHORITY)) {
      person =>
        if (person.country.contains(England) || person.country.contains(Wales))
          person.registrationAuthority.isDefined && !person.registrationAuthority.get.isEmpty
        else true
    }
    .filter(ValidationError("If the registrationAuthority is provided then country must be provided", MISSING_COUNTRY)) {
      person =>
        if (person.registrationAuthority.isDefined && !person.registrationAuthority.get.isEmpty)
          person.country.isDefined
        else true
    }
    .filter(ValidationError("A person must be registered blind in a given country to be able to supply wantSpouseToUseSurplusAllowance", MUST_BE_BLIND_TO_WANT_SPOUSE_TO_USE_SURPLUS_ALLOWANCE)) {
      person =>
        if (person.wantSpouseToUseSurplusAllowance.isDefined) person.country.isDefined else true
    }

  override def example(id: Option[String] = None) =
    BlindPerson(
      country = Some(Wales),
      registrationAuthority = Some("Registrar"),
      spouseSurplusAllowance = Some(2000.05),
      wantSpouseToUseSurplusAllowance = Some(true)
    )
}
