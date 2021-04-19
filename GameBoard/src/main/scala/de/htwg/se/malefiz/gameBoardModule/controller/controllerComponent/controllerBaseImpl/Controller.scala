package de.htwg.se.malefiz.gameBoardModule.controller.controllerComponent.controllerBaseImpl

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.google.inject.{Guice, Inject, Injector}
import de.htwg.se.malefiz.gameBoardModule.GameBoardServerModule
import de.htwg.se.malefiz.gameBoardModule.controller.controllerComponent.GameStates._
import de.htwg.se.malefiz.gameBoardModule.controller.controllerComponent.Statements.Statements
import de.htwg.se.malefiz.gameBoardModule.controller.controllerComponent.{ControllerInterface, GameBoardChanged, Statements, Winner}
import de.htwg.se.malefiz.gameBoardModule.model.gameBoardComponent.GameBoardInterface
import de.htwg.se.malefiz.gameBoardModule.model.gameBoardComponent.gameBoardBaseImpl.{Cell, GameBoard, Player, Point}
import de.htwg.se.malefiz.gameBoardModule.util.UndoManager
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import play.api.libs.json._

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.swing.Publisher
import scala.util.{Failure, Success}

class Controller @Inject()(var gameBoard: GameBoardInterface) extends ControllerInterface with Publisher {

  val injector: Injector = Guice.createInjector(new GameBoardServerModule)
  //val fileIo: FileIOInterface = injector.instance[FileIOInterface]
  val mementoGameBoard: GameBoardInterface = gameBoard
  val state: GameState = GameState(this)
  val undoManager = new UndoManager

  implicit val system = ActorSystem(Behaviors.empty, "GameBoard")
  implicit val executionContext = system.executionContext


  override def gameBoardToString: Option[String] = gameBoard.buildCompleteBoard(gameBoard.cellList)

  override def resetGameBoard(): Unit = gameBoard = mementoGameBoard

  override def setGameBoard(gb: GameBoardInterface): Unit = this.gameBoard = gb

  override def undo(): Unit = {
    undoManager.undoStep()
    publish(new GameBoardChanged)
  }

  override def redo(): Unit = {
    undoManager.redoStep()
    publish(new GameBoardChanged)
  }

  override def weHaveAWinner(): Unit = publish(new Winner)

  override def rollCube: Option[Int] = {
    gameBoard = gameBoard.rollDice()
    gameBoard.dicedNumber
  }

  override def setDicedNumber(dicedNumber: Option[Int]): Unit = gameBoard = gameBoard.setDicedNumber(dicedNumber)

  override def createPlayer(name: String): Unit = {
    gameBoard = gameBoard.createPlayer(name)
    //publish(new GameBoardChanged)
  }

  override def nextPlayer(playerList: List[Option[Player]], playerNumber: Int): Option[Player] =
    gameBoard.nextPlayer(playerList, playerNumber)

  override def setPlayersTurn(player: Option[Player]): Unit = {
    gameBoard = gameBoard.setPlayersTurn(player)
    publish(new GameBoardChanged)
  }

  override def placePlayerFigure(playerNumber: Int, playerFigure: Int, cellNumber: Int): Unit = {
    undoManager.doStep(new SetPlayerCommand(playerNumber, playerFigure, cellNumber, this))
    publish(new GameBoardChanged)
  }

  override def setSelectedFigure(playerNumber: Int, figureNumber: Int): Unit = {
    gameBoard = gameBoard.setSelectedFigure(playerNumber, figureNumber)
    publish(new GameBoardChanged)
  }

  override def getFigurePosition(playerNumber: Int, figureNumber: Int): Int = {
    val position = gameBoard.getPlayerFigure(playerNumber, figureNumber)
    position
  }

  override def resetPossibleCells(): Unit = gameBoard = gameBoard.clearPossibleCells

  override def setStateNumber(stateNumber: Int): Unit = {
    gameBoard = gameBoard.setStateNumber(stateNumber)
    publish(new GameBoardChanged)
  }

  override def calculatePath(startCell: Int, diceNumber: Int): Unit = {
    gameBoard = gameBoard.getPossibleCells(startCell, diceNumber)
    publish(new GameBoardChanged)
  }

  override def removeActualPlayerAndFigureFromCell(playerNumber: Int, figureNumber: Int): Unit = {
    gameBoard = gameBoard.removeActualPlayerAndFigureFromCell(playerNumber, figureNumber)
    publish(new GameBoardChanged)
  }

  override def placeFigure(figureNumber: Int, cellNumber: Int): Unit = {
    gameBoard = gameBoard.setFigure(figureNumber, cellNumber)
    publish(new GameBoardChanged)
  }

