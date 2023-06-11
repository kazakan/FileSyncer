package filesyncer.common

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

object FSFileHash {
    fun md5(file: File): String {
        val md = MessageDigest.getInstance("MD5")
        val buffer = ByteArray(8192)
        val fis = FileInputStream(file)

        var bytesRead = fis.read(buffer)
        while (bytesRead != -1) {
            md.update(buffer, 0, bytesRead)
            bytesRead = fis.read(buffer)
        }

        fis.close()
        val md5HashBytes = md.digest()

        return bytesToHex(md5HashBytes)
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = "0123456789ABCDEF"
        val result = StringBuilder(bytes.size * 2)

        for (byte in bytes) {
            val octet = byte.toInt() and 0xFF
            val firstIndex = octet ushr 4
            val secondIndex = octet and 0x0F
            result.append(hexChars[firstIndex])
            result.append(hexChars[secondIndex])
        }

        return result.toString()
    }
}
