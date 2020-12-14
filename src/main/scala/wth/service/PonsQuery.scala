package wth.service

import wth.model.http.EntitySerDes
import wth.model.pons.PonsQueryConf
import wth.model.pons._
import wth.service.QueryService.QueryService
import wth.service.http.HttpClient.HttpClient
import wth.service.http._
import zio._

/**
 * configuration for wth.model.pons.eu: language variance
 */
object PonsQuery {
  type Configuration = Has[PonsQueryConf]

  def restApiService[R <: Seq[Response], SerDes[R] <: EntitySerDes[R]: TagK]: ZLayer[HttpClient[SerDes] with Has[
    SerDes[Seq[Response]]
  ] with Configuration, Nothing, QueryService[Seq[Response]]] =
    ZLayer.fromServices[HttpClient.Service[SerDes], SerDes[Seq[Response]], PonsQueryConf, QueryService.Service[Seq[
      Response
    ]]] { (client, desSer, configuration) =>
      new QueryService.Service[Seq[Response]] {
        override def query(toQuery: String): Task[Seq[Response]] = {
          import configuration._

          val parameters: Map[String, String] = Map(
            "l" -> s"$source$target",
            "in" -> source,
            "q" -> toQuery
          )

          val headers = Map(
            "X-Secret" -> configuration.secret,
            "Accept" -> "application/json"
          )

          client
            .get[Seq[Response]](baseUrl, Seq("v1", "dictionary"), parameters, headers)(desSer)
            .catchAll(_ => Task.succeed(Nil))
        }

        private val baseUrl: String = "https://api.pons.com/"
      }
    }

}
