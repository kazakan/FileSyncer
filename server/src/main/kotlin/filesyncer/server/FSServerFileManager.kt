package filesyncer.server

import filesyncer.common.file.FSFileMetaData
import filesyncer.common.message.FSFileMetaDataMessage
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.nio.ByteBuffer

class FSServerFileManager(val serverFolder: File = File(System.getProperty("user.home"))) {
    val repoRoot = serverFolder.resolve("FsServerRepo")
    val repoDataRoot = serverFolder.resolve("FsServerData")
    val metaDataSuffix = "fsmeta"

    // files accessible by user. shared file included
    // <username, <filename, metaData>>
    var relation = HashMap<String, HashMap<String, FSFileMetaData>>()

    init {
        // init folders
        if (!repoRoot.exists()) repoRoot.mkdir()
        if (!repoDataRoot.exists()) repoDataRoot.mkdir()

        // init user-file relation
        val files =
            repoDataRoot.listFiles()?.filter { it.isFile && it.name.endsWith(".$metaDataSuffix") }

        if (files != null) {
            for (file in files) {
                val metaData = loadMetaData(file)
                share(metaData, metaData.owner)
                for (sharedUser in metaData.shared) {
                    share(metaData, metaData.owner)
                }
            }
        }
    }

    /** Return Metadata of files available by user [user] */
    fun listFiles(user: String): List<FSFileMetaData> {
        return relation.getOrPut(user) { HashMap() }.values.toMutableList() ?: emptyList()
    }

    /** Return file name used in server for file [metaData] represents. */
    fun realName(metaData: FSFileMetaData): String {
        return "${metaData.owner}-${metaData.name}"
    }

    /** Share file with [user] */
    fun share(metaData: FSFileMetaData, user: String) {
        relation.getOrPut(user) { HashMap() }[metaData.name] = metaData
    }

    /** Create file using given [metaData] */
    fun create(metaData: FSFileMetaData) {
        val realFileName = realName(metaData)
        val path = repoRoot.resolve(realFileName)
        path.createNewFile()

        // share to owner is just adding file data to owner
        share(metaData, metaData.owner)
    }

    /** Remove file using given [metaData] */
    fun delete(metaData: FSFileMetaData) {
        // remove file and meta data file
        val filePath = getFile(metaData)
        val metaDataPath = getMetaDataFile(metaData)
        filePath.delete()
        metaDataPath.delete()

        // remove in user-file relation
        relation[metaData.owner]?.remove(metaData.name)
        for (sharedUser in metaData.shared) {
            relation[sharedUser]?.remove(metaData.name)
        }
    }

    fun saveMetaData(metaData: FSFileMetaData) {
        val savePath = getMetaDataFile(metaData)
        val msg = FSFileMetaDataMessage(metaData, "")

        share(metaData, metaData.owner)

        val ous = savePath.outputStream()
        val dous = DataOutputStream(ous)
        val msgBuffer = msg.marshall()
        dous.write(msgBuffer!!.array())
        dous.flush()

        dous.close()
    }

    fun loadMetaData(file: File): FSFileMetaData {
        val ios = file.inputStream()
        val dios = DataInputStream(ios)
        val msg = FSFileMetaDataMessage()

        val nBytes: Int = dios.readInt()
        val byteBuffer = ByteBuffer.allocate(nBytes)
        byteBuffer.putInt(nBytes)
        while (byteBuffer.hasRemaining()) {
            byteBuffer.put(dios.readByte())
        }

        msg.unmarshall(byteBuffer)

        dios.close()

        return msg.toFileMetaData()
    }

    fun getFile(metaData: FSFileMetaData): File {
        val realFileName = realName(metaData)
        return repoRoot.resolve(realFileName)
    }

    fun getMetaDataFile(metaData: FSFileMetaData): File {
        val name = realName(metaData)
        return repoDataRoot.resolve("$name.$metaDataSuffix")
    }

    fun getNewestMetaData(metaData: FSFileMetaData): FSFileMetaData? {
        return relation[metaData.owner]?.get(metaData.owner)
    }
}
