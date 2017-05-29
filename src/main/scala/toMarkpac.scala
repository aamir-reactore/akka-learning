import akka.actor.SupervisorStrategy.{Restart, Resume}
import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, OneForOneStrategy, Props}
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

  case class PrintSignal(order:Int)

  case class SignalChildren(order: Int)

  class ParentActor extends Actor {
    private var childCount = 0

    def receive = {
      case CreateChild => {
        context.actorOf(Props[ChildActor], s"child$childCount")
        childCount += 1
      }
      case SignalChildren(n) => {
        context.children.map(_ ! PrintSignal(n))
      }
    }
  }

  class ChildActor extends Actor {
    def receive = {
      case PrintSignal(n) => println(s"$n $self")
    }
  }

  val system = ActorSystem("HierarchySystem")
  val actor = system.actorOf(Props[ParentActor], "Parent1")
  val actor2 = system.actorOf(Props[ParentActor], "Parent2")



  actor ! CreateChild
  actor ! SignalChildren(1)
  actor ! CreateChild
  actor ! CreateChild
  actor ! SignalChildren(2)

  actor2 ! CreateChild
  val child0: ActorSelection = system.actorSelection("/user/Parent2/child0") /*akka://HierarchySystem (leave off if system same)*/
  child0 ! PrintSignal(3)

  Thread.sleep(4000)
  system.terminate()

}

object SupervisorStrategyExample extends App {

  case object CreateChild
  case class PrintSignal(order:Int)
  case class SignalChildren(order: Int)
  case class DivideNumbers(n:Int,d:Int)
  case object BadStuff

  class ParentActor extends Actor {
    private var childCount = 0

    def receive = {
      case CreateChild => {
        context.actorOf(Props[ChildActor], s"child$childCount")
        childCount += 1
      }
      case SignalChildren(n) => {
        context.children.map(_ ! PrintSignal(n))
      }
    }

    override val supervisorStrategy = OneForOneStrategy(loggingEnabled = false) {
      case _:ArithmeticException => Resume
      case  _:Exception => Restart
    }
  }


  class ChildActor extends Actor {
    def receive = {
      case PrintSignal(n) => println(s"$n $self")
      case DivideNumbers(n,d) => println(n/d)
      case BadStuff => throw new RuntimeException("Bad stuff happened")
    }

  }

  val system = ActorSystem("SupervisorStrategySystem")
  val actor = system.actorOf(Props[ParentActor], "Parent1")

  actor ! CreateChild
  actor ! CreateChild
  val child0 = system.actorSelection("akka://SupervisorStrategySystem/user/Parent1/child0")

  child0 ! DivideNumbers(4,2)
  child0 ! DivideNumbers(4,0)
  child0 ! DivideNumbers(8,2)
  child0 ! BadStuff

  Thread.sleep(1000)
  system.terminate()
}

