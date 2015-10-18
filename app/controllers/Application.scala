package controllers

import java.io.File

import akka.actor._
import play.api.libs.iteratee.{Concurrent, Iteratee, Enumerator}
import play.api.mvc._
import play.api.Play.current
import play.api.libs.concurrent.Akka.system
class Application extends Controller {

  val basedir = "/Users/rwhaling/Documents/shakespeare"
  val indexActor:ActorRef = system.actorOf(Props(new IndexActor))

  val d = new File(basedir)
  if (d.exists && d.isDirectory) {
    val files = d.listFiles.filter(_.isFile).toList
    files.foreach(f => {
      indexActor ! Parse(f.toString)
    })
  }

  def socket = WebSocket.acceptWithActor[String,String] {request => out =>
    Props(new SocketActor(out,indexActor,basedir))
  }

  def index = Action {
    Ok(views.html.index())
  }
}