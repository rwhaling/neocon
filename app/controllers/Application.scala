package controllers
import java.io.File
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import scala.io.Source
import scala.xml._
import scala.xml.pull._
class Application extends Controller {

  def index = Action {
    Ok(views.html.index("Hi, Your new application is ready."))
  }

  def upload = Action(parse.multipartFormData) { request =>  	
  	request.body.file("text").map { text => 
  		val filename = text.filename
  		val contentType = text.contentType
  		val source = Source.fromFile(text.ref.file)
      var response = "Uploaded " + filename + ": \n"
      val reader = new XMLEventReader(source)
      for (event <- reader) event match {
        case EvText(text) => response = response + text
        case _ =>         
      }
      Ok(response)
  	}.getOrElse {
  		Redirect(routes.Application.index)
  	}
  }
}
