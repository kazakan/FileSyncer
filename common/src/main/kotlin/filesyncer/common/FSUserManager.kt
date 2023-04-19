package filesyncer.common

interface FSUserManager {
    fun initialize()
    fun userExists(user: FSUser): Boolean
    fun addUserSession(user: FSUser, session: FSEventConnWorker): FSEventConnWorker?
    fun removeUserSession(user: FSUser)
    fun registerUser(user: FSUser): Boolean
    fun unregisterUser(user: FSUser): Boolean
    fun removeClosedConnection()
}
