package server

import FSEventConnWorker
import FSUser
import FSUserManager
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

class FSSimpleUserManager(var repositoryRoot: File) : FSUserManager {

    var sessions = HashMap<FSUser, FSEventConnWorker>()
    var users = HashSet<FSUser>()
    var userDataFile = repositoryRoot.resolve("FSUserData.txt")

    override fun initialize() {
        if (!repositoryRoot.isDirectory) throw Exception("Invalid repository root")

        if (userDataFile.exists()) {
            userDataFile.forEachLine {
                val tokens = it.split('\t')
                if (tokens.size < 2) return@forEachLine // just like continue in other lang.
                users.add(FSUser(tokens[0], tokens[1]))
            }
        } else {
            userDataFile.createNewFile()
        }
    }

    override fun userExists(user: FSUser): Boolean {
        return users.contains(user)
    }

    override fun addUserSession(user: FSUser, session: FSEventConnWorker): FSEventConnWorker? {
        if (!userExists(user)) return null
        if (sessions[user] == null) {
            sessions[user] = session
            return session
        }

        return null
    }

    override fun removeUserSession(user: FSUser) {
        if (sessions[user] != null) {
            sessions.remove(user)
        }
    }

    override fun registerUser(user: FSUser): Boolean {
        if (userExists(user)) return false
        users.add(user)
        rewriteFile()
        return true
    }

    override fun unregisterUser(user: FSUser): Boolean {
        if (userExists(user)) {
            if (sessions[user] != null) return false // cannot remove if user session exists
            users.remove(user)
            rewriteFile()
            return true
        }
        return false
    }

    fun rewriteFile() {
        val writer = PrintWriter(FileWriter(userDataFile, false))
        for (itUser in users) {
            writer.println("${itUser.id}\t${itUser.password}")
        }
        writer.close()
    }
}
