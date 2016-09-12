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

package uk.gov.hmrc.selfassessmentapi.controllers.api

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.api.CountryCodes.CountryCode
import uk.gov.hmrc.selfassessmentapi.controllers.definition.EnumJson


case class CountryDetails(name: String, doubleTaxAgreement: Boolean = true)

object CountryCodes extends Enumeration {

  implicit val format = EnumJson.enumFormat(CountryCodes, Some("Country code is invalid"))

  type CountryCode = Value
  val AFG, ALB, DZA, ASM, AND, AGO, AIA, ATG, ARG, ARM, ABW, AUS, AUT, AZE, BHR, BGD, BRB, BLR, BEL, BLZ, BEN, BMU, BTN, BOL, BES1, BIH, BWA, BRA,
  VGB, BRN, BGR, BFA, MMR, BDI, KHM, CMR, CAN, CPV, CYM, CAF, TCD, CHL, CHN, CXR, CCK, COL, COM, COG, COK, CRI, CIV, HRV, CUW, CYP, CZE, COD, DNK,
  DJI, DMA, DOM, ECU, EGY, SLV, GNQ, ERI, EST, ETH, FLK, FRO, FJI, FIN, FRA, GUF, PYF, GAB, GMB, GEO, DEU, GHA, GIB, GRC, GRL, GRD, GLP, GUM, GTM,
  GGY, GIN, GNB, GUY, HTI, HND, HKG, HUN, ISL, IND, IDN, IRN, IRQ, IRL, IMN, ISR, ITA, JAM, JPN, JEY, JOR, KAZ, KEN, KIR, KWT, KGZ, LAO, LVA, LBN,
  LSO, LBR, LBY, LIE, LTU, LUX, MAC, MKD, MDG, MWI, MYS, MDV, MLI, MLT, MHL, MTQ, MRT, MUS, MYT, MEX, FSM, MDA, MCO, MNG, MNE, MSR, MAR, MOZ, NAM,
  NRU, NPL, NLD, NCL, NZL, NIC, NER, NGA, NIU, NFK, PRK, MNP, NOR, OMN, PAK, PLW, PAN, PNG, PRY, PER, PHL, PCN, POL, PRT, PRI, QAT, REU, ROU, RUS,
  RWA, SHN, KNA, LCA, SPM, VCT, BES2, WSM, SMR, STP, SAU, SEN, SRB, SYC, SLE, SGP, BES3, SXM, SVK, SVN, SLB, SOM, ZAF, KOR, SSD, ESP, LKA, SDN, SUR,
  SJM, SWZ, SWE, CHE, SYR, TWN, TJK, TZA, THA, TLS, TGO, TKL, TON, TTO, TUN, TUR, TKM, TCA, TUV, UGA, UKR, ARE, GBR, USA, VIR, URY, UZB, VUT, VAT,
  VEN, VNM, WLF, YEM, ZMB, ZWE, ZZZ = Value
}

case class CountryAndAmount(countryCode: CountryCode, amount: BigDecimal)

object CountryAndAmount extends JsonMarshaller[CountryAndAmount] {
  override implicit val writes = Json.writes[CountryAndAmount]
  override implicit val reads = (
    (__ \ "countryCode").read[CountryCode] and
      (__ \ "amount").read[BigDecimal](positiveAmountValidator("amount"))
    ) (CountryAndAmount.apply _)

  override def example(id: Option[String]) = CountryAndAmount(CountryCodes.GBR, BigDecimal(1000.00))
}

object UkCountryCodes extends Enumeration {
  type UkCountryCode = Value
  val England, NorthernIreland, Scotland, Wales = Value

  implicit val enumFormat = EnumJson.enumFormat(UkCountryCodes, Some("UK country code is invalid"))
}