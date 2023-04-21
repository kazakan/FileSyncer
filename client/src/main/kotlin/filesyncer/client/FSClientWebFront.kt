package filesyncer.client

import jakarta.servlet.Servlet
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.io.File
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.HandlerList
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder

class FSClientWebFront(val client: FSClientFrontInterface, val port: Int = 8080) : FSClientFront() {

    val resourceBase = this::class.java.getResource("/webapps")?.toString()
    val resourceLoader = this::class.java
    var cnt = 0

    fun readHtmlFromFile(filename: String): String? {
        return Files.readString(Paths.get(filename), StandardCharsets.UTF_8)
    }

    fun readHtmlFromFile(file: File): String? {
        return Files.readString(file.toPath(), StandardCharsets.UTF_8)
    }

    fun inputStreamToString(inputstream: InputStream): String {
        return String(inputstream.readAllBytes(), StandardCharsets.UTF_8)
    }

    override fun start() {
        val server = Server(port)
        val handler = ServletContextHandler()
        handler.resourceBase = resourceBase
        handler.contextPath = "/fs"
        handler.addServlet(ServletHolder(LoginServlet()), "/login")
        val jsServletHolder = ServletHolder(DefaultServlet())
        jsServletHolder.setInitParameter(
            "org.eclipse.jetty.servlet.Default.useFileMappedBuffer",
            "false"
        )

        handler.addServlet(ServletHolder(ClientMainPageServlet()), "/clmain")

        handler.addServlet(jsServletHolder, "*.js")

        // APIs
        val apiContextHandler = ServletContextHandler()
        apiContextHandler.contextPath = "/api"

        val apiServlets =
            mapOf<String, Servlet>(
                "checkConnection" to CheckConnectionApiServlet(),
                "loginReq" to LoginRequestApi(),
                "registerReq" to RegisterRequestApi(),
                "showFolder" to ShowFolderApi(),
                "disconnect" to DisconnectApi(),
                "uploadFile" to UploadApi(),
                "msgSse" to MsgSseApi()
            )

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
            val html: String =
                inputStreamToString(resourceLoader.getResourceAsStream("/webapps/login.html"))

            // Write the HTML to the response output stream
            response.writer.write(html)
        }
    }

    inner class ClientMainPageServlet : HttpServlet() {
        override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
            // Set the content type and character encoding for the response
            response.contentType = "text/html;charset=UTF-8"

            // Read the login page HTML from a file
            val html: String =
                inputStreamToString(resourceLoader.getResourceAsStream("/webapps/clientmain.html"))

            // Write the HTML to the response output stream
            response.writer.write(html)
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
            val name = request.getParameter("username")
            val password = request.getParameter("password")
            val address = request.getParameter("address")
            val portStr = request.getParameter("port")
            val port = Integer.parseInt(portStr)

            val result = client.requestLogin(name, password, address, port)

            // Set the content type and character encoding for the response
            response.contentType = "text/json;charset=UTF-8"

            if (result) {
                response.writer.write("{\"result\" : \"ok\"}")
            } else {
                response.writer.write("{\"result\" : \"failed\"}")
            }
        }
    }

    inner class RegisterRequestApi : HttpServlet() {
        override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
            val name = request.getParameter("username")
            val password = request.getParameter("password")
            val address = request.getParameter("address")
            val portStr = request.getParameter("port")
            val port = Integer.parseInt(portStr)

            val result = client.requestRegister(name, password, address, port)

            // Set the content type and character encoding for the response
            response.contentType = "text/json;charset=UTF-8"

            if (result) {
                response.writer.write("{\"result\" : \"ok\"}")
            } else {
                response.writer.write("{\"result\" : \"failed\"}")
            }
        }
    }

    inner class ShowFolderApi : HttpServlet() {
        override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
            val dir = request.getParameter("dir")

            val data = client.showFolder(dir)

            var sss = ""

            sss += "["
            for (map in data) {
                sss += "{"
                for (entry in map) {
                    sss += "\"" + entry.key + "\" : \"" + entry.value + "\","
                }
                sss = sss.dropLast(1)
                sss += "},"
            }
            sss = sss.dropLast(1)
            sss += "]"

            // Set the content type and character encoding for the response
            response.contentType = "text/json;charset=UTF-8"

            response.writer.write(sss)
        }
    }

    inner class DisconnectApi : HttpServlet() {
        override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {

            client.disconnect()

            // Set the content type and character encoding for the response
            response.contentType = "text/json;charset=UTF-8"
            response.writer.write("{\"result\" : \"ok\"}")
        }
    }

    inner class UploadApi : HttpServlet() {
        override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {

            val fname = request.getParameter("fname")

            val result = client.uploadFile(fname)

            // Set the content type and character encoding for the response
            response.contentType = "text/json;charset=UTF-8"

            if (result) {
                response.writer.write("{\"result\" : \"ok\"}")
            } else {
                response.writer.write("{\"result\" : \"failed\"}")
            }
        }
    }

    inner class MsgSseApi : HttpServlet() {
        override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
            resp.contentType = "text/event-stream"
            resp.characterEncoding = "UTF-8"

            val writer = resp.writer

            // Send an initial SSE message to the client
            writer.write("data: Message SSE Connected\n\n")
            writer.flush()

            while (true) {
                val msg = client.takeReportMessage()
                writer.write("data: $msg\n\n")
                writer.flush()
            }
        }
    }
}
