/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.support

import org.scalatestplus.play.BaseOneServerPerSuite
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.libs.ws.{EmptyBody, WSClient, WSRequest, WSResponse}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.http.ws.WSHttpResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


trait HttpComponent {
  self : BaseOneServerPerSuite =>

  object Http {

    lazy val client: WSClient = app.injector.instanceOf[WSClient]

    def get(url: String)(implicit hc: HeaderCarrier, timeout: FiniteDuration): HttpResponse = perform(url) { request =>
      request.withHttpHeaders(hc.extraHeaders:_*).get()
    }

    def post[A](url: String, body: A, headers: Seq[(String, String)] = Seq.empty)(
      implicit writes: Writes[A],
      hc: HeaderCarrier,
      timeout: FiniteDuration): HttpResponse = perform(url) { request =>
      request.withFollowRedirects(false).post(Json.toJson(body))
    }

    def postJson(url: String, body: JsValue, headers: Seq[(String, String)] = Seq.empty)(
      implicit hc: HeaderCarrier,
      timeout: FiniteDuration): HttpResponse = perform(url) { request =>
      request.withFollowRedirects(false).post(body)
    }

    def putJson(url: String, body: JsValue, headers: Seq[(String, String)] = Seq.empty)(
      implicit hc: HeaderCarrier,
      timeout: FiniteDuration): HttpResponse = perform(url) { request =>
      request.put(body)
    }

    def postEmpty(url: String)(implicit hc: HeaderCarrier, timeout: FiniteDuration): HttpResponse = perform(url) {
      request =>
        request.post(EmptyBody)
    }

    def delete(url: String)(implicit hc: HeaderCarrier, timeout: FiniteDuration): HttpResponse = perform(url) {
      request =>
        request.delete()
    }

    private def perform(url: String)(fun: WSRequest => Future[WSResponse])(implicit hc: HeaderCarrier,
                                                                           timeout: FiniteDuration): HttpResponse = {
      val extraHeaders = hc.otherHeaders ++ hc.extraHeaders
      await(fun(client.url(url).withHttpHeaders(extraHeaders:_*).withRequestTimeout(timeout)).map(WSHttpResponse(_)))
    }

    private def await[A](future: Future[A])(implicit timeout: FiniteDuration) = Await.result(future, timeout)

  }
}
