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

package uk.gov.hmrc.selfassessmentapi.domain

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.definition.EnumJson
import uk.gov.hmrc.selfassessmentapi.domain.CountryCodes.CountryCode


case class CountryDetails(name: String, doubleTaxAgreement: Boolean)

object CountryCodes extends Enumeration {

  implicit val format = EnumJson.enumFormat(CountryCodes, Some("Country code is invalid"))

  type CountryCode = Value
  val AFG,ALB,DZA,ASM,AND,AGO,AIA,ATG,ARG,ARM,ABW,AUS,AUT,AZ,BHR,BGD,BRB,BLR,BEL,BLZ,BEN,BMU,BTN,BOL,BES1,BIH,BWA,BRA,
  VGB,BRN,BGR,BFA,MMR,BDI,KHM,CMR,CAN,CPV,CYM,CAF,TCD,CHL,CHN,CXR,CCK,COL,COM,COG,COK,CRI,CIV,HRV,CUW,CYP,CZE,COD,DNK,
  DJI,DMA,DOM,ECU,EGY,SLV,GNQ,ERI,EST,ETH,FLK,FRO,FJI,FIN,FRA,GUF,PYF,GAB,GMB,GEO,DEU,GHA,GIB,GRC,GRL,GRD,GLP,GUM,GTM,
  GGY,GIN,GNB,GUY,HTI,HND,HKG,HUN,ISL,IND,IDN,IRN,IRQ,IRL,IMN,ISR,ITA,JAM,JPN,JEY,JOR,KAZ,KEN,KIR,KWT,KGZ,LAO,LVA,LBN,
  LSO,LBR,LBY,LIE,LTU,LUX,MAC,MKD,MDG,MWI,MYS,MDV,MLI,MLT,MHL,MTQ,MRT,MUS,MYT,MEX,FSM,MDA,MCO,MNG,MNE,MSR,MAR,MOZ,NAM,
  NRU,NPL,NLD,NCL,NZL,NIC,NER,NGA,NIU,NFK,PRK,MNP,NOR,OMN,PAK,PLW,PAN,PNG,PRY,PER,PHL,PCN,POL,PRT,PRI,QAT,REU,ROU,RUS,
  RWA,SHN,KNA,LCA,SPM,VCT,BES2,WSM,SMR,STP,SAU,SEN,SRB,SYC,SLE,SGP,BES3,SXM,SVK,SVN,SLB,SOM,ZAF,KOR,SSD,ESP,LKA,SDN,SUR,
  SJM,SWZ,SWE,CHE,SYR,TWN,TJK,TZA,THA,TLS,TGO,TKL,TON,TTO,TUN,TUR,TKM,TCA,TUV,UGA,UKR,ARE,GBR,USA,VIR,URY,UZB,VUT,VAT,
  VEN,VNM,WLF,YEM,ZMB,ZWE,ZZZ = Value

