import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.model.HttpRequest

import scala.util.{Failure, Success}
import scalax.collection.GraphEdge.DiEdge
import scalax.collection.mutable.Graph
import scalax.collection.immutable
import scalax.collection.GraphPredef._
import scalax.collection.edge.LDiEdge
import scalax.collection.edge.Implicits._

case class StartCrawling(path:String)

object Crawler {
  type Webmap = immutable.Graph[String, DiEdge]
  def props(httpClient: HttpClient, parser:Parser): Props = {
    Props(classOf[Crawler], httpClient, parser)
  }
}

class Crawler(httpClient: HttpClient, parser: Parser) extends Actor {

  case class Crawl(path: String)
  case class ProcessLinks(from:String, links: Seq[LinkType])
  case object Finished

  implicit val system = context.system
  implicit val dispatcher = context.system.dispatcher

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
      httpClient.queue(path).onComplete {

        case Success(html) =>
          val links = parser.parse(html, path)

          // access mutable field in main actor thread
          myself ! ProcessLinks(path, links)

        case Failure(ex) =>
          System.err.println("Exception while parsing " + ex)
      }

      inProgressCount += 1

    case ProcessLinks(from, links) =>

      links.foreach{ link =>
        if (!link.isExternal && !graph.nodes.contains(link.path)) {
          self ! Crawl(link.path)
        }
        graph.add( from ~> link.path)
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
