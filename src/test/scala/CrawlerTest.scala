import Crawler.Webmap
import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentType, HttpEntity, HttpRequest, Uri}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalamock.scalatest.MockFactory
import org.scalatest._

import scala.concurrent.Future
import scala.concurrent.duration._
import scalax.collection.immutable.Graph

/**
  * Created by m4ks on 28/06/2017.
  */
class CrawlerTest extends TestKit(ActorSystem("MySpec")) with ImplicitSender with FlatSpecLike with Matchers
    with BeforeAndAfterAll with MockFactory {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "Crawler" should "ask base url and return graph" in {

      val httpClient = mock[HttpClient]
      val parser = stub[Parser]

      val crawler = system.actorOf(Crawler.props(httpClient, parser))

    (httpClient.queue _) expects ("/") onCall {
      path:String => Future.successful("")
    }
    (parser.parse _) when(*, *) returns(Seq())

    crawler ! StartCrawling("/")

    within(1 second) {
      val webmap = expectMsgClass(classOf[Webmap])
      assert(webmap.nodes.contains("/"))
    }
  }

  it should "ask base url and forward http responses to parser" in {

    val httpClient = stub[HttpClient]
    val parser = mock[Parser]
    val input = "Magic string"

    val crawler = system.actorOf(Crawler.props(httpClient, parser))

    (httpClient.queue _) when(*) returns(Future.successful(input))

    (parser parse (_,_)) expects(input, *) returns(Seq())

    crawler ! StartCrawling("/")

    within(1 second) {
      val webmap = expectMsgClass(classOf[Webmap])
    }
  }

  it should "issue more http request for urls not already in graph" in {

    val httpClient = mock[HttpClient]
    val parser = mock[Parser]

    val crawler = system.actorOf(Crawler.props(httpClient, parser))


    // / links to /a /b and /c
    (httpClient.queue _) expects("/") once() returns(Future.successful("/_content"))
    (parser parse (_,_)) expects("/_content", *) returns(Seq(InternalLink("/a"), InternalLink("/b"), InternalLink("/c")))

    // /a links to /b and /c
    (httpClient.queue _) expects("/a") once() returns(Future.successful("/a_content"))
    (parser parse (_,_)) expects("/a_content", *) returns(Seq(InternalLink("/b"), InternalLink("/c")))

    // /b links to /a and c
    (httpClient.queue _) expects("/b") once() returns(Future.successful("/b_content"))
    (parser parse (_,_)) expects("/b_content", *) returns(Seq(InternalLink("/a"), InternalLink("/c")))

    // /c links to nowhere
    (httpClient.queue _) expects("/c") once() returns(Future.successful("/c_content"))
    (parser parse (_,_)) expects("/c_content", *) returns(Seq())

    crawler ! StartCrawling("/")

    within(1 second) {
      val webmap = expectMsgClass(classOf[Webmap])
    }
  }
}
