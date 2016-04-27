package uk.gov.hmrc.selfassessmentapi.definition

import play.api.libs.json._

object JsonFormatters {

  implicit val formatAPIStatus = EnumJson.enumFormat(APIStatus)
  implicit val formatAuthType = EnumJson.enumFormat(AuthType)
  implicit val formatHttpMethod = EnumJson.enumFormat(HttpMethod)
  implicit val formatResourceThrottlingTier = EnumJson.enumFormat(ResourceThrottlingTier)

  implicit val formatParameter = Json.format[Parameter]
  implicit val formatEndpoint = Json.format[Endpoint]
  implicit val formatAPIVersion = Json.format[APIVersion]
  implicit val formatAPIDefinition = Json.format[APIDefinition]

}

object EnumJson {

  def enumReads[E <: Enumeration](enum: E): Reads[E#Value] = new Reads[E#Value] {
    def reads(json: JsValue): JsResult[E#Value] = json match {
      case JsString(s) => {
        try {
          JsSuccess(enum.withName(s))
        } catch {
          case _: NoSuchElementException =>
            JsError(s"Enumeration expected of type: '${enum.getClass}', but it does not contain '$s'")
        }
      }
      case _ => JsError("String value expected")
    }
  }

  implicit def enumWrites[E <: Enumeration]: Writes[E#Value] = new Writes[E#Value] {
    def writes(v: E#Value): JsValue = JsString(v.toString)
  }

  implicit def enumFormat[E <: Enumeration](enum: E): Format[E#Value] = {
    Format(enumReads(enum), enumWrites)
  }

}
