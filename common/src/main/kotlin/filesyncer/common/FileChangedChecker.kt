package filesyncer.common

import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.StandardWatchEventKinds.OVERFLOW
import java.nio.file.WatchEvent

/**  */
class FileWatcher(rootDir: Path) {

    interface OnFileChangedListener {
        fun onFileChanged(file: File, kind: WatchEvent.Kind<out Any>)
    }

    var fileChangedListener: OnFileChangedListener? = null

    val checkerThread = Thread {
        var watchService = FileSystems.getDefault().newWatchService()
        rootDir.register(
            watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_DELETE,
            StandardWatchEventKinds.ENTRY_MODIFY
        )

        while (true) {
            val key = watchService.take()

            for (event in key.pollEvents()) {
                val kind = event.kind()

                if (kind == OVERFLOW) {
                    continue
                }

                val ev: WatchEvent<Path> = event as WatchEvent<Path>
                val path: Path = ev.context()

                try {
                    fileChangedListener?.onFileChanged(path.toFile(), kind)
                } catch (e: Exception) {
                    e.printStackTrace()
                    watchService = FileSystems.getDefault().newWatchService()
                    rootDir.register(
                        watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY
                    )
                }
            }

            key.reset()
        }
    }

    fun run() {
        checkerThread.start()
    }

    fun stop() {
        checkerThread.interrupt()
    }
}
