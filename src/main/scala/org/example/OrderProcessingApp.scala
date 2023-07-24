import akka.actor._

// Define messages
case class Order(id: Int, customerId: Int, items: List[String])
case class ProcessedOrder(id: Int, customerId: Int, items: List[String], totalPrice: Double)

// Order processing actor
class OrderProcessor extends Actor {
  def receive: Receive = {
    case Order(id, customerId, items) =>
      val totalPrice = calculateTotalPrice(items)
      sender() ! ProcessedOrder(id, customerId, items, totalPrice)
  }

  private def calculateTotalPrice(items: List[String]): Double = {
    // Simplified logic to calculate the total price of items
    items.length * 10.0
  }
}

// Customer actor
class CustomerActor(orderProcessor: ActorRef) extends Actor {
  def receive: Receive = {
    case Order(id, customerId, items) =>
      println(s"Customer $customerId is placing order $id.")
      orderProcessor ! Order(id, customerId, items)

    case ProcessedOrder(id, customerId, items, totalPrice) =>
      println(s"Order $id for customer $customerId processed. Total price: $$${totalPrice}")
  }
}

object OrderProcessingApp extends App {
  // Create an actor system
  val system = ActorSystem("OrderProcessingSystem")

  // Create the order processor actor
  val orderProcessor = system.actorOf(Props[OrderProcessor], "OrderProcessor")

  // Create customer actors
  val customer1 = system.actorOf(Props(classOf[CustomerActor], orderProcessor), "Customer1")
  val customer2 = system.actorOf(Props(classOf[CustomerActor], orderProcessor), "Customer2")

  // Simulate order placement
  customer1 ! Order(1, 101, List("Item1", "Item2", "Item3"))
  customer2 ! Order(2, 102, List("Item2", "Item4", "Item5", "Item6"))

  // Terminate the actor system after some time (for the sake of this example)
  import scala.concurrent.duration._
  import system.dispatcher
  system.scheduler.scheduleOnce(5.seconds) {
    system.terminate()
  }
}
