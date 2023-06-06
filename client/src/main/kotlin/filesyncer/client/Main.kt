package filesyncer.client

import java.io.File
import kotlin.system.exitProcess

fun main(argv: Array<String>) {
    var folder = "~/fsclientRepo"
    var webport = 8080

    try {
        if (argv.isNotEmpty()) {
            folder = argv[0]
        }

        if (argv.size >= 2) {
            webport = argv[1].toInt()
        }
    } catch (e: Exception) {
        println("Wrong argument given")
        println("Arguments would be [folder [webPort]]")
        println("Arguments")
        println(
            "folder : folder to use as local repository. String type. default=\"~/fsclientRepo\""
        )
        println("webPort : port number to show web interface. int type. default=8080")
        exitProcess(-1)
    }

    // handle ~ character for home directory
    folder = folder.replaceFirst("~", System.getProperty("user.home"))

    // start client program
    println("Start Client Program with under configuration")
    println("Local folder path : $folder")
    println("Web port used by client : $webport")

    val client = Client(File(folder))
    val front = FSClientWebFront(client.frontInterface, webport)
    client.front = front
    client.start()
}
