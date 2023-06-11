package filesyncer.client

import filesyncer.common.FSFileHash
import filesyncer.common.file.FSFileMetaData
import java.io.File

class FSClientFileManager(
    val localFolder: File = File(System.getProperty("user.home")),
    val metaDataFolder: File = File(System.getProperty("user.home")).resolve(".fslocalmeta"),
    val user: String
) {

    val metaDataSuffix = "fsmeta"
    var metaDataMap = HashMap<String, FSFileMetaData>()
    init {
        // init folder
        if (!localFolder.exists()) localFolder.mkdir()
        if (!metaDataFolder.exists()) metaDataFolder.mkdir()

        val metaFiles =
            metaDataFolder.listFiles()!!.filter {
                it.isFile && it.name.endsWith(".$metaDataSuffix")
            }

        for (file in metaFiles) {
            val metaData = loadMetaData(file)
            registerMetaData(metaData)
        }
    }

    fun saveMetaData(metaData: FSFileMetaData) {
        val savePath = getMetaDataFile(metaData)
        metaData.write(savePath)
    }

    fun loadMetaData(file: File): FSFileMetaData {
        val metaData = FSFileMetaData()
        metaData.read(file)
        return metaData
    }

    fun getFile(metaData: FSFileMetaData): File {
        return localFolder.resolve(metaData.name)
    }

    fun getMetaDataFile(metaData: FSFileMetaData): File {
        return metaDataFolder.resolve("${metaData.name}.$metaDataSuffix")
    }

    /** Delete [file] and metadata about [file] */
    fun delete(file: File) {
        file.delete()
        val meta = metaDataMap[file.name] ?: return

        deleteMetaData(meta)
    }

    /** Create and add metadata about [file] which is located [localFolder] */
    fun add(file: File, owner: String, timeStamp: Long) {

        val metaData = FSFileMetaData()
        metaData.name = file.name
        metaData.md5 = FSFileHash.md5(file)
        metaData.owner = owner
        metaData.fileSize = file.length()
        metaData.timeStamp = timeStamp

        registerMetaData(metaData)
    }

    fun registerMetaData(metaData: FSFileMetaData) {
        registerMetaData(metaData.name, metaData)
    }

    fun getMetaData(name: String): FSFileMetaData? {
        return metaDataMap[name]
    }

    fun registerMetaData(name: String, data: FSFileMetaData) {
        metaDataMap[name] = data
    }

    fun deleteMetaData(metaData: FSFileMetaData) {
        val metaFile = getMetaDataFile(metaData)
        metaFile.delete()

        metaDataMap.remove(metaData.name)
    }
}
