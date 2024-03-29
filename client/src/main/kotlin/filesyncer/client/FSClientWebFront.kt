package filesyncer.client

import filesyncer.common.file.FSFileMetaData
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
                "msgSse" to MsgSseApi(),
                "listUser" to ListUserApi(),
                "shareFile" to ShareFileApi()
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
            response.writer.flush()
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
            response.writer.flush()
        }
    }

    inner class CheckConnectionApiServlet : HttpServlet() {
        override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
            // Set the content type and character encoding for the response
            response.contentType = "text/json;charset=UTF-8"

            response.writer.write("{'connection' : 'ok'}")
            response.writer.flush()
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
            response.writer.flush()
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
            response.writer.flush()
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
            if (sss.length > 1) sss = sss.dropLast(1)
            sss += "]"

            // Set the content type and character encoding for the response
            response.contentType = "text/json;charset=UTF-8"

            response.writer.write(sss)
            response.writer.flush()
        }
    }

    inner class DisconnectApi : HttpServlet() {
        override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {

            client.disconnect()

            // Set the content type and character encoding for the response
            response.contentType = "text/json;charset=UTF-8"
            response.writer.write("{\"result\" : \"ok\"}")
            response.writer.flush()
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
            response.writer.flush()
        }
    }

    inner class MsgSseApi : HttpServlet() {
        override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
            resp.contentType = "text/plain"
            resp.characterEncoding = "UTF-8"

            val writer = resp.writer

            while (true) {
                val msg = client.takeReportMessage() ?: break
                writer.write("$msg\n")
            }
            resp.writer.flush()
        }
    }

    inner class ListUserApi : HttpServlet() {
        override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
            resp.contentType = "text/plain"
            resp.characterEncoding = "UTF-8"

            val userNames = client.listUsers()
            resp.writer.write(userNames.joinToString("\t"))

            resp.writer.flush()
        }
    }

    inner class ShareFileApi : HttpServlet() {
        override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
            resp.contentType = "text/plain"
            resp.characterEncoding = "UTF-8"

            // TODO("implement")
            val fname = req.getParameter("fname")

            val metaData = FSFileMetaData()
            metaData.name = req.getParameter("name")
            metaData.fileSize = req.getParameter("fileSize").toLong()
            metaData.timeStamp = req.getParameter("timeStamp").toLong()
            metaData.md5 = req.getParameter("md5")
            metaData.owner = req.getParameter("owner")
            metaData.shared = req.getParameter("shared").split("/")

            client.shareFile(metaData)

            val writer = resp.writer
            writer.write("some what message\n")

            resp.writer.flush()
        }
    }
}
