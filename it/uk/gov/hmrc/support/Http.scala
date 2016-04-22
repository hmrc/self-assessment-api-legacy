package uk.gov.hmrc.support

import play.api.Play.current
import play.api.libs.json.{Json, Writes}
import play.api.libs.ws.{WSResponse, WSRequestHolder, WS}
import play.api.mvc.Results
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.play.http.ws.WSHttpResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, Future}

object Http {

  def get(url: String)(implicit hc: HeaderCarrier): HttpResponse = perform(url) { request =>
    request.get()
  }

  def post[A](url: String, body: A, headers: Seq[(String, String)] = Seq.empty)(implicit writes: Writes[A], hc: HeaderCarrier): HttpResponse = perform(url) { request =>
    request.post(Json.toJson(body))
  }

  def postEmpty(url: String)(implicit hc: HeaderCarrier): HttpResponse = perform(url) { request =>
    request.post(Results.EmptyContent())
  }

  def delete(url: String)(implicit hc: HeaderCarrier): HttpResponse = perform(url) { request =>
    request.delete()
  }

  private def perform(url: String)( fun: WSRequestHolder => Future[WSResponse])(implicit hc: HeaderCarrier): WSHttpResponse =
    await(fun(WS.url(url).withHeaders(hc.headers: _*).withRequestTimeout(20000)).map(new WSHttpResponse(_)))

  private def await[A](future: Future[A]) = Await.result(future, Duration(5, SECONDS))

}
