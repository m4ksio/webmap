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
      val parser = mock[Parser]

      val crawler = system.actorOf(Crawler.props(httpClient, parser))

    (httpClient.queue _) expects (where {
      request:HttpRequest => {
        request.uri.equals(Uri("/"))
      }
    }) onCall {
      res:HttpRequest => Future.successful("")
    }

    crawler ! StartCrawling("/")

    within(1 second) {
      val webmap = expectMsgClass(classOf[Webmap])
      assert(webmap.nodes.contains("/"))
    }
  }

  it should "forward http responses to parser" in {

    val httpClient = stub[HttpClient]
    val parser = mock[Parser]
    val input = "Magic string"

    val crawler = system.actorOf(Crawler.props(httpClient, parser))

    (httpClient.queue _) when(*) returns(Future.successful(input))

    (parser parse _) expects(input)

    crawler ! StartCrawling("/")

    within(1 second) {
      val webmap = expectMsgClass(classOf[Webmap])
    }
  }

  it should "issue more http request for urls not already in graph" in {

    val httpClient = stub[HttpClient]
    val parser = stub[Parser]

    val crawler = system.actorOf(Crawler.props(httpClient, parser))

    (httpClient.queue _) when("/") returns(Future.successful("/_content"))
    (parser parse _) when("/_content") returns(Seq(""))

    crawler ! StartCrawling("/")

    within(1 second) {
      val webmap = expectMsgClass(classOf[Webmap])
    }
  }
}
