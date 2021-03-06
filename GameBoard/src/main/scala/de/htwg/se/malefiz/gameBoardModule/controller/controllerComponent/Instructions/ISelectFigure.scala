package de.htwg.se.malefiz.gameBoardModule.controller.controllerComponent.Instructions

import de.htwg.se.malefiz.gameBoardModule.controller.controllerComponent.GameStates.SetFigure
import de.htwg.se.malefiz.gameBoardModule.controller.controllerComponent.Statements.{selectField, selectWrongFigure}
import de.htwg.se.malefiz.gameBoardModule.controller.controllerComponent.{InstructionTrait, Request, StatementRequest, Statements}

object ISelectFigure extends InstructionTrait {

  val select1: Handler0 = {
    case Request(inputList, gameState, controller)
      if inputList.head.toInt ==
        controller.gameBoard.playersTurn.get.playerNumber =>
      controller.calculatePath(
        controller.getFigurePosition(inputList.head.toInt, inputList(1).toInt),
        controller.gameBoard.dicedNumber.get
      )
      Request(inputList, gameState, controller)
  }

  val select2: Handler0 = {
    case Request(inputList, gameState, controller) =>
      controller.setSelectedFigure(inputList.head.toInt, inputList(1).toInt)
      Request(inputList, gameState, controller)
  }

  val select3: Handler0 = {
    case Request(inputList, gameState, controller) =>
      controller.setPossibleCellsTrueOrFalse(controller.gameBoard.possibleCells.toList)
      Request(inputList, gameState, controller)
  }

  val select4: Handler1 = {
    case Request(inputList, gameState, controller) =>
      gameState.nextState(SetFigure(controller))
      controller.setStatementStatus(selectField)
      Statements.value(StatementRequest(controller))
  }


  val select5: Handler0 = {
    case Request(inputList, gameState, controller)
      if inputList.head.toInt != controller.gameBoard.playersTurn.get.playerNumber =>
      Request(inputList, gameState, controller)
  }

  val select6: Handler1 = {
    case Request(inputList, gameState, controller) =>
      controller.setStatementStatus(selectWrongFigure)
      Statements.value(StatementRequest(controller))
  }

  val select: PartialFunction[Request, String] = (select1
    .andThen(select2)
    .andThen(select3)
    .andThen(select4)
    .andThen(log))
    .orElse(select5.andThen(select6).andThen(log))

}
