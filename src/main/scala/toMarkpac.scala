import akka.actor.{Actor, ActorRef, ActorSystem, Props}

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
