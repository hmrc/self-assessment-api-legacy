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


case class CountryDetails(name: String, doubleTaxAgreement: Boolean = true)

object CountryCodes extends Enumeration {

  implicit val format = EnumJson.enumFormat(CountryCodes, Some("Country code is invalid"))

  type CountryCode = Value
  val AFG,ALB,DZA,ASM,AND,AGO,AIA,ATG,ARG,ARM,ABW,AUS,AUT,AZE,BHR,BGD,BRB,BLR,BEL,BLZ,BEN,BMU,BTN,BOL,BES1,BIH,BWA,BRA,
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
      AFG -> CountryDetails("Afghanistan", false),ALB -> CountryDetails("Albania", false),DZA -> CountryDetails("Algeria", false),ASM -> CountryDetails("American Samoa", false),
      AND -> CountryDetails("Andorra", false), AGO -> CountryDetails("Angola", false),AIA -> CountryDetails("Anguilla", false),ATG -> CountryDetails("Antigua and Barbuda"),
      ARG -> CountryDetails("Argentina"),ARM -> CountryDetails("Armenia"),ABW -> CountryDetails("Aruba", false),AUS -> CountryDetails("Australia"),AUT -> CountryDetails("Austria"),
      AZE -> CountryDetails("Azerbaijan"),BHR -> CountryDetails("Bahrain"),BGD -> CountryDetails("Bangladesh"),BRB -> CountryDetails("Barbados"),BLR -> CountryDetails("Belarus"),
      BEL -> CountryDetails("Belgium"),BLZ -> CountryDetails("Belize"),BEN -> CountryDetails("Benin", false),BMU -> CountryDetails("Bermuda", false),BTN -> CountryDetails("Bhutan", false),
      BOL -> CountryDetails("Bolivia"),BES1 -> CountryDetails("Bonaire"),BIH -> CountryDetails("Bosnia and Herzegovina"),BWA -> CountryDetails("Botswana"),
      BRA -> CountryDetails("Brazil", false),VGB -> CountryDetails("British Virgin Islands"),BRN -> CountryDetails("Brunei Darussalam"),BGR -> CountryDetails("Bulgaria"),
      BFA -> CountryDetails("Burkino Faso", false),MMR -> CountryDetails("Burma also known as Myanmar"),BDI -> CountryDetails("Burundi", false),KHM -> CountryDetails("Cambodia", false),
      CMR -> CountryDetails("Cameroon", false),CAN -> CountryDetails("Canada"),CPV -> CountryDetails("Cape Verde", false),CYM -> CountryDetails("Cayman Islands"),
      CAF -> CountryDetails("Central African Republic", false),TCD -> CountryDetails("Chad", false),CHL -> CountryDetails("Chile"), CHN -> CountryDetails("China"),
      CXR -> CountryDetails("Christmas Island"),CCK -> CountryDetails("Cocos Keeling Islands"),COL -> CountryDetails("Colombia", false),COM -> CountryDetails("Comoros", false),
      COG -> CountryDetails("Congo", false),COK -> CountryDetails("Cook Islands", false),CRI -> CountryDetails("Costa Rica", false),CIV -> CountryDetails("Côte d’Ivoire"),
      HRV -> CountryDetails("Croatia"),CUW -> CountryDetails("Curaçao"),CYP -> CountryDetails("Cyprus"),CZE -> CountryDetails("Czech Republic"),
      COD -> CountryDetails("Democratic Republic of the Congo formerly Zaire", false),DNK -> CountryDetails("Denmark"),DJI -> CountryDetails("Djibouti", false),
      DMA -> CountryDetails("Dominica", false),DOM -> CountryDetails("Dominican Republic", false),ECU -> CountryDetails("Ecuador", false),EGY -> CountryDetails("Egypt"),
      SLV -> CountryDetails("El Salvador", false),GNQ -> CountryDetails("Equatorial Guinea", false),ERI -> CountryDetails("Eritrea", false),EST -> CountryDetails("Estonia"),
      ETH -> CountryDetails("Ethiopia"),FLK -> CountryDetails("Falkland Islands"),FRO -> CountryDetails("Faroe Islands"),FJI -> CountryDetails("Fiji"),
      FIN -> CountryDetails("Finland"),FRA -> CountryDetails("France"),GUF -> CountryDetails("French Guiana"),PYF -> CountryDetails("French Polynesia", false),GAB -> CountryDetails("Gabon", false),
      GMB -> CountryDetails("Gambia"),GEO -> CountryDetails("Georgia"),DEU -> CountryDetails("Germany"),GHA -> CountryDetails("Ghana"),GIB -> CountryDetails("Gibraltar", false),
      GRC -> CountryDetails("Greece"),GRL -> CountryDetails("Greenland", false),GRD -> CountryDetails("Grenada"),GLP -> CountryDetails("Guadeloupe"),GUM -> CountryDetails("Guam", false),
      GTM -> CountryDetails("Guatemala", false),GGY -> CountryDetails("Guernsey"),GIN -> CountryDetails("Guinea", false),GNB -> CountryDetails("Guinea-Bissau", false),GUY -> CountryDetails("Guyana"),
      HTI -> CountryDetails("Haiti", false),HND -> CountryDetails("Honduras", false),HKG -> CountryDetails("Hong Kong SAR"),HUN -> CountryDetails("Hungary"),ISL -> CountryDetails("Iceland"),
      IND -> CountryDetails("India"),IDN -> CountryDetails("Indonesia"),IRN -> CountryDetails("Iran", false),IRQ -> CountryDetails("Iraq", false),IRL -> CountryDetails("Ireland Republic of"),
      IMN -> CountryDetails("Isle of Man"),ISR -> CountryDetails("Israel"),ITA -> CountryDetails("Italy"),JAM -> CountryDetails("Jamaica"),JPN -> CountryDetails("Japan"),
      JEY -> CountryDetails("Jersey"),JOR -> CountryDetails("Jordan"),KAZ -> CountryDetails("Kazakhstan"),KEN -> CountryDetails("Kenya"),KIR -> CountryDetails("Kiribati"),
      KWT -> CountryDetails("Kuwait"),KGZ -> CountryDetails("Kyrgyzstan", false),LAO -> CountryDetails("Laos", false),LVA -> CountryDetails("Latvia"),LBN -> CountryDetails("Lebanon", false),
      LSO -> CountryDetails("Lesotho"),LBR -> CountryDetails("Liberia", false),LBY -> CountryDetails("Libya"),LIE -> CountryDetails("Liechtenstein"),LTU -> CountryDetails("Lithuania"),
      LUX -> CountryDetails("Luxembourg"),MAC -> CountryDetails("Macao SAR", false),MKD -> CountryDetails("Macedonia FYR"),MDG -> CountryDetails("Madagascar", false),MWI -> CountryDetails("Malawi"),
      MYS -> CountryDetails("Malaysia"),MDV -> CountryDetails("Maldives", false),MLI -> CountryDetails("Mali", false),MLT -> CountryDetails("Malta"),MHL -> CountryDetails("Marshall Islands", false),
      MTQ -> CountryDetails("Martinique"),MRT -> CountryDetails("Mauritania", false),MUS -> CountryDetails("Mauritius"),MYT -> CountryDetails("Mayotte", false),MEX -> CountryDetails("Mexico"),
      FSM -> CountryDetails("Micronesia", false),MDA -> CountryDetails("Moldova"),MCO -> CountryDetails("Monaco", false),MNG -> CountryDetails("Mongolia"),MNE -> CountryDetails("Montenegro"),
      MSR -> CountryDetails("Montserrat"),MAR -> CountryDetails("Morocco"),MOZ -> CountryDetails("Mozambique", false),NAM -> CountryDetails("Namibia"),NRU -> CountryDetails("Nauru", false),
      NPL -> CountryDetails("Nepal", false),NLD -> CountryDetails("Netherlands"),NCL -> CountryDetails("New Caledonia", false),NZL -> CountryDetails("New Zealand"),NIC -> CountryDetails("Nicaragua", false),
      NER -> CountryDetails("Niger", false),NGA -> CountryDetails("Nigeria"),NIU -> CountryDetails("Niue", false),NFK -> CountryDetails("Norfolk Island"),PRK -> CountryDetails("North Korea", false),
      MNP -> CountryDetails("Northern Mariana Islands", false),NOR -> CountryDetails("Norway"),OMN -> CountryDetails("Oman"),PAK -> CountryDetails("Pakistan"),PLW -> CountryDetails("Palau", false),
      PAN -> CountryDetails("Panama", false),PNG -> CountryDetails("Papua New Guinea"),PRY -> CountryDetails("Paraguay", false),PER -> CountryDetails("Peru", false),PHL -> CountryDetails("Philippines"),
      PCN -> CountryDetails("Pitcairn Island", false),POL -> CountryDetails("Poland"),PRT -> CountryDetails("Portugal"),PRI -> CountryDetails("Puerto Rico", false),QAT -> CountryDetails("Qatar"),
      REU -> CountryDetails("Reunion"),ROU -> CountryDetails("Romania"),RUS -> CountryDetails("Russian Federation"),RWA -> CountryDetails("Rwanda", false),SHN -> CountryDetails("St Helena and Dependencies", false),
      KNA -> CountryDetails("St Kitts and Nevis"),LCA -> CountryDetails("St Lucia", false),SPM -> CountryDetails("St Pierre and Miquelon", false),VCT -> CountryDetails("St Vincent and the Grenadines", false),
      BES2 -> CountryDetails("Saba"),WSM -> CountryDetails("Samoa", false),SMR -> CountryDetails("San Marino", false),STP -> CountryDetails("Sao Tome and Principe", false),SAU -> CountryDetails("Saudi Arabia"),
      SEN -> CountryDetails("Senegal", false),SRB -> CountryDetails("Serbia and Montenegro"),SYC -> CountryDetails("Seychelles", false),SLE -> CountryDetails("Sierra Leone"),SGP -> CountryDetails("Singapore"),
      BES3 -> CountryDetails("Sint Eustatius"),SXM -> CountryDetails("Sint Maarten Dutch part"),SVK -> CountryDetails("Slovak Republic"),SVN -> CountryDetails("Slovenia"),
      SLB -> CountryDetails("Solomon Islands"),SOM -> CountryDetails("Somalia", false),ZAF -> CountryDetails("South Africa"),KOR -> CountryDetails("South Korea"),SSD -> CountryDetails("South Sudan", false),
      ESP -> CountryDetails("Spain"),LKA -> CountryDetails("Sri Lanka"),SDN -> CountryDetails("Sudan"),SUR -> CountryDetails("Suriname", false),SJM -> CountryDetails("Svalbard and Jan Mayen Islands", false),
      SWZ -> CountryDetails("Swaziland"),SWE -> CountryDetails("Sweden"), CHE -> CountryDetails("Switzerland"),SYR -> CountryDetails("Syria", false),TWN -> CountryDetails("Taiwan"),
      TJK -> CountryDetails("Tajikistan"),TZA -> CountryDetails("Tanzania", false),THA -> CountryDetails("Thailand"),TLS -> CountryDetails("Timor-Leste", false),TGO -> CountryDetails("Togo", false),
      TKL -> CountryDetails("Tokelau", false),TON -> CountryDetails("Tonga", false),TTO -> CountryDetails("Trinidad and Tobago"),TUN -> CountryDetails("Tunisia"),TUR -> CountryDetails("Turkey"),
      TKM -> CountryDetails("Turkmenistan"),TCA -> CountryDetails("Turks and Caicos Islands", false),TUV -> CountryDetails("Tuvalu"),UGA -> CountryDetails("Uganda"),UKR -> CountryDetails("Ukraine"),
      ARE -> CountryDetails("United Arab Emirates", false),GBR -> CountryDetails("United Kingdom", false),USA -> CountryDetails("United States of America"),VIR -> CountryDetails("United States Virgin Islands", false),
      URY -> CountryDetails("Uruguay", false),UZB -> CountryDetails("Uzbekistan"),VUT -> CountryDetails("Vanuatu", false),VAT -> CountryDetails("Vatican", false),VEN -> CountryDetails("Venezuela"),
      VNM -> CountryDetails("Vietnam"),WLF -> CountryDetails("Wallis and Futuna Islands", false),YEM -> CountryDetails("Yemen", false),ZMB -> CountryDetails("Zambia"),ZWE -> CountryDetails("Zimbabwe"),
      ZZZ -> CountryDetails("None of the above", false)
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
