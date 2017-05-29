package actortest

import akka.actor.Actor
import akka.testkit.TestActorRef
object MessageFilteringActorProtocol {
  case class SimpleMessage(text: String)
}
class MessageFilteringActor extends Actor {
  import MessageFilteringActorProtocol._

  var messages = Vector[String]()
  def state = messages

   def receive = {
     case SimpleMessage(text) if text startsWith "A" =>
       messages = messages :+ text
  }
}

//now building a test, single threaded

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.specs2.mutable.SpecificationLike

class MessageFilteringActorSpec extends TestKit(ActorSystem())
  with SpecificationLike {

  import MessageFilteringActorProtocol._

  val actor = TestActorRef[MessageFilteringActor]
  "A Message Filtering Actor" should {

    "save only messages that starts with 'A'" in {
      actor ! SimpleMessage("A message to remember")
      actor ! SimpleMessage("This message should not be saved")
      actor ! SimpleMessage("Another message for you")
      actor.underlyingActor.state.length mustEqual 2
    }

  }
}

