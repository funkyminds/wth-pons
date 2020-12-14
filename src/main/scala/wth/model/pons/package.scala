package wth.model
/**
 * Domain model. Comments contain definitions from PONS REST API documentation.
 */
package object pons {

  /**
   * @param lang - a key "lang", that defines the source language and therefore the language direction and
   */
  case class Response(lang: String, hits: Seq[Hit])

  /**
   * an object "hits", that contains the results for this language direction
   */
  case class Hit(`type`: String, opendict: Boolean, roms: Seq[Rom])

  /**
   * A rom contains a headword and linguistic data related to this headword. For each part of speech there is
   * one rom (roman numeral). For example "cut" may be a noun, adjective, interjection, transitive or intransitive
   * verb and has the roms I to V.
   *
   * @param headword - The headword is usually the word you would lookup in a printed dictionary.
   * @param headword_full - headword_full may include additional information, such as phonetics, gender, etc. .
   * @param wordclass
   */
  case class Rom(headword: String, headword_full: String, wordclass: Option[String], arabs: Seq[Arab])

  /**
   * An arab contains a header (arabic numeral) and stands for a specific meaning of the headword described in the rom.
   * For example, the "substantive"-rom of "cut" has 12 arabs.
   */
  case class Arab(header: String, translations: Seq[Translation])

  /**
   * A translation contains a source/target-pair (the actual translations).
   */
  case class Translation(source: String, target: String)
}
