package de.htwg.se.malefiz.controller.controllerComponent.controllerBaseImpl

import com.google.inject.{Guice, Inject, Injector}
import de.htwg.se.malefiz.MalefizModule
import de.htwg.se.malefiz.rest.restComponent.RestControllerInterface
import de.htwg.se.malefiz.rest.restComponent.restBaseImpl.RestController
import net.codingwell.scalaguice.InjectorExtensions._
import de.htwg.se.malefiz.controller.controllerComponent.ControllerInterface

import scala.swing.Publisher

class Controller @Inject() extends ControllerInterface with Publisher {

  val injector: Injector = Guice.createInjector(new MalefizModule)
  val rest: RestControllerInterface = injector.instance[RestController]

  override def execute(input: String): Unit = {
    sendPlayersToGameService(input.split(" ").toList)

  }

  def sendPlayersToGameService(playerList: List[String]): Unit = rest.sendPlayerListRequest(playerList)

  def startGame(): Unit = rest.startGameRequest()

  override def load(): Unit = rest.sendLoadRequest()

  override def checkInput(input: String): Either[String, String] = {
      if(input.split(" ").toList.size > 5)
        Left("Bitte maximal 4 Spieler eintippen (leerzeichen-getrennt)!")
      else
        Right(input)
  }

}