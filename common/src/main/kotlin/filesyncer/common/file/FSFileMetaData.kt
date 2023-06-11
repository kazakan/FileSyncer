package filesyncer.common.file

import java.io.File

data class FSFileMetaData(
    var name: String = "",
    var fileSize: Long = 0L,
    var timeStamp: Long = 0L,
    var md5: String = "",
    var path: File? = null,
    var owner: String = "",
    var shared: List<String> = emptyList()
) {
    fun toStringArray(): Array<String> {
        return arrayOf(name, "$fileSize", "$timeStamp", md5, owner, shared.joinToString("/"))
    }

    fun fromStringArray(arr: Array<String>) {
        if (arr.size < 6) throw error("arr is shorter than 6.")

        name = arr[0]
        fileSize = arr[1].toLong()
        timeStamp = arr[2].toLong()
        md5 = arr[3]
        owner = arr[4]
        shared = arr[5].split("/")
    }

    fun write(file: File) {
        val ous = file.outputStream()
        ous.write(toStringArray().joinToString("\t").toByteArray())
        ous.close()
    }

    fun read(file: File) {
        val line = file.readLines()[0]
        fromStringArray(line.split("\t").toTypedArray())
    }

    fun toMap(): Map<String, String> {
        return mapOf(
            "name" to name,
            "fileSize" to "$fileSize",
            "timeStamp" to "$timeStamp",
            "md5" to md5,
            "owner" to owner,
            "shared" to shared.joinToString("/")
        )
    }

    fun fromMap(map: Map<String, String>) {
        name = map["name"]!!
        fileSize = map["fileSize"]!!.toLong()
        timeStamp = map["timeStamp"]!!.toLong()
        md5 = map["md5"]!!
        owner = map["owner"]!!
        shared = map["shared"]!!.split("/")
    }
}
