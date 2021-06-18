package de.htwg.se.malefiz.gameBoardModule.controller.controllerComponent.GameStates

import de.htwg.se.malefiz.gameBoardModule.controller.controllerComponent.Instructions.ISetup
import de.htwg.se.malefiz.gameBoardModule.controller.controllerComponent.{ControllerInterface, Request, State}

case class Setup(controller: ControllerInterface) extends State[GameState] {

  override def handle(input: String, gameState: GameState): Unit = ISetup.setup(Request(input.split(" ").toList, gameState, controller))

  override def toString: String = "4"

}
