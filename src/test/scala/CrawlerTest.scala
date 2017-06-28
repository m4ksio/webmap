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

      val crawler = system.actorOf(Crawler.props(httpClient))

    (httpClient.queueRequest _) expects (where {
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

      val httpClient = mock[HttpClient]

      val crawler = system.actorOf(Crawler.props(httpClient))

    (httpClient.queueRequest _) expects(*) onCall {
      res:HttpRequest => Future.successful("Magic string")
    }

    crawler ! StartCrawling("/")

    within(1 second) {
      val webmap = expectMsgClass(classOf[Webmap])
      assert(webmap.nodes.contains("/"))
    }
  }
}
