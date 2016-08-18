package demo

import java.util.concurrent.Executors

import org.apache.commons.io.IOUtils
import org.eclipse.jetty.server.{Request, Server}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.eclipse.jetty.server.handler.AbstractHandler

case class GraphQLRequest(query: String)
case class QueryHolder(query: String, variables: String = "", operationName: String)

object GraphqlPlayServer extends App {

  val om = new ObjectMapper
  om.registerModule(DefaultScalaModule)

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
        val postBody = IOUtils.toString(request.getInputStream)
        val queryHolder = om.readValue(postBody, classOf[QueryHolder])
        val result = new RegularGraphQLExecutor(Executors.newFixedThreadPool(4))
//        val result = new MacrosGraphQLExecutor(Executors.newFixedThreadPool(4))
          .execute(GraphQLRequest(queryHolder.query))
        IOUtils.write(om.writeValueAsString(result), httpResponse.getOutputStream)
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
