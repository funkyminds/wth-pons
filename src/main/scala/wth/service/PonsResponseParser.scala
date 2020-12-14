package wth.service

import wth.model.pons._
import wth.service.ResponseParser.ResponseParser
import zio._
import zio.stream._

import scala.annotation.tailrec

object PonsResponseParser {
  val service: TaskLayer[ResponseParser[Seq[Response]]] = ZLayer.succeed(new PonsResponseParser)
}

class PonsResponseParser extends ResponseParser.Service[Seq[Response]] {
  self =>
  override def parse(phrase: String, raw: Seq[Response]): Task[Set[ResponseParser.Translation]] = {
    val translations = getTranslations(phrase, raw)

    ZStream
      .fromIterable(translations)
      .collect(translation => translation.source -> translation.target)
      .run(Sink.collectAllToSet)
  }

  private[service] def getTranslations(phrase: String, raw: Seq[Response]): Seq[Translation] =
    for {
      response <- raw
      hit <- response.hits
      rom <- hit.roms
      if rom.headword == phrase
      arab <- rom.arabs
      translation <- arab.translations
    } yield {
      val news =
        translation.source
          .replaceAll("""<span class="style"><acronym title="informal">inf</acronym></span>""", "")
          .cleanse("""<span class="collocator">""", "</span>", "(", ")")
          .replaceAll("<.*?>", "")
          .apostrophe

      val newt =
        translation.target
          .cleanse("""<span class="perf">[""", """]</span>""", includeMiddle = false)
          .cleanse("""<span class="genus">""", """</span>""", includeMiddle = false)
          .cleanse("""<span class="number">""", """</span>""", includeMiddle = false)
          .replaceAll("<.*?>", "")
          .apostrophe

      translation.copy(source = news, target = newt)
    }

  /**
   * Replace elements that are close to each other with given replacements.
   */
  private def cleanse(toCleanse: String,
                      toFindLeft: String,
                      toFindRight: String,
                      toReplaceLeft: String = "",
                      toReplaceRight: String = "",
                      includeMiddle: Boolean = true): String =
    if (toCleanse.contains(toFindLeft)) {
      @tailrec
      def helper(toCleanse: String, start: Int): String =
        if (start > 0) {
          val left = toCleanse.substring(0, start)
          val end = toCleanse.indexOf(toFindRight, left.length)
          val middle = if (includeMiddle) toCleanse.substring(start + toFindLeft.length, end) else ""
          val right = toCleanse.substring(end + toFindRight.length)
          val replace = s"$left$toReplaceLeft$middle$toReplaceRight$right"

          val nextStart = replace.indexOf(toFindLeft)
          helper(replace, nextStart)
        } else toCleanse

      helper(
        toCleanse,
        toCleanse.indexOf(toFindLeft)
      ).trim
        .replaceAll("  ", " ")
    } else toCleanse

  implicit class Ops(input: String) {
    def cleanse(toFindLeft: String,
                toFindRight: String,
                toReplaceLeft: String = "",
                toReplaceRight: String = "",
                includeMiddle: Boolean = true): String =
      self.cleanse(input, toFindLeft, toFindRight, toReplaceLeft, toReplaceRight, includeMiddle)

    def apostrophe: String = input.replace("&#39;", "'")
  }
}
