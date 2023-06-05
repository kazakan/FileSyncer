package filesyncer.common.file

import filesyncer.common.FSFileHash
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.ServerSocket
import java.net.Socket

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
                val fsize = dios.readLong()
                val fnameLen = dios.readInt()
                val fnameByteArr = ios.readNBytes(fnameLen)
                val fname = String(fnameByteArr)

                val md5Len = dios.readInt()
                val md5ByteArr = ios.readNBytes(md5Len)
                val md5 = String(md5ByteArr)

                val destination = repoDir.resolve(File(fname))
                val metaData = FSFileMetaData(fname, fsize, 0L, md5, destination)

                downloadListener?.onGetMetaData(metaData)

                val fos = FileOutputStream(repoDir.resolve(File(fname)))

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
            val fsocket = Socket(remoteAddress, remotePort)
            val fsize = file.length()
            val fname = file.name
            val fnameByteBuffer = fname.toByteArray()
            val md5 = FSFileHash.md5(file)
            val md5ByteBuffer = md5.toByteArray()

            val ous = fsocket.getOutputStream()
            val dous = DataOutputStream(ous)
            dous.writeLong(fsize)
            dous.writeInt(fnameByteBuffer.size)
            dous.write(fnameByteBuffer)
            dous.writeInt(md5ByteBuffer.size)
            dous.write(md5ByteBuffer)

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
