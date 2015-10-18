package controllers

import java.io.File
import akka.actor._
import play.api.{Play, Application}
import play.api.mvc._
import play.api.Play.current
import play.api.libs.concurrent.Akka.system

import actors._

class Application extends Controller {
  val indexActor:ActorRef = system.actorOf(Props(new IndexActor))

  val basedir = Play.configuration.getString("neocon.basedir").get

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