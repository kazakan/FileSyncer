package client

interface FSClientFrontInterface {
    fun checkConnection(address: String, port: Int): Boolean
    fun requestLogin(id: String, password: String, address: String, port: Int): Boolean
    fun requestRegister(id: String, password: String, address: String, port: Int): Boolean
    fun showFolder(dir: String): List<Map<String, String>>
    fun uploadFile(path: String): Boolean
    fun disconnect()

    /**
     * @return message to show
     *
     *   Take message from client. It should work like as if it takes elements from blocking queue.
     */
    fun takeReportMessage(): String
}
