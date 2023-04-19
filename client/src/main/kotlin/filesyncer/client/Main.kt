package filesyncer.client

import java.io.File

fun main() {
    var client = Client(File(System.getProperty("user.home") + "/fsclientRepo"))
    client.start()
}
