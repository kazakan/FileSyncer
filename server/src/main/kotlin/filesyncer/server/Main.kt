package filesyncer.server

import java.io.File

fun main() {
    val root = File(System.getProperty("user.home") + "/fsrepo")
    println(root.absolutePath)
    var fsServer = FSServer(root)
    fsServer.initialize()
    fsServer.start()
}
