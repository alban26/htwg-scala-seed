package de.htwg.se.malefiz.model

import scala.collection.mutable.Map
import scala.io.Source
import scala.util.{Failure, Success, Try}

case class Creator() {

val a = 4
  def readTextFile(filename: String): Try[Iterator[String]] = {
    Try(Source.fromFile(filename).getLines)
  }

  def getCellList(inputFile: String): List[Cell] = {
      readTextFile(inputFile) match {
        case Success(line) => println("Welcome")
        case Failure(f) => println(f)
      }

    val list = Source.fromFile(inputFile)
    val inputData = list.getLines

      .map(line => line.split(" "))
      .map { case Array(cellNumber, playerNumber, figureNumber, destination, wallPermission, hasWall, x, y, possibleFigures, possibleCells) =>
        Cell(cellNumber.toInt,
          playerNumber.toInt,
          figureNumber.toInt,
          destination.toBoolean,
          wallPermission.toBoolean,
          hasWall.toBoolean,
          Point(x.toInt, y.toInt),
          possibleFigures.toBoolean,
          possibleCells.toBoolean)
      }
      .toList
    list.close()
    inputData
  }

  def getCellGraph(fileInput: String): Map[Int, Set[Int]] = {
    readTextFile(fileInput) match {
      case Success(line) => println("to Malefiz!")
      case Failure(f) => println(f)
    }
    val source = Source.fromFile(fileInput)
    val lines = source.getLines()
    val graph : Map[Int, Set[Int]] = Map.empty
    while (lines.hasNext) {
      val input = lines.next()
      val inputArray: Array[String] = input.split(" ")
      for (i <- 1 until inputArray.length) {
        updateCellGraph(inputArray(0).toInt, inputArray(i).toInt, graph)
      }
    }
    graph
  }

  def updateCellGraph(key: Int, value: Int, map: Map[Int, Set[Int]]) : Map[Int, Set[Int]] = {
    map.get(key)
      .map(_=> map(key) += value)
      .getOrElse(map(key) = Set[Int](value))
    map
  }

  def execute(callback:(String) => List[Cell], y: String) = callback(y)
  def execute1(callback:String => Map[Int, Set[Int]], y:String)= callback(y)
}
