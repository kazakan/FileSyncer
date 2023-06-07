package filesyncer.common.file

import filesyncer.common.FSFileHash
import filesyncer.common.message.FSFileMetaDataMessage
import java.io.*
import java.net.Socket
import java.nio.ByteBuffer

class FSFileTransfer {

    fun _download(sourceStream: InputStream, destStream: OutputStream) {
        onStartDownload()

        val metaDataMessage = receiveMetaData(sourceStream)

        onReceiveMetaData(metaDataMessage)

        destStream.use { sourceStream.copyTo(it) }

        onFinishDownload(metaDataMessage)
    }

    fun _upload(
        requester: String,
        metaData: FSFileMetaData,
        sourceStream: InputStream,
        destStream: OutputStream
    ) {

        sendMetaData(requester, metaData, destStream)

        destStream.use { sourceStream.copyTo(destStream) }
    }

    fun download(socket: Socket, file: File) {
        val ios = socket.getInputStream()
        val ous = file.outputStream()
        _download(ios, ous)
    }

    fun upload(requester: String, socket: Socket, file: File) {

        val ios = file.inputStream()
        val ous = socket.getOutputStream()
        val metaData = FSFileMetaData()

        onStartUpload(metaData)

        metaData.fileSize = file.length()
        metaData.name = file.name
        metaData.md5 = FSFileHash.md5(file)

        _upload(requester, metaData, ios, ous)
        socket.close()
        ios.close()

        onFinishUpload(metaData)
    }

    fun sendMetaData(requester: String, metaData: FSFileMetaData, outputStream: OutputStream) {
        val metaDataMsg = FSFileMetaDataMessage(metaData, requester)
        val msgBuffer = metaDataMsg.marshall()
        outputStream.write(msgBuffer!!.array())
        outputStream.flush()
    }

    fun receiveMetaData(inputStream: InputStream): FSFileMetaDataMessage {
        val msg = FSFileMetaDataMessage()
        val dios = DataInputStream(inputStream)

        val nBytes: Int = dios.readInt()
        val byteBuffer = ByteBuffer.allocate(nBytes)
        byteBuffer.putInt(nBytes)
        while (byteBuffer.hasRemaining()) {
            byteBuffer.put(dios.readByte())
        }

        msg.unmarshall(byteBuffer)

        return msg
    }

    fun onStartDownload() {}
    fun onReceiveMetaData(metaData: FSFileMetaDataMessage) {}
    fun onFinishDownload(metaData: FSFileMetaDataMessage) {}

    fun onStartUpload(metaData: FSFileMetaData) {}
    fun onFinishUpload(metaData: FSFileMetaData) {}
}
