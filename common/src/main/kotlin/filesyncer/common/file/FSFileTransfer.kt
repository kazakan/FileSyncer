package filesyncer.common.file

import filesyncer.common.FSFileHash
import filesyncer.common.message.FSFileMetaDataMessage
import java.io.*
import java.net.Socket
import java.nio.ByteBuffer

class FSFileTransfer {
    interface OnDownloadListener {
        fun onStart()
        fun onGetMetaData(metaData: FSFileMetaData)
        fun onFinish(metaData: FSFileMetaData)
    }

    fun _download(sourceStream: InputStream, destStream: OutputStream) {
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
        downloadListener?.onGetMetaData(metaData)

        destStream.use { sourceStream.copyTo(it) }

        downloadListener?.onFinish(metaData)
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

        metaData.fileSize = file.length()
        metaData.name = file.name
        metaData.md5 = FSFileHash.md5(file)

        _upload(metaData, ios, ous)
        socket.close()
        ios.close()
    }

    var downloadListener: OnDownloadListener? = null
}
