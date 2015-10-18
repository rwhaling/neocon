package controllers

import java.util.Date

import akka.actor.Actor

import scala.collection.immutable.HashMap
import scala.collection.mutable
import scala.io.Source
import scala.xml.pull.{EvText, XMLEventReader}
import scala.math.Ordered.orderingToOrdered

case class SearchResult(w:String, doc:String, pos:Int)
case class FrequencyResult(w:String, freq:Int)
case class DirectoryResult(id:Int,doc:String)
case class IndexRecord(doc:Int, pos:Int) extends Ordered[IndexRecord] {
  def compare(that: IndexRecord): Int = (this.doc,this.pos) compare (that.doc,that.pos)
}

class IndexActor() extends Actor {
  var index = new HashMap[String,mutable.TreeSet[IndexRecord]]
  var docs = new mutable.LinkedHashMap[Int,String]
  var currentDoc = 0

  def receive = {
    case Parse(fn) =>
      System.out.print("indexing " + fn + " ... ")
      val start = new Date().getTime
      val source = Source.fromFile(fn)
      val reader = new XMLEventReader(source)
      docs += currentDoc -> fn
      var elCount = 0
      for (event <- reader) {
        event match {
          case EvText(text) => {
            val pattern = """\w+""".r
            val matches = pattern.findAllIn(text)
            var tokCount = 0
            matches.foreach { tok =>
              val k = tok.toString.toLowerCase
              if (!index.contains(k)) {
                index += k -> new mutable.TreeSet[IndexRecord]
              }
              val r = index(k)
              r += IndexRecord(currentDoc,elCount)
            }
          } case _ =>
        }
        elCount += 1
      }
      val stop = new Date().getTime
      System.out.print("done in " + (stop - start) + "ms\n" )
      currentDoc += 1
    case Drop() =>
      index = index.empty
    case Dir() =>
      System.out.println("DIR RECEIVED")
      for ( (id,doc) <- docs) {
        sender ! DirectoryResult(id,doc.split("/").last)
      }
    case Freq() =>
      println("FREQ")

      for ( (word,entries) <- index.iterator.toList.sortBy(- _._2.size )) {
        sender ! FrequencyResult(word,entries.size)
      }
    case Search(q) =>
      println("SEARCH")
      if (index.contains(q)) {
        for (rec <- index(q)) {
          val fn = docs(rec.doc)
          sender ! SearchResult(q,fn,rec.pos)
        }
      }
  }
}