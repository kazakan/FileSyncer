package filesyncer.server

import java.io.File
import kotlin.system.exitProcess

fun main(argv: Array<String>) {
    var folder = "~/fsrepo"
    var servicePort = 5050

    try {
        if (argv.isNotEmpty()) {
            folder = argv[0]
        }

        if (argv.size >= 2) {
            servicePort = argv[1].toInt()
        }
    } catch (e: Exception) {
        println("Wrong argument given")
        println("Arguments would be [folder [webPort]]")
        println("Arguments")
        println("folder : folder to use as server repository. String type. default=\"~/fsrepo\"")
        println("servicePort : port number where clients connect. int type. default=5050")
        exitProcess(-1)
    }

    // handle ~ character for home directory
    folder = folder.replaceFirst("~", System.getProperty("user.home"))

    // start server program
    println("Start Server Program with under configuration")
    println("Repository folder path : $folder")
    println("Port used by Server : $servicePort")

    val fsServer = FSServer(File(folder), port = servicePort, verbose = true)
    fsServer.initialize()
    fsServer.start()
}
