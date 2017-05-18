package akkahttpclient

import java.io.File
import java.net.URL

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import scala.sys.process._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

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

  def fileDownloader(url: String, remoteFileLocation: String): Future[String] = {
    Future {
      new URL(url) #> new File(remoteFileLocation) !!
    }
  }
  //getDataFromApi
  //fileDownloader("http://192.168.1.123:8081/artifactory/release-artifacts/dbscript_asset_0.8.9.81-DEV.sql", "/home/aamir/Documents/dbscript_asset_0.8.9.81-DEV.sql")
  Thread.sleep(100000)

}