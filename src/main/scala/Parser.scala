import java.net.URL

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupElement
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model._

trait Parser {
  def parse(html:String, path:String): Seq[String]
}

class JSoupParser(baseUrl:URL) extends Parser {

  val jsoup = JsoupBrowser()

  override def parse(html: String, path:String): Seq[String] = {

    val document = jsoup.parseString(html)

    val anchors = document >> elementList("a")

    val basePath = new URL(baseUrl, path)

    val links = anchors
        .filter(_.hasAttr("href"))
        .map(_.attr("href").trim)
        .filterNot(href => href.isEmpty)
        .filterNot(href => href.startsWith("#"))
        .filterNot(href => href.startsWith("http://"))
        .filterNot(href => href.startsWith("https://"))
        .map { href => {
          new URL(basePath, href).getFile
        }

      }

    links
  }

}
