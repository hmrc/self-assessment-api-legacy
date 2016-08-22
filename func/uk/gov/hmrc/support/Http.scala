package uk.gov.hmrc.support

import play.api.Play.current
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.libs.ws.{WS, WSRequestHolder, WSResponse}
import play.api.mvc.Results
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.http.ws.WSHttpResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, Future}

object Http {

  def get(url: String)(implicit hc: HeaderCarrier, timeout: FiniteDuration): HttpResponse = perform(url) { request =>
    request.get()
  }

  def post[A](url: String, body: A, headers: Seq[(String, String)] = Seq.empty)
             (implicit writes: Writes[A], hc: HeaderCarrier, timeout: FiniteDuration): HttpResponse = perform(url) { request =>
    request.post(Json.toJson(body))
  }

  def postJson(url: String, body: JsValue, headers: Seq[(String, String)] = Seq.empty)
              (implicit hc: HeaderCarrier, timeout: FiniteDuration): HttpResponse = perform(url) { request =>
    request.post(body)
  }

  def putJson(url: String, body: JsValue, headers: Seq[(String, String)] = Seq.empty)
             (implicit hc: HeaderCarrier, timeout: FiniteDuration): HttpResponse = perform(url) { request =>
    request.put(body)
  }

  def postEmpty(url: String)
               (implicit hc: HeaderCarrier, timeout: FiniteDuration): HttpResponse = perform(url) { request =>
    request.post(Results.EmptyContent())
  }

  def delete(url: String)
            (implicit hc: HeaderCarrier, timeout: FiniteDuration): HttpResponse = perform(url) { request =>
    request.delete()
  }

  private def perform(url: String)(fun: WSRequestHolder => Future[WSResponse])
                     (implicit hc: HeaderCarrier, timeout: FiniteDuration): WSHttpResponse =
    await(fun(WS.url(url).withHeaders(hc.headers: _*).withRequestTimeout(timeout.toMillis.toInt)).map(new WSHttpResponse(_)))

  private def await[A](future: Future[A])
                      (implicit timeout: FiniteDuration) = Await.result(future, timeout)

}
