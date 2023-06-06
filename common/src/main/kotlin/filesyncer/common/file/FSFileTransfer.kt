package filesyncer.common.file

import filesyncer.common.FSFileHash
import filesyncer.common.message.FSFileMetaDataMessage
import java.io.*
import java.net.Socket
import java.nio.ByteBuffer

class FSFileTransfer {

    fun _download(sourceStream: InputStream, destStream: OutputStream) {
        onStartDownload()

        val dios = DataInputStream(sourceStream)

        val msg = FSFileMetaDataMessage()

        val nBytes: Int = dios.readInt()
        val byteBuffer = ByteBuffer.allocate(nBytes)
        byteBuffer.putInt(nBytes)
        while (byteBuffer.hasRemaining()) {
            byteBuffer.put(dios.readByte())
        }

        msg.unmarshall(byteBuffer)

        val metaData = msg.toFileMetaData()

        onReceiveMetaData(metaData)

        destStream.use { sourceStream.copyTo(it) }

        onFinishDownload(metaData)
    }

    fun _upload(metaData: FSFileMetaData, sourceStream: InputStream, destStream: OutputStream) {

        val dous = DataOutputStream(destStream)

        val metaDataMsg = FSFileMetaDataMessage(metaData)
        val msgBuffer = metaDataMsg.marshall()
        dous.write(msgBuffer!!.array())
        dous.flush()

        destStream.use { sourceStream.copyTo(destStream) }
    }

    fun download(socket: Socket, file: File) {
        val ios = socket.getInputStream()
        val ous = file.outputStream()
        _download(ios, ous)
    }

    fun upload(socket: Socket, file: File) {

        val ios = file.inputStream()
        val ous = socket.getOutputStream()
        val metaData = FSFileMetaData()

        onStartUpload(metaData)

        metaData.fileSize = file.length()
        metaData.name = file.name
        metaData.md5 = FSFileHash.md5(file)

        _upload(metaData, ios, ous)
        socket.close()
        ios.close()

        onFinishUpload(metaData)
    }

    fun onStartDownload() {}
    fun onReceiveMetaData(metaData: FSFileMetaData) {}
    fun onFinishDownload(metaData: FSFileMetaData) {}

    fun onStartUpload(metaData: FSFileMetaData) {}
    fun onFinishUpload(metaData: FSFileMetaData) {}
}
