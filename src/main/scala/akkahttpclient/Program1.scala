package akkahttpclient

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Program1 extends App {

  def httpClient(url: String): Future[String] = {

    implicit val sys: ActorSystem = ActorSystem("ReactorActorSystem")
    implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(sys))

    val request = HttpRequest(HttpMethods.GET, url)
    for {
      response <- Http().singleRequest(request)
      content <- Unmarshal(response.entity).to[String]
    } yield content
  }
  def getDataFromApi = {
    for {
      response <- httpClient("http://192.168.1.123:8081/artifactory/api/search/artifact?name=hr-ui&repos=release-artifacts")
      _=println(response)
    } yield ""
  }
  getDataFromApi
  Thread.sleep(100000)

}