  override def placePlayer(playerNumber: Int, cellNumber: Int): Unit = {
    gameBoard = gameBoard.setPlayer(playerNumber, cellNumber)
    publish(new GameBoardChanged)
  }

  override def placeOrRemoveWall(n: Int, boolean: Boolean): Unit = {
    boolean match {
      case true => undoManager.doStep(new SetWallCommand(n, this))
      case false => gameBoard = gameBoard.removeWall(n)
    }
    publish(new GameBoardChanged)
  }

  override def getGameState: GameState = this.state

  override def setStatementStatus(statement: Statements): Unit = {
    gameBoard = gameBoard.setStatementStatus(statement)
    publish(new GameBoardChanged)
  }

  override def setPossibleCells(possibleCells: Set[Int]): GameBoardInterface = gameBoard.setPossibleCell(possibleCells)

  override def setPossibleCellsTrueOrFalse(availableCells: List[Int]): Unit = {
    gameBoard = gameBoard.setPossibleCellsTrueOrFalse(availableCells, state.currentState.toString)
    publish(new GameBoardChanged)
  }

  override def setPossibleFiguresTrueOrFalse(playerNumber: Int): Unit = {
    gameBoard = gameBoard.setPossibleFiguresTrueOrFalse(playerNumber, state.currentState.toString)
    publish(new GameBoardChanged)
  }

  override def execute(input: String): Unit = state.run(input)

  override def checkInput(input: String): Either[String, String] = {
    if (state.currentState.toString == "4")
      if(input.split(" ").toList.size != 2)
        Left("Bitte Spieler in Form von : 'n Spielername' eintippen \n " +
          "und mit 'n start' dann starten!")
      else
        Right(input)
    else
      Right(input)
  }


