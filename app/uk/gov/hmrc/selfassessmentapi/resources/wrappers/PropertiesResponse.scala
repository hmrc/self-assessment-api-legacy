package uk.gov.hmrc.selfassessmentapi.resources.wrappers

import play.api.libs.json.JsValue
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.http.HttpResponse

class PropertiesResponse(underlying: HttpResponse) {

  val status: Int = underlying.status

  def json: JsValue = underlying.json

  def createLocationHeader(nino: Nino): String = s"/self-assessment/ni/$nino/uk-properties"

}


object PropertiesResponse {
  def apply(response: HttpResponse): PropertiesResponse = new PropertiesResponse(response)
}