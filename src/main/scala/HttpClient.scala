import scala.util.{ Failure, Success }
import scala.concurrent.{ Future, Promise }

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._

import akka.stream.{ OverflowStrategy, QueueOfferResult }

import scala.concurrent.duration._

trait HttpClient {
  def queueRequest(request: HttpRequest): Future[HttpEntity.Strict]
}

class AkkaHttpClient(host: String)(implicit actorSystem: ActorSystem) extends HttpClient {

  // attributed to
  // http://kazuhiro.github.io/scala/akka/akka-http/akka-streams/2016/01/31/connection-pooling-with-akka-http-and-source-queue.html

  import actorSystem.dispatcher
  implicit val materializer = ActorMaterializer()

  val QueueSize = 100

  private val poolClientFlow = Http().cachedHostConnectionPool[Promise[HttpEntity.Strict]](host)
  private val queue =
    Source.queue[(HttpRequest, Promise[HttpEntity.Strict])](QueueSize, OverflowStrategy.dropNew)
      .via(poolClientFlow)
      .toMat(Sink.foreach({
        case ((Success(resp), p)) => resp.entity.toStrict(2 second)
        case ((Failure(e), p))    => p.failure(e)
      }))(Keep.left)
      .run()


  override def queueRequest(request: HttpRequest): Future[HttpEntity.Strict] = {
    val responsePromise = Promise[HttpEntity.Strict]()
    queue.offer(request -> responsePromise).flatMap {
      case QueueOfferResult.Enqueued    => responsePromise.future
      case QueueOfferResult.Dropped     => Future.failed(new RuntimeException("Queue overflowed. Try again later."))
      case QueueOfferResult.Failure(ex) => Future.failed(ex)
      case QueueOfferResult.QueueClosed => Future.failed(new RuntimeException("Queue was closed (pool shut down) while running the request. Try again later."))
    }
  }
}
