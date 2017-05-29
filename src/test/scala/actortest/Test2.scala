import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.TestKit
import org.specs2.mutable.SpecificationLike

object BucketCounterActorProtocol {
  case class Bucket(label: String, quantity: Int)
  // a new message to expose the internal status of the actor
  case class GetCounter(receiver: ActorRef)
}

// adding an optional listener to the class
class BucketCounterActor(listener: Option[ActorRef] = None) extends Actor {
  import BucketCounterActorProtocol._

  var counter = 0

  def receive = {
    case Bucket(label, quantity) =>
      counter = counter + quantity
      print(label)
      // informing the listener of the side effect
      listener.map(_ ! label)

    // logic to expose internal status
    case GetCounter(receiver) => {
      println(s"counter is >> $counter")
      receiver ! counter
    }
  }

}

class BucketCounterActorSpec extends TestKit(ActorSystem()) with SpecificationLike {

  import BucketCounterActorProtocol._

  "A Bucket Counter Actor" should {

    val actorProps = Props(new BucketCounterActor(Some(testActor)))
    val actor = system.actorOf(actorProps, "actor-to-test")

    val firstBucket = Bucket("Yo, I am a bucket", 1)
    val secondBucket = Bucket("I am another bucket", 9)

    "accumulate the quantity of buckets received" in {
      actor ! GetCounter(testActor)
      expectMsg(0)
      success
    }

    "print out the name of the received buckets" in {
      actor ! firstBucket
      expectMsg(firstBucket.label)
      actor ! secondBucket
      expectMsg(secondBucket.label)
      actor ! GetCounter(testActor)
      expectMsg(10)
      success
    }


  }
}