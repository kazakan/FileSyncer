package filesyncer.client

import filesyncer.common.FSFileHash
import filesyncer.common.file.FSFileMetaData
import java.io.File

class FSClientFileManager(
    val localFolder: File = File(System.getProperty("user.home")),
    val metaDataFolder: File = File(System.getProperty("user.home")).resolve(".fslocalmeta")
) {

    val metaDataSuffix = "fsmeta"
    var metaDataMap = HashMap<String, FSFileMetaData>()
    init {
        // init folder
        if (!localFolder.exists()) localFolder.mkdir()
        if (!metaDataFolder.exists()) metaDataFolder.mkdir()

        val metaFiles =
            metaDataFolder.listFiles()?.filter { it.isFile && it.name.endsWith(".$metaDataSuffix") }

        if (metaFiles != null) {
            for (file in metaFiles) {
                val metaData = loadMetaData(file)
                registerMetaData(metaData)
            }
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

        val metaFile = getMetaDataFile(meta)
        metaFile.delete()

        metaDataMap.remove(file.name)
    }

    /** Create and add metadata about [file] which is located [metaDataFolder] */
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
        metaDataMap[metaData.name] = metaData
    }

    /** Handle difference with cloud files */
    fun resolveCloudDiff(cloudFileList: List<FSFileMetaData>) {
        val localList = metaDataMap.values.toMutableList()
    }
}
