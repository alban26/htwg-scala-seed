package de.htwg.se.malefiz.controller.Instructions

import de.htwg.se.malefiz.controller
import de.htwg.se.malefiz.controller.GameStates.Roll
import de.htwg.se.malefiz.controller.{InstructionTrait, Request}

object ISetup extends InstructionTrait {

  val setup1: Handler0 = {
    case Some(Request(x,y,z)) if x.contains("start") || z.gameBoard.players.length == 4 => z.playersTurn = z.gameBoard.players(0)
      Some(controller.Request(x,y,z))
  }

  val setup2: Handler1 = {
    case Some(Request(x,y,z)) => y.nextState(Roll(z))
      s"Lieber ${z.playersTurn} du bist als erstes dran. Drücke eine beliebige Taste um zu würfeln!"
  }

  val setup3: Handler1 = {
    case Some(Request(x, y, z)) => z.createPlayer(x(1))
      s"Spieler ${x(1)} wurde erfolgreich angelegt."
  }

  val setupErr: Handler1 = {
    case None => "Einlesen des Spielers nicht erfolgt."
  }

  val setup = setup1 andThen setup2 orElse setup3 andThen log orElse (setupErr andThen log)
}