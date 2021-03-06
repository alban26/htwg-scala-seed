package de.htwg.se.malefiz.fileIoModule.model.fileIOComponent.fileIoXmlImpl

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse}
import de.htwg.se.malefiz.fileIoModule.model.FileIOInterface

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

class FileIO extends FileIOInterface {

  implicit val system = ActorSystem(Behaviors.empty, "FileIO")
  implicit val executionContext = system.executionContext

  def load: Unit = {
    val gameString = getXmlString
    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(HttpMethods.POST, uri = "http://localhost:8083/gameBoardXml", entity = gameString))
    responseFuture.onComplete {
      case Success(value) => {
        val entityFuture: Future[String] = value.entity.toStrict(5.seconds).map(_.data.decodeString("UTF-8"))
        entityFuture.onComplete {
          case Success(result) => println(result)
          case Failure(exception) => sys.error(exception.toString)
        }
      }
      case Failure(_) => sys.error(_)
    }
  }

  def getXmlString: String = {
    scala.xml.XML.load("FileIO/src/main/scala/de/htwg/se/malefiz/fileIoModule/gameboard.xml").mkString
  }


  override def save(gamestate_json: String, suffix: String): Unit = {
    import java.io._
    val print_writer = new PrintWriter(new File(s"FileIO/src/main/scala/de/htwg/se/malefiz/fileIoModule/gameboard" +
      s".$suffix"))
    print_writer.write(gamestate_json)
    print_writer.close()
  }

}

