package filesyncer.common

import java.io.File

enum class FileChangedCheckDataChanged {
    NOT_CHANGED,
    CHANGED,
    REMOVED
}

data class FileChangedCheckData(val file: File) {
    var timeStamp: Long = -1
    var hash = 0 // currently not available

    init {
        update()
    }

    fun update() {
        timeStamp = file.lastModified()
    }
}

class FileChangedChecker(rootDir: File) {

    var files: HashSet<FileChangedCheckData> = HashSet<FileChangedCheckData>()

    init {
        for (file: File in rootDir.listFiles()!!) {
            if (file.isDirectory) continue
            files.add(FileChangedCheckData(file))
        }
    }

    fun checkFolder() {
        for (file in this.files) {
            when (isFileChanged(file)) {
                FileChangedCheckDataChanged.CHANGED -> {
                    files.remove(file)
                    file.update()
                    files.add(file)
                }
                FileChangedCheckDataChanged.REMOVED -> {
                    files.remove(file)
                }
                else -> {
                    // do nothing
                }
            }
        }
    }

    fun isFileChanged(data: FileChangedCheckData): FileChangedCheckDataChanged {
        if (!data.file.exists()) {
            return FileChangedCheckDataChanged.REMOVED
        }

        if (data.file.lastModified() != data.timeStamp) {
            data.timeStamp = data.file.lastModified()
            return FileChangedCheckDataChanged.CHANGED
        }
        return FileChangedCheckDataChanged.NOT_CHANGED
    }
}
