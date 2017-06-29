import java.net.URL

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._

sealed trait LinkType {
  val path:String
  def isExternal: Boolean
}

case class InternalLink(path: String) extends LinkType {
  override def isExternal = false
}

case class ExternalLink(path: String) extends LinkType {
  override def isExternal = true
}

trait Parser {
  def parse(html:String, path:String): Seq[LinkType]
}

class JSoupParser(baseUrl:URL) extends Parser {

  val jsoup = JsoupBrowser()

  override def parse(html: String, path:String): Seq[LinkType] = {

    val document = jsoup.parseString(html)

    val anchors = document >> elementList("a")

    val basePath = new URL(baseUrl, path)

    anchors
      .filter(_.hasAttr("href"))
      .map(_.attr("href").trim)
      .filterNot(href => href.isEmpty)
      .filterNot(href => href.startsWith("#"))
      .map { href =>
        if (href.startsWith("http://") || href.startsWith("https://")) {
          ExternalLink(href)
        } else {
          InternalLink(new URL(basePath, href).getFile)
        }
      }
  }

}
