package controllers

import akka.actor.Actor

import scala.io.Source
import scala.xml.pull.{EvText, XMLEventReader, XMLEvent}

case class SearchContext(fn:String, text:String)

class ContextActor() extends Actor {
  var currentDoc:String = "BOGUS"
  var currentPos:Int = 0
  var currentStream:Iterator[XMLEvent] = Iterator.empty
  var currentEvent:Option[XMLEvent] = None
  var shortfn = ""

  def receive = {
    case Tuple3(w:String, doc:String, pos:Int) => {
      if (currentDoc != doc) {
        System.out.println("opening " + doc + " for scanning")
        val source = Source.fromFile(doc)
        val reader = new XMLEventReader(source)
        shortfn = doc.split("/").last
        currentDoc = doc
        currentPos = -1
        currentStream = reader.toIterator
      }
      while (currentPos < pos && currentStream.hasNext) {
        currentEvent = Some(currentStream.next)
        currentPos += 1
      }
      currentEvent match {
        case Some(EvText(text:String)) => {
          sender ! SearchContext(shortfn,text)
        } case _ => {}
      }
    }
  }
}
