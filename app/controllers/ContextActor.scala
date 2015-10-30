package actors

import akka.actor.{ActorRef, Actor}

import scala.io.Source
import scala.xml.pull.{EvText, XMLEventReader, XMLEvent}

case class SearchContext(fn:String, text:String)

class ContextActor(out:ActorRef) extends Actor {
  var currentDoc:String = "BOGUS"
  var currentPos:Int = 0
  var currentStream:Iterator[XMLEvent] = Iterator.empty
  var currentEvent:Option[XMLEvent] = None
  var shortfn = ""


  def receive = {
    case msg:SearchResult => {
      if (currentDoc != msg.doc) {
        System.out.println("opening " + msg.doc + " for scanning")
        val source = Source.fromFile(msg.doc)
        val reader = new XMLEventReader(source)
        shortfn = msg.doc.split("/").last
        currentDoc = msg.doc
        currentPos = -1
        currentStream = reader.toIterator
      }
      while (currentPos < msg.pos && currentStream.hasNext) {
        currentEvent = Some(currentStream.next)
        currentPos += 1
      }
      currentEvent match {
        case Some(EvText(text:String)) =>
          out ! shortfn + ": " + text
        case _ =>
      }
    }
  }
}
