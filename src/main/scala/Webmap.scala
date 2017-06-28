import java.net.URL

import akka.actor.{ActorSystem, Props}

import scala.util.{Failure, Success, Try}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._


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
    implicit val system = ActorSystem()

    val httpClient = new AkkaHttpClient(url.getHost)
    val parser = new JSoupParser


    val crawlerRef = system.actorOf(Crawler.props(httpClient, parser))

    implicit val timeout:Timeout = 1 minute
    implicit val dispatcher = system.dispatcher

    crawlerRef.ask(StartCrawling(url.getPath)).onComplete {
      case Success(graph) =>
        println("DONE")
        println(graph)
        system.terminate()

      case Failure(ex) => println("Failed with exception", ex)
        system.terminate()

    }
  }
}
