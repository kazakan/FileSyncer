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
) {}
