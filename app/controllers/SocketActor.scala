package actors

import akka.actor._
import akka.stream.actor.ActorPublisherMessage.Request
import akka.stream.actor.ActorPublisherMessage.Cancel
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.ActorMaterializer
import akka.stream.ActorMaterializerSettings
import scala.collection.immutable.{TreeSet, HashMap}

abstract class Command
case class Search(query: String) extends Command
case class Parse(filename: String) extends Command
case class Dir() extends Command
case class Freq() extends Command
case class Drop() extends Command
case class More(n: Long) extends Command

class SocketActor(out:ActorRef, index:ActorRef, basedir:String) extends Actor {
  var counter = 0
  val searchActor = context.system.actorOf(Props(classOf[ContextActor],out))
  val settings = ActorMaterializerSettings(context.system)
  var flow = ActorRef.noSender
  implicit val mat = ActorMaterializer(settings)

  def receive = {
    case msg: String =>
      val command = parseCommand(msg)
      command match {
        case Dir() =>
          counter += 1
          index ! command
          out ! (counter + " : " + command.toString)
        case Search(_) =>
          counter += 1
          index ! command
          out ! (counter + " : " + command.toString)
        case Freq() =>
          counter += 1
          index ! command
          out ! (counter + " : " + command.toString)
        case More(n) =>
          println("MORE")
          flow.tell(command,self)
        case _ => out ! command
      }

    case d:DirectoryResult =>
      out ! (d.id + ": " + d.doc)
    case FrequencyResult(w:String,count:Int) =>
      out ! w + " : " + count
    case source:Source[SearchResult,_] =>
      println("Source received, creating output stream")
      val sink = Sink.actorSubscriber(Streamer.props(searchActor))
      if (!(flow == ActorRef.noSender))
        flow.tell(Cancel,self)
      flow = source.runWith(sink)
      val r = More(100)
      flow.tell(r,self)
    case _ =>
  }

  def parseCommand(msg: String): Command = {
    if (msg == ":dir") Dir()
    else if (msg == ":freq") Freq()
    else if (msg == ":more") More(100)
    else Search(msg)
  }
}

