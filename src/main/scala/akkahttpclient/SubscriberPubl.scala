package akkahttpclient

import akka.actor._

case class Book(title: String, authors: List[String])

class BookPublisher extends Actor {

  def receive = {
    case book: Book => {
      println(s"Yeah! Publishing a new book: $book")
      context.system.eventStream.publish(book)
    }
  }

}

class BookSubscriber extends Actor {

  override def preStart = context.system.eventStream.subscribe(self, classOf[Book])

  def receive = {
    case book: Book => println(s"My name is ${self.path.name} and I have received a new book: $book")
  }
}

object Main extends App {

  implicit val system = ActorSystem("publisher-subscribers-example")

  val author = "Author"

  val bookPublisher = system.actorOf(Props[BookPublisher], name = "book-publisher")

  val subscriber1 = system.actorOf(Props[BookSubscriber], name = "subscriber-1")
  val subscriber2 = system.actorOf(Props[BookSubscriber], name = "subscriber-2")

  bookPublisher ! Book(title = "A book title", authors = List(author, "Another author"))
  // Yeah! Publishing a new book: Book(A book title,List(Author, Another author))
  // My name is subscriber-1 and I have received a new book: Book(A book title,List(Author, Another author))
  // My name is subscriber-2 and I have received a new book: Book(A book title,List(Author, Another author))

  system.eventStream.unsubscribe(subscriber2, classOf[Book])
   Thread.sleep(2000)
  bookPublisher ! Book(title = "Another book title", authors = List("Another author"))
  // Yeah! Publishing a new book: Book(Another book title,List(Another author))
  // My name is subscriber-1 and I have received a new book: Book(Another book title,List(Another author))
}