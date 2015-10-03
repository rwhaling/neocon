package controllers
import java.io.File
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import scala.io.Source.fromFile

class Application extends Controller {

  def index = Action {
    Ok(views.html.index("Hi, Your new application is ready."))
  }

  def results = Action { 
  	Ok("Results: ")
  }

  def upload = Action(parse.multipartFormData) { request =>  	
	request.body.file("text") match {
		case Some(file) => {
		  val filename = file.filename 
		  val contentType = file.contentType
		  val destFile = new File(s"/tmp/upload/$filename")
		  file.ref.moveTo(destFile)
		  val lines = scala.io.Source.fromFile(destFile).getLines
		  Ok("Uploaded " + filename + " :\n" + lines.reduceLeft(_ + _) )
		}
		case None => {
		  	Redirect(routes.Application.index)
		}
    }
  }
}
