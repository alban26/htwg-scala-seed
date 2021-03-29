package de.htwg.se.malefiz.model.gameBoardComponent.gameBoardBaseImpl

import de.htwg.se.malefiz.Malefiz._
import de.htwg.se.malefiz.controller.controllerComponent.Statements.{Statements, addPlayer}
import de.htwg.se.malefiz.model.gameBoardComponent.GameBoardInterface
import de.htwg.se.malefiz.model.playerComponent.Player

import scala.collection.mutable.Map

case class GameBoard(cellList: List[Cell],
                     players: List[Player],
                     gameBoardGraph: Map[Int, Set[Int]],
                     possibleCells: Set[Int] = Set.empty,
                     dicedNumber: Option[Int],
                     playersTurn: Option[Player],
                     selectedFigure: Option[(Int, Int)],
                     stateNumber: Option[Int],
                     statementStatus: Option[Statements]
                    ) extends GameBoardInterface {

  def this() = this(
    Creator().getCellList(cellConfigFile),
    List.empty,
    Creator().getCellGraph(cellLinksFile),
    Set.empty,
    Option.empty,
    None,
    None,
    None,
    Option(addPlayer)
  )


  override def returnGameBoardAsString(): String = {
    val string = new StringBuilder("Malefiz-GameBoard\n\n")
    string.append("Players: ")
    players.map(player => string.append(player.toString + " / "))
    string.append("\n" + "Playersturn: " + playersTurn.getOrElse("No registered players").toString + "\n")
    string.append("Dice: " + dicedNumber.getOrElse("Dice is not rolled yet").toString + "\n")
    string.append("Status: " + statementStatus.get.toString + "\n")
    string.toString()
  }

  override def execute(callback: Int => GameBoard, y: Int): GameBoard = callback(y)

  override def setPlayersTurn(player: Option[Player]): GameBoard = copy(playersTurn = player)

  override def setSelectedFigure(playerNumber: Int, figureNumber: Int): GameBoard =
    copy(selectedFigure = Option(playerNumber, figureNumber))

  override def setStateNumber(stateNumber: Int): GameBoard = copy(stateNumber = Option(stateNumber))

  override def setStatementStatus(statement: Statements): GameBoard = copy(statementStatus = Option(statement))

  override def setPossibleCell(possibleCells: Set[Int]): GameBoard = copy(possibleCells = possibleCells)

  override def rollDice(): GameBoard = copy(dicedNumber = Dice().rollDice)

  override def setDicedNumber(dicedNumber: Int): GameBoard = copy(dicedNumber = Option(dicedNumber))

  override def clearPossibleCells: GameBoard = copy(possibleCells = Set.empty)

  override def getPossibleCells(startCell: Int, diceNumber: Int): GameBoard = {

    var found: Set[Int] = Set[Int]()
    var needed: Set[Int] = Set[Int]()

    def recurse(currentCell: Int, diceNumber: Int): Unit = {
      if (diceNumber == 0 || cellList(currentCell).hasWall && diceNumber == 0)
        needed += currentCell
      if (diceNumber != 0 && cellList(currentCell).hasWall)
        return
      found += currentCell
      gameBoardGraph(currentCell).foreach(nextCell =>
        if (!found.contains(nextCell) && diceNumber != 0)
          recurse(nextCell, diceNumber - 1))
    }

    recurse(startCell, diceNumber)
    copy(possibleCells = needed)

  }

  override def setPosiesCellTrue(possibleCells: List[Int]): GameBoard =
    copy(setPossibleCell1True(cellList.length - 1, possibleCells, cellList))

  override def setPossibleCell1True(cellListLength: Int, possibleCells: List[Int], cellList: List[Cell]): List[Cell] =
    if (cellListLength == -1)
      cellList
    else if (possibleCells contains cellList(cellListLength).cellNumber)
      setPossibleCell1True(cellListLength - 1, possibleCells, cellList.updated(cellList(cellListLength).cellNumber,
        setPossibleCellTrue(cellListLength)))
    else
      setPossibleCell1True(cellListLength - 1, possibleCells, cellList)

  override def setPossibleCellTrue(cellNumber: Int): Cell = cellList(cellNumber).copy(possibleCells = true)

  override def setPosiesCellFalse(possibleCells: List[Int]): GameBoard =
    copy(setPossibleCell1False(cellList.length - 1, possibleCells, cellList))

  override def setPossibleCell1False(cellListLength: Int, possibleCells: List[Int], cellList: List[Cell]): List[Cell] =
    if (cellListLength == -1)
      cellList
    else if (possibleCells contains cellList(cellListLength).cellNumber)
      setPossibleCell1False(cellListLength - 1, possibleCells, cellList.updated(cellList(cellListLength).cellNumber, setPossibleCellFalse(cellListLength)))
    else
      setPossibleCell1False(cellListLength - 1, possibleCells, cellList)

  override def setPossibleCellFalse(cellNumber: Int): Cell = cellList(cellNumber).copy(possibleCells = false)

  override def removeActualPlayerAndFigureFromCell(playerNumber: Int, figureNumber: Int): GameBoard = {
    val cell = getPlayerFigure(playerNumber, figureNumber)
    copy(cellList.updated(cell, removePlayerFigureOnCell(cell)))
    copy(cellList.updated(cell, removePlayerOnCell(cell)))
  }

  override def removePlayerFigureOnCell(cellNumber: Int): Cell = cellList(cellNumber).copy(figureNumber = 0)

  override def removePlayerOnCell(cellNumber: Int): Cell = cellList(cellNumber).copy(playerNumber = 0)

  override def setWall(cellNumber: Int): GameBoard = copy(updateListWall(cellNumber))

  override def updateListWall(cellNumber: Int): List[Cell] =
    if (cellNumber >= 42 && !cellList(cellNumber).hasWall)
      cellList.updated(cellNumber, placeWall(cellNumber))
    else
      cellList

  override def placeWall(cellNumber: Int): Cell = cellList(cellNumber).copy(hasWall = true)

  override def removeWall(cellNumber: Int): GameBoard = copy(removeListWall(cellNumber))

  override def removeListWall(cellNumber: Int): List[Cell] = cellList.updated(cellNumber, setHasWallFalse(cellNumber))

  override def setHasWallFalse(cellNumber: Int): Cell = cellList(cellNumber).copy(hasWall = false)

  override def createPlayer(text: String): GameBoard = copy(players = players :+ Player(players.length + 1, text))

  override def nextPlayer(playerList: List[Player], playerNumber: Int): Option[Player] =
    if (playerNumber == playerList.length - 1)
      Option(playerList.head)
    else
      Option(playerList(playerNumber + 1))

  override def setFigure(figureNumber: Int, cellNumber: Int): GameBoard = {
    copy(cellList.updated(cellNumber, setPlayerFigureOnCell(figureNumber, cellNumber)))
  }

  override def setPlayerFigureOnCell(figureNumber: Int, cellNumber: Int): Cell =
    cellList(cellNumber).copy(figureNumber = figureNumber)

  override def getPlayerFigure(playerNumber: Int, figureNumber: Int): Int = {
    val location = cellList.filter(cell => cell.playerNumber == playerNumber && cell.figureNumber == figureNumber)
    val cellNumber = location.head.cellNumber
    cellNumber
  }

  override def getHomeNr(playerNumber: Int, figureNumber: Int): Int =
    if (playerNumber == 1 && figureNumber == 1)
      0
    else
      (playerNumber - 1) * 5 + figureNumber - 1

  override def setPlayer(playerNumber: Int, cellNumber: Int): GameBoard =
    copy(cellList.updated(cellNumber, setPlayerOnCell(playerNumber, cellNumber)))

  override def setPlayerOnCell(playerNumber: Int, cellNumber: Int): Cell =
    cellList(cellNumber).copy(playerNumber = playerNumber)

  override def setPosiesTrue(cellNumber: Int): GameBoard =
    copy(setPossibleFiguresTrue(cellList.length - 1, cellNumber, cellList))


  override def setPossibleFiguresTrue(cellListLength: Int, cellNumber: Int, cellList: List[Cell]): List[Cell] =
    if (cellListLength == -1)
      cellList
    else if (cellList(cellListLength).playerNumber == cellNumber)
      setPossibleFiguresTrue(
        cellListLength - 1,
        cellNumber,
        cellList.updated(cellList(cellListLength).cellNumber, setPossibilitiesTrue(cellListLength))
      )
    else
      setPossibleFiguresTrue(cellListLength - 1, cellNumber, cellList)

  override def setPossibilitiesTrue(cellNumber: Int): Cell = cellList(cellNumber).copy(possibleFigures = true)


  override def setPosiesFalse(cellNumber: Int): GameBoard =
    copy(setPossibleFiguresFalse(cellList.length - 1, cellNumber, cellList))

  override def setPossibleFiguresFalse(cellListLength: Int, cellNumber: Int, cellList: List[Cell]): List[Cell] =
    if (cellListLength == -1)
      cellList
    else if (cellList(cellListLength).playerNumber == cellNumber)
      setPossibleFiguresFalse(cellListLength - 1, cellNumber, cellList.updated(cellList(cellListLength).cellNumber, setPossibilitiesFalse(cellListLength)))
    else
      setPossibleFiguresFalse(cellListLength - 1, cellNumber, cellList)

  override def setPossibilitiesFalse(cellNumber: Int): Cell = cellList(cellNumber).copy(possibleFigures = false)

}
