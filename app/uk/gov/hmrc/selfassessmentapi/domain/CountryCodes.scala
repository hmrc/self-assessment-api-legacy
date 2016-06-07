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

object CountryCodes extends Enumeration {
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
      (AFG, ("Afghanistan", false)),(ALB, ("Albania", false)),(DZA, ("Algeria", false)),(ASM, ("American Samoa", false)),(AND, ("Andorra", false)),
      (AGO, ("Angola", false)),(AIA, ("Anguilla", false)),(ATG, ("Antigua and Barbuda", true)),(ARG, ("Argentina", true)),(ARM, ("Armenia", true)),
      (ABW, ("Aruba", false)),(AUS, ("Australia", true)),(AUT, ("Austria", true)),(AZ, ("Azerbaijan", true)),(BHR, ("Bahrain", true)),(BGD, ("Bangladesh", true)),
      (BRB, ("Barbados", true)),(BLR, ("Belarus", true)),(BEL, ("Belgium", true)),(BLZ, ("Belize", true)),(BEN, ("Benin", false)),(BMU, ("Bermuda", false)),
      (BTN, ("Bhutan", false)),(BOL, ("Bolivia", true)),(BES1, ("Bonaire", true)),(BIH, ("Bosnia and Herzegovina", true)),(BWA, ("Botswana", true)),(BRA, ("Brazil", false)),
      (VGB, ("British Virgin Islands", true)),(BRN, ("Brunei Darussalam", true)),(BGR, ("Bulgaria", true)),(BFA, ("Burkino Faso", false)),
      (MMR, ("Burma (also known as Myanmar)", true)),(BDI, ("Burundi", false)),(KHM, ("Cambodia", false)),(CMR, ("Cameroon", false)),(CAN, ("Canada", true)),
      (CPV, ("Cape Verde", false)),(CYM, ("Cayman Islands", true)),(CAF, ("Central African Republic", false)),(TCD, ("Chad", false)),(CHL, ("Chile", true)), (CHN, ("China", true)),
      (CXR, ("Christmas Island", true)),(CCK, ("Cocos (Keeling) Islands", true)),(COL, ("Colombia", false)),(COM, ("Comoros", false)),(COG, ("Congo", false)),
      (COK, ("Cook Islands", false)),(CRI, ("Costa Rica", false)),(CIV, ("Côte d’Ivoire", true)),(HRV, ("Croatia", true)),(CUW, ("Curaçao", true)),(CYP, ("Cyprus", true)),
      (CZE, ("Czech Republic", true)),(COD, ("Democratic Republic of the Congo (formerly Zaire)", false)),(DNK, ("Denmark", true)),(DJI, ("Djibouti", false)),(DMA, ("Dominica", false)),
      (DOM, ("Dominican Republic", false)),(ECU, ("Ecuador", false)),(EGY, ("Egypt", true)),(SLV, ("El Salvador", false)),(GNQ, ("Equatorial Guinea", false)),(ERI, ("Eritrea", false)),
      (EST, ("Estonia", true)),(ETH, ("Ethiopia", true)),(FLK, ("Falkland Islands", true)),(FRO, ("Faroe Islands", true)),(FJI, ("Fiji", true)),(FIN, ("Finland", true)),
      (FRA, ("France", true)),(GUF, ("French Guiana", true)),(PYF, ("French Polynesia", false)),(GAB, ("Gabon", false)),(GMB, ("Gambia", true)),(GEO, ("Georgia", true)),
      (DEU, ("Germany", true)),(GHA, ("Ghana", true)),(GIB, ("Gibraltar", false)),(GRC, ("Greece", true)),(GRL, ("Greenland", false)),(GRD, ("Grenada", true)),
      (GLP, ("Guadeloupe", true)),(GUM, ("Guam", false)),(GTM, ("Guatemala", false)),(GGY, ("Guernsey", true)),(GIN, ("Guinea", false)),(GNB, ("Guinea-Bissau", false)),
      (GUY, ("Guyana", true)),(HTI, ("Haiti", false)),(HND, ("Honduras", false)),(HKG, ("Hong Kong (SAR)", true)),(HUN, ("Hungary", true)),(ISL, ("Iceland", true)),
      (IND, ("India", true)),(IDN, ("Indonesia", true)),(IRN, ("Iran", false)),(IRQ, ("Iraq", false)),(IRL, ("Ireland (Republic of)", true)),(IMN, ("Isle of Man", true)),
      (ISR, ("Israel", true)),(ITA, ("Italy", true)),(JAM, ("Jamaica", true)),(JPN, ("Japan", true)),(JEY, ("Jersey", true)),(JOR, ("Jordan", true)),(KAZ, ("Kazakhstan", true)),
      (KEN, ("Kenya", true)),(KIR, ("Kiribati", true)),(KWT, ("Kuwait", true)),(KGZ, ("Kyrgyzstan", false)),(LAO, ("Laos", false)),(LVA, ("Latvia", true)),(LBN, ("Lebanon", false)),
      (LSO, ("Lesotho", true)),(LBR, ("Liberia", false)),(LBY, ("Libya", true)),(LIE, ("Liechtenstein", true)),(LTU, ("Lithuania", true)),(LUX, ("Luxembourg", true)),
      (MAC, ("Macao (SAR)", false)),(MKD, ("Macedonia (FYR)", true)),(MDG, ("Madagascar", false)),(MWI, ("Malawi", true)),(MYS, ("Malaysia", true)),(MDV, ("Maldives", false)),
      (MLI, ("Mali", false)),(MLT, ("Malta", true)),(MHL, ("Marshall Islands", false)),(MTQ, ("Martinique", true)),(MRT, ("Mauritania", false)),(MUS, ("Mauritius", true)),
      (MYT, ("Mayotte", false)),(MEX, ("Mexico", true)),(FSM, ("Micronesia", false)),(MDA, ("Moldova", true)),(MCO, ("Monaco", false)),(MNG, ("Mongolia", true)),
      (MNE, ("Montenegro", true)),(MSR, ("Montserrat", true)),(MAR, ("Morocco", true)),(MOZ, ("Mozambique", false)),(NAM, ("Namibia", true)),(NRU, ("Nauru", false)),
      (NPL, ("Nepal", false)),(NLD, ("Netherlands", true)),(NCL, ("New Caledonia", false)),(NZL, ("New Zealand", true)),(NIC, ("Nicaragua", false)),(NER, ("Niger", false)),
      (NGA, ("Nigeria", true)),(NIU, ("Niue", false)),(NFK, ("Norfolk Island", true)),(PRK, ("North Korea", false)),(MNP, ("Northern Mariana Islands", false)),(NOR, ("Norway", true)),
      (OMN, ("Oman", true)),(PAK, ("Pakistan", true)),(PLW, ("Palau", false)),(PAN, ("Panama", false)),(PNG, ("Papua New Guinea", true)),(PRY, ("Paraguay", false)),
      (PER, ("Peru", false)),(PHL, ("Philippines", true)),(PCN, ("Pitcairn Island", false)),(POL, ("Poland", true)),(PRT, ("Portugal", true)),(PRI, ("Puerto Rico", false)),
      (QAT, ("Qatar", true)),(REU, ("Reunion", true)),(ROU, ("Romania", true)),(RUS, ("Russian Federation", true)),(RWA, ("Rwanda", false)),
      (SHN, ("St Helena and Dependencies", false)),(KNA, ("St Kitts and Nevis", true)),(LCA, ("St Lucia", false)),(SPM, ("St Pierre and Miquelon", false)),
      (VCT, ("St Vincent and the Grenadines", false)),(BES2, ("Saba", true)),(WSM, ("Samoa", false)),(SMR, ("San Marino", false)),(STP, ("Sao Tome and Principe", false)),
      (SAU, ("Saudi Arabia", true)),(SEN, ("Senegal", false)),(SRB, ("Serbia and Montenegro", true)),(SYC, ("Seychelles", false)),(SLE, ("Sierra Leone", true)),
      (SGP, ("Singapore", true)),(BES3, ("Sint Eustatius", true)),(SXM, ("Sint Maarten (Dutch part)", true)),(SVK, ("Slovak Republic", true)),(SVN, ("Slovenia", true)),
      (SLB, ("Solomon Islands", true)),(SOM, ("Somalia", false)),(ZAF, ("South Africa", true)),(KOR, ("South Korea", true)),(SSD, ("South Sudan", false)),(ESP, ("Spain", true)),
      (LKA, ("Sri Lanka", true)),(SDN, ("Sudan", true)),(SUR, ("Suriname", false)),(SJM, ("Svalbard and Jan Mayen Islands", false)),(SWZ, ("Swaziland", true)),(SWE, ("Sweden", true)),
      (CHE, ("Switzerland", true)),(SYR, ("Syria", false)),(TWN, ("Taiwan", true)),(TJK, ("Tajikistan", true)),(TZA, ("Tanzania", false)),(THA, ("Thailand", true)),
      (TLS, ("Timor-Leste", false)),(TGO, ("Togo", false)),(TKL, ("Tokelau", false)),(TON, ("Tonga", false)),(TTO, ("Trinidad and Tobago", true)),(TUN, ("Tunisia", true)),
      (TUR, ("Turkey", true)),(TKM, ("Turkmenistan", true)),(TCA, ("Turks and Caicos Islands", false)),(TUV, ("Tuvalu", true)),(UGA, ("Uganda", true)),(UKR, ("Ukraine", true)),
      (ARE, ("United Arab Emirates", false)),(GBR, ("United Kingdom", false)),(USA, ("United States of America", true)),(VIR, ("United States Virgin Islands", false)),
      (URY, ("Uruguay", false)),(UZB, ("Uzbekistan", true)),(VUT, ("Vanuatu", false)),(VAT, ("Vatican", false)),(VEN, ("Venezuela", true)),(VNM, ("Vietnam", true)),
      (WLF, ("Wallis and Futuna Islands", false)),(YEM, ("Yemen", false)),(ZMB, ("Zambia", true)),(ZWE, ("Zimbabwe", true)),(ZZZ, ("None of the above", false))
    )
}
