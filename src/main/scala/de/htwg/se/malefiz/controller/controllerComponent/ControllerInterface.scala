package de.htwg.se.malefiz.controller.controllerComponent

import scala.swing.Publisher

trait ControllerInterface extends Publisher {

  def execute(input: String): Unit

  def sendPlayersToGameService(playerList: List[String]): Unit

  def startGame(): Unit

  def load(): Unit

  def checkInput(input: String): Either[String, String]

}

import scala.swing.Button
import scala.swing.event.Event

class GameBoardChanged extends Event

case class ButtonClicked(source: Button) extends Event

class ChangeWall extends Event

class Winner extends Event