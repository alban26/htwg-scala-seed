package de.htwg.se.malefiz.controller.controllerComponent.controllerBaseImpl

import de.htwg.se.malefiz.controller.controllerComponent.GameStates.GameState
import de.htwg.se.malefiz.controller.controllerComponent.State
import de.htwg.se.malefiz.controller.controllerComponent.Statements.Statements
import de.htwg.se.malefiz.model.gameBoardComponent.GameBoardInterface
import de.htwg.se.malefiz.model.playerComponent.Player
import de.htwg.se.malefiz.util.Command

class SetPlayerCommand(playerNumber: Int, playerFigure: Int, cellNumber: Int, controller: Controller) extends Command  {

  var memento: GameBoardInterface = controller.gameBoard
  var mPlayersTurn: Player = controller.playersTurn
  var mStatementStatus: Statements = controller.statementStatus
  var mS: State[GameState] = controller.state.state

  override def doStep(): Unit = {
    controller.gameBoard = controller.gameBoard.removeActualPlayerAndFigureFromCell(playerNumber, playerFigure)

    if (controller.getCellList(cellNumber).playerNumber != 0) {
      controller.gameBoard = controller.gameBoard.setPlayer(controller.getCellList(cellNumber).playerNumber,
        controller.gameBoard.getHomeNr(controller.getCellList(cellNumber).playerNumber,
          controller.getCellList(cellNumber).figureNumber))
      controller.gameBoard = controller.gameBoard.setFigure(controller.getCellList(cellNumber).figureNumber,
        controller.gameBoard.getHomeNr(controller.getCellList(cellNumber).playerNumber,
          controller.getCellList(cellNumber).figureNumber))
    }
    if (controller.getCellList(cellNumber).hasWall) {
      controller.removeWall(cellNumber)
    }
    controller.gameBoard = controller.gameBoard.setPlayer(playerNumber, cellNumber)
    controller.gameBoard = controller.gameBoard.setFigure(playerFigure, cellNumber)
  }

  override def undoStep(): Unit = {

    val new_memento = controller.gameBoard
    val new_mPlayersTurn = controller.playersTurn
    val new_mS = controller.state
    val new_mStatementStatus = controller.statementStatus

    controller.gameBoard = memento
    controller.state.state = mS
    controller.playersTurn = mPlayersTurn
    controller.statementStatus = mStatementStatus

    memento = new_memento
    mS = new_mS.state
    mPlayersTurn = new_mPlayersTurn
    mStatementStatus = new_mStatementStatus
  }

  override def redoStep(): Unit = doStep()
}
