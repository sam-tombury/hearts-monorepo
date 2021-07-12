package uk.co.sgjbryan.hearts

import uk.co.sgjbryan.hearts.card.Deck
import zio._
import zio.duration._
import zio.json._

import zhttp.http._
import zhttp.service._
import zhttp.http.Method._
import zhttp.socket._
import zio.stream.ZStream

object Main extends App {

  private val ping = Socket.open { _ =>
    ZStream
      .repeat(WebSocketFrame.ping)
      .schedule(Schedule.spaced(1.second))
  }

  private val cardEmit = Socket.open { _ =>
    ZStream
      .fromIterable(Deck.standard)
      .schedule(Schedule.spaced(1.second))
      .map {
        _.toJson
      }
      .map {
        WebSocketFrame.Text(_)
      }
  }

  private val wsEcho = Socket.collect { case WebSocketFrame.Text(text) =>
    ZStream
      .repeat(WebSocketFrame.Text(text))
      .schedule(Schedule.spaced(1.second))
      .take(3)
  }

  val app = Http.collect {
    case GET -> Root / "hello" => Response.text("Hello World!")
    case GET -> Root / "cards" =>
      Response.jsonString(Deck.standard.toJson)
    case GET -> Root / "ws" =>
      Response.socket(ping ++ wsEcho ++ cardEmit)
  }

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    Server.start(8080, app.silent).exitCode

}
