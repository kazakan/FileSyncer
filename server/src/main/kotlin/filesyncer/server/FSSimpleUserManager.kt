package filesyncer.server

import filesyncer.common.FSEventConnWorker
import filesyncer.common.FSUser
import filesyncer.common.FSUserManager
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

class FSSimpleUserManager(
    var repositoryRoot: File,
    val verbose: Boolean = false,
    val listener: FSUserManager.FSUserManagerListener? = null
) : FSUserManager {

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
            if (verbose) println("Add User ${user.id}'s Session")
            sessions[user] = session
            listener?.onUserSessionAdded(user)
            return session
        }

        return null
    }

    override fun removeUserSession(user: FSUser) {
        if (sessions[user] != null) {
            if (verbose) println("Remove User ${user.id}'s Session")
            sessions.remove(user)
            listener?.onUserSessionRemoved(user)
        } else {
            if (verbose) println("Cannot Remove User ${user.id}'s Session. Already null")
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

    override fun removeClosedConnection() {
        var removeLists = mutableListOf<FSUser>()
        for (entry in sessions) {
            if (entry.value.isClosed()) {
                if (verbose) println("${entry.value} seems to dead. Add to session removal list")
                removeLists.add(entry.key)
            }
        }
        if (verbose && removeLists.size > 0)
            println("Removing ${removeLists.size} sessions whose connection is dead.")
        for (user in removeLists) {
            removeUserSession(user)
        }
    }

    fun rewriteFile() {
        val writer = PrintWriter(FileWriter(userDataFile, false))
        for (itUser in users) {
            writer.println("${itUser.id}\t${itUser.password}")
        }
        writer.close()
    }
}
