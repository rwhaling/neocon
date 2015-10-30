package actors

import akka.actor.{Props, ActorRef}
import akka.stream.actor.ActorPublisherMessage.Cancel
import akka.stream.actor.ActorSubscriberMessage.OnNext
import akka.stream.actor.ActorSubscriber
import akka.stream.actor.ZeroRequestStrategy


object Streamer {
  def props(out:ActorRef) = Props(new Streamer(out))
}

class Streamer(out:ActorRef) extends ActorSubscriber {
  override val requestStrategy = ZeroRequestStrategy

  def receive = {
    case OnNext(msg) =>
      println("RECEIVED OnNext(StreamMsg)")
      out ! msg
    case More(n:Long) =>
      println("REQUEST " + n.toString)
      request(n)
    case Cancel =>
      println("CANCEL")
      cancel()
    case _ =>
      println("STREAMER RECEIVED SOMETHING ELSE")
  }
}