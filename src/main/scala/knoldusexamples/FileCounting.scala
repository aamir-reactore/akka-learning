package knoldusexamples

import java.io.File

import akka.pattern.ask
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success,Failure}
class StringCounter extends Actor {
  var count = 0

  override def receive = {
    case EOF => sender ! EOF
    case readLine: String => {
      count = readLine.split("\\s+").size
      sender ! count
    }
  }
}

class FileProcessor extends Actor {

  var fileSender: Option[ActorRef] = None
  val stringCounterActor = context.actorOf(Props[StringCounter], "actor")
  var totalCount = 0

  def receive = {
    case file: File => {
      fileSender = Some(sender)
      for (line <- scala.io.Source.fromFile(file).getLines) {
        stringCounterActor ! line
      }
      stringCounterActor ! EOF
    }
    case counter: Int => totalCount += counter
    case EOF => fileSender.map(_ ! totalCount)
  }
}

object ReaderApp extends App {

  val system = ActorSystem("FileReader")
  val actor = system.actorOf(Props(new FileProcessor), "actor")

  val fileName = "/home/aamir/wordcount.txt"
  implicit val time = Timeout(1.seconds)
  val futureRes = actor ? new File(fileName)

  futureRes onComplete {
    case Success(result) => {
      println(s"Total words = $result")
      system.terminate()
    }
    case Failure(msg) => {
      println(s"failure message: $msg")
    }
  }
}

case object EOF