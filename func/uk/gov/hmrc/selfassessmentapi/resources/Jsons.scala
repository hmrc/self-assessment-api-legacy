package uk.gov.hmrc.selfassessmentapi.resources

import org.joda.time.LocalDate
import play.api.libs.json.{JsValue, Json}

object Jsons {

  object Errors {

    private def error(error: (String, String)) = {
      s"""
         |    {
         |      "code": "${error._1}",
         |      "path": "${error._2}"
         |    }
         """.stripMargin
    }
    def invalidRequest(errors: (String, String)*): String = {
      s"""
         |{
         |  "code": "INVALID_REQUEST",
         |  "message": "Invalid request",
         |  "errors": [
         |    ${errors.map { error }.mkString(",")}
         |  ]
         |}
         """.stripMargin
    }
  }

  def selfEmployment(accPeriodStart: String = "2017-04-06", accPeriodEnd: String = "2018-04-05", accountingType: String = "CASH",
                     commencementDate: String = s"${LocalDate.now.minusDays(1)}"): JsValue = {
    Json.parse(
      s"""
         |{
         |  "accountingPeriod": {
         |    "start": "$accPeriodStart",
         |    "end": "$accPeriodEnd"
         |  },
         |  "accountingType": "$accountingType",
         |  "commencementDate": "${LocalDate.now.minusDays(1)}"
         |}
         """.stripMargin)
  }

}
