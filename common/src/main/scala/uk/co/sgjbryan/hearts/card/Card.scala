package uk.co.sgjbryan.hearts.card

import zio.json._

object Json {
  def enumCodec[A <: DisplayNameEnum](all: Set[A]): JsonCodec[A] = {
    val encoder: JsonEncoder[A] = JsonEncoder[String] contramap {
      _.displayName
    }
    val decoder: JsonDecoder[A] = JsonDecoder[String] map {
      _.toLowerCase
    } mapOrFail { str =>
      all.find(_.displayName.toLowerCase == str).toRight("Invalid enum")
    }
    JsonCodec(encoder, decoder)
  }
}

trait DisplayNameEnum {
  val displayName: String
}

sealed abstract class SuitColour(val displayName: String)
    extends DisplayNameEnum
case object Black extends SuitColour("black")
case object Red   extends SuitColour("red")
object SuitColour {
  val all: Set[SuitColour] = Set(Black, Red)
  implicit val codec       = Json.enumCodec[SuitColour](all)
}

sealed case class Suit private (
    displayName: String,
    colour: SuitColour,
    icon: String
) {
  override def toString: String = displayName
}

object Suit {
  object Clubs    extends Suit("Clubs", Black, "♣")
  object Diamonds extends Suit("Diamonds", Red, "♦")
  object Spades   extends Suit("Spades", Black, "♠")
  object Hearts   extends Suit("Hearts", Red, "♥")
  val all: Set[Suit] = Set(Clubs, Diamonds, Spades, Hearts)
  implicit val codec = DeriveJsonCodec.gen[Suit]
}

sealed case class CardValue private (
    value: Int,
    displayName: String,
    shortName: String
)

object CardValue {
  sealed abstract class NumericValue(value: Int)
      extends CardValue(value, value.toString, value.toString)
  sealed abstract class FaceValue(
      displayName: String,
      value: Int
  )            extends CardValue(value, displayName, displayName.head.toString)
  object Two   extends NumericValue(2)
  object Three extends NumericValue(3)
  object Four  extends NumericValue(4)
  object Five  extends NumericValue(5)
  object Six   extends NumericValue(6)
  object Seven extends NumericValue(7)
  object Eight extends NumericValue(8)
  object Nine  extends NumericValue(9)
  object Ten   extends NumericValue(10)
  object Jack  extends FaceValue("Jack", 11)
  object Queen extends FaceValue("Queen", 12)
  object King  extends FaceValue("King", 13)
  object Ace   extends FaceValue("Ace", 14)
  val all: Set[CardValue] = Set(
    Two,
    Three,
    Four,
    Five,
    Six,
    Seven,
    Eight,
    Nine,
    Ten,
    Jack,
    Queen,
    King,
    Ace
  )
  // implicit val encoder = DeriveJsonEncoder.gen[CardValue]
  // implicit val decoder: JsonDecoder[CardValue] = JsonDecoder[String] map {
  //   _.toLowerCase
  // } mapOrFail { str =>
  //   all.find(_.displayName.toLowerCase == str).toRight("Invalid card value")
  // }
  implicit val codec = DeriveJsonCodec.gen[CardValue]
}

case class Card(value: CardValue, suit: Suit) {
  override def toString: String = s"${value.displayName} of ${suit.displayName}"
}
object Card {
  implicit val codec = DeriveJsonCodec.gen[Card]
}

object Deck {
  val standard: Set[Card] = for {
    value <- CardValue.all
    suit  <- Suit.all
  } yield Card(value, suit)
  val specialised: Set[Card] = standard drop 1
}
