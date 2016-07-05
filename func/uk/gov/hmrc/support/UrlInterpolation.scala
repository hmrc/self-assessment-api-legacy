package uk.gov.hmrc.support

import scala.collection.mutable
import scala.util.matching.Regex

trait UrlInterpolation {

  def interpolated(path: String)(implicit urlPathVariables: mutable.Map[String, String]): String = {
    interpolate(interpolate(path, "sourceId"), "summaryId")
  }

  private def interpolate(path: String, pathVariable: String)(implicit pathVariablesValues: mutable.Map[String, String]): String = {
    pathVariablesValues.get(pathVariable) match {
      case Some(variableValue) => path.replace(s"%$pathVariable%", variableValue)
      case None => path
    }
  }

  def interpolated(path: Regex)(implicit urlPathVariables: mutable.Map[String, String]): String = {
    interpolate(interpolate(path.regex, "sourceId"), "summaryId")
  }

}
