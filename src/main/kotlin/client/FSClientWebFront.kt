package client

import jakarta.servlet.Servlet
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.HandlerList
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder

class FSClientWebFront {

  val resourceBase = "src/main/kotlin/client/ui/"

  fun readHtmlFromFile(filename: String): String? {
    return Files.readString(Paths.get(filename), StandardCharsets.UTF_8)
  }

  fun start() {
    val server = Server(8080)
    val handler = ServletContextHandler()
    handler.resourceBase = resourceBase
    handler.contextPath = "/fs"
    handler.addServlet(ServletHolder(LoginServlet()), "/login")
    val jsServletHolder = ServletHolder(DefaultServlet())
    jsServletHolder.setInitParameter(
        "org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false")

    handler.addServlet(jsServletHolder, "*.js")

    // APIs
    val apiContextHandler = ServletContextHandler()
    apiContextHandler.contextPath = "/api"

    val apiServlets =
        mapOf<String, Servlet>(
            "checkConnection" to CheckConnectionApiServlet(),
            "loginReq" to LoginRequestApi(),
            "registerReq" to RegisterRequestApi())

    for (entry in apiServlets) {
      apiContextHandler.addServlet(ServletHolder(entry.value), "/" + entry.key)
    }

    // register to server
    server.handler = HandlerList(handler, apiContextHandler)
    server.start()
    server.join()
  }

  inner class LoginServlet : HttpServlet() {
    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
      // Set the content type and character encoding for the response
      response.contentType = "text/html;charset=UTF-8"

      // Read the login page HTML from a file
      val html: String = readHtmlFromFile(resourceBase + "login.html")!!

      // Write the HTML to the response output stream
      response.writer.write(html)
    }

    override fun doPost(req: HttpServletRequest?, resp: HttpServletResponse?) {
      val address = req!!.getParameter("address")

      val port = Integer.parseInt(req.getParameter("port"))

      doGet(req, resp!!)
    }
  }

  inner class ClientMainPageServlet : HttpServlet() {
    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
      // Set the content type and character encoding for the response
      response.contentType = "text/html;charset=UTF-8"

      // Read the login page HTML from a file
      val html: String = readHtmlFromFile(resourceBase + "login.html")!!

      // Write the HTML to the response output stream
      response.writer.write(html)
    }

    override fun doPost(req: HttpServletRequest?, resp: HttpServletResponse?) {
      doGet(req!!, resp!!)
    }
  }

  inner class CheckConnectionApiServlet : HttpServlet() {
    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
      // Set the content type and character encoding for the response
      response.contentType = "text/json;charset=UTF-8"

      response.writer.write("{'connection' : 'ok'}")
    }
  }

  // API Servlets
  inner class LoginRequestApi : HttpServlet() {
    override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
      val name = request.getParameter("name")
      val password = request.getParameter("password")
      val address = request.getParameter("address")
      val port = request.getParameter("port")

      // Set the content type and character encoding for the response
      response.contentType = "text/json;charset=UTF-8"

      response.writer.write("{'connection' : 'ok'}")
    }
  }

  inner class RegisterRequestApi : HttpServlet() {
    override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
      val name = request.getParameter("name")
      val password = request.getParameter("password")
      val address = request.getParameter("address")
      val port = request.getParameter("port")

      // Set the content type and character encoding for the response
      response.contentType = "text/json;charset=UTF-8"

      response.writer.write("{'connection' : 'ok'}")
    }
  }

  inner class ShowFolderApi : HttpServlet() {
    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
      val name = request.getParameter("name")
      val password = request.getParameter("password")
      val address = request.getParameter("address")
      val port = request.getParameter("port")

      // Set the content type and character encoding for the response
      response.contentType = "text/json;charset=UTF-8"

      response.writer.write("{'connection' : 'ok'}")
    }
  }
}
