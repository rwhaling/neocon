package controllers

import java.io.File
import java.util.Date


import akka.actor._
import scala.collection.immutable.{TreeSet, HashMap}
import scala.xml.pull.{EvText, XMLEventReader, XMLEvent}

abstract class Command
case class Search(query: String) extends Command
case class Parse(filename: String) extends Command
case class Dir() extends Command
case class Freq() extends Command
case class Drop() extends Command

case class TokenRec(file:Int, pos:Int)

class SocketActor(out:ActorRef, index:ActorRef, basedir:String) extends Actor {
  var counter = 0
  var searchActor = context.system.actorOf(Props(new ContextActor))
  def receive = {
    case msg: String =>
      val command = parseCommand(msg)
      counter += 1
      out ! (counter + " : " + command.toString)
      command match {
        case Dir() =>
          index ! command
        case Search(_) => index ! command
        case Freq() => index ! command
        case _ => out ! command
      }
    case d:DirectoryResult =>
      out ! (d.id + ": " + d.doc)
    case d: HashMap[Int, String] =>
      for ((k, v) <- d) {
        out ! k + " " + v
      }
    case FrequencyResult(w:String,count:Int) =>
      out ! w + " : " + count
    case SearchResult(w, doc, pos) =>
      searchActor ! Tuple3(w,doc,pos)
    case SearchContext(doc:String,context:String) =>
      out ! doc + ": " + context
    case _ => {}
  }

  def parseCommand(msg: String): Command = {
    if (msg == ":dir") Dir()
    else if (msg == ":freq") Freq()
    else Search(msg)
  }
}
