package de.htwg.se.malefiz.fileIoModule

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import com.google.inject.{Guice, Injector}
import de.htwg.se.malefiz.fileIoModule.controller.controllerComponent.ControllerInterface
import de.htwg.se.malefiz.gameBoardModule.GameBoardServer.route

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.io.StdIn
import scala.util.{Failure, Success}


object FileIOServer {

  val injector: Injector = Guice.createInjector(new FileIOServerModule)
  val controller: ControllerInterface = injector.getInstance(classOf[ControllerInterface])

  implicit val system = ActorSystem(Behaviors.empty, "FileIO")
  implicit val executionContext = system.executionContext

  val route =
    concat(
      path("save") {
        (post & extract(_.request.entity)) { body =>
          complete {
            if (body.getContentType() == ContentTypes.`text/xml(UTF-8)`) {
              val entityFuture: Future[String] = body.toStrict(1.seconds).map(_.data.decodeString("UTF-8"))
              entityFuture.onComplete {
                case Success(value) =>
                  println("Speichere als XML")
                  controller.save(value, "xml")
                  HttpEntity("Speichern als XML erfolgreich")
                case Failure(exception) => HttpEntity(exception.toString)
              }
              HttpEntity("Fehler beim speichern als XML")
            } else if (body.getContentType() == ContentTypes.`application/json`) {
              val entityFuture: Future[String] = body.toStrict(1.seconds).map(_.data.decodeString("UTF-8"))
              entityFuture.onComplete {
                case Success(value) =>
                  println("Speichere als Json")
                  controller.save(value, "json")
                  HttpEntity("Speichern als Json erfolgreich")
                case Failure(exception) => HttpEntity(exception.toString)
              }
              HttpEntity("Erfolgreich gespeichert!")
            } else {
              HttpEntity("Fehler beim Speichern")
            }
          }
        }
      },
      get {
        path("load") {
          controller.load()
          complete(StatusCodes.OK,"Erfolgreich geladen!")
        }
      }
    )




  def main(args: Array[String]): Unit = {

    route

    val bindingFuture = Http().newServerAt("localhost", 8081).bind(route)

    println(s" GameBoard Server online at http://0.0.0.0:8081/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }

}