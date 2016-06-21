package demo

import java.util.concurrent.Executors

import org.apache.commons.io.IOUtils
import org.eclipse.jetty.server.{Request, Server}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import cats.data.Xor
import org.eclipse.jetty.server.handler.AbstractHandler
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

case class GraphQLRequest(query: String)
case class QueryHolder(query: String, variables: Map[String,Any] = Map.empty)

object GraphqlPlayServer extends App {
  val port: Int = 9000
  private val server = new Server(port)
  server.setHandler(handler)
  start()
  println("Graphql server started")

  def onRequest(target: String, request: Request, httpRequest: HttpServletRequest, httpResponse: HttpServletResponse): Unit = {
    request.setHandled(true)
    (request.getMethod,request.getRequestURI) match {
      case ("GET","/index") =>
        try {
          val stream = getClass.getResourceAsStream("/graphiql.html")
          IOUtils.copy(stream, httpResponse.getOutputStream)
        } catch {
          case e: Exception => e.printStackTrace()
        }
        httpResponse.setContentType("text/html")
        httpResponse.setStatus(200)
      case ("POST","/index") =>
        decode[QueryHolder](IOUtils.toString(request.getInputStream)) match {
          case Xor.Right(queryHolder) =>
            val result = new GraphQLDemo(Executors.newFixedThreadPool(4))
              .execute(GraphQLRequest(queryHolder.query))
            val x = result.asJson.noSpaces
            IOUtils.write(x, httpResponse.getOutputStream)
          case Xor.Left(error) => throw new RuntimeException("Failed to deserialize event", error.fillInStackTrace())
        }
      case _ =>
        httpResponse.setStatus(404)
    }
  }

  def handler: AbstractHandler = new AbstractHandler {
    def handle(target: String, request: Request, httpRequest: HttpServletRequest, httpResponse: HttpServletResponse) {
      onRequest(target, request, httpRequest, httpResponse)
    }
  }

  def start() {
    try {
      server.start()
    }
    catch {
      case e: Exception => throw new RuntimeException(e)
    }
  }

  def stop() {
    try {
      server.stop()
      server.join()
    }
    catch {
      case e: Exception => throw new RuntimeException(e)
    }
  }


}
