package filesyncer.common.file

import filesyncer.common.FSFileHash
import filesyncer.common.message.FSFileMetaDataMessage
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer

class FSFileTransfer(
    val repoDir: File,
    val socket: Int = 7777,
    val remoteAddress: String = "",
    val remotePort: Int = 7777,
    val verbose: Boolean = true
) {
    interface OnDownloadListener {
        fun onStart()
        fun onGetMetaData(metaData: FSFileMetaData)
        fun onFinish(metaData: FSFileMetaData)
    }

    private var running: Boolean = true

    var downloadListener: OnDownloadListener? = null

    var fileDownloadLoopThread = Thread {
        val fss = ServerSocket(socket)
        while (running) {
            val socket = fss.accept()
            val fileDownloadWorkerThread = Thread {
                if (verbose) {
                    println("Start download file from ${socket.remoteSocketAddress.toString()}")
                }
                downloadListener?.onStart()

                val ios = socket.getInputStream()
                val dios = DataInputStream(ios)

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

                val destination = repoDir.resolve(File(metaData.name))

                val fos = FileOutputStream(destination)

                fos.use { ios.copyTo(it) }

                socket.close()

                downloadListener?.onFinish(metaData)
                if (verbose) {
                    println("Done download file")
                }
                // broadcast(FSEventMessage(FSEventMessage.EventType.UPLOAD_DONE, fname))
            }

            fileDownloadWorkerThread.start()
        }
    }

    fun uploadFile(path: String): Boolean {
        val file = repoDir.resolve(File(path))
        var fileUploadThread = Thread {
            val metaData = FSFileMetaData()

            val fsocket = Socket(remoteAddress, remotePort)

            metaData.fileSize = file.length()
            metaData.name = file.name
            metaData.md5 = FSFileHash.md5(file)

            val ous = fsocket.getOutputStream()
            val dous = DataOutputStream(ous)

            val metaDataMsg = FSFileMetaDataMessage(metaData)
            val msgBuffer = metaDataMsg.marshall()
            dous.write(msgBuffer!!.array())
            dous.flush()

            val fios = file.inputStream()

            ous.use { fios.copyTo(ous) }

            fsocket.close()
        }

        fileUploadThread.start()
        fileUploadThread.join()

        return true
    }

    fun startFileServer() {
        running = true
        fileDownloadLoopThread.start()
    }

    fun stopFileServer() {
        running = false
        fileDownloadLoopThread.interrupt()
    }
}
