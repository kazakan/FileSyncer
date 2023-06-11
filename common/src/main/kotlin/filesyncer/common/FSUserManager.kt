package filesyncer.common

interface FSUserManager {
    fun initialize()
    fun userExists(user: FSUser): Boolean
    fun addUserSession(user: FSUser, session: FSSession): FSSession?
    fun removeUserSession(user: FSUser)
    fun registerUser(user: FSUser): Boolean
    fun unregisterUser(user: FSUser): Boolean
    fun removeClosedConnection()
    fun getUserNames(): List<String>

    interface FSUserManagerListener {
        fun onUserSessionRemoved(user: FSUser)
        fun onUserSessionAdded(user: FSUser)
    }
}
