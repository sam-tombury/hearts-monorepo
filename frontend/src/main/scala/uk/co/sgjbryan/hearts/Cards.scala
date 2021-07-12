package uk.co.sgjbryan.hearts

import zio.json._
import uk.co.sgjbryan.hearts.card._
import com.raquo.laminar.api.L._
import io.laminext.fetch.Fetch
import io.laminext.syntax.core._
import io.laminext.websocket.zio._
import io.laminext.websocket.WebSocket

object Cards {
  val decoder = JsonDecoder[List[Card]]
  //TODO: sttp or http4s client instead?
  val serverCards = Fetch.get("/api/cards").text map { res =>
    decoder.decodeJson(res.data).getOrElse(List())
  }
  val selectedStyle = Seq(
    transform := "translateY(-0.5rem)",
    borderColor := "#666666"
  )

  def CardView(card: Card): Element = {
    val selected = Var(false)
    div(
      onClick --> selected.toggleObserver,
      color := (card.suit.colour match {
        case Black => "#666666"
        case Red   => "#ff3e00"
      }), //TODO: use tailwind
      className := card.suit.colour.toString.toLowerCase,
      className := "card",
      selected.signal.classSwitch(
        whenTrue = "selected",
        whenFalse = ""
      ),
      backgroundColor := "#f4f4f4",
      border := "1px solid #cccccc",
      width := "75px",
      height := "110px",
      borderRadius := "5px",
      margin := "5px",
      textAlign := "center",
      position := "relative",
      display := "inline-block",
      cursor := "pointer",
      span(
        position := "absolute",
        top := "10px",
        left := "10px",
        card.suit.icon
      ),
      span(
        position := "relative",
        top := "calc(50% - 1.25rem)",
        fontSize := "2em",
        card.value.shortName
      ),
      span(
        position := "absolute",
        bottom := "10px",
        right := "10px",
        card.suit.icon
      )
    )
  }

  val ws: WebSocket[Card, String] =
    WebSocket
      .path("/api/ws")
      .json[Card, String]
      .build()

  val hoverCard = new EventBus[Card]()

  val serverCardElements = serverCards map {
    _ map { card =>
      CardView(card).amend(
        onMouseOver --> { _ => hoverCard.emit(card) }
      )
    }
  }

  val view = div(
    h1("Raw"),
    CardView(Deck.specialised.head),
    h1("Server"),
    div(
      children <-- serverCardElements
    ),
    h1("Websocket"),
    ws.connect,
    div(
      child <-- (ws.received map CardView)
    ),
    h1("Hovered"),
    div(
      child <-- (hoverCard.events map CardView)
    )
  )

}
