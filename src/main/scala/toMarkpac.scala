import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object toMarkpac extends App {

  case class StartCounting1(n: Int, other: ActorRef)

  case class CountDown1(n: Int)

  class CountDownActor extends Actor {

    override def receive = {
      case StartCounting1(n, other) => {
        println(n)
        other ! CountDown1(n - 1)
      }
      case CountDown1(n) => {
        println(self)
        if (n > 0) {
          println(n)
          sender ! CountDown1(n - 1)
        } else {
          context.system.terminate()
        }
      }
    }

    def foo = println("Normal Method")
  }


  val system = ActorSystem("CountDownActor")
  val actor1 = system.actorOf(Props[CountDownActor], "firstActor--1")
  val actor2 = system.actorOf(Props[CountDownActor], "secondActor--2")

  actor1 ! StartCounting1(5, actor2)
}

object AskPattern extends App {

  case object AskName

  case class AskNameOf(other: ActorRef)

  case class NameResponse(name: String)

  class AskActor(val name: String) extends Actor {

    override def receive = {
      case AskName => sender ! NameResponse(name)
      case AskNameOf(other) => {
        val f = other ? AskName
        f.onComplete {
          case Success(NameResponse(n)) =>
            println(s"They said their name was $n")
          case Success(_) =>
            println("They didn't tell us their name")
          case Failure(_) =>
            println("Asking their name failed")
        }
      }
    }
  }

  val system = ActorSystem("AskActorActorSystem")
  val actor1 = system.actorOf(Props(new AskActor("aamir")), "AskActor1")
  val actor2 = system.actorOf(Props(new AskActor("aamir")), "AskActor2")

  implicit val timeout = Timeout(1.seconds)
  val r: Future[NameResponse] = (actor1 ? AskName).mapTo[NameResponse]
  r.foreach(nameObj => println(s"name is ${nameObj.name}"))

  actor1 ? AskNameOf(actor2)
  system.terminate()
}

object HierarchyExample extends App {

  case object CreateChild

  case object PrintSignal

  case object SignalChildren

  class ParentActor extends Actor {
    private var childCount = 0
    private val children = collection.mutable.Buffer[ActorRef]()

    def receive = {
      case CreateChild => {
        children += context.actorOf(Props[ChildActor], s"child-$childCount")
        childCount += 1
      }
      case SignalChildren => {
        println(s">>>>>>>>>>>buffersize = ${children.length}")
        children.map(_ ! PrintSignal)
      }
    }
  }

  class ChildActor extends Actor {
    def receive = {
      case PrintSignal => println(self)
    }
  }

  val system = ActorSystem("HierarchySystem")
  val actor = system.actorOf(Props[ParentActor], "Parent1")
  actor ! CreateChild
  actor ! SignalChildren
  Thread.sleep(1000)
  actor ! CreateChild
  Thread.sleep(1000)
  actor ! CreateChild
 actor ! CreateChild
  actor ! SignalChildren

  system.terminate()

}