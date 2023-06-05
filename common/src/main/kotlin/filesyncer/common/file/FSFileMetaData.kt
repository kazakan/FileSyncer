package filesyncer.common.file

import java.io.File

class FSFileMetaData(
    var name: String,
    var fileSize: Long,
    var timeStamp: Long = 0L,
    var md5: String = "",
    var path: File? = null
)
