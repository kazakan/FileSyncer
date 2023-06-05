package filesyncer.server

import org.gradle.internal.file.FileMetadata

class FSServerFileManager {
    var set = HashMap<String, HashMap<String, FileMetadata>>() // <username, <filename, filedata>>
}
