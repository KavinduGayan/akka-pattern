import akka.actor._

// Define messages
case class AddActor(actorName: String)
case class RemoveActor(actorName: String)
case class SendMessageToActor(actorName: String, message: Any)
case object PrintAllActors

// Actor manager class
class ActorManager extends Actor {
  // Keep a mutable map to store actor references
  private var actorMap = Map.empty[String, ActorRef]

  def receive: Receive = {
    case AddActor(actorName) =>
      if (!actorMap.contains(actorName)) {
        // Create a new actor if it doesn't exist and add it to the map
        val newActorRef = context.actorOf(Props[SampleActor], actorName)
        actorMap += (actorName -> newActorRef)
        println(s"Actor '$actorName' added.")
      } else {
        println(s"Actor '$actorName' already exists.")
      }

    case RemoveActor(actorName) =>
      actorMap.get(actorName) match {
        case Some(actorRef) =>
          context.stop(actorRef)
          actorMap -= actorName
          println(s"Actor '$actorName' removed.")
        case None =>
          println(s"Actor '$actorName' does not exist.")
      }

    case SendMessageToActor(actorName, message) =>
      actorMap.get(actorName) match {
        case Some(actorRef) => actorRef ! message
        case None => println(s"Actor '$actorName' not found.")
      }

    case PrintAllActors =>
      actorMap.keys.foreach(println)
  }
}

// Sample actor class
class SampleActor extends Actor {
  def receive: Receive = {
    case msg: String => println(s"Received message: $msg")
    case _ => println("Unknown message")
  }
}

object ActorManagerApp extends App {
  // Create an actor system
  val system = ActorSystem("ActorSystem")

  // Create the actor manager
  val actorManager = system.actorOf(Props[ActorManager], "ActorManager")

  // Example usage
  actorManager ! AddActor("Actor1")
  actorManager ! AddActor("Actor2")
  actorManager ! SendMessageToActor("Actor1", "Hello, Actor1!")
  actorManager ! SendMessageToActor("Actor3", "This will not be received.")
  actorManager ! PrintAllActors
  actorManager ! RemoveActor("Actor1")
  actorManager ! RemoveActor("Actor4")
  actorManager ! PrintAllActors

  // Terminate the actor system after some time (for the sake of this example)
  import scala.concurrent.duration._
  import system.dispatcher
  system.scheduler.scheduleOnce(5.seconds) {
    system.terminate()
  }
}
