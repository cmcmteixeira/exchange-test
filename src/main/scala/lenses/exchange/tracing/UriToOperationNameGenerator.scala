package lenses.exchange.tracing

import java.util.Locale

import kamon.http4s.NameGenerator
import org.http4s.Request

class UriToOperationNameGenerator extends NameGenerator {

  val normalizePattern                = """\$([^<]+)<[^>]+>""".r
  val integerRegEx                    = "(/)(\\d+)(/|$)".r
  private def freeStr(prefix: String) = s"(/$prefix/)(.*[^/])(/|$$)".r
  private def generifyUrl(path: String) =
    List(
      integerRegEx -> "$1:id$3",
    ).foldLeft(path)((path, reg) => {
      val (regex, pattern) = reg
      regex.replaceAllIn(path, pattern)
    })

  override def generateOperationName[F[_]](request: Request[F]): String = {
    val p = normalizePattern.replaceAllIn(generifyUrl(request.uri.path), "$1").dropWhile(_ == '/')
    val normalisedPath = {
      if (p.lastOption.exists(_ != '/')) s"$p/"
      else p
    }
    s"${request.method.name.toUpperCase(Locale.ENGLISH)} /${normalisedPath.toLowerCase}"
  }
  override def generateHttpClientOperationName[F[_]](request: Request[F]): String =
    s"${request.uri.authority.getOrElse("_")}"
}
