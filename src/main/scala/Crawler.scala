import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.model.HttpRequest

import scalax.collection.GraphEdge.DiEdge
import scalax.collection.mutable.Graph
import scalax.collection.immutable
import scalax.collection.GraphPredef._
import scalax.collection.edge.LDiEdge
import scalax.collection.edge.Implicits._

sealed trait LinkType

case class StartCrawling(path:String)


object Crawler {
  type Webmap = immutable.Graph[String, DiEdge]
  def props(httpClient: HttpClient, parser:Parser): Props = {
    Props(classOf[Crawler], httpClient, parser)
  }
}

class Crawler(httpClient: HttpClient, parser: Parser) extends Actor {

  case class Crawl(path: String)
  case class ProcessLinks(links: Seq[String])
  case object Finished

  implicit val system = context.system
  implicit val dispatcher = context.system.dispatcher

//  val httpClient = new HttpClient(host)
  var inProgressCount = 0
  var startSender:ActorRef = _

  val graph = Graph.empty[String, DiEdge]


  override def receive: Receive = {

    case StartCrawling(path) =>
      startSender = sender()
      self ! Crawl(path)
      graph.add(path)

    case Crawl(path) =>
      val myself = self
      httpClient.queue(path).map(html => {
        val links = parser.parse(html)

        // access mutable field in main actor thread
        myself ! ProcessLinks(links)


      })
      inProgressCount += 1

    case ProcessLinks(links) =>

      links.filterNot(graph.nodes.contains(_)).foreach { path =>
        graph.add(path)
        self ! Crawl(path)
      }

      self ! Finished


    case Finished =>
      inProgressCount -= 1

      // simple reference counting, but works in single-threaded actors
      if (inProgressCount == 0) {
        startSender ! immutable.Graph.empty ++ graph
      }
  }
}
