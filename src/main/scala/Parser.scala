import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model._

trait Parser {
  def parse(html:String): Seq[String]
}

class JSoupParser extends Parser {

  val jsoup = JsoupBrowser

  override def parse(html: String): Seq[String] = {
      Nil
  }
}
