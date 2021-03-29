package de.htwg.se.malefiz.model.gameBoardComponent

import de.htwg.se.malefiz.controller.controllerComponent.Statements.Statements
import de.htwg.se.malefiz.model.gameBoardComponent.gameBoardBaseImpl.{Cell, GameBoard}
import de.htwg.se.malefiz.model.playerComponent.Player

import scala.collection.mutable.Map
import scala.util.Try

trait GameBoardInterface {

  def cellList: List[Cell]

  def players: List[Player]

  def gameBoardGraph: Map[Int, Set[Int]]

  def possibleCells: Set[Int]

  def dicedNumber: Option[Int]

  def playersTurn: Option[Player]

  def selectedFigure: Option[(Int, Int)]

  def stateNumber: Option[Int]

  def statementStatus: Option[Statements]


  def returnGameBoardAsString(): String

  def execute(callback: Int => GameBoard, y: Int): GameBoard

  def setPlayersTurn(player: Option[Player]): GameBoard

  def setSelectedFigure(playerNumber: Int, figureNumber: Int): GameBoard

  def setStateNumber(stateNumber: Int): GameBoard

  def setStatementStatus(statement: Statements): GameBoard

  def setPossibleCell(possibleCells: Set[Int]): GameBoard

  def rollDice(): GameBoard

  def setDicedNumber(dicedNumber: Int): GameBoard

  def clearPossibleCells: GameBoard

  def getPossibleCells(startCellNumber: Int, cube: Int): GameBoard


  def setPosiesCellTrue(listOfCellNumbers: List[Int]): GameBoard

  def setPossibleCell1True(cellListLength: Int, listOfCellNumbers: List[Int], list: List[Cell]): List[Cell]

  def setPossibleCellTrue(cellNumber: Int): Cell


  def setPosiesCellFalse(cellList: List[Int]): GameBoard

  def setPossibleCell1False(cellListLength: Int, listOfCellNumbers: List[Int], list: List[Cell]): List[Cell]

  def setPossibleCellFalse(cellNumber: Int): Cell


  def removeActualPlayerAndFigureFromCell(playerNumber: Int, figureNumber: Int): GameBoard

  def removePlayerFigureOnCell(cellNumber: Int): Cell

  def removePlayerOnCell(cellNumber: Int): Cell


  def setWall(cellNumber: Int): GameBoard

  def updateListWall(cellNumber: Int): List[Cell]

  def placeWall(cellNumber: Int): Cell


  def removeWall(cellNumber: Int): GameBoard

  def removeListWall(cellNumber: Int): List[Cell]

  def setHasWallFalse(cellNumber: Int): Cell


  def createPlayer(text: String): GameBoard

  def nextPlayer(list: List[Player], playerNumber: Int): Option[Player]

  def setFigure(figureNumber: Int, cellNumber: Int): GameBoard

  def setPlayerFigureOnCell(figureNumber: Int, cellNumber: Int): Cell

  def getPlayerFigure(playerNumber: Int, figureNumber: Int): Int

  def getHomeNr(playerNumber: Int, figureNumber: Int): Int

  def setPlayer(playerNumber: Int, cellNumber: Int): GameBoard

  def setPlayerOnCell(playerNumber: Int, cellNumber: Int): Cell


  def setPosiesTrue(cellNumber: Int): GameBoard

  def setPossibleFiguresTrue(cellListLength: Int, cellNumber: Int, list: List[Cell]): List[Cell]

  def setPossibilitiesTrue(cellNumber: Int): Cell


  def setPosiesFalse(cellNumber: Int): GameBoard

  def setPossibleFiguresFalse(cellListLength: Int, cellNumber: Int, list: List[Cell]): List[Cell]

  def setPossibilitiesFalse(cellNumber: Int): Cell

}

trait DiceInterface {

  def rollDice: Option[Int]

}

trait CreatorInterface {

  def readTextFile(filename: String): Try[Iterator[String]]

  def getCellList(inputFile: String): List[Cell]

  def getCellGraph(fileInput: String): Map[Int, Set[Int]]

  def updateCellGraph(key: Int, value: Int, map: Map[Int, Set[Int]]): Map[Int, Set[Int]]

}
