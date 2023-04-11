package client

interface FSClientFrontInterface {
    fun checkConnection(address: String, port: Int): Boolean
    fun requestLogin(id: String, password: String, address: String, port: Int): Boolean
    fun requestRegister(id: String, password: String, address: String, port: Int): Boolean
    fun showFolder(dir: String): List<Map<String, String>>
    fun uploadFile(path: String): Boolean
    fun disconnect()
}
