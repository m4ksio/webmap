import java.io.PrintWriter
import java.net.URL

import Crawler.Webmap
import akka.actor.{ActorSystem, Props}

import scala.util.{Failure, Success, Try}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scalax.collection.Graph
import scalax.collection.GraphEdge.DiEdge
import scalax.collection.io.dot._


object Webmap {

  def main(args: Array[String]): Unit = {
    if (args.length != 1) {
      System.err.println("Missing starting URL parameter")
      return
    }
    Try(new URL(args.head)) match {
      case Success(url) => crawl(url)
      case Failure(ex) => System.err.println("Invalid starting URL")
    }
  }

  def crawl(url: URL): Unit = {

    val baseUrl = new URL(url, "/")

    implicit val system = ActorSystem()

    val httpClient = new AkkaHttpClient(url.getHost)
    val parser = new JSoupParser(baseUrl)


    val crawlerRef = system.actorOf(Crawler.props(httpClient, parser))

    implicit val timeout:Timeout = 1 minute
    implicit val dispatcher = system.dispatcher

    crawlerRef.ask(StartCrawling(url.getPath)).mapTo[Webmap].onComplete {
      case Success(graph) =>
        toDot(graph, baseUrl.getHost)
        system.terminate()

      case Failure(ex) => println("Failed with exception", ex)
        system.terminate()

    }
  }

  def toDot(graph: Webmap, baseHost:String): Unit = {

    import implicits._

    val dotRoot = DotRootGraph (
      directed = true,
      id        = Some("Webmap")
    )

    def edgeTransformer(innerEdge: Graph[String,DiEdge]#EdgeT): Option[(DotGraph,DotEdgeStmt)] = {
      val edge = innerEdge.edge
      Some(dotRoot,
        DotEdgeStmt(NodeId(edge.from.toString),
          NodeId(edge.to.toString),
          Nil))
    }

    var dot = graph.toDot(dotRoot, edgeTransformer)

    new PrintWriter("graph.dot") { write(dot); close }
  }
}