  def details =
    Map(
      (AFG, CountryDetails("Afghanistan", false)),(ALB, CountryDetails("Albania", false)),(DZA, CountryDetails("Algeria", false)),(ASM, CountryDetails("American Samoa", false)),
      (AND, CountryDetails("Andorra", false)), (AGO, CountryDetails("Angola", false)),(AIA, CountryDetails("Anguilla", false)),(ATG, CountryDetails("Antigua and Barbuda", true)),
      (ARG, CountryDetails("Argentina", true)),(ARM, CountryDetails("Armenia", true)),(ABW, CountryDetails("Aruba", false)),(AUS, CountryDetails("Australia", true)),(AUT, CountryDetails("Austria", true)),
      (AZ, CountryDetails("Azerbaijan", true)),(BHR, CountryDetails("Bahrain", true)),(BGD, CountryDetails("Bangladesh", true)),(BRB, CountryDetails("Barbados", true)),(BLR, CountryDetails("Belarus", true)),
      (BEL, CountryDetails("Belgium", true)),(BLZ, CountryDetails("Belize", true)),(BEN, CountryDetails("Benin", false)),(BMU, CountryDetails("Bermuda", false)),(BTN, CountryDetails("Bhutan", false)),
      (BOL, CountryDetails("Bolivia", true)),(BES1, CountryDetails("Bonaire", true)),(BIH, CountryDetails("Bosnia and Herzegovina", true)),(BWA, CountryDetails("Botswana", true)),
      (BRA, CountryDetails("Brazil", false)),(VGB, CountryDetails("British Virgin Islands", true)),(BRN, CountryDetails("Brunei Darussalam", true)),(BGR, CountryDetails("Bulgaria", true)),
      (BFA, CountryDetails("Burkino Faso", false)),(MMR, CountryDetails("Burma (also known as Myanmar)", true)),(BDI, CountryDetails("Burundi", false)),(KHM, CountryDetails("Cambodia", false)),
      (CMR, CountryDetails("Cameroon", false)),(CAN, CountryDetails("Canada", true)),(CPV, CountryDetails("Cape Verde", false)),(CYM, CountryDetails("Cayman Islands", true)),
      (CAF, CountryDetails("Central African Republic", false)),(TCD, CountryDetails("Chad", false)),(CHL, CountryDetails("Chile", true)), (CHN, CountryDetails("China", true)),
      (CXR, CountryDetails("Christmas Island", true)),(CCK, CountryDetails("Cocos (Keeling) Islands", true)),(COL, CountryDetails("Colombia", false)),(COM, CountryDetails("Comoros", false)),
      (COG, CountryDetails("Congo", false)),(COK, CountryDetails("Cook Islands", false)),(CRI, CountryDetails("Costa Rica", false)),(CIV, CountryDetails("Côte d’Ivoire", true)),
      (HRV, CountryDetails("Croatia", true)),(CUW, CountryDetails("Curaçao", true)),(CYP, CountryDetails("Cyprus", true)),(CZE, CountryDetails("Czech Republic", true)),
      (COD, CountryDetails("Democratic Republic of the Congo (formerly Zaire)", false)),(DNK, CountryDetails("Denmark", true)),(DJI, CountryDetails("Djibouti", false)),
      (DMA, CountryDetails("Dominica", false)),(DOM, CountryDetails("Dominican Republic", false)),(ECU, CountryDetails("Ecuador", false)),(EGY, CountryDetails("Egypt", true)),
      (SLV, CountryDetails("El Salvador", false)),(GNQ, CountryDetails("Equatorial Guinea", false)),(ERI, CountryDetails("Eritrea", false)),(EST, CountryDetails("Estonia", true)),
      (ETH, CountryDetails("Ethiopia", true)),(FLK, CountryDetails("Falkland Islands", true)),(FRO, CountryDetails("Faroe Islands", true)),(FJI, CountryDetails("Fiji", true)),
      (FIN, CountryDetails("Finland", true)),(FRA, CountryDetails("France", true)),(GUF, CountryDetails("French Guiana", true)),(PYF, CountryDetails("French Polynesia", false)),(GAB, CountryDetails("Gabon", false)),
      (GMB, CountryDetails("Gambia", true)),(GEO, CountryDetails("Georgia", true)),(DEU, CountryDetails("Germany", true)),(GHA, CountryDetails("Ghana", true)),(GIB, CountryDetails("Gibraltar", false)),
      (GRC, CountryDetails("Greece", true)),(GRL, CountryDetails("Greenland", false)),(GRD, CountryDetails("Grenada", true)),(GLP, CountryDetails("Guadeloupe", true)),(GUM, CountryDetails("Guam", false)),
      (GTM, CountryDetails("Guatemala", false)),(GGY, CountryDetails("Guernsey", true)),(GIN, CountryDetails("Guinea", false)),(GNB, CountryDetails("Guinea-Bissau", false)),(GUY, CountryDetails("Guyana", true)),
      (HTI, CountryDetails("Haiti", false)),(HND, CountryDetails("Honduras", false)),(HKG, CountryDetails("Hong Kong (SAR)", true)),(HUN, CountryDetails("Hungary", true)),(ISL, CountryDetails("Iceland", true)),
      (IND, CountryDetails("India", true)),(IDN, CountryDetails("Indonesia", true)),(IRN, CountryDetails("Iran", false)),(IRQ, CountryDetails("Iraq", false)),(IRL, CountryDetails("Ireland (Republic of)", true)),
      (IMN, CountryDetails("Isle of Man", true)),(ISR, CountryDetails("Israel", true)),(ITA, CountryDetails("Italy", true)),(JAM, CountryDetails("Jamaica", true)),(JPN, CountryDetails("Japan", true)),
      (JEY, CountryDetails("Jersey", true)),(JOR, CountryDetails("Jordan", true)),(KAZ, CountryDetails("Kazakhstan", true)),(KEN, CountryDetails("Kenya", true)),(KIR, CountryDetails("Kiribati", true)),
      (KWT, CountryDetails("Kuwait", true)),(KGZ, CountryDetails("Kyrgyzstan", false)),(LAO, CountryDetails("Laos", false)),(LVA, CountryDetails("Latvia", true)),(LBN, CountryDetails("Lebanon", false)),
      (LSO, CountryDetails("Lesotho", true)),(LBR, CountryDetails("Liberia", false)),(LBY, CountryDetails("Libya", true)),(LIE, CountryDetails("Liechtenstein", true)),(LTU, CountryDetails("Lithuania", true)),
      (LUX, CountryDetails("Luxembourg", true)),(MAC, CountryDetails("Macao (SAR)", false)),(MKD, CountryDetails("Macedonia (FYR)", true)),(MDG, CountryDetails("Madagascar", false)),(MWI, CountryDetails("Malawi", true)),
      (MYS, CountryDetails("Malaysia", true)),(MDV, CountryDetails("Maldives", false)),(MLI, CountryDetails("Mali", false)),(MLT, CountryDetails("Malta", true)),(MHL, CountryDetails("Marshall Islands", false)),
      (MTQ, CountryDetails("Martinique", true)),(MRT, CountryDetails("Mauritania", false)),(MUS, CountryDetails("Mauritius", true)),(MYT, CountryDetails("Mayotte", false)),(MEX, CountryDetails("Mexico", true)),
      (FSM, CountryDetails("Micronesia", false)),(MDA, CountryDetails("Moldova", true)),(MCO, CountryDetails("Monaco", false)),(MNG, CountryDetails("Mongolia", true)),(MNE, CountryDetails("Montenegro", true)),
      (MSR, CountryDetails("Montserrat", true)),(MAR, CountryDetails("Morocco", true)),(MOZ, CountryDetails("Mozambique", false)),(NAM, CountryDetails("Namibia", true)),(NRU, CountryDetails("Nauru", false)),
      (NPL, CountryDetails("Nepal", false)),(NLD, CountryDetails("Netherlands", true)),(NCL, CountryDetails("New Caledonia", false)),(NZL, CountryDetails("New Zealand", true)),(NIC, CountryDetails("Nicaragua", false)),
      (NER, CountryDetails("Niger", false)),(NGA, CountryDetails("Nigeria", true)),(NIU, CountryDetails("Niue", false)),(NFK, CountryDetails("Norfolk Island", true)),(PRK, CountryDetails("North Korea", false)),
      (MNP, CountryDetails("Northern Mariana Islands", false)),(NOR, CountryDetails("Norway", true)),(OMN, CountryDetails("Oman", true)),(PAK, CountryDetails("Pakistan", true)),(PLW, CountryDetails("Palau", false)),
      (PAN, CountryDetails("Panama", false)),(PNG, CountryDetails("Papua New Guinea", true)),(PRY, CountryDetails("Paraguay", false)),(PER, CountryDetails("Peru", false)),(PHL, CountryDetails("Philippines", true)),
      (PCN, CountryDetails("Pitcairn Island", false)),(POL, CountryDetails("Poland", true)),(PRT, CountryDetails("Portugal", true)),(PRI, CountryDetails("Puerto Rico", false)),(QAT, CountryDetails("Qatar", true)),
      (REU, CountryDetails("Reunion", true)),(ROU, CountryDetails("Romania", true)),(RUS, CountryDetails("Russian Federation", true)),(RWA, CountryDetails("Rwanda", false)),(SHN, CountryDetails("St Helena and Dependencies", false)),
      (KNA, CountryDetails("St Kitts and Nevis", true)),(LCA, CountryDetails("St Lucia", false)),(SPM, CountryDetails("St Pierre and Miquelon", false)),(VCT, CountryDetails("St Vincent and the Grenadines", false)),
      (BES2, CountryDetails("Saba", true)),(WSM, CountryDetails("Samoa", false)),(SMR, CountryDetails("San Marino", false)),(STP, CountryDetails("Sao Tome and Principe", false)),(SAU, CountryDetails("Saudi Arabia", true)),
      (SEN, CountryDetails("Senegal", false)),(SRB, CountryDetails("Serbia and Montenegro", true)),(SYC, CountryDetails("Seychelles", false)),(SLE, CountryDetails("Sierra Leone", true)),(SGP, CountryDetails("Singapore", true)),
      (BES3, CountryDetails("Sint Eustatius", true)),(SXM, CountryDetails("Sint Maarten (Dutch part)", true)),(SVK, CountryDetails("Slovak Republic", true)),(SVN, CountryDetails("Slovenia", true)),
      (SLB, CountryDetails("Solomon Islands", true)),(SOM, CountryDetails("Somalia", false)),(ZAF, CountryDetails("South Africa", true)),(KOR, CountryDetails("South Korea", true)),(SSD, CountryDetails("South Sudan", false)),
      (ESP, CountryDetails("Spain", true)),(LKA, CountryDetails("Sri Lanka", true)),(SDN, CountryDetails("Sudan", true)),(SUR, CountryDetails("Suriname", false)),(SJM, CountryDetails("Svalbard and Jan Mayen Islands", false)),
      (SWZ, CountryDetails("Swaziland", true)),(SWE, CountryDetails("Sweden", true)), (CHE, CountryDetails("Switzerland", true)),(SYR, CountryDetails("Syria", false)),(TWN, CountryDetails("Taiwan", true)),
      (TJK, CountryDetails("Tajikistan", true)),(TZA, CountryDetails("Tanzania", false)),(THA, CountryDetails("Thailand", true)),(TLS, CountryDetails("Timor-Leste", false)),(TGO, CountryDetails("Togo", false)),
      (TKL, CountryDetails("Tokelau", false)),(TON, CountryDetails("Tonga", false)),(TTO, CountryDetails("Trinidad and Tobago", true)),(TUN, CountryDetails("Tunisia", true)),(TUR, CountryDetails("Turkey", true)),
      (TKM, CountryDetails("Turkmenistan", true)),(TCA, CountryDetails("Turks and Caicos Islands", false)),(TUV, CountryDetails("Tuvalu", true)),(UGA, CountryDetails("Uganda", true)),(UKR, CountryDetails("Ukraine", true)),
      (ARE, CountryDetails("United Arab Emirates", false)),(GBR, CountryDetails("United Kingdom", false)),(USA, CountryDetails("United States of America", true)),(VIR, CountryDetails("United States Virgin Islands", false)),
      (URY, CountryDetails("Uruguay", false)),(UZB, CountryDetails("Uzbekistan", true)),(VUT, CountryDetails("Vanuatu", false)),(VAT, CountryDetails("Vatican", false)),(VEN, CountryDetails("Venezuela", true)),
      (VNM, CountryDetails("Vietnam", true)),(WLF, CountryDetails("Wallis and Futuna Islands", false)),(YEM, CountryDetails("Yemen", false)),(ZMB, CountryDetails("Zambia", true)),(ZWE, CountryDetails("Zimbabwe", true)),
      (ZZZ, CountryDetails("None of the above", false))
    )
}
case class CountryAndAmount(countryCode: CountryCode, amount: BigDecimal)

object CountryAndAmount extends BaseDomain[CountryAndAmount]{
  override implicit val writes = Json.writes[CountryAndAmount]
  override implicit val reads = (
    (__ \ "countryCode").read[CountryCode] and
      (__ \ "amount").read[BigDecimal](positiveAmountValidator("amount"))
    )(CountryAndAmount.apply _)
  override def example(id: Option[String]) = CountryAndAmount(CountryCodes.GBR, BigDecimal(1000.00))
}