  override def load(): Unit = {
    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "http://localhost:8081/json"))
      responseFuture.onComplete {
      case Success(value) => {
        val entityFuture: Future[String] = value.entity.toStrict(5.seconds).map(_.data.decodeString("UTF-8"))
        entityFuture.onComplete {
          case Success(result) => evalJson(result)
          case Failure(exception) => sys.error(exception.toString)
        }
      }
      case Failure(_) => sys.error("HttpResponse failure")
    }
  }

  def evalJson(result: String): Unit = {

    val newController = new Controller(loadGameBoardJson(result))
    val json: JsValue = Json.parse(result)

    implicit val playerReader: Reads[Player] = Json.reads[Player]

    val diceNumber: Int = (json \ "diceNumber").as[Int]
    val playersTurn: Player = (json \ "playersTurn").as[Player]
    val f1: Int = (json \ "selectedFigure1").as[Int]
    val f2: Int = (json \ "selectedFigure2").as[Int]
    val gameState: Int = (json \ "gameState").as[Int]

    newController.setDicedNumber(Some(diceNumber))
    newController.setPlayersTurn(
      newController.gameBoard.players(playersTurn.playerNumber - 1)
    )
    newController.setSelectedFigure(f1, f2)
    newController.setStateNumber(gameState)

    val stateNr = newController.gameBoard.stateNumber.get

    this.setGameBoard(newController.gameBoard)
    this.setPossibleCells(newController.gameBoard.possibleCells)
    this.setPlayersTurn(newController.gameBoard.playersTurn)
    this.setSelectedFigure(
      newController.gameBoard.selectedFigure.get._1,
      newController.gameBoard.selectedFigure.get._2
    )

    stateNr match {
      case 1 =>
        this.state.nextState(Roll(this))
        this.setStatementStatus(Statements.nextPlayer)
      case 2 =>
        this.state.nextState(SelectFigure(this))
        this.setStatementStatus(Statements.selectFigure)
      case 3 =>
        this.state.nextState(SetFigure(this))
        this.setStatementStatus(Statements.selectField)
      case 4 =>
        this.state.nextState(Setup(this))
        this.setStatementStatus(Statements.addPlayer)
      case 5 =>
        this.state.nextState(SetWall(this))
        this.setStatementStatus(Statements.wall)
    }

    publish(new GameBoardChanged)
  }

  def loadGameBoardJson(result: String): GameBoard ={
    val injector = Guice.createInjector(new GameBoardServerModule)
    var gameBoard: GameBoard = injector.instance[GameBoard]
    val json: JsValue = Json.parse(result)

    implicit val pointReader: Reads[Point] = Json.reads[Point]
    implicit val cellReader: Reads[Cell] = Json.reads[Cell]
    implicit val playerReader: Reads[Player] = Json.reads[Player]

    val players: List[Player] = (json \ "players").as[List[Player]]
    val posCells: Set[Int] = (json \ "possibleCells").as[Set[Int]]

    var found: Set[Int] = Set[Int]()

    for (index <- 0 until posCells.size) {
      val possCell = (json \ "possibleCells")(index).as[Int]
      gameBoard = gameBoard.setPossibleCellsTrueOrFalse(List(possCell), gameBoard.stateNumber.toString)
      found += possCell
    }
    gameBoard = gameBoard.setPossibleCell(found)

    LazyList.range(0, 131).foreach(index => {
      val cellNumber: Int = ((json \ "cells")(index) \ "cellNumber").as[Int]
      val playerNumber: Int = ((json \ "cells")(index) \ "playerNumber").as[Int]
      val figureNumber: Int = ((json \ "cells")(index) \ "figureNumber").as[Int]
      val hasWall: Boolean = ((json \ "cells")(index) \ "hasWall").as[Boolean]

      gameBoard = gameBoard setPlayer(playerNumber, cellNumber)
      gameBoard = gameBoard setFigure(figureNumber, cellNumber)

      if (hasWall)
        gameBoard = gameBoard.setWall(cellNumber)
      if (!hasWall)
        gameBoard = gameBoard.removeWall(cellNumber)
    })

    players.foreach(player => if (player.name != "") gameBoard = gameBoard.createPlayer(player.name))

    gameBoard = gameBoard.setPossibleCell(posCells)
    gameBoard
  }

  implicit val pointWrites: Writes[Point] = (point: Point) => {
    Json.obj(
      "x" -> JsNumber(point.x_coordinate),
      "y" -> JsNumber(point.y_coordinate)
    )
  }

  implicit val playerWrites: Writes[Player] = (player: Player) => {
    Json.obj(
      "playerNumber" -> player.playerNumber,
      "name" -> player.name
    )
  }

  override def save(): Unit = {
    val jsonGameBoard = gameBoardToJson(gameBoard, this)
    val jsonFile = Json.prettyPrint(jsonGameBoard)

    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(method = HttpMethods.POST, uri =
      "http://localhost:8081/json", entity = jsonFile))
    responseFuture.onComplete {
      case Success(value) =>
        val entityFuture: Future[String] = value.entity.toStrict(5.seconds).map(_.data.decodeString("UTF-8"))
        entityFuture.onComplete {
          case Success(value) =>  println("Spiel wurde erfolgreich gespeichert: " + value)
          case Failure(exception) =>  println("Fehler beim Speichern: " + exception )
        }
      case Failure(exception) => println(" Fehler beim Speichern: " + exception)
    }

  }

  def gameBoardToJson(gameBoard: GameBoardInterface, controller: ControllerInterface): JsObject = Json.obj(
      "players" -> Json.toJson(
        for {
          p <- gameBoard.players
        } yield Json.toJson(p)
      ),
      "playersTurn" -> controller.gameBoard.playersTurn,
      "diceNumber" -> controller.gameBoard.dicedNumber,
      "selectedFigure1" -> controller.gameBoard.selectedFigure.get._1,
      "selectedFigure2" -> controller.gameBoard.selectedFigure.get._2,
      "gameState" -> controller.getGameState.currentState.toString.toInt,
      "possibleCells" -> gameBoard.possibleCells,
      "cells" -> Json.toJson(
        for {
          c <- gameBoard.cellList
        } yield Json.obj(
          "cellNumber" -> c.cellNumber,
          "playerNumber" -> c.playerNumber,
          "figureNumber" -> c.figureNumber,
          "hasWall" -> c.hasWall,
          "coordinates" -> c.coordinates
        )
      )
    )




  /*
  override def load(): Unit = {
    val newController = fileIo.loadController
    val stateNr = newController.gameBoard.stateNumber.get

    this.setGameBoard(newController.gameBoard)
    this.setPossibleCells(newController.gameBoard.possibleCells)
    this.setPlayersTurn(newController.gameBoard.playersTurn)
    this.setSelectedFigure(
      newController.gameBoard.selectedFigure.get._1,
      newController.gameBoard.selectedFigure.get._2
    )

    stateNr match {
      case 1 =>
        this.state.nextState(Roll(this))
        this.setStatementStatus(Statements.nextPlayer)
      case 2 =>
        this.state.nextState(SelectFigure(this))
        this.setStatementStatus(Statements.selectFigure)
      case 3 =>
        this.state.nextState(SetFigure(this))
        this.setStatementStatus(Statements.selectField)
      case 4 =>
        this.state.nextState(Setup(this))
        this.setStatementStatus(Statements.addPlayer)
      case 5 =>
        this.state.nextState(SetWall(this))
        this.setStatementStatus(Statements.wall)
    }

    publish(new GameBoardChanged)
  }*/

}